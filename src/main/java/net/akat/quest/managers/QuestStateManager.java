package net.akat.quest.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import net.akat.quest.models.Quest;
import net.akat.quest.models.state.QuestState;

public class QuestStateManager {

    private final File playersFolder;

    public QuestStateManager(File dataFolder) {
        this.playersFolder = initializePlayersFolder(dataFolder);
    }

    private File initializePlayersFolder(File dataFolder) {
        File folder = new File(dataFolder, "players");
        if (!folder.exists() && folder.mkdirs()) {
            System.out.println("����� players ���� �������.");
        }
        return folder;
    }

    // �������� ��������� ������ ��� ������
    public QuestState loadQuestState(Player player, String questId) {
        FileConfiguration config = loadPlayerConfig(player);
        return getQuestStateFromConfig(config, questId);
    }

    // ���������� ��������� ������ ��� ������
    public void saveQuestState(Player player, String questId, QuestState state) {
        File playerFile = getPlayerFile(player);
        FileConfiguration config = loadPlayerConfig(player);

        config.set("quests." + questId + ".state", state.name());
        savePlayerConfig(config, playerFile);
    }

    // �������� ��������� ������� ��� ������
    public HashMap<String, Integer> loadQuestProgress(Player player, String questId) {
        FileConfiguration config = loadPlayerConfig(player);
        return getQuestProgressFromConfig(config, questId);
    }

    // ���������� ��������� ������� ��� ������
    public void saveQuestProgress(Player player, String questId, HashMap<String, Integer> progress) {
        File playerFile = getPlayerFile(player);
        FileConfiguration config = loadPlayerConfig(player);

        config.set("quests." + questId + ".progress", progress);
        savePlayerConfig(config, playerFile);
    }

    // ���������� ��������� ���������� ������, ���� ������� ����� ��������
    public void updateNextQuestState(Player player, List<Quest> quests) {
        for (int i = 0; i < quests.size() - 1; i++) {
            Quest currentQuest = quests.get(i);
            QuestState currentState = loadQuestState(player, currentQuest.getId());
            if (currentState == QuestState.COMPLETED) {
                Quest nextQuest = quests.get(i + 1);
                QuestState nextQuestState = loadQuestState(player, nextQuest.getId());
                if (nextQuestState == QuestState.UNAVAILABLE) {
                    saveQuestState(player, nextQuest.getId(), QuestState.AVAILABLE);
                }
            }
        }
    }

    // �������� ������������ ������
    private FileConfiguration loadPlayerConfig(Player player) {
        File playerFile = getPlayerFile(player);

        if (!playerFile.exists()) {
            createPlayerFile(playerFile);
        }

        return YamlConfiguration.loadConfiguration(playerFile);
    }

    // ��������� ��������� ������ �� ������������
    private QuestState getQuestStateFromConfig(FileConfiguration config, String questId) {
        String state = config.getString("quests." + questId + ".state", "UNAVAILABLE");
        return QuestState.valueOf(state.toUpperCase());
    }

    // ��������� ��������� ������� �� ������������
    private HashMap<String, Integer> getQuestProgressFromConfig(FileConfiguration config, String questId) {
        ConfigurationSection section = config.getConfigurationSection("quests." + questId + ".progress");

        if (section == null) {
            return new HashMap<>();
        }

        HashMap<String, Integer> progress = new HashMap<>();
        for (String key : section.getKeys(false)) {
            progress.put(key, section.getInt(key));
        }
        return progress;
    }

    // ���������� ������������ ������
    private void savePlayerConfig(FileConfiguration config, File playerFile) {
        try {
            config.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ��������� ����� ������
    private File getPlayerFile(Player player) {
        String playerName = player.getName();
        return new File(playersFolder, playerName + ".yml");
    }

    // �������� ����� ��� ������, ���� ��� ���
    private void createPlayerFile(File playerFile) {
        try {
            if (playerFile.createNewFile()) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
                initializePlayerConfig(config, playerFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ������������� ������������ ������
    private void initializePlayerConfig(FileConfiguration config, File playerFile) {
        config.set("quests", new HashMap<String, String>());
        savePlayerConfig(config, playerFile);
    }
}
