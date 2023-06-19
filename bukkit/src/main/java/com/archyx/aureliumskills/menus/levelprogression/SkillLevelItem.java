package com.archyx.aureliumskills.menus.levelprogression;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.ability.AbilityManager;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.mana.MAbility;
import com.archyx.aureliumskills.mana.ManaAbilityManager;
import com.archyx.aureliumskills.menus.common.AbstractItem;
import com.archyx.aureliumskills.rewards.MoneyReward;
import com.archyx.aureliumskills.rewards.Reward;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.math.RomanNumber;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.slate.item.provider.TemplateItemProvider;
import com.archyx.slate.menu.ActiveMenu;
import com.google.common.collect.ImmutableList;
import fr.minuskube.inv.content.SlotPos;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class SkillLevelItem extends AbstractItem implements TemplateItemProvider<Integer> {

    private final List<Integer> track;
    
    public SkillLevelItem(AureliumSkills plugin) {
        super(plugin);
        this.track = new ArrayList<>();
        track.add(9); track.add(18); track.add(27); track.add(36); track.add(37);
        track.add(38); track.add(29); track.add(20); track.add(11); track.add(12);
        track.add(13); track.add(22); track.add(31); track.add(40); track.add(41);
        track.add(42); track.add(33); track.add(24); track.add(15); track.add(16);
        track.add(17); track.add(26); track.add(35); track.add(44);
    }

    @Override
    public Class<Integer> getContext() {
        return Integer.class;
    }

    @Override
    public SlotPos getSlotPos(Player player, ActiveMenu activeMenu, Integer context) {
        int index = context - 2;
        int pos = track.get(index);
        return SlotPos.of(pos / 9, pos % 9);
    }

    @Override
    public ItemStack onItemModify(ItemStack baseItem, Player player, ActiveMenu activeMenu, Integer num) {
        // Functionality for showing the level as the amount on the item
        int page = activeMenu.getCurrentPage();
        int level = num + page * (int) activeMenu.getProperty("items_per_page");
        // Don't show if level is more than max skill level
        Skill skill = (Skill) activeMenu.getProperty("skill");
        if (level > OptionL.getMaxLevel(skill)) {
            return null;
        }
        // Amount matching level functionality
        if (activeMenu.getOption(Boolean.class, "use_level_as_amount", true)) {
            if (level <= 64) {
                baseItem.setAmount(level);
            } else {
                int overMaxStackAmount = activeMenu.getOption(Integer.class, "overMaxStackAmount", 1);
                baseItem.setAmount(overMaxStackAmount);
            }
        }
        return baseItem;
    }

    protected String getRewardsLore(Skill skill, int level, Player player, Locale locale) {
        ImmutableList<Reward> rewards = plugin.getRewardManager().getRewardTable(skill).getRewards(level);
        StringBuilder message = new StringBuilder();
        double totalMoney = 0;
        for (Reward reward : rewards) {
            message.append(reward.getMenuMessage(player, locale, skill, level));
            if (reward instanceof MoneyReward) {
                totalMoney += ((MoneyReward) reward).getAmount();
            }
        }
        // Legacy money rewards
        if (plugin.isVaultEnabled()) {
            if (OptionL.getBoolean(Option.SKILL_MONEY_REWARDS_ENABLED)) {
                double base = OptionL.getDouble(Option.SKILL_MONEY_REWARDS_BASE);
                double multiplier = OptionL.getDouble(Option.SKILL_MONEY_REWARDS_MULTIPLIER);
                totalMoney += base + (multiplier * level * level);
            }
        }
        if (totalMoney > 0) {
            message.append(TextUtil.replace(Lang.getMessage(MenuMessage.MONEY_REWARD, locale), "{amount}", NumberUtil.format2(totalMoney)));
        }
        return TextUtil.replace(Lang.getMessage(MenuMessage.REWARDS, locale),"{rewards}", message.toString());
    }

    protected String getAbilityLore(Skill skill, int level, Locale locale) {
        StringBuilder abilityLore = new StringBuilder();
        AbilityManager manager = plugin.getAbilityManager();
        for (Ability ability : manager.getAbilities(skill, level)) {
            if (manager.isEnabled(ability)) {
                if (level == manager.getUnlock(ability)) {
                    abilityLore.append(TextUtil.replace(Lang.getMessage(MenuMessage.ABILITY_UNLOCK, locale)
                            , "{ability}", ability.getDisplayName(locale)
                            , "{desc}", TextUtil.replace(ability.getDescription(locale)
                                    , "{value_2}", NumberUtil.format1(manager.getValue2(ability, 1))
                                    , "{value}", NumberUtil.format1(manager.getValue(ability, 1)))));
                } else {
                    int abilityLevel = ((level - manager.getUnlock(ability)) / manager.getLevelUp(ability)) + 1;
                    if (abilityLevel <= manager.getMaxLevel(ability) || manager.getMaxLevel(ability) == 0) { // Check max level
                        abilityLore.append(TextUtil.replace(Lang.getMessage(MenuMessage.ABILITY_LEVEL, locale)
                                , "{ability}", ability.getDisplayName(locale)
                                , "{level}", RomanNumber.toRoman(abilityLevel)
                                , "{desc}", TextUtil.replace(ability.getDescription(locale)
                                        , "{value_2}", NumberUtil.format1(manager.getValue2(ability, abilityLevel))
                                        , "{value}", NumberUtil.format1(manager.getValue(ability, abilityLevel)))));
                    }
                }
            }
        }
        return abilityLore.toString();
    }

    protected String getManaAbilityLore(Skill skill, int level, Locale locale) {
        ManaAbilityManager manager = plugin.getManaAbilityManager();
        MAbility mAbility = manager.getManaAbility(skill, level);
        StringBuilder manaAbilityLore = new StringBuilder();
        if (mAbility != null) {
            if (plugin.getAbilityManager().isEnabled(mAbility)) {
                if (level == manager.getUnlock(mAbility)) {
                    manaAbilityLore.append(TextUtil.replace(Lang.getMessage(MenuMessage.MANA_ABILITY_UNLOCK, locale)
                            , "{mana_ability}", mAbility.getDisplayName(locale)
                            , "{desc}", TextUtil.replace(mAbility.getDescription(locale)
                                    , "{value}", NumberUtil.format1(manager.getDisplayValue(mAbility, 1))
                                    , "{duration}", NumberUtil.format1(getDuration(mAbility, 1))
                                    , "{haste_level}", String.valueOf(manager.getOptionAsInt(MAbility.SPEED_MINE, "haste_level", 10)))));
                }
                else {
                    int manaAbilityLevel = ((level - manager.getUnlock(mAbility)) / manager.getLevelUp(mAbility)) + 1;
                    if (manaAbilityLevel <= manager.getMaxLevel(mAbility) || manager.getMaxLevel(mAbility) == 0) {
                        manaAbilityLore.append(TextUtil.replace(Lang.getMessage(MenuMessage.MANA_ABILITY_LEVEL, locale)
                                , "{mana_ability}", mAbility.getDisplayName(locale)
                                , "{level}", RomanNumber.toRoman(manaAbilityLevel)
                                , "{desc}", TextUtil.replace(mAbility.getDescription(locale)
                                        , "{value}", NumberUtil.format1(manager.getDisplayValue(mAbility, manaAbilityLevel))
                                        , "{duration}", NumberUtil.format1(getDuration(mAbility, manaAbilityLevel))
                                        , "{haste_level}", String.valueOf(manager.getOptionAsInt(MAbility.SPEED_MINE, "haste_level", 10)))));
                    }
                }
            }
        }
        return manaAbilityLore.toString();
    }

    private double getDuration(MAbility mAbility, int level) {
        if (mAbility == MAbility.LIGHTNING_BLADE) {
            double baseDuration = plugin.getManaAbilityManager().getOptionAsDouble(MAbility.LIGHTNING_BLADE, "base_duration");
            double durationPerLevel = plugin.getManaAbilityManager().getOptionAsDouble(MAbility.LIGHTNING_BLADE, "duration_per_level");
            return baseDuration + (durationPerLevel * (level - 1));
        } else {
            return plugin.getManaAbilityManager().getValue(mAbility, level);
        }
    }

    protected int getItemsPerPage(ActiveMenu activeMenu) {
        Object property = activeMenu.getProperty("items_per_page");
        int itemsPerPage;
        if (property instanceof Integer) {
            itemsPerPage = (Integer) property;
        } else {
            itemsPerPage = 24;
        }
        return itemsPerPage;
    }

    protected int getLevel(ActiveMenu activeMenu, int position) {
        int itemsPerPage = getItemsPerPage(activeMenu);
        int currentPage = activeMenu.getCurrentPage();
        return currentPage * itemsPerPage + position;
    }

}
