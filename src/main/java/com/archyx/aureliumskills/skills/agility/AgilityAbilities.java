package com.archyx.aureliumskills.skills.agility;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.ability.AbilityProvider;
import com.archyx.aureliumskills.api.event.CustomRegenEvent;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.configuration.OptionValue;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.data.PlayerDataLoadEvent;
import com.archyx.aureliumskills.lang.AbilityMessage;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.util.mechanics.PotionUtil;
import com.archyx.aureliumskills.util.text.TextUtil;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class AgilityAbilities extends AbilityProvider implements Listener {

    private final Random r = new Random();

    public AgilityAbilities(@NotNull AureliumSkills plugin) {
        super(plugin, Skills.AGILITY);
    }

    private void lightFall(@NotNull EntityDamageEvent event, @NotNull PlayerData playerData) {
        if (event.getFinalDamage() > 0.0) {
            double percentReduction = getValue(Ability.LIGHT_FALL, playerData);
            event.setDamage(event.getDamage() * (1 - (percentReduction / 100)));
        }
    }

    // For potion splashes
    @EventHandler(priority = EventPriority.HIGH)
    public void sugarRushSplash(@NotNull PotionSplashEvent event) {
        if (!event.isCancelled()) {
            if (blockDisabled(Ability.SUGAR_RUSH)) return;
            for (PotionEffect effect : event.getPotion().getEffects()) {
                if (effect.getType().equals(PotionEffectType.SPEED) || effect.getType().equals(PotionEffectType.JUMP)) {
                    for (LivingEntity entity : event.getAffectedEntities()) {
                        if (entity instanceof Player) {
                            Player player = (Player) entity;
                            if (blockAbility(player)) return;
                            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                            if (playerData == null) return;
                            if (playerData.getAbilityLevel(Ability.SUGAR_RUSH) > 0) {
                                double intensity = event.getIntensity(player);
                                double multiplier = 1 + (getValue(Ability.SUGAR_RUSH, playerData) / 100);
                                PotionUtil.applyEffect(player, new PotionEffect(effect.getType(), (int) (effect.getDuration() * multiplier * intensity), effect.getAmplifier()));
                            }
                        }
                    }
                }
            }
        }
    }

    public double getSugarRushSplashMultiplier(@NotNull Player player) {
        if (player.hasPermission("aureliumskills.agility") && plugin.getAbilityManager().isEnabled(Ability.SUGAR_RUSH)) {
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null) {
                if (playerData.getAbilityLevel(Ability.SUGAR_RUSH) > 0) {
                    return 1 + (getValue(Ability.SUGAR_RUSH, playerData) / 100);
                }
            }
        }
        return 1.0;
    }

    // For potion drinking
    @EventHandler(priority = EventPriority.HIGH)
    public void sugarRushDrink(@NotNull PlayerItemConsumeEvent event) {
        if (!event.isCancelled()) {
            if (blockDisabled(Ability.SUGAR_RUSH)) return;
            Player player = event.getPlayer();
            if (blockAbility(player)) return;
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData == null) return;
            if (playerData.getAbilityLevel(Ability.SUGAR_RUSH) > 0) {
                ItemStack item = event.getItem();
                if (item.getType() == Material.POTION) {
                    if (item.getItemMeta() instanceof PotionMeta) {
                        @Nullable PotionMeta meta = (PotionMeta) item.getItemMeta();
                        if (meta == null)
                            return;
                        PotionData potion = meta.getBasePotionData();
                        double multiplier = 1 + (getValue(Ability.SUGAR_RUSH, playerData) / 100);
                        if (potion.getType() == PotionType.SPEED || potion.getType() == PotionType.JUMP) {
                            int amplifier = 0;
                            if (potion.isUpgraded()) {
                                amplifier = 1;
                            }
                            PotionEffectType potionEffectType;
                            if (potion.getType() == PotionType.SPEED) {
                                potionEffectType = PotionEffectType.SPEED;
                            } else {
                                potionEffectType = PotionEffectType.JUMP;
                            }
                            int duration;
                            if (potion.isExtended()) {
                                duration = 480;
                            } else if (potion.isUpgraded()) {
                                duration = 90;
                            } else {
                                duration = 180;
                            }
                            duration = (int) (multiplier * duration);
                            PotionUtil.applyEffect(player, new PotionEffect(potionEffectType, duration * 20, amplifier));
                        }
                        // Apply custom effects
                        if (meta.hasCustomEffects()) {
                            for (PotionEffect effect : meta.getCustomEffects()) {
                                if (effect.getType().equals(PotionEffectType.SPEED) || effect.getType().equals(PotionEffectType.JUMP)) {
                                    PotionUtil.applyEffect(player, new PotionEffect(effect.getType(), (int) (effect.getDuration() * multiplier), effect.getAmplifier()));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void fleeting(@NotNull EntityDamageEvent event, @NotNull PlayerData playerData, @NotNull Player player) {
        AttributeInstance attribute = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH));
        double maxHealth = attribute.getValue();
        if (player.getHealth() - event.getFinalDamage() < getFleetingHealthRequired() * maxHealth) {
            if (!player.hasMetadata("AureliumSkills-Fleeting")) {
                double percent = getValue(Ability.FLEETING, playerData);
                float boostFactor = 1 + ((float) percent / 100);
                float newSpeed = player.getWalkSpeed() * boostFactor;
                if (newSpeed > 1) {
                    newSpeed = 1.0f;
                    percent = (newSpeed / player.getWalkSpeed() - 1) * 100;
                }
                float walkSpeedChange = newSpeed - player.getWalkSpeed();
                player.setWalkSpeed(newSpeed);
                player.setMetadata("AureliumSkills-Fleeting", new FixedMetadataValue(plugin, walkSpeedChange));
                Locale locale = plugin.getLang().getLocale(player);
                plugin.getAbilityManager().sendMessage(player, TextUtil.replace(Lang.getMessage(AbilityMessage.FLEETING_START, locale), "{value}", String.valueOf((int) percent)));
            }
        }
    }

    public void removeFleeting(@NotNull Player player) {
        AttributeInstance attribute = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH));
        double maxHealth = attribute.getValue();
        if (player.getHealth() >= getFleetingHealthRequired() * maxHealth) {
            if (player.hasMetadata("AureliumSkills-Fleeting")) {
                float walkSpeedChange = player.getMetadata("AureliumSkills-Fleeting").get(0).asFloat();
                player.setWalkSpeed(player.getWalkSpeed() - walkSpeedChange);
                player.removeMetadata("AureliumSkills-Fleeting", plugin);
                Locale locale = plugin.getLang().getLocale(player);
                plugin.getAbilityManager().sendMessage(player, Lang.getMessage(AbilityMessage.FLEETING_END, locale));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void fleetingEnd(@NotNull EntityRegainHealthEvent event) {
        if (!event.isCancelled()) {
            if (event.getEntity() instanceof Player) {
                fleetingRemove((Player) event.getEntity(), event.getAmount());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void fleetingEndCustom(@NotNull CustomRegenEvent event) {
        if (!event.isCancelled()) {
            Player player = event.getPlayer();
            fleetingRemove(player, event.getAmount());
        }
    }

    public void fleetingRemove(@NotNull Player player, double amountRegenerated) {
        if (blockAbility(player)) return;
        AttributeInstance attribute = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH));
        double maxHealth = attribute.getValue();
        if (player.getHealth() + amountRegenerated >= getFleetingHealthRequired() * maxHealth) {
            if (player.hasMetadata("AureliumSkills-Fleeting")) {
                float walkSpeedChange = player.getMetadata("AureliumSkills-Fleeting").get(0).asFloat();
                player.setWalkSpeed(player.getWalkSpeed() - walkSpeedChange);
                player.removeMetadata("AureliumSkills-Fleeting", plugin);
                Locale locale = plugin.getLang().getLocale(player);
                plugin.getAbilityManager().sendMessage(player, Lang.getMessage(AbilityMessage.FLEETING_END, locale));
            }
        }
    }

    @EventHandler
    public void fleetingLeave(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        removeFleetingQuit(player);
    }

    public void removeFleetingQuit(@NotNull Player player) {
        if (player.hasMetadata("AureliumSkills-Fleeting")) {
            float walkSpeedChange = player.getMetadata("AureliumSkills-Fleeting").get(0).asFloat();
            player.setWalkSpeed(player.getWalkSpeed() - walkSpeedChange);
            player.removeMetadata("AureliumSkills-Fleeting", plugin);
        }
    }

    @EventHandler
    public void fleetingJoin(@NotNull PlayerDataLoadEvent event) {
        if (!OptionL.isEnabled(Skills.AGILITY)) return;
        if (!plugin.getAbilityManager().isEnabled(Ability.FLEETING)) return;
        Player player = event.getPlayerData().getPlayer();
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attribute == null) return;
        if (player.getHealth() < getFleetingHealthRequired() * attribute.getValue()) {
            if (!player.hasMetadata("AureliumSkills-Fleeting")) {
                double percent = getValue(Ability.FLEETING, event.getPlayerData());
                float boostFactor = 1 + ((float) percent / 100);
                float newSpeed = player.getWalkSpeed() * boostFactor;
                if (newSpeed > 1) {
                    newSpeed = 1.0f;
                }
                float walkSpeedChange = newSpeed - player.getWalkSpeed();
                player.setWalkSpeed(newSpeed);
                player.setMetadata("AureliumSkills-Fleeting", new FixedMetadataValue(plugin, walkSpeedChange));
            }
        }
    }

    @EventHandler
    public void fleetingDeath(@NotNull PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.hasMetadata("AureliumSkills-Fleeting")) {
            float walkSpeedChange = player.getMetadata("AureliumSkills-Fleeting").get(0).asFloat();
            player.setWalkSpeed(player.getWalkSpeed() - walkSpeedChange);
            player.removeMetadata("AureliumSkills-Fleeting", plugin);
        }
    }

    private double getFleetingHealthRequired() {
        double healthPercentRequired = 20;
        // Get configurable health percent value
        OptionValue optionValue = plugin.getAbilityManager().getOption(Ability.FLEETING, "health_percent_required");
        if (optionValue != null) {
            healthPercentRequired = optionValue.asDouble();
        }
        return healthPercentRequired / 100;
    }

    public void thunderFall(@NotNull EntityDamageEvent event, @NotNull PlayerData playerData, @NotNull Player player) {
        if (player.isSneaking()) {
            // If chance
            if (r.nextDouble() < getValue(Ability.THUNDER_FALL, playerData) / 100) {
                // Get damage values
                double percent = getValue2(Ability.THUNDER_FALL, playerData);
                double thunderFallDamage = (percent / 100) * event.getDamage();
                // Get entities nearby
                for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), 3, 3, 1)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        // Damage the entity
                        LivingEntity livingEntity = (LivingEntity) entity;
                        livingEntity.damage(thunderFallDamage, player);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void agilityListener(@NotNull EntityDamageEvent event) {
        if (!event.isCancelled()) {
            if (OptionL.isEnabled(Skills.AGILITY)) {
                if (event.getEntity() instanceof Player) {
                    Player player = (Player) event.getEntity();
                    if (blockAbility(player)) return;
                    @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                    if (playerData == null) return;
                    // If from fall damage
                    if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                        if (plugin.getAbilityManager().isEnabled(Ability.THUNDER_FALL)) {
                            if (playerData.getAbilityLevel(Ability.THUNDER_FALL) > 0) {
                                // Activate thunder fall
                                thunderFall(event, playerData, player);
                            }
                        }
                        if (plugin.getAbilityManager().isEnabled(Ability.LIGHT_FALL)) {
                            if (playerData.getAbilityLevel(Ability.LIGHT_FALL) > 0) {
                                // Activate light fall
                                lightFall(event, playerData);
                            }
                        }
                    }
                    if (plugin.getAbilityManager().isEnabled(Ability.FLEETING)) {
                        if (playerData.getAbilityLevel(Ability.FLEETING) > 0) {
                            // Activate fleeting
                            fleeting(event, playerData, player);
                        }
                    }
                }
            }
        }
    }

}
