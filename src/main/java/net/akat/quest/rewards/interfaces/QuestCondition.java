package net.akat.quest.rewards.interfaces;

import org.bukkit.entity.Player;

public interface QuestCondition {
    boolean isCompleted(Player player, String questId);
    String getDescription();
}
