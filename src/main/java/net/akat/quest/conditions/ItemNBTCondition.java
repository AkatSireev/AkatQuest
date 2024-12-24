package net.akat.quest.conditions;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import net.akat.quest.conditions.interfaces.QuestCondition;

public class ItemNBTCondition implements QuestCondition {
    private final Material material;
    private final int amount;
    private final boolean take;
    private final Map<String, Object> tags;

    public ItemNBTCondition(Material material, int amount, boolean take, Map<String, Object> tags) {
        this.material = material;
        this.amount = amount;
        this.take = take;
        this.tags = tags;
    }

    public boolean isTake() {
        return take;
    }

    @Override
    public boolean isCompleted(Player player, String questId) {
        int itemCount = countItems(player);

        if (itemCount >= amount) {
            if (take) {
                removeItems(player, amount);
            }
            return true;
        }

        return false;
    }

    // ������� ���������� ��������� � ������
    private int countItems(Player player) {
        int itemCount = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                itemCount += getItemAmount(item);
            }
        }

        return itemCount;
    }

    // ��������� ���������� ���������, �������� NBT
    @SuppressWarnings("deprecation")
	private int getItemAmount(ItemStack item) {
        int itemAmount = 0;

        if (tags != null && !tags.isEmpty()) {
            NBTItem nbtItem = new NBTItem(item);
            if (matchesNBT(nbtItem)) {
                itemAmount = item.getAmount();
            }
        } else {
            itemAmount = item.getAmount();
        }

        return itemAmount;
    }

    // �������� ��������� �� ���������
    @SuppressWarnings("deprecation")
	private void removeItems(Player player, int amountToRemove) {
        int remainingAmount = amountToRemove;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                NBTItem nbtItem = new NBTItem(item);

                if (tags != null && !tags.isEmpty() && !matchesNBT(nbtItem)) {
                    continue;
                }

                int amountToTake = Math.min(item.getAmount(), remainingAmount);
                item.setAmount(item.getAmount() - amountToTake);
                remainingAmount -= amountToTake;

                if (remainingAmount <= 0) {
                    break;
                }
            }
        }
    }

    // �������� ���������� NBT �����
    @SuppressWarnings("unchecked")
	private boolean matchesNBT(NBTItem nbtItem) {
        if (tags == null || tags.isEmpty()) {
            return true;
        }

        for (Map.Entry<String, Object> tagEntry : tags.entrySet()) {
            String tagName = tagEntry.getKey();
            Object expectedValue = tagEntry.getValue();

            if (expectedValue instanceof Map) {
                NBTCompound compound = nbtItem.getCompound(tagName);
                if (compound == null || !matchesTag(compound, (Map<String, Object>) expectedValue)) {
                    return false;
                }
            } else {
                String actualValue = nbtItem.getString(tagName);
                if (actualValue == null || !expectedValue.equals(actualValue)) {
                    return false;
                }
            }
        }

        return true;
    }

    // ����������� �������� ��������� �����
    private boolean matchesTag(NBTCompound compound, Map<String, Object> expectedTags) {
        for (Map.Entry<String, Object> expectedTag : expectedTags.entrySet()) {
            String tagName = expectedTag.getKey();
            Object expectedValue = expectedTag.getValue();

            String actualValue = compound.getString(tagName);
            if (actualValue == null || !expectedValue.equals(actualValue)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getDescription() {
        return "Количество x" + amount + ": " + material;
    }
}
