import aiohttp
from typing import Optional
from uuid import UUID
from enum import Enum

class Player:
    session = None

    class Category(Enum):
        ALLY = 0; NEUTRAL = 1; TARGET = 2

        @property
        def color(self) -> int:
            match self:
                case self.ALLY: return 0x00FF00
                case self.NEUTRAL: return 0xFFFF00
                case self.TARGET: return 0xFF0000

    @classmethod
    async def connect(cls):
        cls.session = aiohttp.ClientSession()

    def __init__(self, mc_uuid: Optional[UUID], mc_name: Optional[str], cat: Category, org: Optional[str], alias: Optional[str], dc_id: Optional[int]):
        self.mc_uuid = mc_uuid
        self.mc_name = mc_name
        self.cat = cat
        self.org = org
        self.alias = alias
        self.dc_id = dc_id

    @classmethod
    async def create(cls, mc_uuid: Optional[UUID], mc_name: Optional[str], cat: Category, org: str = None, alias: str = None, dc_id: int = None):
        if not mc_uuid and not mc_name:
            raise TypeError("Both UUID and username not provided")

        if mc_uuid is None:
            async with Player.session.get(f"https://api.mojang.com/users/profiles/minecraft/{mc_name}") as response:
                if response.status != 200:
                    raise ValueError("Player username invalid")
                mc_uuid = UUID((await response.json())["id"])

        uuid_str = str(mc_uuid).replace("-", "")
        async with Player.session.get(f"https://sessionserver.mojang.com/session/minecraft/profile/{uuid_str}") as response:
            if response.status != 200:
                raise ValueError("Player UUID invalid")
            mc_name = (await response.json())["name"]

        return cls(mc_uuid, mc_name, cat, org, alias, dc_id)

    async def update(self):
        uuid_str = str(self.mc_uuid).replace("-", "")
        async with Player.session.get(f"https://sessionserver.mojang.com/session/minecraft/profile/{uuid_str}") as response:
            if response.status == 200: self.mc_name = (await response.json())["name"]

    @classmethod
    async def close(cls):
        await cls.session.close()