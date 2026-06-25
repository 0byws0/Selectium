import aiosqlite
from enum import Enum
from uuid import UUID
import hashlib

from src.database.player_handler import Player

class DatabaseManager:
    class Fields(Enum):
        UUID = "mc_uuid"
        USERNAME = "mc_name"
        CATEGORY = "cat"
        CLAN = "org"
        ALT = "alias"
        DISCORD = "dc_id"
        TOKEN = "token_hash"

        def convert(self, field_input):
            if field_input is None: return None
            try:
                match self:
                    case self.UUID:
                        return field_input.bytes if isinstance(field_input, UUID) else UUID(field_input).bytes

                    case self.USERNAME | self.CLAN | self.ALT:
                        return str(field_input)

                    case self.CATEGORY:
                        try:
                            return Player.Category[field_input].name
                        except KeyError:
                            return Player.Category(field_input).name

                    case self.DISCORD:
                        return int(field_input)

                    case self.TOKEN:
                        return field_input if (isinstance(field_input, bytes) and len(field_input) == 32) else \
                            (hashlib.sha256(field_input.encode("utf-8") if isinstance(field_input, str) else field_input).digest())

            except (ValueError, TypeError, KeyError, AttributeError):
                return None

        def collation(self):
            return "COLLATE NOCASE" if self in (self.USERNAME, self.ALT, self.CLAN) else ""

    def __init__(self, db_path: str = "data/database.db"):
        self.conn = None
        self.db_path = db_path

    async def connect(self):
        self.conn = await aiosqlite.connect(self.db_path)

        await self.conn.execute("PRAGMA journal_mode=WAL")
        await self.conn.execute(f"""CREATE TABLE IF NOT EXISTS players (
                        {self.Fields.UUID.value} BLOB PRIMARY KEY,
                        {self.Fields.USERNAME.value} TEXT NOT NULL,
                        {self.Fields.CATEGORY.value} TEXT CHECK ({self.Fields.CATEGORY.value} IN ({", ".join(f"'{cat.name}'" for cat in Player.Category)})),
                        {self.Fields.CLAN.value} TEXT,
                        {self.Fields.ALT.value} TEXT,
                        {self.Fields.DISCORD.value} INTEGER,
                        {self.Fields.TOKEN.value} BLOB)""")
        await self.conn.execute(f"""CREATE TABLE IF NOT EXISTS lists (
                                server_id INTEGER PRIMARY KEY,
                                ally_msg_id INTEGER,
                                neutral_msg_id INTEGER,
                                target_msg_id INTEGER)""")
        await self.conn.commit()

    async def close(self):
        await self.conn.close()

    async def insert_player(self, player_data: Player):
        cur = await self.conn.execute(f"INSERT INTO players "
                                      f"VALUES (:mc_uuid, :mc_name, :cat, :org, :alias, :dc_id, :token_hash) "
                                      f"ON CONFLICT ({self.Fields.UUID.value}) DO NOTHING",
                                      {"mc_uuid": player_data.mc_uuid.bytes,
                                       "mc_name": player_data.mc_name,
                                       "cat": player_data.cat.name,
                                       "org": player_data.org,
                                       "alias": player_data.alias,
                                       "dc_id": player_data.dc_id,
                                       "token_hash":player_data.token_hash})

        inserted = (cur.rowcount != 0)
        await self.conn.commit()

        return inserted

    async def update_player(self, field: Fields, field_input, **updates):
        if not isinstance(field, self.Fields): raise TypeError("field is not a valid Fields enum")

        field_input = field.convert(field_input)
        database_columns = [f.value for f in self.Fields]
        set_clauses = []
        payload = {"input":field_input}

        for column, col_input in updates.items():
            if column in database_columns:
                set_clauses.append(f"{column}=:{column}")
                payload[column] = self.Fields(column).convert(col_input)

        if not set_clauses: raise TypeError("No parameters provided")

        cur = await self.conn.execute(f"UPDATE players SET "
                    f"{', '.join(set_clauses)} "
                    f"WHERE {field.value}=:input "
                    f"{field.collation()} "
                    f"RETURNING *",
                    payload)

        rows = await cur.fetchall()
        await self.conn.commit()

        return [Player(UUID(bytes=row[0]), row[1], Player.Category[row[2]], row[3], row[4], row[5], row[6]) for row in rows]

    # async def get_field(self, field: Fields):
    #     if not isinstance(field, self.Fields): raise TypeError("field is not a valid Fields enum")
    #     cur = await self.conn.execute(f"SELECT DISTINCT {field.value} from players "
    #                                   f"WHERE {field.value} IS NOT NULL")
    #     return [row[0] for row in await cur.fetchall()]

    async def get_player(self, field: Fields = None, field_input = None):
        if field is None:
            cur = await self.conn.execute(f"SELECT * FROM players ORDER BY {self.Fields.UUID.value}")
        else:
            if not isinstance(field, self.Fields): raise TypeError("field is not a valid Fields enum")

            field_input = field.convert(field_input)
            cur = await self.conn.execute(f"SELECT * FROM players "
                        f"WHERE {field.value}=:input "
                        f"{field.collation()} "
                        f"ORDER BY {self.Fields.UUID.value}",
                        {"input": field_input})

        return [Player(UUID(bytes=row[0]), row[1], Player.Category[row[2]], row[3], row[4], row[5], row[6]) for row in await cur.fetchall()]

    async def remove_player(self, field: Fields, field_input):
        if not isinstance(field, self.Fields): raise TypeError("field is not a valid Fields enum")

        field_input = field.convert(field_input)
        cur = await self.conn.execute(f"DELETE FROM players "
                    f"WHERE {field.value}=:input "
                    f"{field.collation()} "
                    f"RETURNING *",
                    {"input":field_input})

        rows = await cur.fetchall()
        await self.conn.commit()

        return [Player(UUID(bytes=row[0]), row[1], Player.Category[row[2]], row[3], row[4], row[5], row[6]) for row in rows]

    async def insert_list_id(self, server_id: int, ally_msg_id: int = None, neutral_msg_id: int = None, target_msg_id: int = None):
        if ally_msg_id is None and neutral_msg_id is None and target_msg_id is None: raise TypeError("No parameters provided")

        await self.conn.execute("INSERT INTO lists "
                                "VALUES (:server_id, :ally_msg_id, :neutral_msg_id, :target_msg_id) "
                                "ON CONFLICT (server_id) DO UPDATE SET "
                                "ally_msg_id=COALESCE(:ally_msg_id, ally_msg_id), "
                                "neutral_msg_id=COALESCE(:neutral_msg_id, neutral_msg_id), "
                                "target_msg_id=COALESCE(:target_msg_id, target_msg_id)",
                                {"server_id": server_id,
                                 "ally_msg_id": ally_msg_id,
                                 "neutral_msg_id": neutral_msg_id,
                                 "target_msg_id": target_msg_id})
        await self.conn.commit()

    async def get_list_id(self, server_id: int):
        cur = await self.conn.execute("SELECT * FROM lists "
                                      "WHERE server_id=:server_id",
                                      {"server_id": server_id})
        return row[1:] if (row := await cur.fetchone()) else None