package com.archyx.aureliumskills.skills.fishing;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.ability.AbilityProvider;
import com.archyx.aureliumskills.api.event.LootDropCause;
import com.archyx.aureliumskills.api.event.PlayerLootDropEvent;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.skills.Skills;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

public class FishingAbilities extends AbilityProvider implements Listener {

	private final Random r = new Random();

	public FishingAbilities(AureliumSkills plugin) {
		super(plugin, Skills.FISHING);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void luckyCatch(PlayerFishEvent event) {
		if (blockDisabled(Ability.LUCKY_CATCH)) return;
		Player player = event.getPlayer();
		if (blockAbility(player)) return;
		if (event.isCancelled()) return;
		if (event.getCaught() instanceof Item) {
			if (event.getExpToDrop() > 0) {
				PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
				if (playerData != null) {
					if (r.nextDouble() < (getValue(Ability.LUCKY_CATCH, playerData) / 100)) {
						Entity caught = event.getCaught();
						if (caught == null)
							return;
						Item item = (Item) caught;
						ItemStack drop = item.getItemStack();
						if (drop.getMaxStackSize() > 1) {
							drop.setAmount(drop.getAmount() * 2);
							PlayerLootDropEvent dropEvent = new PlayerLootDropEvent(player, drop, item.getLocation(), LootDropCause.LUCKY_CATCH);
							Bukkit.getPluginManager().callEvent(dropEvent);
							if (!event.isCancelled()) {
								item.setItemStack(dropEvent.getItemStack());
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void grappler(PlayerFishEvent event) {
		if (blockDisabled(Ability.GRAPPLER)) return;
		if (event.getCaught() != null) {
			if (!(event.getCaught() instanceof Item)) {
				PlayerData playerData = plugin.getPlayerManager().getPlayerData(event.getPlayer());
				if (playerData != null) {
					Player player = event.getPlayer();
					if (blockAbility(player)) return;
					Entity caught = event.getCaught();
					if (caught == null)
						return;
					Vector vector = player.getLocation().toVector().subtract(caught.getLocation().toVector());
					Vector result = vector.multiply(0.004 + (getValue(Ability.GRAPPLER, playerData) / 25000));

					if (isUnsafeVelocity(result)) { // Prevent excessive velocity warnings
						return;
					}
					caught.setVelocity(result);
				}
			}
		}
	}

	private boolean isUnsafeVelocity(Vector vector) {
		double x = vector.getX();
		double y = vector.getY();
		double z = vector.getZ();

		return x > 4 || x < -4 || y > 4 || y < -4 || z > 4 || z < -4;
	}

}
