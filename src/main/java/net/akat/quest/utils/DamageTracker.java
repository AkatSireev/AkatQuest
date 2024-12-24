package net.akat.quest.utils;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

public class DamageTracker {
    private final long creditTimeMillis;
    private final Map<UUID, Long> damageTimestamps = new HashMap<>();

    /**
     * Конструктор для задания времени кредита.
     *
     * @param creditTimeMillis время (в миллисекундах), за которое можно получить кредит после нанесения урона
     */
    public DamageTracker(long creditTimeMillis) {
        this.creditTimeMillis = creditTimeMillis;
    }

    /**
     * Регистрирует игрока, который нанес урон.
     *
     * @param player игрок, который нанес урон
     */
    public void recordDamage(Player player) {
        damageTimestamps.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Получает список игроков, которые еще имеют право на кредит, если их урон был нанесен в пределах времени.
     *
     * @return список игроков, которые имеют право на кредит
     */
    public List<Player> getEligiblePlayers() {
        long now = System.currentTimeMillis();
        return damageTimestamps.entrySet().stream()
                .filter(entry -> (now - entry.getValue()) <= creditTimeMillis)
                .map(entry -> org.bukkit.Bukkit.getPlayer(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Очищает старые записи, которые превышают время кредита.
     */
    public void cleanupOldEntries() {
        long now = System.currentTimeMillis();
        damageTimestamps.entrySet().removeIf(entry -> (now - entry.getValue()) > creditTimeMillis);
    }
}
