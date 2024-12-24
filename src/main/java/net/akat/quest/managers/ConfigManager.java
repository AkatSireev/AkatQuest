package net.akat.quest.managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import net.akat.quest.Main;

public class ConfigManager {

    private final Main plugin;
    private final Map<String, Long> mythicMobCreditTimes = new HashMap<>();
    private long defaultCreditTime;
    private boolean mythicMobsEnabled;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        mythicMobsEnabled = plugin.getConfig().getBoolean("mythicmobs-enable", true);
        defaultCreditTime = plugin.getConfig().getLong("mythicmobs.default-credit-time", 0);

        mythicMobCreditTimes.clear();
        if (plugin.getConfig().isConfigurationSection("mythicmobs.credit-times")) {
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("mythicmobs.credit-times");
            for (String mobType : section.getKeys(false)) {
                long time = section.getLong(mobType, 0);
                mythicMobCreditTimes.put(mobType, time);
            }
        }

        plugin.getLogger().info("Конфигурация успешно загружена.");
    }

    public boolean isMythicMobsEnabled() {
        return mythicMobsEnabled;
    }

    public long getDefaultCreditTime() {
        return defaultCreditTime;
    }

    public Map<String, Long> getMythicMobCreditTimes() {
        return new HashMap<>(mythicMobCreditTimes);
    }
}
