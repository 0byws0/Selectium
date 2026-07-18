from uuid import UUID
from enum import Enum

from src.database.mojang_api import MojangAPI

class Player:
    class Category(Enum):
        ALLY = 0; NEUTRAL = 1; TARGET = 2

        @property
        def color(self) -> int:
            match self:
                case self.ALLY: return 0x00AA00
                case self.NEUTRAL: return 0xFFAA00
                case self.TARGET: return 0xAA0000

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