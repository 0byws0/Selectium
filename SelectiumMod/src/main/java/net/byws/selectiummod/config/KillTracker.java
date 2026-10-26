package net.byws.selectiummod.config;

import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class KillTracker
{
    private static final Pattern NON_ASCII = Pattern.compile("[^\\x20-\\x7E]+");
    private static final List<Pattern> KILL_PATTERNS = List.of(
                    "<player> was slain by <killer>.",
                    "<player> was killed by <killer> using magic.",
                    "<killer> OwO'd <player> to death!",
                    "<killer> used <player> as a punching bag.",
                    "<player> was murdered by <killer>!",
                    "<killer> flabbergasted <player> with death.",
                    "<player> was bombarded by <killer>.",
                    "<player> got a face lift by <killer>.",
                    "<killer> killed <player>. Now now... don't be salty!",
                    "<killer> killed <player>. LOL!",
                    "<killer> betrayed <player>.",
                    "<player> was pummelled by <killer>'s Kung Fu moves.",
                    "<player> was smacked into next week by <killer>.",
                    "<player> has been sent into the afterlife by <killer>.",
                    "<killer> bought <player> a gift... Oh wait, it's just death.",
                    "<killer> gave <player> the L!",
                    "<killer> sent <player> into a deep sleep!",
                    "<killer> made <player> disappear.",
                    "<player> was annihilated by <killer>.",
                    "<killer> sent <player> to the underworld.",
                    "<player> was poked to death by <killer>.",
                    "<killer> went too hard on <player>.",
                    "<player> got turned into dust by <killer>.",
                    "<player> was RKO'd out of nowhere by <killer>.",
                    "<killer> tucked <player> in for a dirt nap.",
                    "<killer> and <player> settled their differences and became friends... Just kidding, <killer> killed <player>.",
                    "<killer> had to register their hands as deadly weapons after what they did to <player>...",
                    "<killer> went medieval on <player>.",
                    "<player> got snapped away by <killer>.",
                    "<killer> killed <player>. RIP!",
                    "<killer> destroyed <player> into oblivion.",
                    "<killer> sent <player> to the shadow realm.",
                    "<killer> slapped <player> to death!",
                    "<killer> wanted to show <player> something cool... DEATH!",
                    "<killer> sent <player> into a deep freeze.",
                    "<killer> wiped the floor with <player>.",
                    "<killer> was forced to kill <player>.",
                    "<killer> turned <player> into a spooky ghost!",
                    "<killer> showcased their weapon to <player>.",
                    "<player> was finished by <killer>. Fatality!",
                    "<killer> yeeted their weapon into <player>.",
                    "<killer> brawled <player> to death.",
                    "<killer> UwU'd <player> to death!",
                    "<player> was tickled to death by <killer>.",
                    "<player> was sniped by <killer>.",
                    "<player> was shot by <killer>.",
                    "<killer> gave <player> all of their projectiles!",
                    "<killer> showed <player> their Katniss Everdeen impression.",
                    "<player> was stuck with a projectile by <killer>.",
                    "<killer> stuck their projectile into <player>.",
                    "<killer> pew pew'd <player>.",
                    "<player> was impaled by <killer>.",
                    "<player> was shot through the heart and <killer> is to blame.",
                    "<player> isn't the best at dodging <killer>'s projectile.",
                    "<killer> used <player> for target practice.",
                    "<killer> sniped <player>. HEADSHOT!",
                    "<killer> went all Katniss Everdeen on <player>.",
                    "<killer> turned <player> into Swiss cheese.",
                    "<killer> yeeted a projectile at <player>.",
                    "<player> just got BULLSEYED by <killer>!",
                    "<player> was slapped by a projectile from <killer>.",
                    "<killer> got an epic shot on <player>.",
                    "<player> thought they could survive in the void while escaping <killer>.",
                    "<player> died in the void while escaping <killer>.",
                    "<player> was sucked into a black hole while <killer> watched.",
                    "<killer> assisted <player> with their void-trauma.",
                    "<killer> kicked <player> into the void.",
                    "<player> was pulled into the void by <killer>'s pet space kraken.",
                    "<killer> tripped <player> into the void.",
                    "<killer> sent <player> into the Upside Down.",
                    "<player> tried being a ninja against <killer> but it didn't turn out so well...",
                    "<killer> kicked <player> into the abyss.",
                    "<player> was too scared to fight <killer>.",
                    "<killer> made <player> jump into the void.",
                    "<killer> screamed \"This is Sparta!\" before kicking <player> into the void.",
                    "<player> got sent into the void by <killer>.",
                    "<killer> blew <player> into the void.",
                    "<player> was slapped into the void by <killer>.",
                    "<killer> scared <player> into the void."
            ).stream().map(KillTracker::compileTemplate).toList();

    private static KillTracker instance;

    private int serverSideKillCount;

    public static void init()
    {
        if (instance != null) return;
        instance = new KillTracker();
    }

    public static KillTracker getInstance()
    {
        if (instance == null) throw new IllegalStateException("KillTracker not initialized");
        return instance;
    }

    public int serverSideKills()
    {
        ConfigManager configManager = ConfigManager.getInstance();
        int temp = serverSideKillCount;

        serverSideKillCount = 0;
        configManager.setKills(configManager.getKills(false) + temp, false);

        return temp;
    }

    public void update(String message)
    {
        String username = Minecraft.getInstance().getUser().getName();
        message = NON_ASCII.matcher(message).replaceAll("").trim();
        if (!message.contains(username)) return;

        ConfigManager configManager = ConfigManager.getInstance();

        for (Pattern pattern : KILL_PATTERNS)
        {
            Matcher matcher = pattern.matcher(message);
            if (!matcher.find()) continue;

            String killerName = matcher.group("killer");
            String playerName = matcher.group("player");
            if (!username.equals(killerName) || killerName.equals(playerName)) continue;

            ConfigJsonObj.PlayerData player = configManager.getPlayerData(playerName);
            if (player == null) continue;

            if (ConfigJsonObj.Category.TARGET.equals(player.clientType))
            {
                configManager.setKills(configManager.getKills(true) + 1, true);
            }
            if (ConfigJsonObj.Category.TARGET.equals(player.serverType)) ++serverSideKillCount;

            break;
        }
    }

    private static Pattern compileTemplate(String template)
    {
        StringBuilder regex = new StringBuilder();
        boolean hasPlayer = false;
        boolean hasKiller = false;

        int index = 0;
        while (index < template.length())
        {
            if (template.startsWith("<player>", index))
            {
                regex.append(hasPlayer ? "\\k<player>" : "(?<player>\\w{2,16})");
                hasPlayer = true;
                index += "<player>".length();
            }
            else if (template.startsWith("<killer>", index))
            {
                regex.append(hasKiller ? "\\k<killer>" : "(?<killer>\\w{2,16})");
                hasKiller = true;
                index += "<killer>".length();
            }
            else
            {
                char character = template.charAt(index++);
                regex.append(Pattern.quote(String.valueOf(character)));
                if (!Character.isLetterOrDigit(character) && !Character.isWhitespace(character)) regex.append('?');
            }
        }

        return Pattern.compile(regex.toString());
    }
}