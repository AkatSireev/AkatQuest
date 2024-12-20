package net.akat.quest.conditions;

import java.util.HashMap;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.akat.quest.managers.QuestStateManager;
import net.akat.quest.rewards.interfaces.QuestCondition;

public class KillMobCondition implements QuestCondition {

    private final EntityType mobType;
    private final int amount;

    public KillMobCondition(EntityType mobType, int amount) {
        this.mobType = mobType;
        this.amount = amount;
    }

    public EntityType getMobType() {
        return mobType;
    }

    public int getAmount() {
        return amount;
    }
    
    @Override
    public boolean isCompleted(Player player, String questId) {
        int killedMobs = getKilledMobsCount(player, questId);
        return killedMobs >= amount;
    }

    @Override
    public String getDescription() {
        return "Убейте " + amount + " мобов типа " + mobType.name();
    }

    public int getKilledMobsCount(Player player, String questId) {
        QuestStateManager stateManager = new QuestStateManager(player.getServer().getPluginManager().getPlugin("AkatQuest").getDataFolder());
        HashMap<String, Integer> progress = stateManager.loadQuestProgress(player, questId);

        return progress.getOrDefault(mobType.name(), 0);
    }
    
    public void updateProgress(Player player, int killedMobs, String questId) {
        QuestStateManager stateManager = new QuestStateManager(player.getServer().getPluginManager().getPlugin("AkatQuest").getDataFolder());
        HashMap<String, Integer> progress = stateManager.loadQuestProgress(player, questId);

        progress.put(mobType.name(), killedMobs);
        stateManager.saveQuestProgress(player, questId, progress);
    }  
}
