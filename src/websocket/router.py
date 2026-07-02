from fastapi import APIRouter, WebSocket, WebSocketDisconnect, WebSocketException, status, Depends, Header
import secrets

from src.database.database_manager import DatabaseManager

DUMMY_TOKEN = DatabaseManager.Fields.TOKEN.convert(secrets.token_hex(32))
ws_router = APIRouter()

async def verify_headers(websocket: WebSocket,
                         conn_uuid: str = Header(alias="uuid"),
                         conn_token: str = Header(alias="token")):
    database = websocket.app.state.database
    player_data = await database.get_player(DatabaseManager.Fields.UUID, conn_uuid)

    player_valid = bool(player_data)
    token_valid = secrets.compare_digest(DatabaseManager.Fields.TOKEN.convert(conn_token),
                                         (player_data[0].token_hash if player_valid else None) or DUMMY_TOKEN)

    if not player_valid or not token_valid: raise WebSocketException(status.WS_1008_POLICY_VIOLATION)

    return conn_uuid

@ws_router.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket, conn_uuid = Depends(verify_headers)):
    conn_manager = websocket.app.state.conn_manager
    await conn_manager.connect(conn_uuid, websocket)

    try:
        await conn_manager.init_broadcast(conn_uuid)
        while True:
            await websocket.receive_text()
    except WebSocketDisconnect:
        await conn_manager.disconnect(conn_uuid)