package net.akat.quest.rewards;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.akat.quest.rewards.interfaces.Reward;

public class CommandReward implements Reward {
    private final String command;

    public CommandReward(String command) {
        this.command = command;
    }

    @Override
    public void giveReward(Player player) {
        String parsedCommand = command.replace("%player%", player.getName());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
    }

	@Override
	public String getRewardMessage() {
		// TODO Auto-generated method stub
		return null;
	}
    
}
