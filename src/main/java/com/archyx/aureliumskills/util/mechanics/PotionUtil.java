package com.archyx.aureliumskills.util.mechanics;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

public class PotionUtil {

    public static int getDuration(@NotNull PotionData potionData) {
        String type = potionData.getType().toString();
        if (potionData.isUpgraded()) {
            switch (type) {
                case "POISON":
                    return 420;
                case "REGEN":
                    return 440;
                case "SLOWNESS":
                case "TURTLE_MASTER":
                    return 400;
                default:
                    return 1800;
            }
        }
        else if (potionData.isExtended()) {
            switch (type) {
                case "POISON":
                case "REGEN":
                    return 1800;
                case "SLOWNESS":
                case "SLOW_FALLING":
                case "WEAKNESS":
                    return 4800;
                case "TURTLE_MASTER":
                    return 800;
                default:
                    return 9600;
            }
        }
        else {
            switch (type) {
                case "POISON":
                case "REGEN":
                    return 900;
                case "TURTLE_MASTER":
                    return 400;
                case "SLOWNESS":
                case "SLOW_FALLING":
                case "WEAKNESS":
                    return 1800;
                default:
                    return 3600;
            }
        }
    }

    public static @NotNull String formatDuration(int duration) {
        int seconds = duration / 20;
        int minutes = seconds / 60;
        return minutes + ":" + String.format("%02d", seconds % 60);
    }

    @SuppressWarnings("deprecation")
    public static void applyEffect(@NotNull Player player, @NotNull PotionEffect effect) {
        if (!effect.getType().isInstant()) {
            if (XMaterial.isNewVersion()) {
                player.addPotionEffect(effect);
            } else {
                PotionEffect currentEffect = player.getPotionEffect(effect.getType());
                // Force apply the effect if effect has greater amplifier or longer duration if same amplifier
                if (currentEffect != null) {
                    if (effect.getDuration() > currentEffect.getDuration() && effect.getAmplifier() == currentEffect.getAmplifier()) {
                        player.addPotionEffect(effect, true);
                    } else if (effect.getAmplifier() > currentEffect.getAmplifier()) {
                        player.addPotionEffect(effect, true);
                    }
                } else {
                    player.addPotionEffect(effect);
                }
            }
        }
    }

    public static boolean isNegativePotion(@NotNull PotionType potionType) {
        switch (potionType) {
            case POISON:
            case SLOWNESS:
            case INSTANT_DAMAGE:
            case WEAKNESS:
                return true;
            default:
                break;
        }
        return false;
    }

}
