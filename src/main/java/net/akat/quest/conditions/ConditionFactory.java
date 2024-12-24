package net.akat.quest.conditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import net.akat.quest.conditions.interfaces.QuestCondition;
import net.akat.quest.utils.DependencyChecker;

public class ConditionFactory {
	
	private final DependencyChecker dependencyChecker;

    public ConditionFactory(DependencyChecker dependencyChecker) {
        this.dependencyChecker = dependencyChecker;
    }

    public List<QuestCondition> createConditions(ConfigurationSection section) {
        List<QuestCondition> conditions = new ArrayList<>();

        if (section == null) {
            Bukkit.getLogger().warning("Невозможно загрузить условия из конфигурации!");
            return conditions;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection conditionSection = section.getConfigurationSection(key);
            if (conditionSection == null) {
                Bukkit.getLogger().warning("Раздел условия " + key + " не найден.");
                continue;
            }

            String type = conditionSection.getString("type");
            if (type == null) {
                Bukkit.getLogger().warning("В разделе " + key + " не указан тип.");
                continue;
            }

            QuestCondition condition = createCondition(type, conditionSection);
            if (condition != null) {
                conditions.add(condition);
            }
        }

        return conditions;
    }

    private QuestCondition createCondition(String type, ConfigurationSection conditionSection) {
        switch (type.toLowerCase()) {
            case "kill_regular_mob":
                return createKillRegularMobCondition(conditionSection);

            case "kill_mythic_mob":
                if (dependencyChecker.isMythicMobsAvailable()) {
                    return createKillMythicMobCondition(conditionSection);
                } else {
                    Bukkit.getLogger().warning("MythicMobs не найдены, условие 'kill_mythic_mob' не будет выполнено.");
                    return null;
                }

            case "bring_item_with_nbt":
                return createBringItemWithNBTCondition(conditionSection);
                
            default:
                Bukkit.getLogger().warning("Неизвестный тип условия: " + type);
                return null;
        }
    }
    
    private QuestCondition createKillRegularMobCondition(ConfigurationSection section) {
        EntityType mobType = EntityType.valueOf(section.getString("mobType").toUpperCase());
        int amount = section.getInt("amount", 1);
        return new KillMobCondition(mobType, amount);
    }

    private QuestCondition createKillMythicMobCondition(ConfigurationSection section) {
        String mobName = section.getString("mobName");
        int amount = section.getInt("amount", 1);
        return new KillMythicMobCondition(mobName,    amount);
    }

    private QuestCondition createBringItemWithNBTCondition(ConfigurationSection conditionSection) {
        Material material = Material.getMaterial(conditionSection.getString("material").toUpperCase());
        int amount = conditionSection.getInt("amount", 1);
        boolean take = conditionSection.getBoolean("take", false);

        Map<String, Object> tags = null;
        ConfigurationSection tagsSection = conditionSection.getConfigurationSection("tags");
        if (tagsSection != null) {
        	tags = parseNBTSection(tagsSection);
        }

        if (material != null) {
            return new ItemNBTCondition(material, amount, take, tags);
        } else {
            Bukkit.getLogger().warning("Не удалось найти материал для условия 'bring_item_with_nbt'.");
            return null;
        }
    }
    
    private Map<String, Object> parseNBTSection(ConfigurationSection section) {
        Map<String, Object> result = new HashMap<>();

        for (String key : section.getKeys(false)) {
            Object value = section.get(key);

            if (value instanceof ConfigurationSection) {
                result.put(key, parseNBTSection((ConfigurationSection) value));
            } else {
                result.put(key, value);
            }
        }

        return result; 
    }
}
