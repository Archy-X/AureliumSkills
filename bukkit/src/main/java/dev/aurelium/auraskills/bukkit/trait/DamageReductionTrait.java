package dev.aurelium.auraskills.bukkit.trait;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.common.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class DamageReductionTrait extends TraitImpl {

    @Nullable
    private Expression formula;

    DamageReductionTrait(AuraSkills plugin) {
        super(plugin, Traits.DAMAGE_REDUCTION);
    }

    @Override
    public double getBaseLevel(Player player, Trait trait) {
        return 0;
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return NumberUtil.format1(getReductionValue(value) * 100) + "%";
    }

    public void onDamage(EntityDamageByEntityEvent event, User user) {
        double reduction = user.getEffectiveTraitLevel(Traits.DAMAGE_REDUCTION);
        event.setDamage(event.getDamage() * (1 - getReductionValue(reduction)));
    }

    public void resetFormula() {
        formula = null;
    }

    private double getReductionValue(double value) {
        Trait trait = Traits.DAMAGE_REDUCTION;
        if (formula == null) {
            formula = new Expression(trait.optionString("formula"));
        }
        formula.with("value", value);
        try {
            return formula.evaluate().getNumberValue().doubleValue();
        } catch (EvaluationException | ParseException e) {
            plugin.logger().warn("Failed to evaluate formula for trait auraskills/damage_reduction: " + e.getMessage());
        }
        // Default formula
        return -1.0 * Math.pow(1.01, -1.0 * value) + 1;
    }

}
