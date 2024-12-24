package net.akat.quest.conditions;

import java.util.HashMap;
import java.util.Optional;

import org.bukkit.entity.Player;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import net.akat.quest.managers.QuestStateManager;
import net.akat.quest.conditions.interfaces.QuestCondition;

public class KillMythicMobCondition implements QuestCondition {

    private final String mobName;
    private final int amount;

    public KillMythicMobCondition(String mobName, int amount) {
        this.mobName = mobName;
        this.amount = amount;
    }

    public String getMobName() {
        return mobName;
    }

    public int getAmount() {
        return amount;
    }
    
    @Override
    public boolean isCompleted(Player player, String questId) {
        int killedMobs = getKilledMythicMobsCount(player, questId);
        return killedMobs >= amount;
    }

    @Override
    public String getDescription() {
    	String customMobName = getCustomMobName();
        return "Убить " + amount + " мобов с именем " + customMobName;
    }

    public int getKilledMythicMobsCount(Player player, String questId) {
        QuestStateManager stateManager = new QuestStateManager(player.getServer().getPluginManager().getPlugin("AkatQuest").getDataFolder());
        HashMap<String, Integer> progress = stateManager.loadQuestProgress(player, questId);

        return progress.getOrDefault(mobName, 0);
    }
    
    public void updateProgress(Player player, int killedMobs, String questId) {
        QuestStateManager stateManager = new QuestStateManager(player.getServer().getPluginManager().getPlugin("AkatQuest").getDataFolder());
        HashMap<String, Integer> progress = stateManager.loadQuestProgress(player, questId);

        progress.put(mobName, killedMobs);
        stateManager.saveQuestProgress(player, questId, progress);
    }
    
    public String getCustomMobName() {
        Optional<MythicMob> optionalMob = MythicBukkit.inst().getMobManager().getMythicMob(mobName);
        if (optionalMob.isPresent()) {
            MythicMob mob = optionalMob.get();
            return mob.getDisplayName().toString();
        }
        return mobName;
    }
}
