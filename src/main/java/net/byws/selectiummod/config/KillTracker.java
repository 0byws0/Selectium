package net.byws.selectiummod.config;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class KillTracker
{
    private record PatternTemplate(Pattern pattern)
    {
        public String match(String message)
        {
            Matcher matcher = pattern.matcher(message);
            if (!matcher.matches()) return null;

            String playerName = matcher.group("player");
            String killerName = matcher.group("killer");

            if (ConfigManager.getInstance().getPlayerNameList().get(killerName) != null)
            {
                return playerName;
            }

            if (!killerName.equals(Minecraft.getInstance().getUser().getName())) return null; // || killerName.equals(playerName)

            return playerName;
        }
    }

    private static final List<PatternTemplate> TEMPLATES = List.of(
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
    ).stream().map(KillTracker::parseTemplate).toList();

    public static void update(String message)
    {
        message = message.replaceAll("[^\\x20-\\x7E]+", "").trim();
        Minecraft.getInstance().gui.getChat().addMessage(Component.literal(message));

        for (PatternTemplate template : TEMPLATES)
        {
            if (ConfigManager.getInstance().addKill(template.match(message))) break;
        }
    }

    private static PatternTemplate parseTemplate(String killMsg)
    {
        StringBuilder regex = new StringBuilder(".*?");
        boolean hasPlayerTag = false;
        boolean hasKillerTag = false;

        int i = 0;
        while (i < killMsg.length())
        {
            if (killMsg.startsWith("<player>", i))
            {
                regex.append(hasPlayerTag ? "\\k<player>" : "(?<player>\\w{2,16})");
                hasPlayerTag = true;
                i += "<player>".length();
            }
            else if (killMsg.startsWith("<killer>", i))
            {
                regex.append(hasKillerTag ? "\\k<killer>" : "(?<killer>\\w{2,16})");
                hasKillerTag = true;
                i += "<killer>".length();
            }
            else
            {
                char c = killMsg.charAt(i);
                regex.append(Pattern.quote(String.valueOf(c)));
                if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) regex.append("?");
                ++i;
            }
        }
        regex.append(".*");

        return new PatternTemplate(Pattern.compile(regex.toString()));
    }
}