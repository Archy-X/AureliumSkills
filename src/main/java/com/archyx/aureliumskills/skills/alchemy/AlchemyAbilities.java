package com.archyx.aureliumskills.skills.alchemy;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.ability.AbilityProvider;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.AbilityMessage;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.modifier.StatModifier;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.skills.agility.AgilityAbilities;
import com.archyx.aureliumskills.stats.Stats;
import com.archyx.aureliumskills.util.item.NBTAPIUser;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.mechanics.PotionUtil;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.aureliumskills.util.version.VersionUtils;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.LingeringPotion;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class AlchemyAbilities extends AbilityProvider implements Listener {

    private final @NotNull AgilityAbilities agilityAbilities;

    public AlchemyAbilities(@NotNull AureliumSkills plugin) {
        super(plugin, Skills.ALCHEMY);
        this.agilityAbilities = new AgilityAbilities(plugin);
        wiseEffect();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void alchemist(@NotNull BrewEvent event) {
        if (blockDisabled(Ability.ALCHEMIST)) return;
        if (!event.isCancelled()) {
            if (event.getBlock().hasMetadata("skillsBrewingStandOwner")) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(event.getBlock().getMetadata("skillsBrewingStandOwner").get(0).asString()));
                if (offlinePlayer.isOnline()) {
                    Player player = offlinePlayer.getPlayer();
                    BrewerInventory inventory = event.getContents();
                    if (player != null) {
                        if (blockAbility(player)) return;
                        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                        if (playerData == null) return;
                        if (playerData.getAbilityLevel(Ability.ALCHEMIST) > 0) {
                            updateBrewingStand(inventory, playerData, playerData.getLocale());
                        }
                    }
                }
            }
        }
    }

    private void updateBrewingStand(@NotNull BrewerInventory inventory, @NotNull PlayerData playerData, @Nullable Locale locale) {
        new BukkitRunnable() {
            @Override
            public void run() {
                @Nullable ItemStack[] contents = inventory.getContents();
                double multiplier = 1 + (getValue(Ability.ALCHEMIST, playerData) / 100);
                for (int i = 0; i < contents.length; i++) {
                    @Nullable ItemStack item = contents[i];
                    if (item != null) {
                        if (item.getItemMeta() instanceof PotionMeta) {
                            contents[i] = applyDurationData(item, multiplier, locale);
                        }
                    }
                }
                inventory.setContents(contents);
            }
        }.runTaskLater(plugin, 1L);
    }

    private boolean isApplicablePotion(@NotNull PotionType potionType) {
        switch (potionType) {
            case INSTANT_DAMAGE:
            case INSTANT_HEAL:
            case AWKWARD:
            case MUNDANE:
            case THICK:
            case WATER:
                return false;
            default:
                return true;
        }
    }

    private @NotNull ItemStack applyDurationData(@NotNull ItemStack originalItem, double multiplier, Locale locale) {
        if (NBTAPIUser.isNBTDisabled(plugin)) return originalItem;
        PotionMeta potionMeta = (PotionMeta) originalItem.getItemMeta();
        if (potionMeta != null) {
            PotionData potionData = potionMeta.getBasePotionData();
            if (isApplicablePotion(potionData.getType())) {
                int originalDuration = PotionUtil.getDuration(potionData);
                int duration = (int) (originalDuration * multiplier); // Get duration in ticks
                int durationBonus = duration - originalDuration;
                // Add NBT data
                NBTItem nbtItem = new NBTItem(originalItem);
                NBTCompound nbtCompound = nbtItem.getCompound("skillsPotion");
                if (nbtCompound == null) {
                    nbtItem.addCompound("skillsPotion").setInteger("durationBonus", durationBonus);
                }
                if (nbtCompound != null) {
                    nbtCompound.setInteger("durationBonus", durationBonus);
                }
                ItemStack item = nbtItem.getItem();
                ItemMeta meta = item.getItemMeta();
                if (duration != 0 && meta != null) {
                    // Add lore
                    if (plugin.getAbilityManager().getOptionAsBooleanElseTrue(Ability.ALCHEMIST, "add_item_lore")) {
                        List<@NotNull String> lore = new ArrayList<>();
                        lore.add(TextUtil.replace(Lang.getMessage(AbilityMessage.ALCHEMIST_LORE, locale)
                                , "{duration}", PotionUtil.formatDuration(durationBonus)
                                , "{value}", NumberUtil.format1((multiplier - 1) * 100)));
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                    }
                }
                return item;
            }
        }
        return originalItem;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrink(@NotNull PlayerItemConsumeEvent event) {
        if (blockDisabled(Ability.ALCHEMIST)) return;
        Player player = event.getPlayer();
        if (blockAbility(player)) return;
        if (event.isCancelled()) return;
        ItemStack item = event.getItem();
        if (item.getType() == Material.POTION && item.getItemMeta() instanceof PotionMeta) {
            int durationBonus = 0;
            if (!NBTAPIUser.isNBTDisabled(plugin)) {
                NBTItem nbtItem = new NBTItem(item);
                NBTCompound compound = nbtItem.getCompound("skillsPotion");
                if (compound != null) {
                    durationBonus = compound.getInteger("durationBonus");
                }
            }
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            if (meta != null) {
                PotionData potionData = meta.getBasePotionData();
                PotionType potionType = potionData.getType();
                PotionEffectType effectType = potionType.getEffectType();
                if (effectType != null) {
                    if (!potionType.toString().equals("TURTLE_MASTER")) {
                        // Get amplifier
                        int amplifier = 0;
                        if (potionData.isUpgraded()) {
                            if (potionType.equals(PotionType.SLOWNESS)) {
                                amplifier = 3;
                            } else {
                                amplifier = 1;
                            }
                        }
                        // Apply effect
                        if (effectType.equals(PotionEffectType.SPEED) || effectType.equals(PotionEffectType.JUMP)) {
                            PotionUtil.applyEffect(player, new PotionEffect(effectType, (int) ((PotionUtil.getDuration(potionData) + durationBonus) * agilityAbilities.getSugarRushSplashMultiplier(player)), amplifier));
                        } else {
                            PotionUtil.applyEffect(player, new PotionEffect(effectType, PotionUtil.getDuration(potionData) + durationBonus, amplifier));
                        }
                    }
                    // Special case for Turtle Master
                    else {
                        if (!potionData.isUpgraded()) {
                            PotionUtil.applyEffect(player, new PotionEffect(PotionEffectType.SLOW, PotionUtil.getDuration(potionData) + durationBonus, 3));
                            PotionUtil.applyEffect(player, new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, PotionUtil.getDuration(potionData) + durationBonus, 2));
                        } else {
                            PotionUtil.applyEffect(player, new PotionEffect(PotionEffectType.SLOW, PotionUtil.getDuration(potionData) + durationBonus, 5));
                            PotionUtil.applyEffect(player, new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, PotionUtil.getDuration(potionData) + durationBonus, 3));
                        }
                    }
                }
            }
        }
    }

    // Handles duration boosts for splash potions. Includes Alchemist, Sugar Rush, and Splasher.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSplash(@NotNull PotionSplashEvent event) {
        if (blockDisabled(Ability.ALCHEMIST)) return;
        if (event.isCancelled()) return;
        ItemStack item = event.getPotion().getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;
        if (meta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) meta;
            PotionData potionData = potionMeta.getBasePotionData();
            if (potionMeta.hasCustomEffects() && OptionL.getBoolean(Option.ALCHEMY_IGNORE_CUSTOM_POTIONS)) return;
            // Get potion duration bonus from Alchemist ability
            int durationBonus = 0;
            if (!NBTAPIUser.isNBTDisabled(plugin)) {
                NBTItem nbtItem = new NBTItem(item);
                NBTCompound compound = nbtItem.getCompound("skillsPotion");
                if (compound != null) {
                    durationBonus = compound.getInteger("durationBonus");
                }
            }
            // Add effects for each player
            for (PotionEffect effect : event.getPotion().getEffects()) {
                for (LivingEntity entity : event.getAffectedEntities()) {
                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        if (blockAbility(player)) return;
                        // Calculate and get multipliers
                        double splasherMultiplier = getSplasherMultiplier(event.getPotion().getShooter(), event.getAffectedEntities());
                        double intensity = event.getIntensity(player);
                        int duration = (int) ((PotionUtil.getDuration(potionData) + durationBonus) * splasherMultiplier * intensity);
                        // Apply normal effects
                        if (!potionData.getType().toString().equals("TURTLE_MASTER")) {
                            // Apply Sugar Rush
                            if (effect.getType().equals(PotionEffectType.SPEED) || effect.getType().equals(PotionEffectType.JUMP)) {
                                PotionUtil.applyEffect(player, new PotionEffect(effect.getType(), (int) (duration * agilityAbilities.getSugarRushSplashMultiplier(player)), effect.getAmplifier()));
                            } else {
                                PotionUtil.applyEffect(player, new PotionEffect(effect.getType(), duration, effect.getAmplifier()));
                            }

                        }
                        // Special case for Turtle Master
                        else {
                            if (!potionData.isUpgraded()) {
                                PotionUtil.applyEffect(player, new PotionEffect(PotionEffectType.SLOW, duration, 3));
                                PotionUtil.applyEffect(player, new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, 2));
                            } else {
                                PotionUtil.applyEffect(player, new PotionEffect(PotionEffectType.SLOW, duration, 5));
                                PotionUtil.applyEffect(player, new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,duration, 3));
                            }
                        }
                    }
                }
            }
        }
    }

    private double getSplasherMultiplier(ProjectileSource source, @NotNull Collection<LivingEntity> affectedEntities) {
        double splasherMultiplier = 1.0;
        if (source instanceof Player) {
            Player player = (Player) source;
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData != null && plugin.getAbilityManager().isEnabled(Ability.SPLASHER)) {
                if (playerData.getAbilityLevel(Ability.SPLASHER) > 0) {
                    double splasherPercent = getValue(Ability.SPLASHER, playerData);
                    int affectedPlayers = (int) affectedEntities.stream().filter(entity -> entity instanceof Player).filter(entity -> plugin.getPlayerManager().getPlayerData(entity.getUniqueId()) != null).count();
                    splasherMultiplier = 1 + (splasherPercent / 100 * affectedPlayers);
                }
            }
        }
        return splasherMultiplier;
    }

    // Handles the Lingering ability
    @EventHandler
    @SuppressWarnings("deprecation")
    public void lingering(@NotNull LingeringPotionSplashEvent event) {
        if (blockDisabled(Ability.LINGERING)) return;
        if (event.isCancelled()) return;
        Player player = null;
        if (VersionUtils.isAtLeastVersion(14)) {
            if (event.getEntity().getShooter() instanceof Player) {
                player = (Player) event.getEntity().getShooter();
            }
        } else {
            try {
                Object lingeringPotionObject = event.getClass().getDeclaredMethod("getEntity").invoke(event);
                if (lingeringPotionObject instanceof LingeringPotion) {
                    LingeringPotion lingeringPotion = (LingeringPotion) lingeringPotionObject;
                    if (lingeringPotion.getShooter() instanceof Player) {
                        player = (Player) lingeringPotion.getShooter();
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) { }
        }
        if (player != null) {
            if (blockAbility(player)) return;
            @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData == null) return;
            if (playerData.getAbilityLevel(Ability.LINGERING) > 0) {
                AreaEffectCloud cloud = event.getAreaEffectCloud();
                if (cloud.hasCustomEffects() && OptionL.getBoolean(Option.ALCHEMY_IGNORE_CUSTOM_POTIONS)) return;
                // Get values
                double naturalDecay = 1 - (getValue(Ability.LINGERING, playerData) / 100);
                double entityDecay = 1 - (getValue2(Ability.LINGERING, playerData) / 100);
                // 1% limit
                if (naturalDecay <= 0.01) naturalDecay = 0.01;
                if (entityDecay <= 0.01) entityDecay = 0.01;
                // Apply values
                cloud.setRadiusPerTick(cloud.getRadiusPerTick() * (float) naturalDecay);
                cloud.setRadiusOnUse(cloud.getRadiusOnUse() * (float) entityDecay);
            }
        }
    }

    private void wiseEffect() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!blockDisabled(Ability.WISE_EFFECT)) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                        if (playerData != null) {
                            if (player.getActivePotionEffects().size() > 0) {
                                if (playerData.getAbilityLevel(Ability.WISE_EFFECT) <= 0) {
                                    continue;
                                }
                                // Get unique active potion effects
                                if (!blockAbility(player)) {
                                    Set<PotionEffectType> uniqueTypesSet = new HashSet<>();
                                    for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                                        uniqueTypesSet.add(potionEffect.getType());
                                    }
                                    int uniqueTypes = uniqueTypesSet.size();
                                    // Apply modifier
                                    double wisdomPerType = getValue(Ability.WISE_EFFECT, playerData);
                                    double modifierValue = wisdomPerType * uniqueTypes;
                                    if (modifierValue > 0.0) {
                                        StatModifier modifier = new StatModifier("AbilityModifier-WiseEffect", Stats.WISDOM, modifierValue);
                                        playerData.addStatModifier(modifier, false);
                                    }
                                }
                            } else {
                                playerData.removeStatModifier("AbilityModifier-WiseEffect", false);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 10L);
    }

}
