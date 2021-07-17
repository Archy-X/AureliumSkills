package com.archyx.aureliumskills.util.item;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MaterialUtil {

    @Nullable
    public static Material parse(String name) {
        Material material = Material.getMaterial(name);
        if (material != null) {
            return material;
        }
        Optional<XMaterial> materialOptional = XMaterial.matchXMaterial(name);
        return materialOptional.map(XMaterial::parseMaterial).orElse(null);
    }


}
