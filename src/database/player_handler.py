import aiohttp
from uuid import UUID
from enum import Enum

class MojangAPI:
    session = None

    @classmethod
    async def connect(cls):
        cls.session = aiohttp.ClientSession(timeout=aiohttp.ClientTimeout(5.0))

    @classmethod
    async def close(cls):
        await cls.session.close()

    @classmethod
    async def get_uuid(cls, username_str: str) -> str | None:
        try:
            async with cls.session.get(
                    f"https://api.mojang.com/users/profiles/minecraft/{username_str}") as response:
                if response.status == 200: return (await response.json())["id"]

                else: print(response.status)
        except Exception as e: # aiohttp.ClientError
            print(e)
            pass
        return None

    @classmethod
    async def get_username(cls, uuid_str: str) -> str | None:
        uuid_str = uuid_str.replace("-", "")
        try:
            async with cls.session.get(
                    f"https://sessionserver.mojang.com/session/minecraft/profile/{uuid_str}") as response:
                if response.status == 200: return (await response.json())["name"]

                else: print(response.status)
        except Exception as e: # aiohttp.ClientError
            print(e)
            pass
        return None

class Player:
    class Category(Enum):
        ALLY = 0; NEUTRAL = 1; TARGET = 2

        @property
        def color(self) -> int:
            match self:
                case self.ALLY: return 0x00FF00
                case self.NEUTRAL: return 0xFFFF00
                case self.TARGET: return 0xFF0000

    def __init__(self, mc_uuid: UUID | None, mc_name: str | None, cat: Category, org: str | None,
                 alias: str | None, dc_id: int | None, token_hash: bytes | None):
        self.mc_uuid = mc_uuid
        self.mc_name = mc_name
        self.cat = cat
        self.org = org
        self.alias = alias
        self.dc_id = dc_id
        self.token_hash = token_hash

    @classmethod
    async def create(cls, mc_uuid: UUID | None, mc_name: str | None, cat: Category, org: str = None,
                     alias: str = None, dc_id: int = None, token_hash: bytes = None):
        if not mc_uuid and not mc_name:
            raise TypeError("Both UUID and username not provided")

        if mc_uuid is None:
            if uuid_str := await MojangAPI.get_uuid(mc_name):
                mc_uuid = UUID(uuid_str)
            else:
                raise ValueError("Player username invalid")

        if name_str := await MojangAPI.get_username(str(mc_uuid)):
            mc_name = name_str
        else:
            raise ValueError("Player UUID invalid")

        return cls(mc_uuid, mc_name, cat, org, alias, dc_id, token_hash)

    async def update(self):
        if name_str := await MojangAPI.get_username(str(self.mc_uuid)):
            self.mc_name = name_str