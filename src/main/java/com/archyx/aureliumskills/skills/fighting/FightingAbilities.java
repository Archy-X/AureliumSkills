package com.archyx.aureliumskills.skills.fighting;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.ability.AbilityProvider;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.configuration.OptionValue;
import com.archyx.aureliumskills.data.AbilityData;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.AbilityMessage;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.skills.Skills;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Locale;
import java.util.Random;

public class FightingAbilities extends AbilityProvider implements Listener {

    private final Random r = new Random();

    public FightingAbilities(AureliumSkills plugin) {
        super(plugin, Skills.FIGHTING);
    }

    public void swordMaster(EntityDamageByEntityEvent event, Player player, PlayerData playerData) {
        if (OptionL.isEnabled(Skills.FIGHTING)) {
            if (plugin.getAbilityManager().isEnabled(Ability.SWORD_MASTER)) {
                if (!player.hasPermission("aureliumskills.fighting")) {
                    return;
                }
                if (playerData.getAbilityLevel(Ability.SWORD_MASTER) > 0) {
                    //Modifies damage
                    double modifier = getValue(Ability.SWORD_MASTER, playerData) / 100;
                    event.setDamage(event.getDamage() * (1 + modifier));
                }
            }
        }
    }

    public void firstStrike(EntityDamageByEntityEvent event, PlayerData playerData, Player player) {
        if (OptionL.isEnabled(Skills.FIGHTING)) {
            if (plugin.getAbilityManager().isEnabled(Ability.FIRST_STRIKE)) {
                if (!player.hasMetadata("AureliumSkills-FirstStrike")) {
                    if (playerData.getAbilityLevel(Ability.FIRST_STRIKE) > 0) {
                        Locale locale = playerData.getLocale();
                        //Modifies damage
                        double modifier = getValue(Ability.FIRST_STRIKE, playerData) / 100;
                        event.setDamage(event.getDamage() * (1 + modifier));
                        if (plugin.getAbilityManager().getOptionAsBooleanElseTrue(Ability.FIRST_STRIKE, "enable_message")) {
                            plugin.getAbilityManager().sendMessage(player, Lang.getMessage(AbilityMessage.FIRST_STRIKE_DEALT, locale));
                        }
                        //Adds metadata
                        player.setMetadata("AureliumSkills-FirstStrike", new FixedMetadataValue(plugin, true));
                        //Increments counter
                        AbilityData abilityData = playerData.getAbilityData(Ability.FIRST_STRIKE);
                        if (abilityData.containsKey("counter")) {
                            abilityData.setData("counter", abilityData.getInt("counter") + 1);
                        } else {
                            abilityData.setData("counter", 0);
                        }
                        int id = abilityData.getInt("counter");
                        //Schedules metadata removal
                        long cooldown = 6000L;
                        OptionValue optionValue = plugin.getAbilityManager().getOption(Ability.FIRST_STRIKE, "cooldown_ticks");
                        if (optionValue != null) {
                            cooldown = optionValue.asInt();
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (playerData.getAbilityData(Ability.FIRST_STRIKE).containsKey("counter")) {
                                    if (playerData.getAbilityData(Ability.FIRST_STRIKE).getInt("counter") == id) {
                                        player.removeMetadata("AureliumSkills-FirstStrike", plugin);
                                    }
                                }
                            }
                        }.runTaskLater(plugin, cooldown);
                    }
                }
            }
        }
    }

    public void bleed(EntityDamageByEntityEvent event, PlayerData playerData, LivingEntity entity) {
        if (r.nextDouble() < (getValue(Ability.BLEED, playerData) / 100)) {
            if (event.getFinalDamage() < entity.getHealth()) {
                String key;
                OptionValue optionValue;
                if (!entity.hasMetadata("AureliumSkills-BleedTicks")) {
                    key = "base_ticks";
                    optionValue = plugin.getAbilityManager().getOption(Ability.BLEED, key);
                    if (optionValue == null)
                        throw new IllegalStateException("Invalid ability manager option index key: " + key);
                    int baseTicks = optionValue.asInt();
                    entity.setMetadata("AureliumSkills-BleedTicks", new FixedMetadataValue(plugin, baseTicks));
                    // Send messages
                    if (plugin.getAbilityManager().getOptionAsBooleanElseTrue(Ability.BLEED, "enable_enemy_message")) {
                        Locale locale = playerData.getLocale();
                        if (event.getDamager() instanceof Player) {
                            Player player = (Player) event.getDamager();
                            plugin.getAbilityManager().sendMessage(player, Lang.getMessage(AbilityMessage.BLEED_ENEMY_BLEEDING, locale));
                        }
                    }
                    if (entity instanceof Player) {
                        if (plugin.getAbilityManager().getOptionAsBooleanElseTrue(Ability.BLEED, "enable_self_message")) {
                            Player player = (Player) entity;
                            Locale locale = plugin.getLang().getLocale(player);
                            plugin.getAbilityManager().sendMessage(player, Lang.getMessage(AbilityMessage.BLEED_SELF_BLEEDING, locale));
                        }
                    }
                    // Schedule applying bleed tick damage
                    scheduleBleedTicks(entity, playerData);
                } else {
                    int currentTicks = entity.getMetadata("AureliumSkills-BleedTicks").get(0).asInt();
                    key = "added_ticks";
                    optionValue = plugin.getAbilityManager().getOption(Ability.BLEED, key);
                    if (optionValue == null)
                        throw new IllegalStateException("Invalid ability manager option index key: " + key);
                    int addedTicks = optionValue.asInt();
                    key = "max_ticks";
                    optionValue = plugin.getAbilityManager().getOption(Ability.BLEED, key);
                    if (optionValue == null)
                        throw new IllegalStateException("Invalid ability manager option index key: " + key);
                    int maxTicks = optionValue.asInt();
                    int resultingTicks = currentTicks + addedTicks;
                    if (resultingTicks <= maxTicks) { // Check that resulting bleed ticks does not exceed maximum
                        entity.setMetadata("AureliumSkills-BleedTicks", new FixedMetadataValue(plugin, resultingTicks));
                    }
                }
            }
        }
    }

    private void scheduleBleedTicks(LivingEntity entity, PlayerData playerData) {
        String key = "tick_period";
        OptionValue optionValue = plugin.getAbilityManager().getOption(Ability.BLEED, key);
        if (optionValue == null)
            throw new IllegalStateException("Invalid ability manager option index key: " + key);
        // Schedules bleed ticks
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!entity.isValid()) { // Stop if entity died/transformed
                    cancel();
                    return;
                }
                if (entity.hasMetadata("AureliumSkills-BleedTicks")) {
                    int bleedTicks = entity.getMetadata("AureliumSkills-BleedTicks").get(0).asInt();
                    if (bleedTicks > 0) {
                        // Apply bleed
                        double damage = plugin.getAbilityManager().getValue2(Ability.BLEED, playerData.getAbilityLevel(Ability.BLEED));
                        double healthBefore = entity.getHealth();
                        entity.damage(damage);
                        double healthAfter = entity.getHealth();
                        if (healthAfter != healthBefore) { // Only display particles if damage was actually done
                            displayBleedParticles(entity);
                        }
                        // Decrement bleed ticks
                        if (bleedTicks != 1) {
                            entity.setMetadata("AureliumSkills-BleedTicks", new FixedMetadataValue(plugin, bleedTicks - 1));
                        } else {
                            entity.removeMetadata("AureliumSkills-BleedTicks", plugin);
                        }
                        return;
                    }
                }
                if (entity instanceof Player) {
                    if (plugin.getAbilityManager().getOptionAsBooleanElseTrue(Ability.BLEED, "enable_stop_message")) {
                        Player player = (Player) entity;
                        Locale locale = plugin.getLang().getLocale(player);
                        plugin.getAbilityManager().sendMessage(player, Lang.getMessage(AbilityMessage.BLEED_STOP, locale));
                    }
                }
                cancel();
            }
        }.runTaskTimer(plugin, 40L, optionValue.asInt());
    }

    @SuppressWarnings("deprecation")
    private void displayBleedParticles(LivingEntity entity) {
        // Check if disabled
        if (!plugin.getAbilityManager().getOptionAsBooleanElseTrue(Ability.BLEED, "show_particles")) {
            return;
        }
        Location location = entity.getLocation().add(0, entity.getHeight() * 0.6, 0);
        Object particleData;
        if (XMaterial.isNewVersion()) {
            particleData = Material.REDSTONE_BLOCK.createBlockData();
        } else {
            particleData = new MaterialData(Material.REDSTONE_BLOCK);
        }
        entity.getWorld().spawnParticle(Particle.BLOCK_DUST, location, 30, particleData);
    }

    @EventHandler
    public void onDeath(PlayerRespawnEvent event) {
        event.getPlayer().removeMetadata("AureliumSkills-BleedTicks", plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void fightingListener(EntityDamageByEntityEvent event) {
        if (OptionL.isEnabled(Skills.FIGHTING)) {
            if (!event.isCancelled()) {
                if (event.getDamager() instanceof Player) {
                    Player player = (Player) event.getDamager();
                    if (blockAbility(player)) return;
                    //If player used sword
                    if (player.getInventory().getItemInMainHand().getType().name().toUpperCase().contains("SWORD")) {
                        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                        if (playerData == null) return;
                        if (isEnabled(Ability.BLEED)) {
                            if (event.getEntity() instanceof LivingEntity) {
                                bleed(event, playerData, (LivingEntity) event.getEntity());
                            }
                        }
                    }
                }
            }
        }
    }
}
