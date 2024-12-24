package net.akat.quest.conditions.interfaces;

import org.bukkit.entity.Player;

public interface QuestCondition {
    boolean isCompleted(Player player, String questId);
    String getDescription();
}
