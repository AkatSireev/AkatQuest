package net.akat.quest.listeners;

import java.util.List;
import java.util.Optional;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.akat.quest.conditions.KillMythicMobCondition;
import net.akat.quest.conditions.KillMobCondition;
import net.akat.quest.managers.QuestStateManager;
import net.akat.quest.models.Quest;
import net.akat.quest.models.state.QuestState;
import net.akat.quest.rewards.interfaces.QuestCondition;
import net.akat.quest.utils.DependencyChecker;

public class QuestEventListener implements Listener {

    private final List<Quest> quests;
    private final QuestStateManager questStateManager;
    private final DependencyChecker dependencyChecker;

    public QuestEventListener(List<Quest> quests, QuestStateManager questStateManager, DependencyChecker dependencyChecker) {
        this.quests = quests;
        this.questStateManager = questStateManager;
        this.dependencyChecker = dependencyChecker;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) {
            return;
        }

        Player player = event.getEntity().getKiller();
        Entity killedEntity = event.getEntity();

        for (Quest quest : quests) {
            QuestState questState = questStateManager.loadQuestState(player, quest.getId());

            if (questState != QuestState.IN_PROGRESS) {
                continue;
            }

            for (QuestCondition condition : quest.getConditions()) {
                if (condition instanceof KillMobCondition) {
                    KillMobCondition mobCondition = (KillMobCondition) condition;
                    if (mobCondition.getMobType() == killedEntity.getType()) {
                        updateQuestProgress(player, mobCondition, quest.getId());
                    }
                } else if (condition instanceof KillMythicMobCondition) {
                	if (!dependencyChecker.isMythicMobsAvailable()) {
                        continue;
                    }
                	
                    KillMythicMobCondition mythicMobCondition = (KillMythicMobCondition) condition;

                    Optional<MythicMob> mythicMob = MythicBukkit.inst().getMobManager().getMythicMob(mythicMobCondition.getMobName());

                    if (mythicMob.isPresent() && killedEntity instanceof org.bukkit.entity.LivingEntity) {
                        Optional<ActiveMob> activeMob = MythicBukkit.inst().getMobManager().getActiveMob(killedEntity.getUniqueId());
                        if (activeMob.isPresent()) {
                            ActiveMob active = activeMob.get();
                            if (active.getMobType().equals(mythicMobCondition.getMobName())) {
                                updateQuestProgress(player, mythicMobCondition, quest.getId());
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateQuestProgress(Player player, KillMobCondition mobCondition, String questId) {
        int killedMobs = mobCondition.getKilledMobsCount(player, questId) + 1;
        mobCondition.updateProgress(player, killedMobs, questId);
    }

    private void updateQuestProgress(Player player, KillMythicMobCondition mythicMobCondition, String questId) {
        int killedMobs = mythicMobCondition.getKilledMythicMobsCount(player, questId) + 1;
        mythicMobCondition.updateProgress(player, killedMobs, questId);
    }
}

