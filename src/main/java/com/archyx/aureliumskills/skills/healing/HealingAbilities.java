package com.archyx.aureliumskills.skills.healing;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.ability.AbilityProvider;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.AbilityMessage;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.modifier.StatModifier;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.stats.Stats;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.aureliumskills.util.version.VersionUtils;
import com.cryptomorin.xseries.ReflectionUtils;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.util.Locale;

public class HealingAbilities extends AbilityProvider implements Listener {

    private Class<?> entityLivingClass;
    private Class<?> craftPlayerClass;

    public HealingAbilities(AureliumSkills plugin) {
        super(plugin, Skills.HEALING);
    }

    @EventHandler
    public void lifeEssence(EntityRegainHealthEvent event) {
        if (blockDisabled(Ability.LIFE_ESSENCE)) return;
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (blockAbility(player)) return;
            if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.MAGIC) {
                PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                if (playerData == null) return;
                if (playerData.getAbilityLevel(Ability.LIFE_ESSENCE) > 0) {
                    double multiplier = 1 + getValue(Ability.LIFE_ESSENCE, playerData) / 100;
                    event.setAmount(event.getAmount() * multiplier);
                }
            }
        }
    }

    @EventHandler
    public void lifeSteal(EntityDeathEvent event) {
        if (blockDisabled(Ability.LIFE_STEAL)) return;
        LivingEntity entity = event.getEntity();
        boolean hostile = entity instanceof Monster || entity instanceof Player;
        if (XMaterial.isNewVersion()) {
            if (entity instanceof Phantom) {
                hostile = true;
            }
        }
        if (hostile) {
            if (entity.getKiller() == null) return;
            Player player = entity.getKiller();
            if (player == null)
                return;
            if (player.equals(entity)) return;
            if (blockAbility(player)) return;
            PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData == null) return;
            if (playerData.getAbilityLevel(Ability.LIFE_STEAL) > 0) {
                AttributeInstance entityAttribute = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (entityAttribute == null) return;
                double maxHealth = entityAttribute.getValue();
                double percent = getValue(Ability.LIFE_STEAL, playerData) / 100;
                double healthRegen = maxHealth * percent;
                AttributeInstance playerAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (playerAttribute == null) return;
                player.setHealth(player.getHealth() + Math.min(healthRegen, playerAttribute.getValue() - player.getHealth()));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void goldenHeart(EntityDamageEvent event) {
        if (blockDisabled(Ability.GOLDEN_HEART)) return;
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (blockAbility(player)) return;
            PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData == null) return;
            if (getAbsorptionAmount(player) > 0 && playerData.getAbilityLevel(Ability.GOLDEN_HEART) > 0) {
                double multiplier = 1 - getValue(Ability.GOLDEN_HEART, playerData) / 100;
                if (multiplier < 0.01) { // Cap at 99% reduction
                    multiplier = 0.01;
                }
                event.setDamage(event.getDamage() * multiplier);
            }
        }
    }

    @EventHandler
    public void revival(PlayerRespawnEvent event) {
        if (blockDisabled(Ability.REVIVAL)) return;
        Player player = event.getPlayer();
        if (blockAbility(player)) return;
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null) return;
        if (playerData.getAbilityLevel(Ability.REVIVAL) > 0) {
            double healthBonus = getValue(Ability.REVIVAL, playerData);
            double regenerationBonus = getValue2(Ability.REVIVAL, playerData);
            StatModifier healthModifier = new StatModifier("AureliumSkills.Ability.Revival.Health", Stats.HEALTH, healthBonus);
            StatModifier regenerationModifier = new StatModifier("AureliumSkills.Ability.Revival.Regeneration", Stats.REGENERATION, regenerationBonus);
            playerData.addStatModifier(healthModifier);
            playerData.addStatModifier(regenerationModifier);
            if (plugin.getAbilityManager().getOptionAsBooleanElseTrue(Ability.REVIVAL, "enable_message")) {
                Locale locale = playerData.getLocale();
                plugin.getAbilityManager().sendMessage(player, TextUtil.replace(Lang.getMessage(AbilityMessage.REVIVAL_MESSAGE, locale)
                        , "{value}", NumberUtil.format1(healthBonus)
                        , "{value_2}", NumberUtil.format1(regenerationBonus)));
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    playerData.removeStatModifier("AureliumSkills.Ability.Revival.Health");
                    playerData.removeStatModifier("AureliumSkills.Ability.Revival.Regeneration");
                }
            }.runTaskLater(plugin, 30 * 20);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void revivalLeave(PlayerQuitEvent event) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(event.getPlayer());
        if (playerData == null) {
            return;
        }
        playerData.removeStatModifier("AureliumSkills.Ability.Revival.Health");
        playerData.removeStatModifier("AureliumSkills.Ability.Revival.Regeneration");
    }

    private double getAbsorptionAmount(Player player) {
        if (VersionUtils.isAtLeastVersion(14, 4)) {
            return player.getAbsorptionAmount();
        } else {
            Class<?> entityLivingClass = this.entityLivingClass;
            Class<?> craftPlayerClass = this.craftPlayerClass;
            if (entityLivingClass == null) {
                entityLivingClass = ReflectionUtils.getNMSClass("EntityLiving");
            }
            if (craftPlayerClass == null) {
                craftPlayerClass = ReflectionUtils.getCraftClass("entity.CraftPlayer");
            }
            if (craftPlayerClass != null && entityLivingClass != null) {
                try {
                    Method getHandle = craftPlayerClass.getDeclaredMethod("getHandle");
                    Object o = entityLivingClass.cast(getHandle.invoke(craftPlayerClass.cast(player)));
                    return (float) entityLivingClass.getDeclaredMethod("getAbsorptionHearts").invoke(o);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return 0.0;
    }


}
