package com.archyx.aureliumskills.menus.levelprogression;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.menus.common.AbstractSkillItem;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.slate.menu.ActiveMenu;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class StaticSkillItem extends AbstractSkillItem {

    public StaticSkillItem(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public Set<Skill> getDefinedContexts(Player player, ActiveMenu activeMenu) {
        Set<Skill> skills = new HashSet<>();
        skills.add(getSkill(activeMenu));
        return skills;
    }

    private Skill getSkill(ActiveMenu activeMenu) {
        Object property = activeMenu.getProperty("skill");
        if (!(property instanceof Skill)) {
            throw new IllegalArgumentException("Could not get menu skill property");
        }
        return (Skill) property;
    }

}
