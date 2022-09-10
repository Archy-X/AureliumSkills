package com.archyx.aureliumskills.skills.archery;

import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.source.SourceManager;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public enum ArcherySource implements Source {

    PLAYER,
    BAT,
    CAT,
    CHICKEN,
    COD,
    COW,
    DONKEY,
    FOX,
    GIANT,
    HORSE,
    MUSHROOM_COW("mooshroom"),
    MULE,
    OCELOT,
    PARROT,
    PIG,
    RABBIT,
    SALMON,
    SHEEP,
    SKELETON_HORSE,
    SNOWMAN("snow_golem"),
    SQUID,
    STRIDER,
    TROPICAL_FISH,
    TURTLE,
    VILLAGER,
    WANDERING_TRADER,
    BEE,
    CAVE_SPIDER,
    DOLPHIN,
    ENDERMAN,
    IRON_GOLEM,
    LLAMA,
    PIGLIN,
    PANDA,
    POLAR_BEAR,
    PUFFERFISH,
    SPIDER,
    WOLF,
    ZOMBIFIED_PIGLIN,
    BLAZE,
    CREEPER,
    DROWNED,
    ELDER_GUARDIAN,
    ENDERMITE,
    EVOKER,
    GHAST,
    GUARDIAN,
    HOGLIN,
    HUSK,
    ILLUSIONER,
    MAGMA_CUBE,
    PHANTOM,
    PIGLIN_BRUTE,
    PILLAGER,
    RAVAGER,
    SHULKER,
    SILVERFISH,
    SKELETON,
    SLIME,
    STRAY,
    VEX,
    VINDICATOR,
    WITCH,
    WITHER_SKELETON,
    ZOGLIN,
    ZOMBIE,
    ZOMBIE_VILLAGER,
    ENDER_DRAGON,
    WITHER,
    AXOLOTL,
    GLOW_SQUID,
    GOAT,
    ALLAY,
    FROG,
    TADPOLE,
    WARDEN;

    private String configName;

    ArcherySource() {

    }

    ArcherySource(String configName) {
        this.configName = configName;
    }

    @Override
    public String toString() {
        String name = configName;
        return name != null ? name.toUpperCase(Locale.ROOT) : name();
    }

    @Override
    public String getPath() {
        String name = configName;
        if (name == null) {
            return getSkill().toString().toLowerCase(Locale.ROOT) + "." + toString().toLowerCase(Locale.ROOT);
        } else {
            return getSkill().toString().toLowerCase(Locale.ROOT) + "." + name.toLowerCase(Locale.ROOT);
        }
    }

    @Override
    public Skill getSkill() {
        return Skills.ARCHERY;
    }

    @Override
    public String getUnitName() {
        if (OptionL.getBoolean(Option.ARCHERY_DAMAGE_BASED)) {
            return "damage";
        }
        return null;
    }

    @Override
    public ItemStack getMenuItem() {
        return SourceManager.getMenuItem(this);
    }

}
