import aiohttp

class MojangAPI:
    session = None

    @classmethod
    async def connect(cls):
        cls.session = aiohttp.ClientSession(timeout=aiohttp.ClientTimeout(10.0))

    @classmethod
    async def close(cls):
        await cls.session.close()

    @classmethod
    async def get_uuid(cls, username_str: str) -> str | None:
        try:
            async with cls.session.get(f"https://api.mojang.com/users/profiles/minecraft/{username_str}") as response:
                if response.status == 200: return (await response.json())["id"]
        except Exception:
            pass
        return None

    @classmethod
    async def get_username(cls, uuid_str: str) -> str | None:
        uuid_str = uuid_str.replace("-", "")
        try:
            async with cls.session.get(f"https://sessionserver.mojang.com/session/minecraft/profile/{uuid_str}") as response:
                if response.status == 200: return (await response.json())["name"]
        except Exception:
            pass
        return None