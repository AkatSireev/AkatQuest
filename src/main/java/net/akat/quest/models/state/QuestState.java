package net.akat.quest.models.state;

import org.bukkit.Material;

public enum QuestState {
    UNAVAILABLE(Material.BOOK, "����������", false, "�c"),
    AVAILABLE(Material.WRITABLE_BOOK, "��������", false, "�a"),
    IN_PROGRESS(Material.WRITABLE_BOOK, "�����������", true, "�e"),
    COMPLETED(Material.BOOK, "��������", true, "�b");

    private final Material material;
    private final String displayName;
    private final boolean enchanted;
    private final String colorCode;

    QuestState(Material material, String displayName, boolean enchanted, String colorCode) {
        this.material = material;
        this.displayName = displayName;
        this.enchanted = enchanted;
        this.colorCode = colorCode;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return colorCode + displayName;
    }

    public boolean isEnchanted() {
        return enchanted;
    }

    public String getColorCode() {
        return colorCode;
    }
}
