package dev.king.livePlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class LiveTabCompleter implements TabCompleter {

    private final LiveManager manager;
    private static final List<String> SUBS = List.of("register", "on", "off");
    private static final List<String> PLATFORMS = List.of("twitch", "youtube", "tiktok");

    public LiveTabCompleter(LiveManager manager) {
        this.manager = manager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        if (args.length == 1) {
            return filter(SUBS, args[0]);
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("register")) {
                return filter(PLATFORMS, args[1]);
            }
            if (args[0].equalsIgnoreCase("on")) {
                // Suggerisci solo piattaforme che il player ha registrato
                var owned = new ArrayList<>(manager.getAllLinks(player).keySet());
                if (owned.isEmpty()) owned.addAll(PLATFORMS);
                return filter(owned, args[1]);
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("register")) {
            // Suggerisci l'inizio del link per comodità
            return filter(List.of("https://", "http://"), args[2]);
        }

        return Collections.emptyList();
    }

    private List<String> filter(Collection<String> options, String typed) {
        String t = typed == null ? "" : typed.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String s : options) if (s.toLowerCase(Locale.ROOT).startsWith(t)) out.add(s);
        return out;
    }
}
