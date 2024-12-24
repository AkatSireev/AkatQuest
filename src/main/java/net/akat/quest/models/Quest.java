package net.akat.quest.models;

import java.util.List;

import org.bukkit.entity.Player;

import net.akat.quest.models.state.QuestState;
import net.akat.quest.rewards.RewardHandler;
import net.akat.quest.conditions.interfaces.QuestCondition;
import net.akat.quest.rewards.interfaces.Reward;
import net.md_5.bungee.api.ChatColor;

public class Quest {
    private final String id;
    private final String name;
    private final String description;
    private final String npcName;
    private final boolean linear;
    private final String depends;
    private QuestState state;
    private List<Reward> rewards;
    private List<QuestCondition> conditions;

    public Quest(String id, String name, String description, String npcName, boolean linear, List<Reward> rewards, List<QuestCondition> conditions, String depends) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.npcName = npcName;
        this.depends = depends;
        this.linear = linear;
        this.state = QuestState.UNAVAILABLE;
        this.rewards = rewards;
        this.conditions = conditions;
    }
    
    public boolean isConditionsMet(Player player) {
        for (QuestCondition condition : conditions) {
            if (!condition.isCompleted(player, id)) {
                return false;
            }
        }
        return true;
    }
    
    public List<QuestCondition> getConditions() {
        return conditions;
    }
    
    public String getDepends() {
        return depends;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getNpcName() {
        return npcName;
    }

    public boolean isLinear() {
        return linear;
    }
    
    public QuestState getState() {
        return state;
    }

    public void setState(QuestState state) {
        this.state = state;
    }
    
    public List<Reward> getRewards() {
        return rewards;
    }

    public String getNpcNameWithoutColor() {
        return ChatColor.stripColor(npcName);
    }
    
    public void giveRewards(Player player) {
        RewardHandler.giveRewardsToPlayer(player, rewards, name);
    }
}
