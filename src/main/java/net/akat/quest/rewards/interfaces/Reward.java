package net.akat.quest.rewards.interfaces;

import org.bukkit.entity.Player;

public interface Reward {
	public abstract void giveReward(Player player);
    public abstract String getRewardMessage();
}