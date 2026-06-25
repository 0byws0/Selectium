from fastapi import WebSocket
import asyncio
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
                                     "nickname":player_data.alias}
                                for player_data in payload_data}

            except (ValueError, TypeError, KeyError, AttributeError):
                return None

    def __init__(self, db):
        self.active_connections: list[WebSocket] = []
        self.db = db

    async def connect(self, websocket: WebSocket):
        await websocket.accept()
        self.active_connections.append(websocket)

    async def disconnect(self, websocket: WebSocket):
        if websocket in self.active_connections: self.active_connections.remove(websocket)

    async def broadcast(self, action: Actions, payload_data, websocket: WebSocket = None):
        message = json.dumps({"action":action.value, "payload":action.convert(payload_data)}, separators=(",", ":"))

        async def send_message(conn):
            try:
                await conn.send_text(message)
            except Exception:
                await self.disconnect(conn)

        if websocket_list := (self.active_connections.copy() if websocket is None else [websocket]):
            await asyncio.gather(*(send_message(websocket) for websocket in websocket_list), return_exceptions=True)

    async def init_broadcast(self, websocket: WebSocket):
        await self.broadcast(self.Actions.LIST_UPDATE, await self.db.get_player(), websocket)