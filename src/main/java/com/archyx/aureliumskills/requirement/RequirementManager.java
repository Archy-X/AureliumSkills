package com.archyx.aureliumskills.requirement;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.modifier.ModifierType;
import com.archyx.aureliumskills.skills.Skill;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RequirementManager implements Listener {

    private Set<GlobalRequirement> globalRequirements;
    private final @NotNull Map<UUID, Integer> errorMessageTimer;
    private final @NotNull AureliumSkills plugin;

    public RequirementManager(@NotNull AureliumSkills plugin) {
        errorMessageTimer = new HashMap<>();
        this.plugin = plugin;
        tickTimer();
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();
        this.globalRequirements = new HashSet<>();
        for (ModifierType type : ModifierType.values()) {
            List<String> list = config.getStringList("requirement." + type.name().toLowerCase(Locale.ENGLISH) + ".global");
            for (String text : list) {
                String[] splitText = text.split(" ");
                Optional<XMaterial> potentialMaterial = XMaterial.matchXMaterial(splitText[0].toUpperCase());
                if (potentialMaterial.isPresent()) {
                    XMaterial material = potentialMaterial.get();
                    Map<Skill, Integer> requirements = new HashMap<>();
                    for (int i = 1; i < splitText.length; i++) {
                        String requirementText = splitText[i];
                        try {
                            Skill skill = plugin.getSkillRegistry().getSkill(requirementText.split(":")[0]);
                            if (skill != null) {
                                int level = Integer.parseInt(requirementText.split(":")[1]);
                                requirements.put(skill, level);
                            }
                        } catch (Exception e) {
                            Bukkit.getLogger().warning("[AureliumSkills] Error parsing global skill " + type.name().toLowerCase(Locale.ENGLISH) + " requirement skill level pair with text " + requirementText);
                        }
                    }
                    GlobalRequirement globalRequirement = new GlobalRequirement(type, material, requirements);
                    globalRequirements.add(globalRequirement);
                } else {
                    Bukkit.getLogger().warning("[AureliumSkills] Error parsing global skill " + type.name().toLowerCase(Locale.ENGLISH) + " requirement material with text " + splitText[0]);
                }
            }
        }
    }

    public Set<GlobalRequirement> getGlobalRequirements() {
        return globalRequirements;
    }

    public @NotNull Set<GlobalRequirement> getGlobalRequirementsType(ModifierType type) {
        Set<GlobalRequirement> matched = new HashSet<>();
        for (GlobalRequirement requirement : globalRequirements) {
            if (requirement.getType() == type) {
                matched.add(requirement);
            }
        }
        return matched;
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        errorMessageTimer.remove(event.getPlayer().getUniqueId());
    }

    public void tickTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID id : errorMessageTimer.keySet()) {
                    Integer timer = errorMessageTimer.get(id);
                    if (timer == null)
                        throw new IllegalStateException("Invalid requirements tick timer index key: " + id);
                    if (timer != 0) {
                        errorMessageTimer.put(id, timer - 1);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 5);
    }

    public @NotNull Map<UUID, Integer> getErrorMessageTimer() {
        return errorMessageTimer;
    }

}
