package com.archyx.aureliumskills.skills.fishing;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.leveler.Leveler;
import com.archyx.aureliumskills.leveler.SkillLeveler;
import com.archyx.aureliumskills.skills.Skills;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;

public class FishingLeveler extends SkillLeveler implements Listener {

	public FishingLeveler(AureliumSkills plugin) {
		super(plugin, Ability.FISHER);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFish(PlayerFishEvent event) {
		if (OptionL.isEnabled(Skills.FISHING)) {
			//Check cancelled
			if (OptionL.getBoolean(Option.FISHING_CHECK_CANCELLED)) {
				if (event.isCancelled()) {
					return;
				}
			}
			if (event.getState().equals(State.CAUGHT_FISH)) {
				Player player = event.getPlayer();
				if (blockXpGain(player)) return;
				if (event.getCaught() instanceof Item) {
					ItemStack item = ((Item) event.getCaught()).getItemStack();
					Leveler leveler = plugin.getLeveler();
					FishingSource source = FishingSource.valueOf(item);
					if (source != null) {
						leveler.addXp(player, Skills.FISHING, getXp(player, source));
					}
				}
			}
		}
	}
	
	
}
