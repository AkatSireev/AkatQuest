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
            System.out.println("Папка players была создана.");
        }
        return folder;
    }

    // Загрузка состояния квеста для игрока
    public QuestState loadQuestState(Player player, String questId) {
        FileConfiguration config = loadPlayerConfig(player);
        return getQuestStateFromConfig(config, questId);
    }

    // Сохранение состояния квеста для игрока
    public void saveQuestState(Player player, String questId, QuestState state) {
        File playerFile = getPlayerFile(player);
        FileConfiguration config = loadPlayerConfig(player);

        config.set("quests." + questId + ".state", state.name());
        savePlayerConfig(config, playerFile);
    }

    // Загрузка прогресса условий для квеста
    public HashMap<String, Integer> loadQuestProgress(Player player, String questId) {
        FileConfiguration config = loadPlayerConfig(player);
        return getQuestProgressFromConfig(config, questId);
    }

    // Сохранение прогресса условий для квеста
    public void saveQuestProgress(Player player, String questId, HashMap<String, Integer> progress) {
        File playerFile = getPlayerFile(player);
        FileConfiguration config = loadPlayerConfig(player);

        config.set("quests." + questId + ".progress", progress);
        savePlayerConfig(config, playerFile);
    }

    // Обновление состояния следующего квеста, если текущий квест завершён
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

    // Загрузка конфигурации игрока
    private FileConfiguration loadPlayerConfig(Player player) {
        File playerFile = getPlayerFile(player);

        if (!playerFile.exists()) {
            createPlayerFile(playerFile);
        }

        return YamlConfiguration.loadConfiguration(playerFile);
    }

    // Получение состояния квеста из конфигурации
    private QuestState getQuestStateFromConfig(FileConfiguration config, String questId) {
        String state = config.getString("quests." + questId + ".state", "UNAVAILABLE");
        return QuestState.valueOf(state.toUpperCase());
    }

    // Получение прогресса условий из конфигурации
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

    // Сохранение конфигурации игрока
    private void savePlayerConfig(FileConfiguration config, File playerFile) {
        try {
            config.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Получение файла игрока
    private File getPlayerFile(Player player) {
        String playerName = player.getName();
        return new File(playersFolder, playerName + ".yml");
    }

    // Создание файла для игрока, если его нет
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

    // Инициализация конфигурации игрока
    private void initializePlayerConfig(FileConfiguration config, File playerFile) {
        config.set("quests", new HashMap<String, String>());
        savePlayerConfig(config, playerFile);
    }
}
