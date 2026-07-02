from fastapi import FastAPI
import uvicorn
from contextlib import asynccontextmanager
import asyncio

from src.config import log_listener, TOKEN
from src.database.player_handler import MojangAPI
from src.database.database_manager import DatabaseManager
from src.websocket.connection_manager import ConnectionManager
from src.websocket.router import ws_router
from src.discord.bot import SelectiumManager

@asynccontextmanager
async def lifespan(app):
    database = DatabaseManager()
    conn_manager = ConnectionManager(database)
    bot = SelectiumManager(database, conn_manager)
    app.state.database = database
    app.state.conn_manager = conn_manager

    log_listener.start()
    await MojangAPI.connect()
    await database.connect()
    bot_task = asyncio.create_task(bot.start(TOKEN, reconnect=True))

    yield

    await bot.close()
    bot_task.cancel()
    try:
        await bot_task
    except asyncio.CancelledError:
        pass
    await database.close()
    await MojangAPI.close()
    log_listener.stop()

app = FastAPI(lifespan=lifespan)
app.include_router(ws_router)

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=False, log_config=None)