from fastapi import FastAPI, WebSocket, WebSocketDisconnect, WebSocketException, status, Depends
import uvicorn
from contextlib import asynccontextmanager
import asyncio
import secrets

from src.config import TOKEN
from src.database.database_manager import DatabaseManager
from src.websocket.connection_manager import ConnectionManager
from src.discord.bot import SelectiumManager
from src.database.player_handler import MojangAPI

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

DUMMY_TOKEN = DatabaseManager.Fields.TOKEN.convert(secrets.token_hex(32))
app = FastAPI(lifespan=lifespan)

async def verify_headers(websocket: WebSocket):
    conn_uuid = websocket.headers.get("uuid")
    conn_token = websocket.headers.get("token")

    if not conn_uuid or not conn_token: raise WebSocketException(code=status.WS_1008_POLICY_VIOLATION)

    player_data = await database.get_player(DatabaseManager.Fields.UUID, conn_uuid)

    player_valid = bool(player_data)
    token_valid = secrets.compare_digest(DatabaseManager.Fields.TOKEN.convert(conn_token),
                                         (player_data[0].token_hash if player_valid else None) or DUMMY_TOKEN)

    if not player_valid or not token_valid:
        raise WebSocketException(code=status.WS_1008_POLICY_VIOLATION)

@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket, auth = Depends(verify_headers)):
    await asyncio.sleep(3) # remove later
    await conn_manager.connect(websocket)

    try:
        await conn_manager.init_broadcast(websocket)
        while True:
            data = await websocket.receive_text()
    except WebSocketDisconnect:
        await conn_manager.disconnect(websocket)

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=False)