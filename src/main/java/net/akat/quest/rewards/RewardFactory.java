package net.akat.quest.rewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import net.akat.quest.rewards.interfaces.Reward;
import net.milkbowl.vault.economy.Economy;

public class RewardFactory {

    private final Economy economy;

    public RewardFactory(Economy economy) {
        this.economy = economy;
    }

    public List<Reward> createRewards(ConfigurationSection section) {
        List<Reward> rewards = new ArrayList<>();

        if (section == null) {
            Bukkit.getLogger().warning("Не найден раздел с наградами в конфигурации!");
            return rewards;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection rewardSection = section.getConfigurationSection(key);
            if (rewardSection == null) {
                Bukkit.getLogger().warning("Раздел награды " + key + " отсутствует.");
                continue;
            }

            String type = rewardSection.getString("type");
            if (type == null) {
                Bukkit.getLogger().warning("В награде " + key + " не указан тип.");
                continue;
            }

            Reward reward = createReward(type, rewardSection);
            if (reward != null) {
                rewards.add(reward);
            }
        }

        if (rewards.isEmpty()) {
            Bukkit.getLogger().warning("Не были созданы награды!");
        }

        return rewards;
    }

    private Reward createReward(String type, ConfigurationSection rewardSection) {
        switch (type.toLowerCase()) {
            case "command":
                return createCommandReward(rewardSection);
            case "item":
                return createItemReward(rewardSection);
            case "money":
                return createMoneyReward(rewardSection);
            default:
                Bukkit.getLogger().warning("Неизвестный тип награды: " + type);
                return null;
        }
    }

    private Reward createCommandReward(ConfigurationSection commandSection) {
        String command = commandSection.getString("command");
        if (command != null) {
            return new CommandReward(command);
        } else {
            Bukkit.getLogger().warning("Награда типа 'command' не содержит команды.");
            return null;
        }
    }

    private Reward createItemReward(ConfigurationSection itemSection) {
        String materialName = itemSection.getString("material");
        if (materialName != null) {
            Material material = Material.getMaterial(materialName.toUpperCase());
            if (material != null) {
                int amount = itemSection.getInt("amount", 1);
                String name = itemSection.getString("name", null);
                List<String> description = itemSection.getStringList("description");

                Map<String, Object> nbtTags = null;

                ConfigurationSection tagsSection = itemSection.getConfigurationSection("tags");
                if (tagsSection != null) {
                    nbtTags = parseNBTSection(tagsSection);
                }

                return new ItemReward(material, amount, name, description, nbtTags);
            } else {
                Bukkit.getLogger().warning("Не удалось найти материал для награды типа 'item': " + materialName);
                return null;
            }
        } else {
            Bukkit.getLogger().warning("Награда типа 'item' не содержит материала.");
            return null;
        }
    }

    private Reward createMoneyReward(ConfigurationSection moneySection) {
        double moneyAmount = moneySection.getDouble("amount", 0.0);
        if (moneyAmount > 0) {
            return new MoneyReward(moneyAmount, economy);
        } else {
            Bukkit.getLogger().warning("Награда типа 'money' не содержит количества монет или оно равно нулю.");
            return null;
        }
    }

    private Map<String, Object> parseNBTSection(ConfigurationSection section) {
        Map<String, Object> result = new HashMap<>();

        if (section != null) {
            for (String key : section.getKeys(false)) {
                Object value = section.get(key);

                if (value instanceof ConfigurationSection) {
                    result.put(key, parseNBTSection((ConfigurationSection) value));
                } else {
                    result.put(key, value);
                }
            }
        }

        return result;
    }
}
