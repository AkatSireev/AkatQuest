package net.akat.quest.rewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import net.akat.quest.rewards.interfaces.Reward;
import net.md_5.bungee.api.ChatColor;

public class ItemReward implements Reward {
    private final Material material;
    private final int amount;
    private final String name;
    private final List<String> description;
    private final Map<String, Object> nbtTags;

    public ItemReward(Material material, int amount, String name, List<String> description, Map<String, Object> nbtTags) {
        this.material = material;
        this.amount = amount;
        this.name = name;
        this.description = description != null ? description : new ArrayList<>();
        this.nbtTags = nbtTags != null ? nbtTags : new HashMap<>();
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public List<String> getDescription() {
        return description;
    }

    @Override
    public void giveReward(Player player) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        applyItemName(meta);
        applyItemDescription(meta);

        item.setItemMeta(meta);

        item = applyNBTTags(item);

        player.getInventory().addItem(item);
    }

    @SuppressWarnings("deprecation")
    private void applyItemName(ItemMeta meta) {
        if (name != null && !name.isEmpty()) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        }
    }

    @SuppressWarnings("deprecation")
    private void applyItemDescription(ItemMeta meta) {
        if (!description.isEmpty()) {
            List<String> formattedDescription = new ArrayList<>();
            for (String line : description) {
                formattedDescription.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(formattedDescription);
        }
    }

    // Метод для применения NBT тегов к предмету
    @SuppressWarnings("deprecation")
    private ItemStack applyNBTTags(ItemStack item) {
        if (nbtTags != null && !nbtTags.isEmpty()) {
            NBTItem nbtItem = new NBTItem(item);
            setNBTData(nbtItem, nbtTags);
            return nbtItem.getItem();
        }
        return item;
    }
    
    @SuppressWarnings("unchecked")
	private void setNBTData(NBTCompound compound, Map<String, Object> tags) {
        for (Map.Entry<String, Object> entry : tags.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                compound.setString(key, (String) value);
            } else if (value instanceof Integer) {
                compound.setInteger(key, (Integer) value);
            } else if (value instanceof Double) {
                compound.setDouble(key, (Double) value);
            } else if (value instanceof Boolean) {
                compound.setBoolean(key, (Boolean) value);
            } else if (value instanceof Map) {
                NBTCompound nestedCompound = compound.addCompound(key);
                setNBTData(nestedCompound, (Map<String, Object>) value);
            }
        }
    }

    @Override
    public String getRewardMessage() {
        String displayName = getItemDisplayName();
        return "§7" + amount + "x " + displayName;
    }

    private String getItemDisplayName() {
        return (name != null && !name.isEmpty()) ? ChatColor.translateAlternateColorCodes('&', name) : material.name();
    }
}
