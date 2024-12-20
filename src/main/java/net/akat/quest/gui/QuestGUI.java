package net.akat.quest.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.akat.quest.managers.QuestStateManager;
import net.akat.quest.models.Quest;
import net.akat.quest.models.state.QuestState;
import net.akat.quest.rewards.ItemReward;
import net.akat.quest.rewards.MoneyReward;
import net.akat.quest.rewards.interfaces.QuestCondition;
import net.akat.quest.rewards.interfaces.Reward;

import net.md_5.bungee.api.ChatColor;

public class QuestGUI {

    private final List<Quest> quests;
    private final QuestStateManager questStateManager;

    public QuestGUI(List<Quest> quests, QuestStateManager questStateManager) {
        this.quests = quests;
        this.questStateManager = questStateManager;
    }

    public void open(Player player) {
        sortQuests();
        Quest firstLinearQuest = getFirstLinearQuest();
        setFirstQuestState(player, firstLinearQuest);
        updateQuestStates(player);
        Inventory inventory = createInventory();
        fillInventory(player, inventory);
        player.openInventory(inventory);
    }

    private void sortQuests() {
        quests.sort(Comparator.comparingInt(this::extractNumericId));
    }

    private int extractNumericId(Quest quest) {
        String id = quest.getId();
        try {
            return Integer.parseInt(id.replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            Bukkit.getLogger().warning("Некорректный формат ID квеста: " + id);
            return 0;
        }
    }

    private Quest getFirstLinearQuest() {
        for (Quest quest : quests) {
            if (quest.isLinear()) {
                return quest;
            }
        }
        return null;
    }

    private void setFirstQuestState(Player player, Quest firstLinearQuest) {
        QuestState firstQuestState = questStateManager.loadQuestState(player, firstLinearQuest.getId());
        if (firstQuestState == QuestState.UNAVAILABLE) {
            questStateManager.saveQuestState(player, firstLinearQuest.getId(), QuestState.AVAILABLE);
        }
    }

    private void updateQuestStates(Player player) {
        questStateManager.updateNextQuestState(player, quests);
    }

    @SuppressWarnings("deprecation")
	private Inventory createInventory() {
        int size = ((quests.size() / 9) + 1) * 9;
        return Bukkit.createInventory(null, size, "Квесты NPC");
    }

    private void fillInventory(Player player, Inventory inventory) {
        for (int i = 0; i < quests.size(); i++) {
            Quest quest = quests.get(i);
            QuestState questState = getEffectiveQuestState(player, quest);
            ItemStack item = createQuestItem(quest, questState);
            inventory.setItem(i, item);
        }
    }
    
    private QuestState getEffectiveQuestState(Player player, Quest quest) {
        if (quest.getDepends() != null) {
            QuestState dependentState = questStateManager.loadQuestState(player, quest.getDepends());
            
            if (dependentState == null || dependentState != QuestState.COMPLETED) {
                return QuestState.UNAVAILABLE;
            }
        }
        
        QuestState currentState = questStateManager.loadQuestState(player, quest.getId());
        
        return currentState;
    }

    @SuppressWarnings("deprecation")
    private ItemStack createQuestItem(Quest quest, QuestState questState) {
        ItemStack item = new ItemStack(questState.getMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§a" + quest.getName());
            List<String> lore = buildLore(quest, questState);
            meta.setLore(lore);

            if (questState.isEnchanted()) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.SILK_TOUCH, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    private List<String> buildLore(Quest quest, QuestState questState) {
        List<String> lore = new ArrayList<>();

        if (questState == QuestState.UNAVAILABLE) {
            lore.add("§cВам нужно выполнить предыдущие квесты.");
        } else {
            lore.add("§7Описание:");
            addQuestDescription(quest, lore);
            addQuestRewardsAndConditions(quest, questState, lore);
        }
        
        lore.add("");
        lore.add("§7Линейный: " + (quest.isLinear() ? "§aДа" : "§cНет"));
        lore.add("§7Статус: " + questState.getDisplayName());
        lore.add("");
        lore.add("§8ID: §8" + quest.getId());

        return lore;
    }

    private void addQuestDescription(Quest quest, List<String> lore) {
        String[] descriptionLines = quest.getDescription().split("\n");
        for (String line : descriptionLines) {
            lore.add("§f" + line);
        }
    }

    private void addQuestRewardsAndConditions(Quest quest, QuestState questState, List<String> lore) {
        if (questState == QuestState.IN_PROGRESS) {
            lore.add("");
            lore.add("§7Награды:");

            List<Reward> rewards = quest.getRewards();
            if (rewards != null && !rewards.isEmpty()) {
                for (Reward reward : rewards) {
                    if (reward instanceof ItemReward) {
                        addItemRewardToLore((ItemReward) reward, lore);
                    } else if (reward instanceof MoneyReward) {
                        lore.add("§f- " + ((MoneyReward) reward).getRewardMessage());
                    }
                }
            } else {
                lore.add("§f- Нет наград");
            }

            lore.add("");
            lore.add("§7Условия выполнения:");
            List<QuestCondition> conditions = quest.getConditions();
            if (conditions != null && !conditions.isEmpty()) {
                for (QuestCondition condition : conditions) {
                    lore.add("§f- " + condition.getDescription());
                }
            } else {
                lore.add("§f- Нет условий");
            }
        }
    }

    private void addItemRewardToLore(ItemReward itemReward, List<String> lore) {
        String itemName = itemReward.getName();
        if (itemName != null && !itemName.isEmpty()) {
            lore.add("§f- " + itemReward.getAmount() + "x " + ChatColor.translateAlternateColorCodes('&', itemName));
        } else {
            lore.add("§f- " + itemReward.getAmount() + "x " + itemReward.getMaterial().name());
        }
    }
}
