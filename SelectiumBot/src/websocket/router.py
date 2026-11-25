from fastapi import APIRouter, WebSocket, WebSocketException, status, Depends, Header
import secrets

from src.database.database_manager import DatabaseManager
from src.websocket.connection_manager import ConnectionManager

DUMMY_TOKEN = DatabaseManager.Fields.TOKEN.convert(secrets.token_hex(32))
ws_router = APIRouter()

async def verify_headers(websocket: WebSocket, conn_uuid: str = Header(alias="uuid"), conn_token: str = Header(alias="token")):
    database = websocket.app.state.database
    player_data = await database.get_player(DatabaseManager.Fields.UUID, conn_uuid)

    player_valid = bool(player_data)
    token_valid = secrets.compare_digest(DatabaseManager.Fields.TOKEN.convert(conn_token),
                                         (player_data[0].token_hash if player_valid else None) or DUMMY_TOKEN)

    if not player_valid or not token_valid: raise WebSocketException(status.WS_1008_POLICY_VIOLATION)

    return {"uuid":player_data[0].mc_uuid, "name":player_data[0].mc_name}

@ws_router.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket, conn_data = Depends(verify_headers)):
    conn_manager = websocket.app.state.conn_manager
    database = websocket.app.state.database
    await conn_manager.connect(conn_data["uuid"], websocket)

    try:
        await conn_manager.broadcast(ConnectionManager.Actions.LIST_UPDATE, await database.get_player(), conn_data["uuid"])

        while True:
            received_data = await websocket.receive_json()

            match received_data["action"]:
                case "update_player_data":
                    player_data = (await database.get_player(DatabaseManager.Fields.UUID, conn_data["uuid"]))[0]
                    await database.update_player(DatabaseManager.Fields.UUID, conn_data["uuid"],
                                                 **{DatabaseManager.Fields.KILLS.value:(player_data.vanquishes or 0) + received_data["payload"]["kills"]})
                case "chat_message":
                    await conn_manager.broadcast(ConnectionManager.Actions.CHAT, f"{conn_data['name']}: {received_data['payload']}")
    except Exception:
        pass
    finally:
        await conn_manager.disconnect(conn_data["uuid"])