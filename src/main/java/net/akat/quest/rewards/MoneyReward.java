package net.akat.quest.rewards;

import org.bukkit.entity.Player;

import net.akat.quest.rewards.interfaces.Reward;
import net.milkbowl.vault.economy.Economy;

public class MoneyReward implements Reward {
    private final double amount;
    private final Economy economy;

    public MoneyReward(double amount, Economy economy) {
        this.amount = amount;
        this.economy = economy;
    }

    @Override
    public void giveReward(Player player) {
        economy.depositPlayer(player, amount);
    }

    @Override
    public String getRewardMessage() {
        return "§7Деньги: §e" + amount + " §7монет";
    }
}
