package dev.king.livePlugin;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class LiveManager {

    private final LivePlugin plugin;


    private final Map<UUID, Map<String, String>> links = new HashMap<>();

    private static final Set<String> SUPPORTED = Set.of("twitch", "youtube", "tiktok");

    public LiveManager(LivePlugin plugin) {
        this.plugin = plugin;
        loadAll();
    }



    public final void loadAll() {
        links.clear();
        FileConfiguration cfg = plugin.getPlayersConfig();
        if (!cfg.isConfigurationSection("players")) return;

        for (String uuidStr : cfg.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                Map<String, String> map = new HashMap<>();
                for (String platform : cfg.getConfigurationSection("players." + uuidStr).getKeys(false)) {
                    String link = cfg.getString("players." + uuidStr + "." + platform, "");
                    if (!link.isBlank()) map.put(platform.toLowerCase(Locale.ROOT), link);
                }
                if (!map.isEmpty()) links.put(uuid, map);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void saveAll() {
        FileConfiguration cfg = plugin.getPlayersConfig();
        cfg.set("players", null);
        for (Map.Entry<UUID, Map<String, String>> e : links.entrySet()) {
            String uuid = e.getKey().toString();
            for (Map.Entry<String, String> p : e.getValue().entrySet()) {
                cfg.set("players." + uuid + "." + p.getKey(), p.getValue());
            }
        }
        plugin.savePlayersConfig();
    }



    public boolean isSupportedPlatform(String p) {
        return SUPPORTED.contains(p.toLowerCase(Locale.ROOT));
    }

    public Map<String, String> getAllLinks(Player player) {
        return links.getOrDefault(player.getUniqueId(), Collections.emptyMap());
    }

    public String getLink(Player player, String platform) {
        return getAllLinks(player).get(platform.toLowerCase(Locale.ROOT));
    }

    public boolean registerLink(Player player, String platform, String link) {
        platform = platform.toLowerCase(Locale.ROOT);
        if (!isSupportedPlatform(platform)) return false;


        if (link == null || !link.matches("^(https?://)\\S+\\.[\\w\\-]+.*$")) {
            player.sendMessage(plugin.color(
                    plugin.getConfig().getString("messages.prefix", "") +
                            plugin.getConfig().getString("messages.invalid-link")));
            return false;
        }

        links.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(platform, link);
        saveAll();
        return true;
    }



    public void goLive(Player player, String platform) {
        platform = platform.toLowerCase(Locale.ROOT);
        if (!isSupportedPlatform(platform)) {
            player.sendMessage(plugin.color(plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.invalid-platform")));
            return;
        }

        String link = getLink(player, platform);
        if (link == null) {
            String msg = plugin.getConfig().getString("messages.no-link-platform");
            player.sendMessage(plugin.color(plugin.getConfig().getString("messages.prefix", "") +
                    msg.replace("%platform%", platform)));
            return;
        }


        var group = plugin.getConfig().getString("live-group", "live");
        var lp = tryGetLuckPerms(player);
        if (lp == null) return;

        User user = lp.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            player.sendMessage(plugin.color("&cImpossibile ottenere l'utente LuckPerms."));
            return;
        }

        user.data().add(InheritanceNode.builder(group).build());
        lp.getUserManager().saveUser(user);

        String msg = plugin.getConfig().getString("messages.start");
        Bukkit.broadcastMessage(plugin.color(
                plugin.getConfig().getString("messages.prefix", "") +
                        msg.replace("%player%", player.getName())
                                .replace("%platform%", platform)
                                .replace("%link%", link)
        ));
    }

    public void stopLive(Player player) {
        var group = plugin.getConfig().getString("live-group", "live");
        var lp = tryGetLuckPerms(player);
        if (lp == null) return;

        User user = lp.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            player.sendMessage(plugin.color("&cImpossibile ottenere l'utente LuckPerms."));
            return;
        }

        user.data().remove(InheritanceNode.builder(group).build());
        lp.getUserManager().saveUser(user);

        String msg = plugin.getConfig().getString("messages.stop");
        Bukkit.broadcastMessage(plugin.color(
                plugin.getConfig().getString("messages.prefix", "") +
                        msg.replace("%player%", player.getName())
        ));
    }



    private net.luckperms.api.LuckPerms tryGetLuckPerms(Player player) {
        try {
            return LuckPermsProvider.get();
        } catch (IllegalStateException e) {
            player.sendMessage(plugin.color(
                    plugin.getConfig().getString("messages.prefix", "") +
                            plugin.getConfig().getString("messages.luckperms-missing")));
            return null;
        }
    }
}

