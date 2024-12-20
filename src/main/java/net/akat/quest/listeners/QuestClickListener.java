package net.akat.quest.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.akat.quest.gui.QuestGUI;
import net.akat.quest.managers.QuestManager;
import net.akat.quest.managers.QuestStateManager;
import net.akat.quest.models.Quest;
import net.akat.quest.models.state.QuestState;
import net.md_5.bungee.api.ChatColor;

public class QuestClickListener implements Listener {

    private final QuestStateManager stateManager;
    private final QuestManager questManager;

    public QuestClickListener(QuestStateManager stateManager, QuestManager questManager) {
        this.stateManager = stateManager;
        this.questManager = questManager;
    }

    @EventHandler
    public void onQuestMenuClick(InventoryClickEvent event) {
        if (!isQuestMenu(event)) {
            return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (!isValidItem(clickedItem)) {
            return;
        }

        handleQuestInteraction(player, clickedItem);
    }

    @SuppressWarnings("deprecation")
    private boolean isQuestMenu(InventoryClickEvent event) {
        return event.getView().getTitle().equals("Квесты NPC");
    }

    private boolean isValidItem(ItemStack item) {
        return item != null && item.hasItemMeta();
    }

    private void handleQuestInteraction(Player player, ItemStack clickedItem) {
        String questId = getQuestIdFromItem(clickedItem);

        if (questId == null) {
            player.sendMessage("§cОшибка: не удалось определить ID квеста.");
            return;
        }

        Quest quest = questManager.getQuestById(questId);
        if (quest == null) {
            player.sendMessage("§cОшибка: не найден квест с ID " + questId);
            return;
        }

        QuestState currentState = getQuestState(player, quest);

        if (currentState == QuestState.UNAVAILABLE) {
            player.sendMessage("§cДля выполнения этого квеста вам нужно выполнить предыдущие.");
            return;
        }

        if (currentState == QuestState.COMPLETED) {
            player.sendMessage("§cЭтот квест уже выполнен.");
            return;
        }

        if (currentState == QuestState.IN_PROGRESS) {
        	if (!quest.isConditionsMet(player)) {
                player.sendMessage("§cВы не выполнили все условия для завершения квеста.");
                return;
            }
            quest.giveRewards(player);
        }
        
        updateQuestState(player, quest);

        openUpdatedQuestMenu(player, quest);
    }

    @SuppressWarnings("deprecation")
    private String getQuestIdFromItem(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            return item.getItemMeta().getLore().stream()
                    .filter(line -> line.startsWith("§8ID: §8"))
                    .map(line -> line.substring("§8ID: §8".length()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private void updateQuestState(Player player, Quest quest) {
        QuestState currentState = stateManager.loadQuestState(player, quest.getId());
        QuestState nextState = getNextQuestState(currentState);
        stateManager.saveQuestState(player, quest.getId(), nextState);
    }

    private QuestState getNextQuestState(QuestState currentState) {
        switch (currentState) {
            case UNAVAILABLE:
                return QuestState.AVAILABLE;
            case AVAILABLE:
                return QuestState.IN_PROGRESS;
            case IN_PROGRESS:
                return QuestState.COMPLETED;
            case COMPLETED:
                return QuestState.UNAVAILABLE;
            default:
                return QuestState.UNAVAILABLE;
        }
    }

    private void openUpdatedQuestMenu(Player player, Quest quest) {
        String npcName = quest.getNpcName();
        List<Quest> npcQuests = getNpcQuestsForPlayer(player, npcName);

        QuestGUI questGUI = new QuestGUI(npcQuests, stateManager);
        questGUI.open(player);
    }

    private List<Quest> getNpcQuestsForPlayer(Player player, String npcName) {
        List<Quest> npcQuests = new ArrayList<>();
        for (Quest quest : questManager.getQuests().values()) {
            if (isQuestForNpc(quest, npcName)) {
                npcQuests.add(quest);
            }
        }
        return npcQuests;
    }

    private boolean isQuestForNpc(Quest quest, String npcName) {
        return quest.getNpcNameWithoutColor().equalsIgnoreCase(ChatColor.stripColor(npcName));
    }
    
    private QuestState getQuestState(Player player, Quest quest) {
        if (quest.getDepends() != null) {
            QuestState dependentState = stateManager.loadQuestState(player, quest.getDepends());

            if (dependentState == null || dependentState != QuestState.COMPLETED) {
                return QuestState.UNAVAILABLE;
            }
        }

        return stateManager.loadQuestState(player, quest.getId());
    }
}
