package net.akat.quest.rewards;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import net.akat.quest.rewards.interfaces.Reward;

public class RewardHandler {
    
    public static void giveRewardsToPlayer(Player player, List<Reward> rewards, String questName) {
        if (rewards == null || rewards.isEmpty()) {
            player.sendMessage("§eЭтот квест не имеет наград.");
            return;
        }
        
        List<String> rewardMessages = collectRewardMessages(player, rewards);
        
        if (!rewardMessages.isEmpty()) {
            sendRewardMessagesToPlayer(player, rewardMessages, questName);
        } else {
            player.sendMessage("§eВы получили награду без описания.");
        }
    }

    private static void giveReward(Player player, Reward reward) {
        reward.giveReward(player);
    }

    private static List<String> collectRewardMessages(Player player, List<Reward> rewards) {
        List<String> rewardMessages = new ArrayList<>();
        for (Reward reward : rewards) {
            giveReward(player, reward);
            String message = reward.getRewardMessage();
            
            if (message != null && !message.isEmpty()) {
                rewardMessages.add(message);
            }
        }
        return rewardMessages;
    }

    private static void sendRewardMessagesToPlayer(Player player, List<String> rewardMessages, String questName) {
        player.sendMessage("§eВы получили следующие награды:");
        for (String message : rewardMessages) {
            player.sendMessage(message);
        }
    }
}
