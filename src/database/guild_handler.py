from dataclasses import dataclass

@dataclass
class GuildConfig:
    guild_msg_id: int | None
    leaderboard_msg_id: int | None
    ally_msg_id: int | None
    neutral_msg_id: int | None
    target_msg_id: int | None