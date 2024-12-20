package net.akat.quest;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.akat.quest.listeners.NPCClickListener;
import net.akat.quest.listeners.QuestClickListener;
import net.akat.quest.listeners.QuestEventListener;
import net.akat.quest.managers.QuestManager;
import net.akat.quest.managers.QuestStateManager;
import net.akat.quest.models.Quest;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {

    private QuestManager questManager;
    private QuestStateManager questStateManager;
    private Economy economy;
    private List<Quest> allQuests;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().warning("Vault не найден!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        questStateManager = new QuestStateManager(getDataFolder());
        
        questManager = new QuestManager(this, economy, questStateManager);

        allQuests = new ArrayList<>(questManager.getQuests().values());
        
        getServer().getPluginManager().registerEvents(new QuestEventListener(allQuests, questStateManager), this);
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
}
