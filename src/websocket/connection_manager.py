from fastapi import WebSocket, status
from enum import Enum
import json

class ConnectionManager:
    class Actions(Enum):
        LIST_UPDATE = "update_list_players"
        LIST_ADD = "add_list_player"
        LIST_EDIT = "edit_list_player"
        LIST_REMOVE = "remove_list_player"

        def convert(self, payload_data):
            if payload_data is None: return None
            try:
                match self:
                    case self.LIST_UPDATE | self.LIST_ADD | self.LIST_EDIT | self.LIST_REMOVE:
                        if not isinstance(payload_data, (list, tuple)): payload_data = [payload_data]

                        return {str(player_data.mc_uuid):
                                    {"shared":True,
                                     "type":player_data.cat.name,
                                     "nickname":player_data.alias} # player_data.alias or ""
                                for player_data in payload_data}

            except (ValueError, TypeError, KeyError, AttributeError):
                return None

    def __init__(self, db):
        self.active_connections: dict[str, WebSocket] = {}
        self.db = db

    async def connect(self, conn_uuid: str, websocket: WebSocket):
        await self.disconnect(conn_uuid)
        await websocket.accept()
        self.active_connections[conn_uuid] = websocket

    async def disconnect(self, conn_uuid: str):
        if conn := self.active_connections.pop(conn_uuid, None):
            try:
                await conn.close(status.WS_1000_NORMAL_CLOSURE)
            except Exception:
                pass

    async def broadcast(self, action: Actions, payload_data, conn_uuid: str = None):
        message = json.dumps({"action":action.value, "payload":action.convert(payload_data)}, separators=(",", ":"))
        conn_snapshot = [conn_uuid] if conn_uuid else list(self.active_connections.keys())

        for conn_uuid in conn_snapshot:
            try:
                if conn := self.active_connections.get(conn_uuid): await conn.send_text(message)
            except Exception:
                await self.disconnect(conn_uuid)

    async def init_broadcast(self, conn_uuid: str):
        await self.broadcast(self.Actions.LIST_UPDATE, await self.db.get_player(), conn_uuid)