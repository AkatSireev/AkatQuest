package net.akat.quest.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import net.akat.quest.Main;
import net.akat.quest.conditions.ConditionFactory;
import net.akat.quest.models.Quest;
import net.akat.quest.models.state.QuestState;
import net.akat.quest.rewards.RewardFactory;
import net.akat.quest.conditions.interfaces.QuestCondition;
import net.akat.quest.rewards.interfaces.Reward;
import net.milkbowl.vault.economy.Economy;

public class QuestManager {
    private final Main plugin;
    private final Map<String, Quest> quests = new HashMap<>();
    private File questsFile;
    private FileConfiguration questsConfig;
    private final RewardFactory rewardFactory;
    private final ConditionFactory conditionFactory;
    private final QuestStateManager stateManager;
    

    public QuestManager(Main plugin, Economy economy, QuestStateManager stateManager) {
        this.plugin = plugin;
        this.rewardFactory = new RewardFactory(economy);
        this.conditionFactory = new ConditionFactory(plugin.getDependencyChecker());
        this.stateManager = stateManager;
        loadQuestFile();
        loadQuests();
    }

    private void loadQuestFile() {
        questsFile = new File(plugin.getDataFolder(), "quests.yml");
        if (!questsFile.exists()) {
            plugin.saveResource("quests.yml", false);
            plugin.getLogger().info("Файл quests.yml не найден. Создаю новый...");
        } else {
            plugin.getLogger().info("Файл quests.yml найден.");
        }
        questsConfig = YamlConfiguration.loadConfiguration(questsFile);
    }

    public void loadQuests() {
        quests.clear();

        ConfigurationSection questsSection = questsConfig.getConfigurationSection("quests");
        if (questsSection == null) {
            plugin.getLogger().warning("Не найден раздел 'quests' в quests.yml.");
            return;
        }

        questsSection.getKeys(false).forEach(key -> {
            Quest quest = loadQuest(key);
            if (quest != null) {
                quests.put(key, quest);
            }
        });

        plugin.getLogger().info("Загружено квестов: " + quests.size());
    }

    private Quest loadQuest(String key) {
        String path = "quests." + key + ".";
        String name = questsConfig.getString(path + "name", "Безымянный квест");
        String description = questsConfig.getString(path + "description", "Описание не задано");
        String npcName = questsConfig.getString(path + "npcName", "Безымянный NPC");
        boolean linear = questsConfig.getBoolean(path + "linear", false);
        String depends = questsConfig.getString(path + "depends", null);

        ConfigurationSection rewardsSection = questsConfig.getConfigurationSection(path + "rewards");
        List<Reward> rewards = loadRewards(rewardsSection);

        ConfigurationSection conditionsSection = questsConfig.getConfigurationSection(path + "conditions");
        List<QuestCondition> conditions = loadConditions(conditionsSection);

        if (name.equals("Безымянный квест")) {
            plugin.getLogger().warning("Квест " + key + " не имеет названия!");
            return null;
        }

        return new Quest(key, name, description, npcName, linear, rewards, conditions, depends);
    }

    public boolean isDependencyCompleted(Player player, Quest quest) {
        String depends = quest.getDepends();
        if (depends == null || depends.isEmpty()) {
            return true;
        }

        Quest dependentQuest = getQuestById(depends);
        if (dependentQuest == null) {
            plugin.getLogger().warning("Квест " + quest.getId() + " ссылается на несуществующий квест " + depends);
            return false;
        }

        QuestState dependentState = stateManager.loadQuestState(player, depends);
        return dependentState == QuestState.COMPLETED;
    }

    private List<QuestCondition> loadConditions(ConfigurationSection conditionsSection) {
        return conditionFactory.createConditions(conditionsSection);
    }

    private List<Reward> loadRewards(ConfigurationSection rewardsSection) {
        List<Reward> rewards = new ArrayList<>();

        if (rewardsSection != null) {
            rewards = rewardFactory.createRewards(rewardsSection);
        } else {
            plugin.getLogger().warning("Для квеста нет указанных наград!");
        }

        return rewards;
    }

    public Quest getQuestById(String questId) {
        return quests.get(questId);
    }

    public Map<String, Quest> getQuests() {
        return quests;
    }
}
