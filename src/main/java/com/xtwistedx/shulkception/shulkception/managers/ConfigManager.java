package com.xtwistedx.shulkception.shulkception.managers;
import com.xtwistedx.shulkception.shulkception.ShulkceptionPlugin;
import org.bukkit.configuration.ConfigurationOptions;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final ShulkceptionPlugin plugin;
    public void setupConfig() {
        FileConfiguration config = plugin.getConfig();

        ConfigurationOptions options = config.options();
        options.copyDefaults();

        String testValue1 = config.getString("testValue1");

        plugin.saveDefaultConfig();
    }

    public ConfigManager(ShulkceptionPlugin plugin) {
        this.plugin = plugin;
    }
}
