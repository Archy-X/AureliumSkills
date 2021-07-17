package com.archyx.aureliumskills.listeners;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.mana.Absorption;
import com.archyx.aureliumskills.mana.ChargedShot;
import com.archyx.aureliumskills.mana.MAbility;
import com.archyx.aureliumskills.mana.ManaAbilityProvider;
import com.archyx.aureliumskills.skills.archery.ArcheryAbilities;
import com.archyx.aureliumskills.skills.defense.DefenseAbilities;
import com.archyx.aureliumskills.skills.excavation.ExcavationAbilities;
import com.archyx.aureliumskills.skills.farming.FarmingAbilities;
import com.archyx.aureliumskills.skills.fighting.FightingAbilities;
import com.archyx.aureliumskills.skills.foraging.ForagingAbilities;
import com.archyx.aureliumskills.skills.mining.MiningAbilities;
import com.archyx.aureliumskills.stats.Strength;
import com.archyx.aureliumskills.stats.Toughness;
import com.archyx.aureliumskills.util.mechanics.DamageType;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

    private final Strength strength;
    private final Critical critical;
    private final AureliumSkills plugin;
    private final ExcavationAbilities excavationAbilities;
    private final FarmingAbilities farmingAbilities;
    private final MiningAbilities miningAbilities;
    private final ForagingAbilities foragingAbilities;
    private final ArcheryAbilities archeryAbilities;
    private final FightingAbilities fightingAbilities;
    private final DefenseAbilities defenseAbilities;

    public DamageListener(AureliumSkills plugin, DefenseAbilities defenseAbilities, FightingAbilities fightingAbilities) {
        strength = new Strength();
        this.plugin = plugin;
        this.critical = new Critical(plugin);
        this.excavationAbilities = new ExcavationAbilities(plugin);
        this.farmingAbilities = new FarmingAbilities(plugin);
        this.miningAbilities = new MiningAbilities(plugin);
        this.foragingAbilities = new ForagingAbilities(plugin);
        this.archeryAbilities = new ArcheryAbilities(plugin);
        this.defenseAbilities = defenseAbilities;
        this.fightingAbilities = fightingAbilities;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent event) {

        //Check if not cancelled
        if (event.isCancelled()) {
            return;
        }

        //Gets the player who dealt damage
        Player player = getDamager(event.getDamager());
        if (player != null) {

            //Check disabled world
            if (plugin.getWorldManager().isInDisabledWorld(player.getLocation())) {
                return;
            }
            //Gets player skill
            PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData == null) return;

            DamageType damageType = getDamageType(event, player);

            //Applies strength
            strength.strength(event, playerData, damageType);

            //Applies master abilities
            switch (damageType) {
                case SWORD:
                    fightingAbilities.swordMaster(event, player, playerData);
                    break;
                case BOW:
                    archeryAbilities.bowMaster(event, player, playerData);
                    break;
                case AXE:
                    foragingAbilities.axeMaster(event, player, playerData);
                    break;
                case PICKAXE:
                    miningAbilities.pickMaster(event, player, playerData);
                    break;
                case HOE:
                    farmingAbilities.scytheMaster(event, player, playerData);
                    break;
                case SHOVEL:
                    excavationAbilities.spadeMaster(event, player, playerData);
                    break;
            }

            //First strike
            if (damageType == DamageType.SWORD) {
                fightingAbilities.firstStrike(event, playerData, player);
            }

            //Critical
            if (OptionL.criticalEnabled(damageType)) {
                critical.applyCrit(event, player, playerData);
            }

            // Charged shot
            if (damageType == DamageType.BOW) {
                ManaAbilityProvider provider = plugin.getManaAbilityManager().getProvider(MAbility.CHARGED_SHOT);
                if (provider instanceof ChargedShot) {
                    ChargedShot chargedShot = (ChargedShot) provider;
                    chargedShot.applyChargedShot(event);
                }
            }
        }
        //Handles being damaged
        if (event.getEntity() instanceof Player) {
            onDamaged(event, (Player) event.getEntity());
        }
    }

    private void onDamaged(EntityDamageByEntityEvent event, Player player) {
        // Check disabled world
        if (plugin.getWorldManager().isInDisabledWorld(player.getLocation())) {
            return;
        }
        // Gets player skill
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null) return;
        // Checks for absorption activation and applies
        ManaAbilityProvider provider = plugin.getManaAbilityManager().getProvider(MAbility.ABSORPTION);
        if (provider instanceof Absorption) {
            Absorption absorption = (Absorption) provider;
            absorption.handleAbsorption(event, player, playerData);
        }
        if (event.isCancelled()) return;

        // Handles toughness
        Toughness.onDamage(event, playerData);

        // Handles mob master
        defenseAbilities.mobMaster(event, playerData);

        // Handles shielding
        defenseAbilities.shielding(event, playerData, player);
    }

    @SuppressWarnings("deprecation")
    private DamageType getDamageType(EntityDamageByEntityEvent event, Player player) {
        if (event.getDamager() instanceof Arrow || event.getDamager() instanceof SpectralArrow || event.getDamager() instanceof TippedArrow) {
            return DamageType.BOW;
        }
        Material material = player.getInventory().getItemInMainHand().getType();
        if (material.name().contains("SWORD")) {
            return DamageType.SWORD;
        }
        else if (material.name().contains("_AXE")) {
            return DamageType.AXE;
        }
        else if (material.name().contains("PICKAXE")) {
            return DamageType.PICKAXE;
        }
        else if (material.name().contains("SHOVEL") || material.name().contains("SPADE")) {
            return DamageType.SHOVEL;
        }
        else if (material.name().contains("HOE")) {
            return DamageType.HOE;
        }
        else if (material.equals(Material.AIR)) {
            return DamageType.HAND;
        }
        else if (XMaterial.isNewVersion()) {
            if (event.getDamager() instanceof Trident) {
                return DamageType.BOW;
            }
        }
        return DamageType.OTHER;
    }

    private Player getDamager(Entity entity) {
        Player player = null;
        if (entity instanceof Player) {
            player = (Player) entity;
        }
        else if (entity instanceof Projectile) {
            Projectile projectile = (Projectile) entity;
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
