from fastapi import FastAPI, WebSocket, WebSocketDisconnect, WebSocketException, status, Depends
import uvicorn
from contextlib import asynccontextmanager
import asyncio
import json

from src.config import TOKEN
from src.database_manager import DatabaseManager
from src.bot import SelectiumManager
from src.player_handler import MojangAPI

class ConnectionManager:
    def __init__(self, db):
        self.active_connections: list[WebSocket] = []
        self.db = db

    async def connect(self, websocket: WebSocket):
        await websocket.accept()
        self.active_connections.append(websocket)

    async def disconnect(self, websocket: WebSocket):
        if websocket in self.active_connections: self.active_connections.remove(websocket)

    # async def broadcast_update(self, action: str, payload):
    #     message = json.dumps({"action": action, "payload":payload}, separators=(",", ":"))
    #
    #     async def send_message(conn):
    #         try:
    #             await conn.send(message)
    #         except Exception:
    #             await self.disconnect(conn)
    #
    #     if websocket_list := self.active_connections.copy():
    #         await asyncio.gather(*(send_message(websocket) for websocket in websocket_list), return_exceptions=True)

    async def broadcast(self,  websocket: WebSocket = None):
        players = await self.db.get_player()
        message = json.dumps({"action":"update_all_players",
                              "payload":{str(player_data.mc_uuid):
                                             {"shared":True,
                                              "type":player_data.cat.name,
                                              "nickname":player_data.alias or ""}
                                         for player_data in players}
                              }, separators=(",", ":")) #.encode("utf-8")

        async def send_message(conn):
            try:
                await conn.send_text(message)
            except Exception:
                await self.disconnect(conn)

        if websocket_list := (self.active_connections.copy() if websocket is None else [websocket]):
            await asyncio.gather(*(send_message(conn) for conn in websocket_list), return_exceptions=True)

database = DatabaseManager()
conn_manager = ConnectionManager(database)
bot = SelectiumManager(database, conn_manager)

@asynccontextmanager
async def lifespan(app):
    await MojangAPI.connect()
    await database.connect()
    bot_task = asyncio.create_task(bot.start(TOKEN, reconnect=True))

    yield

    await bot.close()
    await bot_task
    await database.close()
    await MojangAPI.close()

app = FastAPI(lifespan=lifespan)

async def verify_headers(websocket: WebSocket):
    conn_uuid = websocket.headers.get("uuid")
    conn_token = websocket.headers.get("token")

    if not conn_uuid or not conn_token: raise WebSocketException(code=status.WS_1008_POLICY_VIOLATION)

    if conn_token != "test": raise WebSocketException(code=status.WS_1008_POLICY_VIOLATION)


@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket, auth = Depends(verify_headers)):
    await conn_manager.connect(websocket)

    try:
        await conn_manager.broadcast(websocket)
        while True:
            data = await websocket.receive_text()
    except WebSocketDisconnect:
        await conn_manager.disconnect(websocket)

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=False)