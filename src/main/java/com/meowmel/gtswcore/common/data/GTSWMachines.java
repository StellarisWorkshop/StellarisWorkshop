package com.meowmel.gtswcore.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.*;

import net.minecraft.network.chat.Component;

import com.meowmel.gtswcore.GTSWCore;
import com.meowmel.gtswcore.common.machine.electric.HarvesterMachine;
import com.meowmel.gtswcore.common.machine.electric.MobSimulatorMachine;
import com.meowmel.gtswcore.common.machine.electric.NetherCollectorMachine;
import com.meowmel.gtswcore.common.machine.electric.SieveMachine;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;

import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.GTValues.VLVH;
import static com.gregtechceu.gtceu.api.GTValues.VLVT;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.ELECTRIC_OVERCLOCK;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.*;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;
import static com.meowmel.gtswcore.api.registries.GTSWRegistires.REGISTRATE;

public class GTSWMachines {

    public static final Int2IntFunction largeTankSizeFunction = (tier) -> Math.min(4 * (1 << tier - 1), 256) * 1000;

    public static final int[] NETHER_COLLECTOR_TIERS = GTValues.tiersBetween(GTValues.EV,
            GTCEuAPI.isHighTier() ? GTValues.MAX : GTValues.UHV);
    public final static MachineDefinition[] NETHER_COLLECTOR = registerTieredMachines("nether_collector",
            NetherCollectorMachine::new,
            (tier, builder) -> builder
                    .langValue("%s Nether collector".formatted(GTValues.VNF[tier]))
                    .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTSWCore.id("nether_collector"),
                            GTSWRecipeTypes.NETHER_COLLECTOR_RECIPES))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .workableTieredHullModel(GTSWCore.id("block/machines/nether_collector"))
                    .recipeType(GTSWRecipeTypes.NETHER_COLLECTOR_RECIPES)
                    .tooltips(GTSWMultiMachines.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            GTSWRecipeTypes.NETHER_COLLECTOR_RECIPES, defaultTankSizeFunction.apply(tier), true))
                    .register(),
            NETHER_COLLECTOR_TIERS);

    public static final int[] HARVESTER_TIERS = GTValues.tiersBetween(GTValues.LV, GTValues.IV);
    public final static MachineDefinition[] HARVESTER = registerTieredMachines("harvester", HarvesterMachine::new,
            (tier, builder) -> builder
                    .langValue("%s Harvester".formatted(GTValues.VNF[tier]))
                    .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTSWCore.id("harvester"),
                            GTSWRecipeTypes.HARVESTER_RECIPES))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .workableTieredHullModel(GTSWCore.id("block/machines/harvester"))
                    .recipeType(GTSWRecipeTypes.HARVESTER_RECIPES)
                    .tooltips(Component.translatable("gtsw.machine.harvester.tooltip"))
                    .register(),
            HARVESTER_TIERS);

    public final static MachineDefinition[] MOB_SIMULATOR = registerTieredMachines("mob_simulator",
            MobSimulatorMachine::new,
            (tier, builder) -> builder
                    .langValue("%s Harvester".formatted(GTValues.VNF[tier]))
                    .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTSWCore.id("mob_simulator"),
                            GTSWRecipeTypes.MOB_SIMULATOR_RECIPES))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .workableTieredHullModel(GTSWCore.id("block/machines/mob_simulator"))
                    .recipeModifiers(ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
                    .recipeType(GTSWRecipeTypes.MOB_SIMULATOR_RECIPES)
                    .register(),
            ALL_TIERS);

    // 蒸汽筛子

    public static final Pair<MachineDefinition, MachineDefinition> STEAM_SIEVE = registerSimpleSteamMachines(
            "sieve", GTSWRecipeTypes.SIEVE_RECIPES);

    // electricSieve
    public static final MachineDefinition[] SIEVE = registerTieredMachines("sieve",
            SieveMachine::new,
            (tier, builder) -> builder
                    .langValue("%s Sieve".formatted(GTValues.VNF[tier]))
                    .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTSWCore.id("sieve"),
                            GTSWRecipeTypes.SIEVE_RECIPES))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .workableTieredHullModel(GTSWCore.id("block/machines/sieve"))
                    .recipeModifiers(ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
                    .recipeType(GTSWRecipeTypes.SIEVE_RECIPES)
                    .register(),
            ALL_TIERS);

    //

    // generator
    public static final MachineDefinition[] COMBUSTION = registerSimpleGenerator("combustion",
            GTRecipeTypes.COMBUSTION_GENERATOR_FUELS, largeTankSizeFunction, 0.1f, GTValues.EV, GTValues.IV);
    public static final MachineDefinition[] GAS_TURBINE = registerSimpleGenerator("gas_turbine",
            GTRecipeTypes.GAS_TURBINE_FUELS, largeTankSizeFunction, 0.1f, GTValues.EV, GTValues.IV);
    public static final MachineDefinition[] PLASMA_TURBINE = registerSimpleGenerator("plasma_turbine",
            GTRecipeTypes.PLASMA_GENERATOR_FUELS, largeTankSizeFunction, 0.1f, GTValues.LuV, GTValues.ZPM, GTValues.UV);

    public static MachineDefinition[] registerSimpleGenerator(String name,
                                                              GTRecipeType recipeType,
                                                              Int2IntFunction tankScalingFunction,
                                                              float hazardStrengthPerOperation,
                                                              int... tiers) {
        return registerTieredMachines(name,
                (holder, tier) -> new SimpleGeneratorMachine(holder, tier, hazardStrengthPerOperation * tier,
                        tankScalingFunction),
                (tier, builder) -> builder
                        .langValue("%s %s Generator %s".formatted(VLVH[tier], toEnglishName(name), VLVT[tier]))
                        .editableUI(SimpleGeneratorMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id(name), recipeType))
                        .rotationState(RotationState.ALL)
                        .recipeType(recipeType)
                        .recipeModifier(SimpleGeneratorMachine::recipeModifier, true)
                        .addOutputLimit(ItemRecipeCapability.CAP, 0)
                        .addOutputLimit(FluidRecipeCapability.CAP, 0)
                        .simpleGeneratorModel(GTCEu.id("block/generators/" + name))
                        .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, recipeType,
                                tankScalingFunction.apply(tier), false))
                        .register(),
                tiers);
    }

    public static MachineDefinition[] registerTieredMachines(String name,
                                                             BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                             BiFunction<Integer, MachineBuilder<MachineDefinition, ?>, MachineDefinition> builder,
                                                             int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = REGISTRATE
                    .machine(GTValues.VN[tier].toLowerCase() + "_" + name, holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static Pair<MachineDefinition, MachineDefinition> registerSimpleSteamMachines(String name,
                                                                                         GTRecipeType recipeType) {
        return registerSteamMachines(REGISTRATE, "steam_" + name, SimpleSteamMachine::new,
                (pressure, builder) -> builder
                        .rotationState(RotationState.ALL)
                        .recipeType(recipeType)
                        .recipeModifier(SimpleSteamMachine::recipeModifier)
                        .modelProperty(GTMachineModelProperties.VENT_DIRECTION, RelativeDirection.BACK)
                        .workableSteamHullModel(pressure, GTSWCore.id("block/machines/" + name))
                        .register());
    }

    public static void init() {}
}
