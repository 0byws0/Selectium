from fastapi import WebSocket, status, WebSocketException
from enum import Enum

class ConnectionManager:
    class Actions(Enum): # rework later
        LIST_UPDATE = "update_list_players"
        LIST_ADD = "add_list_player"
        LIST_EDIT = "edit_list_player"
        LIST_REMOVE = "remove_list_player"

        def convert(self, payload_data):
            if payload_data is None: return None
            try:
                match self:
                    case self.LIST_UPDATE | self.LIST_ADD | self.LIST_EDIT:
                        if not isinstance(payload_data, (list, tuple)): payload_data = [payload_data]

                        return {str(player_data.mc_uuid):
                                    {"shared":True, # old data
                                     "type":player_data.cat.name, # old data
                                     "serverType":player_data.cat.name,
                                     "group":player_data.org,
                                     "nickname":player_data.alias}
                                for player_data in payload_data}

                    case self.LIST_REMOVE:
                        if not isinstance(payload_data, (list, tuple)): payload_data = [payload_data]

                        return [str(player_data.mc_uuid)
                                for player_data in payload_data]

            except (ValueError, TypeError, KeyError, AttributeError):
                return None

    def __init__(self):
        self.active_connections: dict[str, WebSocket] = {} # change to uuid later

    async def connect(self, conn_uuid: str, conn_ws: WebSocket):
        if conn_uuid in self.active_connections:
            raise WebSocketException(status.WS_1008_POLICY_VIOLATION)
        else:
            self.active_connections[conn_uuid] = conn_ws
            await conn_ws.accept()

    async def disconnect(self, conn_uuid: str):
        if conn_ws := self.active_connections.pop(conn_uuid, None):
            try:
                await conn_ws.close(status.WS_1000_NORMAL_CLOSURE)
            except Exception:
                pass

    async def broadcast(self, action: Actions, payload_data, conn_uuid: str = None):
        if conn_uuid is None:
            conn_snapshot = list(self.active_connections.values())
        else:
            conn_snapshot = [conn_ws] if (conn_ws := self.active_connections.get(conn_uuid)) else []

        for ws in conn_snapshot:
            try:
                await ws.send_json({"action":action.value, "payload":action.convert(payload_data)})
            except Exception:
                pass