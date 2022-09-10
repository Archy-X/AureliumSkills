package com.archyx.aureliumskills.util.block;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.NetherWartsState;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.Crops;
import org.bukkit.material.NetherWarts;
import org.jetbrains.annotations.NotNull;

public class BlockUtil {

    @SuppressWarnings("deprecation")
    public static boolean isFullyGrown(@NotNull Block block) {
        if (XMaterial.isNewVersion()) {
            if (block.getBlockData() instanceof Ageable) {
                Ageable crop = (Ageable) block.getBlockData();
                return crop.getMaximumAge() == crop.getAge();
            }
        }
        else if (block.getState().getData() instanceof Crops) {
            Crops crops = (Crops) block.getState().getData();
            return crops.getState().equals(CropState.RIPE);
        }
        else if (block.getState().getData() instanceof NetherWarts) {
            NetherWarts wart = (NetherWarts) block.getState().getData();
            return wart.getState().equals(NetherWartsState.RIPE);
        }
        else if (block.getState().getData() instanceof CocoaPlant) {
            CocoaPlant cocoaPlant = (CocoaPlant) block.getState().getData();
            return cocoaPlant.getSize().equals(CocoaPlant.CocoaPlantSize.LARGE);
        }
        return false;
    }

    public static int getGrowthStage(@NotNull Block block) {
        if (XMaterial.isNewVersion()) {
            if (block.getBlockData() instanceof Ageable) {
                Ageable crop = (Ageable) block.getBlockData();
                return crop.getAge();
            }
        }
        return 0;
    }

    public static boolean isCarrot(@NotNull Material material) {
        return material == Material.CARROT || material.name().equals("CARROTS");
    }

    public static boolean isPotato(@NotNull Material material) {
        return material == Material.POTATO || material.name().equals("POTATOES");
    }

    public static boolean isBeetroot(@NotNull Material material) {
        return material == Material.BEETROOT || material.name().equals("BEETROOTS") || material.name().equals("BEETROOT_BLOCK");
    }

    public static boolean isWheat(@NotNull Material material) {
        return material == Material.WHEAT || material.name().equals("CROPS");
    }

    public static boolean isNetherWart(@NotNull Material material) {
        return material.name().equals("NETHER_WART") || material.name().equals("NETHER_WARTS") || material.name().equals("NETHER_STALK");
    }

    public static boolean isReplenishable(@NotNull Material material) {
        return isWheat(material) || isCarrot(material) || isPotato(material) || isBeetroot(material) || isNetherWart(material);
    }
}
