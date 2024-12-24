package net.akat.quest;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.akat.quest.listeners.NPCClickListener;
import net.akat.quest.listeners.QuestClickListener;
import net.akat.quest.listeners.QuestEventListener;
import net.akat.quest.managers.ConfigManager;
import net.akat.quest.managers.QuestManager;
import net.akat.quest.managers.QuestStateManager;
import net.akat.quest.models.Quest;
import net.akat.quest.utils.DependencyChecker;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {

    private QuestManager questManager; 
    private QuestStateManager questStateManager;
    private Economy economy;
    private List<Quest> allQuests;
    private DependencyChecker dependencyChecker;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
    	initializeConfigManager();

        if (!initializeEconomy()) {
            return;
        }

        initializeDependencies();

        initializeManagers();

        registerEventListeners();
    }

    private void initializeConfigManager() {
        configManager = new ConfigManager(this);
        configManager.loadConfig();
    }

    private boolean initializeEconomy() {
        if (!setupEconomy()) {
            getLogger().warning("Vault не найден! Плагин будет отключён.");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
        getLogger().info("Vault успешно настроен.");
        return true;
    }

    private void initializeDependencies() {
        dependencyChecker = new DependencyChecker(configManager.isMythicMobsEnabled());

        if (!dependencyChecker.isMythicMobsAvailable()) {
            getLogger().warning("MythicMobs функциональность отключена, так как плагин не найден.");
        } else {
            getLogger().info("MythicMobs функциональность включена и доступна.");
        }
    }

    private void initializeManagers() {
        questStateManager = new QuestStateManager(getDataFolder());
        questManager = new QuestManager(this, economy, questStateManager);
        allQuests = new ArrayList<>(questManager.getQuests().values());
    }

    private void registerEventListeners() {
    	getServer().getPluginManager().registerEvents(new QuestEventListener(allQuests, questStateManager, dependencyChecker, configManager.getMythicMobCreditTimes(), configManager.getDefaultCreditTime()), this);
        getServer().getPluginManager().registerEvents(new NPCClickListener(allQuests, questStateManager), this);
        getServer().getPluginManager().registerEvents(new QuestClickListener(questStateManager, questManager), this);
    }

    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
        return economy != null;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public List<Quest> getAllQuests() {
        return allQuests;
    }

    public QuestStateManager getQuestStateManager() {
        return questStateManager;
    }

    public DependencyChecker getDependencyChecker() {
        return dependencyChecker;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
}
