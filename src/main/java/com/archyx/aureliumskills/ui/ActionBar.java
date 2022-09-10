package com.archyx.aureliumskills.ui;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.ActionBarMessage;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.support.ProtocolLibSupport;
import com.archyx.aureliumskills.util.math.BigNumber;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.aureliumskills.util.version.VersionUtils;
import com.cryptomorin.xseries.XMaterial;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Jukebox;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

public class ActionBar implements Listener {

	private final AureliumSkills plugin;

	public ActionBar(AureliumSkills plugin) {
		this.plugin = plugin;
	}

	private final HashSet<Player> isPaused = new HashSet<>();
	private final HashSet<Player> isGainingXp = new HashSet<>();
	private final HashMap<Player, Integer> timer = new HashMap<>();
	private final HashMap<Player, Integer> currentAction = new HashMap<>();
	private final HashSet<UUID> actionBarDisabled = new HashSet<>();

	public void startUpdateActionBar() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			if (OptionL.getBoolean(Option.ACTION_BAR_ENABLED) && OptionL.getBoolean(Option.ACTION_BAR_IDLE)) {
				for (Player player : Bukkit.getOnlinePlayers()) {
					//Check disabled worlds
					if (!actionBarDisabled.contains(player.getUniqueId())) {
						if (!plugin.getWorldManager().isDisabledWorld(player.getWorld())) {
							if (!currentAction.containsKey(player)) {
								currentAction.put(player, 0);
							}
							if (!isGainingXp.contains(player) && !isPaused.contains(player)) {
								PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
								if (playerData != null) {
									Locale locale = playerData.getLocale();
									sendActionBar(player, TextUtil.replace(Lang.getMessage(ActionBarMessage.IDLE, locale)
											, "{hp}", getHp(player)
											, "{max_hp}", getMaxHp(player)
											, "{mana}", getMana(playerData)
											, "{max_mana}", getMaxMana(playerData)));
								}
							}
						}
					}
				}
			}
		}, 0L, OptionL.getInt(Option.ACTION_BAR_UPDATE_PERIOD));
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			if (OptionL.getBoolean(Option.ACTION_BAR_ENABLED)) {
				for (Player player : Bukkit.getOnlinePlayers()) {
					Integer time = timer.get(player);
					if (time != null) {
						if (time != 0) {
							timer.put(player, time - 1);
						}
					} else {
						timer.put(player, 0);
					}
				}
			}
		}, 0L, 2L);
	}

	public void sendXpActionBar(Player player, Skill skill, double xpAmount) {
		if (OptionL.getBoolean(Option.ACTION_BAR_ENABLED)) { // If action bar enabled
			if (!actionBarDisabled.contains(player.getUniqueId())) { // If the player's action bar is enabled
				// Get player skill data
				PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
				if (playerData != null) {
					Locale locale = playerData.getLocale();
					// Check enabled/disabled for max
					boolean notMaxed = plugin.getLeveler().getXpRequirements().getListSize(skill) > playerData.getSkillLevel(skill) - 1 && playerData.getSkillLevel(skill) < OptionL.getMaxLevel(skill);
					if (notMaxed && !OptionL.getBoolean(Option.ACTION_BAR_XP)) {
						return;
					}
					if (!notMaxed && !OptionL.getBoolean(Option.ACTION_BAR_MAXED)) {
						return;
					}
					if (isPaused.contains(player)) return;
					// Set timer and is gaining xp
					isGainingXp.add(player);
					timer.put(player, 20);
					// Put current action if not present
					if (!currentAction.containsKey(player)) {
						currentAction.put(player, 0);
					}
					//Add to current action
					Integer action = currentAction.get(player);
					if (action == null) {
						plugin.getLogger().warning("Current action is not cached for player: " + player.getName());
						return;
					}
					int thisAction = action + 1;
					currentAction.put(player, thisAction);
					new BukkitRunnable() {
						@Override
						public void run() {
							// If is gaining xp
							if (isGainingXp.contains(player)) {
								// If latest action
								Integer actionBarCurrentAction = currentAction.get(player);
								if (actionBarCurrentAction != null) {
									if (thisAction == actionBarCurrentAction) {
										boolean notMaxed = plugin.getLeveler().getXpRequirements().getListSize(skill) > playerData.getSkillLevel(skill) - 1 && playerData.getSkillLevel(skill) < OptionL.getMaxLevel(skill);
										// Not maxed
										if (notMaxed) {
											if (OptionL.getBoolean(Option.ACTION_BAR_XP)) {
												// Xp gained
												if (xpAmount >= 0) {
													if (!OptionL.getBoolean(Option.ACTION_BAR_ROUND_XP)) {
														sendActionBar(player, TextUtil.replace(TextUtil.replace(Lang.getMessage(ActionBarMessage.XP, locale)
																, "{hp}", getHp(player)
																, "{max_hp}", getMaxHp(player)
																, "{xp_gained}", NumberUtil.format1(xpAmount)
																, "{skill}", skill.getDisplayName(locale)
																, "{current_xp}", NumberUtil.format1(playerData.getSkillXp(skill)))
																, "{level_xp}", BigNumber.withSuffix(plugin.getLeveler().getXpRequirements().getXpRequired(skill, playerData.getSkillLevel(skill) + 1))
																, "{mana}", getMana(playerData)
																, "{max_mana}", getMaxMana(playerData)));
													}
													else {
														sendActionBar(player, TextUtil.replace(TextUtil.replace(Lang.getMessage(ActionBarMessage.XP, locale)
																, "{hp}", getHp(player)
																, "{max_hp}", getMaxHp(player)
																, "{xp_gained}", NumberUtil.format1(xpAmount)
																, "{skill}", skill.getDisplayName(locale)
																, "{current_xp}", String.valueOf((int) playerData.getSkillXp(skill)))
																, "{level_xp}", BigNumber.withSuffix(plugin.getLeveler().getXpRequirements().getXpRequired(skill, playerData.getSkillLevel(skill) + 1))
																, "{mana}", getMana(playerData)
																, "{max_mana}", getMaxMana(playerData)));
													}
												}
												// Xp removed
												else {
													if (!OptionL.getBoolean(Option.ACTION_BAR_ROUND_XP)) {
														sendActionBar(player, TextUtil.replace(TextUtil.replace(Lang.getMessage(ActionBarMessage.XP_REMOVED, locale)
																, "{hp}", getHp(player)
																, "{max_hp}", getMaxHp(player)
																, "{xp_removed}", NumberUtil.format1(xpAmount)
																, "{skill}", skill.getDisplayName(locale)
																, "{current_xp}", NumberUtil.format1(playerData.getSkillXp(skill)))
																, "{level_xp}", BigNumber.withSuffix(plugin.getLeveler().getXpRequirements().getXpRequired(skill, playerData.getSkillLevel(skill) + 1))
																, "{mana}", getMana(playerData)
																, "{max_mana}", getMaxMana(playerData)));
													}
													else {
														sendActionBar(player, TextUtil.replace(TextUtil.replace(Lang.getMessage(ActionBarMessage.XP, locale)
																, "{hp}", getHp(player)
																, "{max_hp}", getMaxHp(player)
																, "{xp_gained}", NumberUtil.format1(xpAmount)
																, "{skill}", skill.getDisplayName(locale)
																, "{current_xp}", String.valueOf((int) playerData.getSkillXp(skill)))
																, "{level_xp}", BigNumber.withSuffix(plugin.getLeveler().getXpRequirements().getXpRequired(skill, playerData.getSkillLevel(skill) + 1))
																, "{mana}", getMana(playerData)
																, "{max_mana}", getMaxMana(playerData)));
													}
												}
											}
										}
										// Maxed
										else {
											if (OptionL.getBoolean(Option.ACTION_BAR_MAXED)) {
												// Xp gained
												if (xpAmount >= 0) {
													sendActionBar(player, TextUtil.replace(Lang.getMessage(ActionBarMessage.MAXED, locale)
															, "{hp}", getHp(player)
															, "{max_hp}", getMaxHp(player)
															, "{xp_gained}", NumberUtil.format1(xpAmount)
															, "{skill}", skill.getDisplayName(locale)
															, "{mana}", getMana(playerData)
															, "{max_mana}", getMaxMana(playerData)));
												}
												// Xp removed
												else {
													sendActionBar(player, TextUtil.replace(Lang.getMessage(ActionBarMessage.MAXED_REMOVED, locale)
															, "{hp}", getHp(player)
															, "{max_hp}", getMaxHp(player)
															, "{xp_removed}", NumberUtil.format1(xpAmount)
															, "{skill}", skill.getDisplayName(locale)
															, "{mana}", getMana(playerData)
															, "{max_mana}", getMaxMana(playerData)));
												}
											}
										}
									}
									else {
										cancel();
									}
								}
								else {
									cancel();
								}
							}
							else {
								cancel();
							}
						}
					}.runTaskTimer(plugin, 0L, OptionL.getInt(Option.ACTION_BAR_UPDATE_PERIOD));
					// Schedule stop gaining xp
					new BukkitRunnable() {
						@Override
						public void run() {
							Integer timerNum = timer.get(player);
							if (timerNum != null) {
								if (timerNum.equals(0)){
									isGainingXp.remove(player);
								}
							}
						}
					}.runTaskLater(plugin, 41L);
				}
			}
		}
	}

	public void sendAbilityActionBar(Player player, String message) {
		if (!actionBarDisabled.contains(player.getUniqueId())) {
			PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
			if (playerData == null) return;
			sendActionBar(player, TextUtil.replace(Lang.getMessage(ActionBarMessage.ABILITY, playerData.getLocale()),
					"{hp}", getHp(player),
					"{max_hp}", getMaxHp(player),
					"{mana}", getMana(playerData),
					"{max_mana}", getMaxMana(playerData),
					"{message}", message));
			setPaused(player, 40);
		}
	}

	private String getHp(Player player) {
		return String.valueOf(Math.round(player.getHealth() * OptionL.getDouble(Option.HEALTH_HP_INDICATOR_SCALING)));
	}

	private String getMaxHp(Player player) {
		AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if (attribute != null) {
			return String.valueOf(Math.round(attribute.getValue() * OptionL.getDouble(Option.HEALTH_HP_INDICATOR_SCALING)));
		}
		return "";
	}

	private String getMana(PlayerData playerData) {
		return String.valueOf(Math.round(playerData.getMana()));
	}

	private String getMaxMana(PlayerData playerData) {
		return String.valueOf(Math.round(playerData.getMaxMana()));
	}

	private void sendActionBar(Player player, String message) {
		if (OptionL.getBoolean(Option.ACTION_BAR_PLACEHOLDER_API) && plugin.isPlaceholderAPIEnabled()) {
			message = PlaceholderAPI.setPlaceholders(player, message);
		}
		ProtocolLibSupport protocolLibSupport = plugin.getProtocolLibSupport();
		if (plugin.isProtocolLibEnabled() && protocolLibSupport != null) {
			if (VersionUtils.isAtLeastVersion(17)) {
				protocolLibSupport.sendNewActionBar(player, message);
			} else {
				protocolLibSupport.sendLegacyActionBar(player, message);
			}
		} else {
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
		}
	}

	public void resetActionBars() {
		isGainingXp.clear();
		timer.clear();
		currentAction.clear();
		isPaused.clear();
	}

	public void resetActionBar(Player player) {
		isGainingXp.remove(player);
		timer.remove(player);
		currentAction.remove(player);
		isPaused.remove(player);
	}

	public HashSet<UUID> getActionBarDisabled() {
		return actionBarDisabled;
	}

	public void setPaused(Player player, int ticks) {
		isPaused.add(player);
		Integer action = currentAction.get(player);
		if (action != null) {
			currentAction.put(player, action + 1);
		} else {
			currentAction.put(player, 0);
		}
		Integer thisAction = this.currentAction.get(player);
		if (thisAction == null)
			throw new IllegalStateException("Invalid player index key for: " + player.getName());
		new BukkitRunnable() {
			@Override
			public void run() {
				Integer actionBarCurrentAction = currentAction.get(player);
				if (actionBarCurrentAction != null) {
					if (thisAction == actionBarCurrentAction) {
						isPaused.remove(player);
					}
				}
			}
		}.runTaskLater(plugin, ticks);
	}

	@EventHandler
	@SuppressWarnings("deprecation")
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			if (block == null) return;
			// Pauses action bar if is used by vanilla bed
			if (block.getType().name().equals("BED_BLOCK") || block.getType().name().contains("_BED")) {
				Location center;
				Block other = block;
				if (XMaterial.isNewVersion()) {
					if (block.getBlockData() instanceof Bed) {
						Bed bed = (Bed) block.getBlockData();
						// Find the other bed block
						if (bed.getPart() == Bed.Part.FOOT) {
							other = block.getRelative(bed.getFacing());
						} else {
							other = block.getRelative(bed.getFacing().getOppositeFace());
						}

					}
				} else {
					if (block.getState().getData() instanceof org.bukkit.material.Bed) {
						org.bukkit.material.Bed bed = (org.bukkit.material.Bed) block.getState().getData();
						if (bed.isHeadOfBed()) {
							other = block.getRelative(bed.getFacing().getOppositeFace());
						} else {
							other = block.getRelative(bed.getFacing());
						}
					}
				}
				// Get the closest block
				Location mainLoc = block.getLocation().add(0.5, 0, 0.5);
				Location otherLoc = other.getLocation().add(0.5, 0, 0.5);
				if (mainLoc.distanceSquared(player.getLocation()) < otherLoc.distanceSquared(player.getLocation())) {
					center = mainLoc;
				} else {
					center = otherLoc;
				}
				if (player.getLocation().distanceSquared(center) >= 9) { // If player not is close enough to the bed
					setPaused(player, 40);
				} else {
					// If night time
					if (player.getWorld().getTime() >= 12541 && player.getWorld().getTime() <= 23458) {
						for (Entity entity : player.getWorld().getNearbyEntities(center, 8, 5, 8)) {
							EntityType type = entity.getType();
							// Check if mob is hostile
							if (type == EntityType.BLAZE || type == EntityType.CREEPER || type.name().equals("DROWNED") ||
								type == EntityType.ENDERMITE || type == EntityType.EVOKER || type == EntityType.GIANT ||
								type == EntityType.GUARDIAN || type == EntityType.ELDER_GUARDIAN || type == EntityType.ILLUSIONER ||
								type.name().equals("PIGLIN_BRUTE") || type.name().equals("PILLAGER") || type.name().equals("PHANTOM") ||
								type.name().equals("RAVAGER") || type == EntityType.SILVERFISH || type == EntityType.SKELETON ||
								type.name().equals("STRAY") || type == EntityType.WITHER_SKELETON || type == EntityType.SPIDER ||
								type == EntityType.CAVE_SPIDER || type == EntityType.VEX || type == EntityType.VINDICATOR ||
								type == EntityType.WITCH || type == EntityType.WITHER || type.name().equals("ZOGLIN") ||
								type == EntityType.ZOMBIE || type == EntityType.ZOMBIE_VILLAGER || type.name().equals("HUSK")) {
								setPaused(player, 40);
								break;
							} else if (type == EntityType.ENDERMAN && entity instanceof Enderman) {
								Enderman enderman = (Enderman) entity;
								if (enderman.getTarget() != null) {
									setPaused(player, 40);
									break;
								}
							} else if (type.name().equals("ZOMBIFIED_PIGLIN") || type.name().equals("PIG_ZOMBIE")) {
								if (entity instanceof PigZombie) {
									PigZombie pigZombie = (PigZombie) entity;
									if (pigZombie.isAngry()) {
										setPaused(player, 40);
										break;
									}
								}
							}
						}
					} else {
						setPaused(player, 40);
					}
				}
			}
			// Pause if playing jukebox
			else if (block.getType() == Material.JUKEBOX) {
				ItemStack item = event.getItem();
				if (item != null) {
					if (item.getType().name().contains("MUSIC_DISC") || item.getType().name().contains("RECORD")) {
						boolean isPlace = false;
						if (XMaterial.isNewVersion()) {
							if (block.getBlockData() instanceof Jukebox) {
								Jukebox jukebox = (Jukebox) block.getBlockData();
								if (!jukebox.hasRecord()) {
									isPlace = true;
								}
							}
						} else {
							if (block.getState() instanceof org.bukkit.block.Jukebox) {
								org.bukkit.block.Jukebox jukebox = (org.bukkit.block.Jukebox) block.getState();
								if (!jukebox.isPlaying()) {
									isPlace = true;
								}
							}
						}
						if (isPlace) {
							// Pause action bar of any player within 65 blocks
							for (Entity entity : block.getWorld().getNearbyEntities(block.getLocation(), 65, 65, 65)) {
								if (entity instanceof Player) {
									if (entity.getLocation().distanceSquared(block.getLocation()) <= 4225) {
										Player listener = (Player) entity;
										setPaused(listener, 40);
									}
								}
							}
						}
					}
				}
			}
			// Pause if height limit message
			else {
				ItemStack item = event.getItem();
				if (item != null) {
					if (item.getType().isBlock()) {
						if (block.getY() == block.getWorld().getMaxHeight() - 1) {
							if (event.getBlockFace() == BlockFace.UP)	{
								setPaused(player, 40);
							}
						}
					}
				}
			}
		}
	}

}
