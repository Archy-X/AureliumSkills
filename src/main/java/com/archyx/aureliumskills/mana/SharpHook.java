package com.archyx.aureliumskills.mana;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.ManaAbilityMessage;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class SharpHook extends ManaAbilityProvider {

    public SharpHook(@NotNull AureliumSkills plugin) {
        super(plugin, MAbility.SHARP_HOOK, ManaAbilityMessage.SHARP_HOOK_USE, ManaAbilityMessage.NONE);
    }

    @Override
    public void onActivate(@NotNull Player player, @NotNull PlayerData playerData) {
        if (plugin.getManaAbilityManager().getOptionAsBooleanElseTrue(MAbility.SHARP_HOOK, "enable_sound")) {
            if (XMaterial.isNewVersion()) {
                player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1f, 1.5f);
            } else {
                player.playSound(player.getLocation(), "entity.bobber.retrieve", 1f, 1.5f);
            }
        }
    }

    @Override
    public void onStop(@NotNull Player player, @NotNull PlayerData playerData) {

    }

    @EventHandler
    public void sharpHook(@NotNull PlayerInteractEvent event) {
        if (!OptionL.isEnabled(Skills.FISHING) || !plugin.getAbilityManager().isEnabled(MAbility.SHARP_HOOK)) return;
        // If left click with fishing rod
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.FISHING_ROD) return;

        Player player = event.getPlayer();
        if (blockAbility(player)) return;
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null) {
            return;
        }
        if (playerData.getManaAbilityLevel(MAbility.SHARP_HOOK) <= 0) {
            return;
        }
        // Check for player just casting rod
        for (Entity entity : player.getNearbyEntities(0.1, 0.1, 0.1)) {
            if (entity instanceof FishHook) {
                FishHook fishHook = (FishHook) entity;
                ProjectileSource source = fishHook.getShooter();
                if (fishHook.isValid() && source instanceof Player) {
                    if (source.equals(player)) {
                        return;
                    }
                }
            }
        }
        // Check entities
        for (Entity entity : player.getNearbyEntities(33, 33 ,33)) {
            if (!(entity instanceof FishHook)) continue;
            FishHook fishHook = (FishHook) entity;
            ProjectileSource source = fishHook.getShooter();
            if (fishHook.isValid() && source instanceof Player) {
                if (!source.equals(player)) continue;
                for (Entity hooked : fishHook.getNearbyEntities(0.1, 0.1, 0.1)) {
                    if (hooked instanceof LivingEntity) {
                        LivingEntity livingEntity = (LivingEntity) hooked;
                        if (!livingEntity.isDead() && livingEntity.isValid()) {
                            int cooldown = manager.getPlayerCooldown(player.getUniqueId(), MAbility.SHARP_HOOK);
                            if (cooldown == 0) {
                                if (areValidLocations(player, livingEntity)) { // Check that the locations of the entities are valid
                                    activateSharpHook(player, playerData, livingEntity);
                                }
                            } else {
                                if (manager.getErrorTimer(player.getUniqueId(), MAbility.SHARP_HOOK) == 0) {
                                    Locale locale = playerData.getLocale();
                                    plugin.getAbilityManager().sendMessage(player, TextUtil.replace(Lang.getMessage(ManaAbilityMessage.NOT_READY, locale), "{cooldown}", NumberUtil.format1((double) (cooldown) / 20)));
                                    manager.setErrorTimer(player.getUniqueId(), MAbility.SHARP_HOOK, 2);
                                }
                            }
                            break;
                        }
                    }
                }
                break;
            }
        }
    }

    private void activateSharpHook(@NotNull Player player, @NotNull PlayerData playerData, @NotNull LivingEntity caught) {
        if (hasEnoughMana(player)) {
            double damage = plugin.getManaAbilityManager().getValue(MAbility.SHARP_HOOK, playerData);
            double healthBefore = caught.getHealth();
            caught.damage(damage, player);
            double healthAfter = caught.getHealth();
            if (healthBefore != healthAfter) { // Only activate if the entity got damaged
                activate(player);
            }
        }
    }

    private boolean areValidLocations(@NotNull Player damager, @NotNull LivingEntity hooked) {
        Location damagerLocation = damager.getLocation();
        Location hookedLocation = hooked.getLocation();
        // Disallow if in different worlds
        World damagerWorld = damagerLocation.getWorld();
        World hookedWorld = hookedLocation.getWorld();
        if (damagerWorld != null && hookedWorld != null) {
            if (!damagerWorld.equals(hookedWorld)) {
                return false;
            }
        }
        // Disallow if more than 33 blocks away
        return !(damagerLocation.distanceSquared(hookedLocation) > 1089);
    }

    @Override
    protected int getDuration(@NotNull PlayerData playerData) {
        return 0;
    }
}
