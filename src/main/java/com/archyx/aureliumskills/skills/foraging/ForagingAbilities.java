package com.archyx.aureliumskills.skills.foraging;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.ability.AbilityProvider;
import com.archyx.aureliumskills.api.event.LootDropCause;
import com.archyx.aureliumskills.api.event.PlayerLootDropEvent;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.modifier.StatModifier;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.stats.Stats;
import com.archyx.aureliumskills.util.item.ItemUtils;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ForagingAbilities extends AbilityProvider implements Listener {

	private final Random r = new Random();
	
	public ForagingAbilities(AureliumSkills plugin) {
		super(plugin, Skills.FORAGING);
	}
	
	public void lumberjack(Player player, Block block) {
		if (OptionL.isEnabled(Skills.FORAGING)) {
			if (plugin.getAbilityManager().isEnabled(Ability.LUMBERJACK)) {
				if (player.getGameMode().equals(GameMode.SURVIVAL)) {
					PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
					if (playerData == null) return;
					if (playerData.getAbilityLevel(Ability.LUMBERJACK) > 0) {
						if (r.nextDouble() < ((getValue(Ability.LUMBERJACK, playerData)) / 100)) {
							for (ItemStack item : block.getDrops(player.getInventory().getItemInMainHand())) {
								PlayerLootDropEvent event = new PlayerLootDropEvent(player, item.clone(), block.getLocation().add(0.5, 0.5, 0.5), LootDropCause.LUMBERJACK);
								Bukkit.getPluginManager().callEvent(event);
								if (!event.isCancelled()) {
									block.getWorld().dropItem(event.getLocation(), event.getItemStack());
								}
							}
						}
					}
				}
			}
		}
	}

	public void axeMaster(EntityDamageByEntityEvent event, Player player, PlayerData playerData) {
		if (OptionL.isEnabled(Skills.FORAGING)) {
			if (plugin.getAbilityManager().isEnabled(Ability.AXE_MASTER)) {
				//Check permission
				if (!player.hasPermission("aureliumskills.foraging")) {
					return;
				}
				if (playerData.getAbilityLevel(Ability.AXE_MASTER) > 0) {
					event.setDamage(event.getDamage() * (1 + (getValue(Ability.AXE_MASTER, playerData) / 100)));
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void shredder(PlayerItemDamageEvent event) {
		if (blockDisabled(Ability.SHREDDER)) return;
		if (!event.isCancelled()) {
			//If is item taking durabilty damage is armor
			if (ItemUtils.isArmor(event.getItem().getType())) {
				//If last damage was from entity
				if (event.getPlayer().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
					EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event.getPlayer().getLastDamageCause();
					//If last damage was from player
					if (e.getDamager() instanceof Player) {
						Player player = (Player) e.getDamager();
						if (blockAbility(player)) return;
						//If damage was an attack
						if (e.getCause().equals(DamageCause.ENTITY_ATTACK)) {
							//If item used was an axe
							Material mat = player.getInventory().getItemInMainHand().getType();
							if (mat.equals(Material.DIAMOND_AXE) || mat.equals(Material.IRON_AXE) || mat.equals(XMaterial.GOLDEN_AXE.parseMaterial())
									|| mat.equals(Material.STONE_AXE) || mat.equals(XMaterial.WOODEN_AXE.parseMaterial())) {
								PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
								if (playerData == null) return;
								//Checks if shredder is used
								if (playerData.getAbilityLevel(Ability.SHREDDER) > 0) {
									if (r.nextDouble() < (getValue(Ability.SHREDDER, playerData)) / 100) {
										event.setDamage(event.getDamage() * 3);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public void applyValor(PlayerData playerData) {
		if (OptionL.isEnabled(Skills.FORAGING)) {
			if (plugin.getAbilityManager().isEnabled(Ability.VALOR)) {
				if (playerData.getAbilityLevel(Ability.VALOR) > 0) {
					playerData.addStatModifier(new StatModifier("foraging-valor", Stats.STRENGTH, (int) getValue(Ability.VALOR, playerData)));
				}
			}
		}
	}

	public void removeValor(PlayerData playerData) {
		playerData.removeStatModifier("foraging-valor");
	}
}
