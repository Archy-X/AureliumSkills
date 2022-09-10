package com.archyx.aureliumskills.loot.handler;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.ability.AbilityProvider;
import com.archyx.aureliumskills.api.event.LootDropCause;
import com.archyx.aureliumskills.api.event.PlayerLootDropEvent;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.CustomMessageKey;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.stats.Stats;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.lootmanager.loot.Loot;
import com.archyx.lootmanager.loot.LootPool;
import com.archyx.lootmanager.loot.context.LootContext;
import com.archyx.lootmanager.loot.type.CommandLoot;
import com.archyx.lootmanager.loot.type.ItemLoot;
import com.archyx.lootmanager.util.CommandExecutor;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public abstract class LootHandler extends AbilityProvider {

    private final Random random = new Random();
    private final Ability ability;

    public LootHandler(AureliumSkills plugin, Skill skill, Ability ability) {
        super(plugin, skill);
        this.ability = ability;
    }

    protected void giveCommandLoot(Player player, CommandLoot loot, Source source) {
        // Apply placeholders to command
        String finalCommand = TextUtil.replace(loot.getCommand(), "{player}", player.getName());
        if (plugin.isPlaceholderAPIEnabled()) {
            finalCommand = PlaceholderAPI.setPlaceholders(player, finalCommand);
        }
        // Execute command
        CommandExecutor executor = loot.getExecutor();
        if (executor == CommandExecutor.CONSOLE) {
            Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), finalCommand);
        } else if (executor == CommandExecutor.PLAYER) {
            Bukkit.dispatchCommand(player, finalCommand);
        }
        attemptSendMessage(player, loot);
        giveXp(player, loot, source);
    }

    protected void giveBlockItemLoot(Player player, ItemLoot loot, BlockBreakEvent event, Source source, LootDropCause cause) {
        Block block = event.getBlock();
        ItemStack drop = loot.getItem().clone();
        drop.setAmount(generateAmount(loot.getMinAmount(), loot.getMaxAmount()));
        Location location = block.getLocation().add(0.5, 0.5, 0.5);
        // Call event
        PlayerLootDropEvent dropEvent = new PlayerLootDropEvent(player, drop, location, cause);
        Bukkit.getPluginManager().callEvent(dropEvent);
        if (dropEvent.isCancelled()) return;
        if (dropEvent.getItemStack().getType() == Material.AIR) return;
        block.getWorld().dropItem(dropEvent.getLocation(), dropEvent.getItemStack());
        attemptSendMessage(player, loot);
        giveXp(player, loot, source);
    }

    protected void giveFishingItemLoot(Player player, ItemLoot loot, PlayerFishEvent event, Source source, LootDropCause cause) {
        if (!(event.getCaught() instanceof Item)) return;
        Entity itemEntity = event.getCaught();

        int amount = generateAmount(loot.getMinAmount(), loot.getMaxAmount());
        if (amount == 0) return;

        ItemStack drop = loot.getItem().clone();
        drop.setAmount(amount);
        
        if (itemEntity == null)
            return;
        
        Location location = itemEntity.getLocation();
        // Call event
        PlayerLootDropEvent dropEvent = new PlayerLootDropEvent(player, drop, location, cause);
        Bukkit.getPluginManager().callEvent(dropEvent);
        if (dropEvent.isCancelled()) return;
        ((Item)itemEntity).setItemStack(drop);
        attemptSendMessage(player, loot);
        giveXp(player, loot, source);
    }

    protected Loot selectLoot(LootPool pool, Source source) {
        List<Loot> lootList = new ArrayList<>();
        // Add applicable loot to list for selection
        for (Loot loot : pool.getLoot()) {
            Set<LootContext> sourcesContext = loot.getContexts().get("sources");
            if (sourcesContext != null && source != null) {
                for (LootContext context : sourcesContext) { // Go through LootContext and cast to Source
                    if (context instanceof Source) {
                        Source lootSource = (Source) context;
                        if (lootSource.equals(source)) { // Check if source matches one of the contexts
                            lootList.add(loot);
                            break;
                        }
                    }
                }
            } else {
                lootList.add(loot);
            }
        }
        // Loot selected based on weight
        int totalWeight = 0;
        for (Loot loot : lootList) {
            totalWeight += loot.getWeight();
        }
        if (totalWeight == 0) { // Don't attempt selection if no loot entries are applicable
            return null;
        }
        int selected = random.nextInt(totalWeight);
        int currentWeight = 0;
        Loot selectedLoot = null;
        for (Loot loot : lootList) {
            if (selected >= currentWeight && selected < currentWeight + loot.getWeight()) {
                selectedLoot = loot;
                break;
            }
            currentWeight += loot.getWeight();
        }
        return selectedLoot;
    }

    private void giveXp(Player player, Loot loot, Source source) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null) return;
        double xp = loot.getOption("xp", Double.class, -1.0);
        if (xp == -1.0 && source != null) {
            plugin.getLeveler().addXp(player, skill, getXp(player, source, ability));
        } else if (xp > 0) {
            plugin.getLeveler().addXp(player, skill, getXp(player, xp));
        }
    }

    private double getXp(Player player, double input) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            double output = input;
            if (plugin.getAbilityManager().isEnabled(ability)) {
                double modifier = 1;
                modifier += plugin.getAbilityManager().getValue(ability, playerData.getAbilityLevel(ability)) / 100;
                output *= modifier;
            }
            return output;
        }
        return 0.0;
    }

    private int generateAmount(int minAmount, int maxAmount) {
        return new Random().nextInt(maxAmount - minAmount + 1) + minAmount;
    }

    private void attemptSendMessage(Player player, Loot loot) {
        String message = loot.getMessage();
        if (message != null && !message.equals("")) {
            PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData == null) return;

            Locale locale = playerData.getLocale();
            // Try to get message as message key
            CustomMessageKey key = new CustomMessageKey(message);
            // Use input as message if fail
            String finalMessage = message;
            try {
                finalMessage = Lang.getMessage(key, locale);
            }
            catch (IllegalStateException ex) {
                // No custom message exists when using the message as a key
                plugin.getLogger().warning("Unknown custom message with path: " + key);
            }
            // Replace placeholders
            if (plugin.isPlaceholderAPIEnabled()) {
                finalMessage = PlaceholderAPI.setPlaceholders(player, finalMessage);
            }
            player.sendMessage(finalMessage);
        }
    }

    protected double getCommonChance(LootPool pool, PlayerData playerData) {
        double chancePerLuck = pool.getOption("chance_per_luck", Double.class, 0.0) / 100;
        return pool.getBaseChance() + chancePerLuck * playerData.getStatLevel(Stats.LUCK);
    }

}
