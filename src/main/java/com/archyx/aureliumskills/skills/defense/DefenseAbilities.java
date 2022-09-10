package com.archyx.aureliumskills.skills.defense;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.ability.AbilityProvider;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.skills.Skills;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class DefenseAbilities extends AbilityProvider implements Listener {

    private final Random r = new Random();

    public DefenseAbilities(@NotNull AureliumSkills plugin) {
        super(plugin, Skills.DEFENSE);
    }

    public void shielding(@NotNull EntityDamageByEntityEvent event, @NotNull PlayerData playerData, @NotNull Player player) {
        if (plugin.getAbilityManager().isEnabled(Ability.SHIELDING)) {
            if (player.isSneaking()) {
                if (playerData.getAbilityLevel(Ability.SHIELDING) > 0) {
                    double damageReduction = 1 - (getValue(Ability.SHIELDING, playerData) / 100);
                    event.setDamage(event.getDamage() * damageReduction);
                }
            }
        }
    }

    public void mobMaster(@NotNull EntityDamageByEntityEvent event, @NotNull PlayerData playerData) {
        if (plugin.getAbilityManager().isEnabled(Ability.MOB_MASTER)) {
            if (event.getDamager() instanceof LivingEntity && !(event.getDamager() instanceof Player)) {
                if (playerData.getAbilityLevel(Ability.MOB_MASTER) > 0) {
                    double damageReduction = 1 - (getValue(Ability.MOB_MASTER, playerData) / 100);
                    event.setDamage(event.getDamage() * damageReduction);
                }
            }
        }
    }

    public void immunity(@NotNull EntityDamageByEntityEvent event, @NotNull PlayerData playerData) {
        double chance = getValue(Ability.IMMUNITY, playerData) / 100;
        if (r.nextDouble() < chance) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void noDebuff(@NotNull PotionSplashEvent event) {
        if (blockDisabled(Ability.NO_DEBUFF)) return;
        for (LivingEntity entity : event.getAffectedEntities()) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                if (blockAbility(player)) return;
                for (PotionEffect effect : event.getPotion().getEffects()) {
                    PotionEffectType type = effect.getType();
                    if (type.equals(PotionEffectType.POISON) || type.equals(PotionEffectType.UNLUCK) || type.equals(PotionEffectType.WITHER) ||
                            type.equals(PotionEffectType.WEAKNESS) || type.equals(PotionEffectType.SLOW_DIGGING) || type.equals(PotionEffectType.SLOW) ||
                            type.equals(PotionEffectType.HUNGER) || type.equals(PotionEffectType.HARM) || type.equals(PotionEffectType.CONFUSION) ||
                            type.equals(PotionEffectType.BLINDNESS)) {
                        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                        if (playerData == null) return;
                        double chance = getValue(Ability.NO_DEBUFF, playerData) / 100;
                        if (r.nextDouble() < chance) {
                            if (!player.hasPotionEffect(type)) {
                                event.setIntensity(entity, 0);
                            }
                        }
                    }
                }
            }
        }
    }

    public void noDebuffFire(@NotNull PlayerData playerData, @NotNull Player player, @NotNull LivingEntity entity) {
        if (entity.getEquipment() != null) {
            @Nullable EntityEquipment equipment = entity.getEquipment();
            if (equipment != null && equipment.getItemInMainHand().getEnchantmentLevel(Enchantment.FIRE_ASPECT) > 0) {
                double chance = getValue(Ability.NO_DEBUFF, playerData) / 100;
                if (r.nextDouble() < chance) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.setFireTicks(0);
                        }
                    }.runTaskLater(plugin, 1L);
                }
            }
        }
    }

    @EventHandler
    public void defenseListener(@NotNull EntityDamageByEntityEvent event) {
        if (OptionL.isEnabled(Skills.DEFENSE)) {
            if (!event.isCancelled()) {
                if (event.getEntity() instanceof Player) {
                    Player player = (Player) event.getEntity();
                    if (blockAbility(player)) return;
                    @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                    if (playerData == null) return;
                    if (plugin.getAbilityManager().isEnabled(Ability.NO_DEBUFF)) {
                        if (event.getDamager() instanceof LivingEntity) {
                            LivingEntity entity = (LivingEntity) event.getDamager();
                            noDebuffFire(playerData, player, entity);
                        }
                    }
                    if (plugin.getAbilityManager().isEnabled(Ability.IMMUNITY)) {
                        immunity(event, playerData);
                    }
                }
            }
        }
    }

}
