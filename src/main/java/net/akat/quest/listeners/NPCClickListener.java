package net.akat.quest.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import net.akat.quest.gui.QuestGUI;
import net.akat.quest.managers.QuestStateManager;
import net.akat.quest.models.Quest;
import net.md_5.bungee.api.ChatColor;

public class NPCClickListener implements Listener {
    private final List<Quest> allQuests;
    private final QuestStateManager questStateManager;
    private final Map<UUID, Long> lastClickTimes = new HashMap<>();

    public NPCClickListener(List<Quest> allQuests, QuestStateManager questStateManager) {
        this.allQuests = allQuests;
        this.questStateManager = questStateManager;
    }

    @EventHandler
    public void onNPCClick(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (!(event.getRightClicked() instanceof LivingEntity)) {
            return;
        }

        if (isSpamClick(player)) {
            return;
        }

        String npcName = getNpcName(event.getRightClicked());
        if (npcName == null) {
            player.sendMessage("§cУ этого NPC нет имени.");
            return;
        }

        String npcNameWithoutColor = ChatColor.stripColor(npcName);

        // Список доступных квестов для этого NPC
        List<Quest> npcQuests = getQuestsForNpc(player, npcNameWithoutColor);
        if (npcQuests.isEmpty()) {
            player.sendMessage("§cУ этого NPC нет доступных квестов.");
            return;
        }

        openQuestGUI(player, npcQuests);
    }

    private boolean isSpamClick(Player player) {
        UUID playerUUID = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        if (lastClickTimes.containsKey(playerUUID)) {
            long lastClickTime = lastClickTimes.get(playerUUID);
            if (currentTime - lastClickTime < 300) {
                return true;
            }
        }
        lastClickTimes.put(playerUUID, currentTime);
        return false;
    }

    @SuppressWarnings("deprecation")
    private String getNpcName(Entity entity) {
        return entity.getCustomName();
    }

    private List<Quest> getQuestsForNpc(Player player, String npcNameWithoutColor) {
        return allQuests.stream()
                .filter(quest -> {
                    String questNpcName = quest.getNpcNameWithoutColor();
                    return questNpcName.equalsIgnoreCase(npcNameWithoutColor);
                })
                .collect(Collectors.toList());
    }

    private void openQuestGUI(Player player, List<Quest> npcQuests) {
        QuestGUI questGUI = new QuestGUI(npcQuests, questStateManager);
        questGUI.open(player);
    }
}
