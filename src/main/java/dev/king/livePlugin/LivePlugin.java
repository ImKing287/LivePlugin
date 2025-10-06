package dev.king.livePlugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class LivePlugin extends JavaPlugin {

    private File playersFile;
    private FileConfiguration playersConfig;
    private LiveManager liveManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setupPlayersConfig();

        liveManager = new LiveManager(this);


        var cmd = getCommand("live");
        if (cmd != null) {
            cmd.setExecutor(new LiveCommand(this, liveManager));
            cmd.setTabCompleter(new LiveTabCompleter(liveManager));
        } else {
            getLogger().severe("Comando 'live' non trovato nel plugin.yml!");
        }

        getLogger().info("LivePlugin abilitato.");
    }

    @Override
    public void onDisable() {

        liveManager.saveAll();
        getLogger().info("LivePlugin disabilitato.");
    }

    private void setupPlayersConfig() {
        playersFile = new File(getDataFolder(), "players.yml");
        if (!playersFile.exists()) {
            playersFile.getParentFile().mkdirs();
            try {
                playersFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Impossibile creare players.yml");
                e.printStackTrace();
            }
        }
        playersConfig = YamlConfiguration.loadConfiguration(playersFile);
        if (!playersConfig.isConfigurationSection("players")) {
            playersConfig.createSection("players");
            savePlayersConfig();
        }
    }

    public FileConfiguration getPlayersConfig() {
        return playersConfig;
    }

    public void savePlayersConfig() {
        try {
            playersConfig.save(playersFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String color(String text) {
        return text != null ? text.replace("&", "§") : "";
    }
}

