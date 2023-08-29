package dev.aurelium.auraskills.bukkit.listeners;

import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.bukkit.skills.archery.ArcheryAbilities;
import dev.aurelium.auraskills.bukkit.skills.archery.ChargedShot;
import dev.aurelium.auraskills.bukkit.skills.defense.Absorption;
import dev.aurelium.auraskills.bukkit.skills.defense.DefenseAbilities;
import dev.aurelium.auraskills.bukkit.skills.excavation.ExcavationAbilities;
import dev.aurelium.auraskills.bukkit.skills.farming.FarmingAbilities;
import dev.aurelium.auraskills.bukkit.skills.fighting.FightingAbilities;
import dev.aurelium.auraskills.bukkit.skills.foraging.ForagingAbilities;
import dev.aurelium.auraskills.bukkit.skills.mining.MiningAbilities;
import dev.aurelium.auraskills.bukkit.trait.AttackDamageTrait;
import dev.aurelium.auraskills.bukkit.trait.DamageReductionTrait;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.mechanics.DamageType;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

    private final AuraSkills plugin;
    private final CriticalHandler criticalHandler;

    public DamageListener(AuraSkills plugin) {
        this.plugin = plugin;
        this.criticalHandler = new CriticalHandler(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent event) {
        // Check if not cancelled
        if (event.isCancelled()) {
            return;
        }

        // Gets the player who dealt damage
        Player player = getDamager(event.getDamager());
        if (player == null) {
            if (event.getEntity() instanceof Player) {
                onDamaged(event, (Player) event.getEntity());
            }
            return;
        }

        // Check disabled world
        if (plugin.getWorldManager().isInDisabledWorld(player.getLocation())) {
            return;
        }
        // Gets player skill
        User user = plugin.getUser(player);

        DamageType damageType = getDamageType(event, player);

        // Applies attack damage trait
        var attackDamage = plugin.getTraitManager().getTraitImpl(AttackDamageTrait.class);
        attackDamage.strength(event, user, damageType);

        // Apply master abilities
        var abManager = plugin.getAbilityManager();
        switch (damageType) {
            case SWORD -> abManager.getAbilityImpl(FightingAbilities.class).swordMaster(event, player, user);
            case BOW -> abManager.getAbilityImpl(ArcheryAbilities.class).bowMaster(event, player, user);
            case HOE -> abManager.getAbilityImpl(FarmingAbilities.class).scytheMaster(event, player, user);
            case AXE -> abManager.getAbilityImpl(ForagingAbilities.class).axeMaster(event, player, user);
            case PICKAXE -> abManager.getAbilityImpl(MiningAbilities.class).pickMaster(event, player, user);
            case SHOVEL -> abManager.getAbilityImpl(ExcavationAbilities.class).spadeMaster(event, player, user);
        }

        // Apply First Strike
        if (damageType == DamageType.SWORD) {
            abManager.getAbilityImpl(FightingAbilities.class).firstStrike(event, user, player);
        }

        // Apply critical
        if (plugin.configBoolean(Option.valueOf("CRITICAL_ENABLED_" + damageType.name()))) {
            criticalHandler.applyCrit(event, player, user);
        }

        // Charged shot
        if (damageType == DamageType.BOW) {
            plugin.getManaAbilityManager().getProvider(ChargedShot.class).applyChargedShot(event);
        }
    }

    private void onDamaged(EntityDamageByEntityEvent event, Player player) {
        // Check disabled world
        if (plugin.getWorldManager().isInDisabledWorld(player.getLocation())) {
            return;
        }
        if (event.isCancelled()) return;
        User user = plugin.getUser(player);

        // Handles absorption
        plugin.getManaAbilityManager().getProvider(Absorption.class).handleAbsorption(event, player, user);
        if (event.isCancelled()) return;

        // Handles damage reduction trait
        var damageReduction = plugin.getTraitManager().getTraitImpl(DamageReductionTrait.class);
        damageReduction.onDamage(event, user);

        DefenseAbilities defenseAbilities = plugin.getAbilityManager().getAbilityImpl(DefenseAbilities.class);

        // Handles mob master
        defenseAbilities.mobMaster(event, user, player);

        // Handles shielding
        defenseAbilities.shielding(event, user, player);
    }

    private DamageType getDamageType(EntityDamageByEntityEvent event, Player player) {
        if (event.getDamager() instanceof Arrow || event.getDamager() instanceof SpectralArrow || event.getDamager() instanceof TippedArrow) {
            return DamageType.BOW;
        }
        Material material = player.getInventory().getItemInMainHand().getType();
        if (material.name().contains("SWORD")) {
            return DamageType.SWORD;
        } else if (material.name().contains("_AXE")) {
            return DamageType.AXE;
        } else if (material.name().contains("PICKAXE")) {
            return DamageType.PICKAXE;
        } else if (material.name().contains("SHOVEL") || material.name().contains("SPADE")) {
            return DamageType.SHOVEL;
        } else if (material.name().contains("HOE")) {
            return DamageType.HOE;
        } else if (material.equals(Material.AIR)) {
            return DamageType.HAND;
        } else if (event.getDamager() instanceof Trident) {
            return DamageType.BOW;
        }
        return DamageType.OTHER;
    }

    private Player getDamager(Entity entity) {
        Player player = null;
        if (entity instanceof Player) {
            player = (Player) entity;
        }
        else if (entity instanceof Projectile projectile) {
            EntityType type = projectile.getType();
            if (type == EntityType.ARROW || type == EntityType.SPECTRAL_ARROW || type.toString().equals("TRIDENT") ||
                    type.toString().equals("TIPPED_ARROW")) {
                if (projectile.getShooter() instanceof Player) {
                    player = (Player) projectile.getShooter();
                }
            }
        }
        return player;
    }
}