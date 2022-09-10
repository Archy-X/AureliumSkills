package com.archyx.aureliumskills.listeners;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerManager;
import com.archyx.aureliumskills.data.storage.MySqlStorageProvider;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.util.version.ReleaseData;
import com.archyx.aureliumskills.util.version.UpdateChecker;
import dev.dbassett.skullcreator.SkullCreator;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class PlayerJoinQuit implements Listener {

	private final @NotNull AureliumSkills plugin;

	public PlayerJoinQuit(@NotNull AureliumSkills plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
		Player player = event.getPlayer();
		PlayerManager playerManager = plugin.getPlayerManager();
		if (plugin.getStorageProvider() instanceof MySqlStorageProvider) { // Handles MySQL storage
			if (OptionL.getBoolean(Option.MYSQL_ALWAYS_LOAD_ON_JOIN) || playerManager.getPlayerData(player) == null) {
				int loadDelay = OptionL.getInt(Option.MYSQL_LOAD_DELAY);
				if (loadDelay == 0) {
					// Load immediately
					loadPlayerDataAsync(player);
				} else {
					// Delay loading
					new BukkitRunnable() {
						@Override
						public void run() {
							loadPlayerDataAsync(player);
						}
					}.runTaskLater(plugin, loadDelay);
				}
			}
		} else { // Yaml storage
			if (playerManager.getPlayerData(player) == null) {
				loadPlayerDataAsync(player);
			}
		}
		// Load player skull
		Location playerLoc = player.getLocation();
		Location loc = new Location(playerLoc.getWorld(), playerLoc.getX(), 0, playerLoc.getZ());
		Block b = loc.getBlock();
		if (!(b.getState() instanceof InventoryHolder)) {
			BlockState state = b.getState();
			SkullCreator.blockWithUuid(b, player.getUniqueId());
			state.update(true);
		}
		// Update message
		if (OptionL.getBoolean(Option.CHECK_FOR_UPDATES) && player.hasPermission("aureliumskills.checkupdates")) {
			if (System.currentTimeMillis() > ReleaseData.RELEASE_TIME + 21600000L) {
				// Check for updates
				new UpdateChecker(plugin, 81069).getVersion(version -> {
					if (!plugin.getDescription().getVersion().contains("Pre-Release") && !plugin.getDescription().getVersion().contains("Build")) {
						if (!plugin.getDescription().getVersion().equalsIgnoreCase(version)) {
							player.sendMessage(AureliumSkills.getPrefix(Lang.getDefaultLanguage()) + ChatColor.WHITE + "New update available! You are on version " + ChatColor.AQUA + plugin.getDescription().getVersion() + ChatColor.WHITE + ", latest version is " + ChatColor.AQUA + version);
							player.sendMessage(AureliumSkills.getPrefix(Lang.getDefaultLanguage()) + ChatColor.WHITE + "Download it on Spigot: " + ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "https://spigotmc.org/resources/81069");
						}
					}
				});
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
		Player player = event.getPlayer();
		new BukkitRunnable() {
			@Override
			public void run() {
				plugin.getStorageProvider().save(player);
			}
		}.runTaskAsynchronously(plugin);
		plugin.getActionBar().resetActionBar(player);
	}

	private void loadPlayerDataAsync(@NotNull Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				plugin.getStorageProvider().load(player);
			}
		}.runTaskAsynchronously(plugin);
	}

}
