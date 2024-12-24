package net.akat.quest.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.akat.quest.conditions.KillMythicMobCondition;
import net.akat.quest.conditions.KillMobCondition;
import net.akat.quest.managers.QuestStateManager;
import net.akat.quest.models.Quest;
import net.akat.quest.models.state.QuestState;
import net.akat.quest.conditions.interfaces.QuestCondition;
import net.akat.quest.utils.DamageTracker;
import net.akat.quest.utils.DependencyChecker;

public class QuestEventListener implements Listener {

    private final List<Quest> quests;
    private final QuestStateManager questStateManager;
    private final DependencyChecker dependencyChecker;
    private final Map<String, Long> mythicMobCreditTimes = new HashMap<>();
    private final Map<UUID, DamageTracker> damageTrackers = new HashMap<>();
    private final long defaultCreditTime;

    public QuestEventListener(
        List<Quest> quests,
        QuestStateManager questStateManager,
        DependencyChecker dependencyChecker,
        Map<String, Long> mythicMobCreditTimes,
        long defaultCreditTime
    ) {
        this.quests = quests;
        this.questStateManager = questStateManager;
        this.dependencyChecker = dependencyChecker;
        this.mythicMobCreditTimes.putAll(mythicMobCreditTimes);
        this.defaultCreditTime = defaultCreditTime;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        Player player = (Player) event.getDamager();
        LivingEntity entity = (LivingEntity) event.getEntity();

        Optional<ActiveMob> activeMob = MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId());
        if (activeMob.isPresent()) {
            String mobType = activeMob.get().getMobType();
            long creditTimeMillis = mythicMobCreditTimes.getOrDefault(mobType, defaultCreditTime) * 1000;

            if (creditTimeMillis > 0) {
                DamageTracker tracker = damageTrackers.computeIfAbsent(entity.getUniqueId(), uuid -> new DamageTracker(creditTimeMillis));
                tracker.recordDamage(player);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity killedEntity = event.getEntity();
        Optional<ActiveMob> activeMob = MythicBukkit.inst().getMobManager().getActiveMob(killedEntity.getUniqueId());

        if (activeMob.isPresent()) {
            String mobType = activeMob.get().getMobType();
            long creditTimeMillis = mythicMobCreditTimes.getOrDefault(mobType, defaultCreditTime) * 1000;

            if (creditTimeMillis == 0) {
                Player killer = killedEntity.getKiller();
                if (killer != null) {
                    handleQuestProgressForPlayer(killer, killedEntity);
                }
                return;
            }

            DamageTracker tracker = damageTrackers.remove(killedEntity.getUniqueId());
            if (tracker != null) {
                for (Player player : tracker.getEligiblePlayers()) {
                    handleQuestProgressForPlayer(player, killedEntity);
                }
            }
        }
    }

    private void handleQuestProgressForPlayer(Player player, Entity killedEntity) {
        for (Quest quest : quests) {
            QuestState questState = questStateManager.loadQuestState(player, quest.getId());
            if (questState != QuestState.IN_PROGRESS) {
                continue;
            }
            processQuestConditions(player, killedEntity, quest);
        }
    }

    private void processQuestConditions(Player player, Entity killedEntity, Quest quest) {
        for (QuestCondition condition : quest.getConditions()) {
            if (condition instanceof KillMobCondition) {
                handleKillMobCondition(player, killedEntity, (KillMobCondition) condition, quest.getId());
            } else if (condition instanceof KillMythicMobCondition) {
                handleKillMythicMobCondition(player, killedEntity, (KillMythicMobCondition) condition, quest.getId());
            }
        }
    }

    private void handleKillMobCondition(Player player, Entity killedEntity, KillMobCondition mobCondition, String questId) {
        if (mobCondition.getMobType() == killedEntity.getType()) {
            updateQuestProgress(player, mobCondition, questId);
            player.sendMessage("Вы убили " + killedEntity.getType().name() + " для выполнения квеста.");
        }
    }

    private void handleKillMythicMobCondition(Player player, Entity killedEntity, KillMythicMobCondition mythicMobCondition, String questId) {
        if (!dependencyChecker.isMythicMobsAvailable()) {
            return;
        }

        Optional<MythicMob> mythicMob = MythicBukkit.inst().getMobManager().getMythicMob(mythicMobCondition.getMobName());
        if (mythicMob.isPresent() && killedEntity instanceof LivingEntity) {
            Optional<ActiveMob> activeMob = MythicBukkit.inst().getMobManager().getActiveMob(killedEntity.getUniqueId());
            if (activeMob.isPresent()) {
                ActiveMob active = activeMob.get();
                if (active.getMobType().equals(mythicMobCondition.getMobName())) {
                    updateQuestProgress(player, mythicMobCondition, questId);
                    String customMobName = getCustomMobName(mythicMobCondition.getMobName());
                    player.sendMessage("Вы убили " + customMobName + " для выполнения квеста.");
                }
            }
        }
    }
    
    private String getCustomMobName(String mobName) {
        Optional<MythicMob> optionalMob = MythicBukkit.inst().getMobManager().getMythicMob(mobName);
        if (optionalMob.isPresent()) {
            MythicMob mob = optionalMob.get();
            return mob.getDisplayName().toString();
        }
        return mobName;
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

