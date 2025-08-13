package dev.king.livePlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LiveCommand implements CommandExecutor {

    private final LivePlugin plugin;
    private final LiveManager manager;

    public LiveCommand(LivePlugin plugin, LiveManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Solo player
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Questo comando può essere usato solo in gioco.");
            return true;
        }

        // Permesso base
        if (!player.hasPermission("live.use")) {
            player.sendMessage(plugin.color(plugin.getConfig().getString("messages.no-perm", "&cNon hai il permesso.")));
            return true;
        }

        // Nessun argomento -> help
        if (args.length == 0) {
            help(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "register" -> {
                if (args.length < 3) {
                    player.sendMessage(plugin.color(plugin.getConfig().getString("messages.help1",
                            "&eUsa: /live register <twitch|youtube|tiktok> <link>")));
                    return true;
                }

                String platform = args[1].toLowerCase();
                String link = args[2];

                // Piattaforma supportata?
                if (!manager.isSupportedPlatform(platform)) {
                    player.sendMessage(plugin.color(
                            plugin.getConfig().getString("messages.prefix", "") +
                                    plugin.getConfig().getString("messages.invalid-platform",
                                            "&cPiattaforma non valida! Usa: twitch, youtube o tiktok.")
                    ));
                    return true;
                }

                // Validazione link e salvataggio (fa anche save su disco)
                if (manager.registerLink(player, platform, link)) {
                    String msg = plugin.getConfig().getString("messages.registered",
                            "&aRegistrato link per &e%platform%&a: &e%link%");
                    player.sendMessage(plugin.color(
                            plugin.getConfig().getString("messages.prefix", "") +
                                    msg.replace("%platform%", platform).replace("%link%", link)
                    ));
                }
                return true;
            }

            case "on" -> {
                if (args.length < 2) {
                    player.sendMessage(plugin.color(plugin.getConfig().getString("messages.help2",
                            "&eUsa: /live on <piattaforma>")));
                    return true;
                }

                String platform = args[1].toLowerCase();

                // Controllo esplicito: se non hai link registrato per quella piattaforma, messaggio chiaro
                String link = manager.getLink(player, platform);
                if (link == null) {
                    String msg = plugin.getConfig().getString("messages.no-link-platform",
                            "&cNon hai registrato alcun link per &e%platform%&c. Usa &e/live register %platform% <link>");
                    player.sendMessage(plugin.color(
                            plugin.getConfig().getString("messages.prefix", "") +
                                    msg.replace("%platform%", platform)
                    ));
                    return true;
                }

                // Procede con l'attivazione (aggiunge gruppo LP e annuncia)
                manager.goLive(player, platform);
                return true;
            }

            case "off" -> {
                manager.stopLive(player);
                return true;
            }

            default -> {
                help(player);
                return true;
            }
        }
    }

    private void help(Player player) {
        player.sendMessage(plugin.color(plugin.getConfig().getString("messages.help1",
                "&eUsa: /live register <twitch|youtube|tiktok> <link>")));
        player.sendMessage(plugin.color(plugin.getConfig().getString("messages.help2",
                "&eUsa: /live on <piattaforma>")));
        player.sendMessage(plugin.color(plugin.getConfig().getString("messages.help3",
                "&eUsa: /live off")));
    }
}
