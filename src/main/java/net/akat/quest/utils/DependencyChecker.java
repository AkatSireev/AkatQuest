package net.akat.quest.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class DependencyChecker {

    private final boolean isMythicMobsEnabled;

    private boolean isMythicMobsAvailable;

    public DependencyChecker(boolean isMythicMobsEnabled) {
        this.isMythicMobsEnabled = isMythicMobsEnabled;
        checkDependencies();
    }

    private void checkDependencies() {
        if (isMythicMobsEnabled) {
            isMythicMobsAvailable = checkPlugin("MythicMobs");
            if (!isMythicMobsAvailable) {
                Bukkit.getLogger().severe("MythicMobs не найден в системе, однако он был активирован в настройках. Пожалуйста, установите его.");
            }
        }
    }

    private boolean checkPlugin(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }

    public boolean isMythicMobsAvailable() {
        return isMythicMobsAvailable;
    }
}
