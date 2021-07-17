package com.archyx.aureliumskills.loot.handler;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.ability.AbilityProvider;
import com.archyx.aureliumskills.api.event.LootDropCause;
import com.archyx.aureliumskills.api.event.PlayerLootDropEvent;
import com.archyx.aureliumskills.commands.CommandExecutor;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.CustomMessageKey;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.loot.Loot;
import com.archyx.aureliumskills.loot.LootPool;
import com.archyx.aureliumskills.loot.type.CommandLoot;
import com.archyx.aureliumskills.loot.type.ItemLoot;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.stats.Stats;
import com.archyx.aureliumskills.util.text.TextUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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

    protected void giveBlockItemLoot(Player player, ItemLoot loot, BlockBreakEvent event, @Nullable Source source, LootDropCause cause) {
        Block block = event.getBlock();
        ItemStack drop = loot.getItem().clone();
        drop.setAmount(generateAmount(loot.getMinAmount(), loot.getMaxAmount()));
        Location location = block.getLocation().add(0.5, 0.5, 0.5);
        // Call event
        PlayerLootDropEvent dropEvent = new PlayerLootDropEvent(player, drop, location, cause);
        if (cause != null) {
            Bukkit.getPluginManager().callEvent(dropEvent);
            if (dropEvent.isCancelled()) return;
        }
        block.getWorld().dropItem(dropEvent.getLocation(), dropEvent.getItemStack());
        attemptSendMessage(player, loot);
        giveXp(player, loot, source);
    }

    protected void giveFishingItemLoot(Player player, ItemLoot loot, PlayerFishEvent event, @Nullable Source source) {
        if (!(event.getCaught() instanceof Item)) return;
        Item itemEntity = (Item) event.getCaught();

        int amount = generateAmount(loot.getMinAmount(), loot.getMaxAmount());
        if (amount == 0) return;

        ItemStack drop = loot.getItem().clone();
        drop.setAmount(amount);
        itemEntity.setItemStack(drop);
        attemptSendMessage(player, loot);
        giveXp(player, loot, source);
    }

    protected Loot selectLoot(LootPool pool, @Nullable Source source) {
        List<Loot> lootList = new ArrayList<>();
        // Add applicable loot to list for selection
        for (Loot loot : pool.getLoot()) {
            if (loot.getSources().size() > 0 && source != null) {
                for (Source lootSource : loot.getSources()) {
                    if (lootSource.equals(source)) {
                        lootList.add(loot);
                        break;
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

    private void giveXp(Player player, Loot loot, @Nullable Source source) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null) return;
        if (loot.getXp() == -1.0 && source != null) {
            plugin.getLeveler().addXp(player, skill, getXp(player, source, ability));
        } else {
            plugin.getLeveler().addXp(player, skill, getXp(player, loot.getXp()));
        }
    }

    private double getXp(Player player, double input) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            double output = input;
            if (ability != null) {
                if (plugin.getAbilityManager().isEnabled(ability)) {
                    double modifier = 1;
                    modifier += plugin.getAbilityManager().getValue(ability, playerData.getAbilityLevel(ability)) / 100;
                    output *= modifier;
                }
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
            String finalMessage = Lang.getMessage(key, locale);
            // Use input as message if fail
            if (finalMessage == null) {
                finalMessage = message;
            }
            // Replace placeholders
            if (plugin.isPlaceholderAPIEnabled()) {
                finalMessage = PlaceholderAPI.setPlaceholders(player, finalMessage);
            }
            player.sendMessage(finalMessage);
        }
    }

    protected double getCommonChance(LootPool pool, PlayerData playerData) {
        return pool.getBaseChance() + pool.getChancePerLuck() * playerData.getStatLevel(Stats.LUCK);
    }

}
