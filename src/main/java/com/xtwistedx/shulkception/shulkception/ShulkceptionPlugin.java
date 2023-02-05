package com.xtwistedx.shulkception.shulkception;
import com.xtwistedx.shulkception.shulkception.events.InventoryEvents;
import com.xtwistedx.shulkception.shulkception.managers.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ShulkceptionPlugin extends JavaPlugin {
    private static ShulkceptionPlugin plugin;
    public static ShulkceptionPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
//        ConfigManager configManager = new ConfigManager(this);
//        configManager.setupConfig();
        new InventoryEvents(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
