package com.meowmel.gtswcore.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderHelper;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.SteamHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.SteamParallelMultiblockMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.Shapes;

import com.meowmel.gtswcore.GTSWCore;
import com.meowmel.gtswcore.api.machine.multiblock.HullWorkableElectricMultiblockMachine;
import com.meowmel.gtswcore.client.renderer.machine.GTSWDynamicRenderHelper;
import com.meowmel.gtswcore.common.machine.multiblock.electric.*;
import com.meowmel.gtswcore.common.machine.multiblock.part.LargeSteamHatchPartMachine;
import com.meowmel.gtswcore.common.machine.multiblock.steam.IndustrialSteamParallelMultiblockMachine;
import com.meowmel.gtswcore.common.machine.multiblock.steam.SteamOreWaherMachine;
import com.meowmel.gtswcore.config.GTSWConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.*;
import static com.gregtechceu.gtceu.common.data.GCYMRecipeTypes.ALLOY_BLAST_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.CHEMICAL_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.DUMMY_RECIPES;
import static com.gregtechceu.gtceu.common.data.machines.GCYMMachines.PARALLEL_HATCH;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;
import static com.meowmel.gtswcore.api.machine.multiblock.HullWorkableElectricMultiblockMachine.MACHINE_CASING_ALL;
import static com.meowmel.gtswcore.api.recipe.OverclockingLogic.PERFECT_OVERCLOCK_SUBSECOND;
import static com.meowmel.gtswcore.api.registries.GTSWRegistires.REGISTRATE;
import static com.meowmel.gtswcore.common.data.GTSWRecipeTypes.SIEVE_RECIPES;
import static com.meowmel.gtswcore.common.machine.multiblock.generator.LargeAdvancedTurbineMachine.registerAdvancedLargeTurbine;
import static net.minecraft.world.level.block.Blocks.*;

public class GTSWMultiMachines {

    public static final MachineDefinition LARGE_STEAM_HATCH = REGISTRATE
            .machine("large_steam_input_hatch", LargeSteamHatchPartMachine::new)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.STEAM)
            .overlaySteamHullModel("large_steam_hatch")
            .tooltips(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                    SteamHatchPartMachine.INITIAL_TANK_CAPACITY * 64),
                    Component.translatable("gtceu.machine.steam.steam_hatch.tooltip"))
            .register();
    public static final MultiblockMachineDefinition STEAM_CENTRIFUGE = REGISTRATE
            .multiblock("steam_centrifuge", IndustrialSteamParallelMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .tooltips(Component.translatable("gtsw.machine.large_steam_machine.tooltip"))
            .appearanceBlock(CASING_INDUSTRIAL_STEAM)
            .recipeType(GTRecipeTypes.CENTRIFUGE_RECIPES)
            .recipeModifier(GTSWMultiMachines::industrialSteamMachineRecipeModifier, true)
            .addOutputLimit(ItemRecipeCapability.CAP, 4)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(" FFF ", " FFF ", " FFF ", " FFF ", "     ", " FFF ")
                    .aisle("FFFFF", "FFPFF", "FFPFF", "FFPFF", "  P  ", "FFFFF")
                    .aisle("FFFFF", "FPGPF", "FPGPF", "FPGPF", " PGP ", "FFFFF")
                    .aisle("FFFFF", "FFPFF", "FFPFF", "FFPFF", "  P  ", "FFFFF")
                    .aisle(" FFF ", " FSF ", " FFF ", " FFF ", "     ", " FFF ")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('P', Predicates.blocks(CASING_BRONZE_PIPE.get()))
                    .where('G', Predicates.blocks(CASING_BRONZE_GEARBOX.get()))
                    .where(' ', Predicates.any())
                    .where('F', blocks(CASING_INDUSTRIAL_STEAM.get()).setMinGlobalLimited(50)
                            .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.STEAM).setExactLimit(1))
                            .or(Predicates.abilities(IMPORT_FLUIDS, PartAbility.EXPORT_FLUIDS,
                                    IMPORT_ITEMS, EXPORT_ITEMS)))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/industrial_steam_casing"),
                    GTSWCore.id("block/multiblock/steam_centrifuge"))
            .register();

    public static final MultiblockMachineDefinition STEAM_ORE_WASHER = REGISTRATE
            .multiblock("steam_ore_washer", SteamOreWaherMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .tooltips(Component.translatable("gtsw.machine.large_steam_machine.tooltip"))
            .appearanceBlock(CASING_INDUSTRIAL_STEAM)
            .recipeType(GTRecipeTypes.ORE_WASHER_RECIPES)
            .recipeModifier(GTSWMultiMachines::industrialSteamMachineRecipeModifier, true)
            .addOutputLimit(ItemRecipeCapability.CAP, 4)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("FFFFF", "FFFFF", "FFFFF")
                    .aisle("FFFFF", "FPPPF", "F   F")
                    .aisle("FFFFF", "FWWWF", "F   F")
                    .aisle("FFFFF", "FPPPF", "F   F")
                    .aisle("FFFFF", "FFSFF", "FFFFF")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('P', Predicates.blocks(CASING_BRONZE_PIPE.get()))
                    .where(' ', Predicates.any())
                    .where('W', Predicates.blocks(WATER))
                    .where('F', blocks(CASING_INDUSTRIAL_STEAM.get()).setMinGlobalLimited(40)
                            .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.STEAM).setExactLimit(1))
                            .or(Predicates.abilities(IMPORT_FLUIDS, PartAbility.EXPORT_FLUIDS,
                                    IMPORT_ITEMS, EXPORT_ITEMS)))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/industrial_steam_casing"),
                    GTSWCore.id("block/multiblock/steam_ore_washer"))
            .register();

    public static final MultiblockMachineDefinition STEAM_MIXER = REGISTRATE
            .multiblock("steam_mixer", IndustrialSteamParallelMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .tooltips(Component.translatable("gtsw.machine.large_steam_machine.tooltip"))
            .appearanceBlock(CASING_INDUSTRIAL_STEAM)
            .recipeType(GTRecipeTypes.MIXER_RECIPES)
            .recipeModifier(GTSWMultiMachines::industrialSteamMachineRecipeModifier, true)
            .addOutputLimit(ItemRecipeCapability.CAP, 4)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(" FFF ", " FGF ", " FGF ", " FGF ", " FFF ", "  F  ")
                    .aisle("FFFFF", "F P F", "F   F", "F P F", "F   F", "  F  ")
                    .aisle("FFFFF", "GPBPG", "G B G", "GPBPG", "F B F", "FFBFF")
                    .aisle("FFFFF", "F P F", "F   F", "F P F", "F   F", "  F  ")
                    .aisle(" FFF ", " FSF ", " FGF ", " FGF ", " FFF ", "  F  ")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where(' ', Predicates.any())
                    .where('P', Predicates.blocks(CASING_BRONZE_PIPE.get()))
                    .where('B', Predicates.blocks(CASING_BRONZE_GEARBOX.get()))
                    .where('G', Predicates.blocks(GLASS))
                    .where('F', blocks(CASING_INDUSTRIAL_STEAM.get()).setMinGlobalLimited(50)
                            .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.STEAM).setExactLimit(1))
                            .or(Predicates.abilities(IMPORT_FLUIDS, PartAbility.EXPORT_FLUIDS,
                                    IMPORT_ITEMS, EXPORT_ITEMS)))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/industrial_steam_casing"),
                    GTSWCore.id("block/multiblock/steam_mixer"))
            .register();

    public static final MachineDefinition STEAM_ALLOY_SMELTER = REGISTRATE
            .multiblock("steam_foundry", IndustrialSteamParallelMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .tooltips(Component.translatable("gtsw.machine.large_steam_machine.tooltip"))
            .appearanceBlock(CASING_INDUSTRIAL_STEAM)
            .recipeType(GTRecipeTypes.ALLOY_SMELTER_RECIPES)
            .recipeModifier(GTSWMultiMachines::industrialSteamMachineRecipeModifier, true)
            .addOutputLimit(ItemRecipeCapability.CAP, 1)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("FFF", "XXX", "XXX")
                    .aisle("FFF", "X#X", "XXX")
                    .aisle("FFF", "XSX", "XXX")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('F', blocks(FIREBOX_BRONZE.get())
                            .or(Predicates.abilities(PartAbility.STEAM).setExactLimit(1)))
                    .where('#', Predicates.air())
                    .where(' ', Predicates.any())
                    .where('X', blocks(CASING_BRONZE_BRICKS.get()).setMinGlobalLimited(6)
                            .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1)))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/industrial_steam_casing"),
                    GTSWCore.id("block/multiblock/steam_foundry"))
            .register();

    public static final MultiblockMachineDefinition VOID_MINER = REGISTRATE
            .multiblock("void_miner", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .appearanceBlock(CASING_STEEL_SOLID)
            .recipeType(GTSWRecipeTypes.VOID_MINER_RECIPES)
            .recipeModifiers(DEFAULT_ENVIRONMENT_REQUIREMENT, GTRecipeModifiers.PARALLEL_HATCH, OC_PERFECT_SUBTICK,
                    BATCH_MODE)
            .pattern(definition -> {
                var casing = blocks(CASING_STEEL_SOLID.get()).setMinGlobalLimited(20);
                var abilities = Predicates.autoAbilities(definition.getRecipeTypes())
                        .or(Predicates.autoAbilities(true, true, true));
                return FactoryBlockPattern.start()
                        .aisle("F   F", "F   F", "F   F", "XXXXX", "     ", "     ", "     ", "     ", "     ", "     ",
                                "     ")
                        .aisle("     ", "     ", "     ", "XXXXX", " XXX ", "  F  ", "  F  ", "  F  ", "     ", "     ",
                                "     ")
                        .aisle("     ", "     ", "     ", "XXXXX", " XXX ", " FCF ", " FCF ", " FCF ", "  F  ", "  F  ",
                                "  F  ")
                        .aisle("     ", "     ", "     ", "XXXXX", " XXX ", "  F  ", "  F  ", "  F  ", "     ", "     ",
                                "     ")
                        .aisle("F   F", "F   F", "F   F", "XXSXX", "     ", "     ", "     ", "     ", "     ", "     ",
                                "     ")
                        .where('S', Predicates.controller(blocks(definition.getBlock())))
                        .where(' ', Predicates.any())
                        .where('C', blocks(CASING_STEEL_PIPE.get()))
                        .where('F',
                                blocks(GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.Steel)
                                        .get()))
                        .where('X', casing.or(abilities))
                        .build();
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/bedrock_ore_miner"))
            .register();

    public static final MultiblockMachineDefinition TREE_FARM = REGISTRATE.multiblock("tree_farm", TreeFarmMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTSWRecipeTypes.TREE_FARM_RECIPES)
            .appearanceBlock(CASING_STEEL_SOLID)
            .recipeModifiers(GTSWRecipeModifiers::fastPerfectOcSubsecondParallel)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "X#X", "XXX")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('X', blocks(CASING_STEEL_SOLID.get()).setMinGlobalLimited(10)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes())))
                    .where('#', Predicates.air())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .tooltips(Component.translatable("gtceu.machine.perfect_oc"))
            .register();

    public static final MultiblockMachineDefinition INDUSTRIAL_PYROLYSE_OVEN = REGISTRATE
            .multiblock("industrial_pyrolyse_oven", CoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.PYROLYSE_RECIPES)
            .recipeModifiers(GTSWRecipeModifiers::industrialPyrolyseOvenOverclock)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_TITANIUM_STABLE)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("CCC", "C#C", "CCC")
                    .aisle("CCC", "C#C", "CCC")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', Predicates.controller(blocks(definition.get())))
                    .where('X',
                            blocks(CASING_TITANIUM_STABLE.get()).setMinGlobalLimited(6)
                                    .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                                    .or(Predicates.autoAbilities(true, true, true)))
                    .where('C', Predicates.heatingCoils())
                    .where('#', Predicates.air())
                    .build())
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                var builder = MultiblockShapeInfo.builder()
                        .aisle("IXO", "XSX", "FMD")
                        .aisle("CCC", "C#C", "CCC")
                        .aisle("CCC", "C#C", "CCC")
                        .aisle("EEJ", "XHX", "XXX")
                        .where('S', definition, Direction.NORTH)
                        .where('X', CASING_TITANIUM_STABLE.getDefaultState())
                        .where('J', PARALLEL_HATCH[GTValues.IV], Direction.SOUTH)
                        .where('E', ENERGY_INPUT_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('I', ITEM_IMPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('O', ITEM_EXPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('F', FLUID_IMPORT_HATCH[GTValues.LV], Direction.NORTH)
                        .where('D', FLUID_EXPORT_HATCH[GTValues.LV], Direction.NORTH)
                        .where('H', MUFFLER_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('M', MAINTENANCE_HATCH, Direction.NORTH)
                        .where('#', Blocks.AIR.defaultBlockState());
                GTCEuAPI.HEATING_COILS.entrySet().stream()
                        .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
                        .forEach(
                                coil -> shapeInfo.add(builder.shallowCopy().where('C', coil.getValue().get()).build()));
                return shapeInfo;
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_stable_titanium"),
                    GTSWCore.id("block/multiblock/industrial_pyrolyse_oven"))
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtsw.machine.industrial_pyrolyse_oven.tooltip.1"),
                    Component.translatable("gtceu.machine.perfect_oc"))
            .additionalDisplay((controller, components) -> {
                if (controller instanceof CoilWorkableElectricMultiblockMachine coilMachine && controller.isFormed()) {
                    components.add(Component.translatable("gtceu.multiblock.pyrolyse_oven.speed",
                            coilMachine.getCoilTier() == 0 ? 75 : 50 * (coilMachine.getCoilTier() + 1)));
                }
            })
            .register();

    public final static MultiblockMachineDefinition INDUSTRIAL_BLAST_ALLOY_SMELTER = REGISTRATE
            .multiblock("industrial_alloy_blast_smelter", CoilWorkableElectricMultiblockMachine::new)
            .langValue("Industrial Alloy Blast Smelter")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.alloy_blast_smelter")))
            .tooltips(Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.0"),
                    Component.translatable("gtceu.machine.perfect_oc"))
            .rotationState(RotationState.ALL)
            .recipeType(ALLOY_BLAST_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTSWRecipeModifiers::industrialEbfOverclock, BATCH_MODE)
            .appearanceBlock(CASING_HIGH_TEMPERATURE_SMELTING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("#XXX#", "#CCC#", "#GGG#", "#CCC#", "#XXX#")
                    .aisle("XXXXX", "CAAAC", "GAAAG", "CAAAC", "XXXXX")
                    .aisle("XXXXX", "CAAAC", "GAAAG", "CAAAC", "XXMXX")
                    .aisle("XXXXX", "CAAAC", "GAAAG", "CAAAC", "XXXXX")
                    .aisle("#XSX#", "#CCC#", "#GGG#", "#CCC#", "#XXX#")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_HIGH_TEMPERATURE_SMELTING.get()).setMinGlobalLimited(30)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('C', heatingCoils())
                    .where('M', abilities(PartAbility.MUFFLER))
                    .where('G', blocks(HEAT_VENT.get()))
                    .where('A', air())
                    .where('#', any())
                    .build())
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                var builder = MultiblockShapeInfo.builder()
                        .aisle("#XSX#", "#CCC#", "#GGG#", "#CCC#", "#XMX#")
                        .aisle("IXXXX", "CAAAC", "GAAAG", "CAAAC", "XXXXX")
                        .aisle("XXXXD", "CAAAC", "GAAAG", "CAAAC", "XXHXX")
                        .aisle("FXXXX", "CAAAC", "GAAAG", "CAAAC", "XXXXX")
                        .aisle("#EXJ#", "#CCC#", "#GGG#", "#CCC#", "#XXX#")
                        .where('X', CASING_HIGH_TEMPERATURE_SMELTING.getDefaultState())
                        .where('S', definition, Direction.NORTH)
                        .where('G', HEAT_VENT.getDefaultState())
                        .where('A', Blocks.AIR.defaultBlockState())
                        .where('E', ENERGY_INPUT_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('J', PARALLEL_HATCH[GTValues.IV], Direction.SOUTH)
                        .where('I', ITEM_IMPORT_BUS[GTValues.LV], Direction.WEST)
                        .where('F', FLUID_IMPORT_HATCH[GTValues.LV], Direction.WEST)
                        .where('D', FLUID_EXPORT_HATCH[GTValues.LV], Direction.EAST)
                        .where('H', MUFFLER_HATCH[GTValues.LV], Direction.UP)
                        .where('M', MAINTENANCE_HATCH, Direction.NORTH);
                GTCEuAPI.HEATING_COILS.entrySet().stream()
                        .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
                        .forEach(
                                coil -> shapeInfo.add(builder.shallowCopy().where('C', coil.getValue().get()).build()));
                return shapeInfo;
            })
            .workableCasingModel(GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                    GTCEu.id("block/multiblock/gcym/blast_alloy_smelter"))
            .additionalDisplay((controller, components) -> {
                if (controller instanceof CoilWorkableElectricMultiblockMachine coilMachine && controller.isFormed()) {
                    components.add(Component.translatable("gtceu.multiblock.blast_furnace.max_temperature",
                            Component
                                    .translatable(
                                            FormattingUtil
                                                    .formatNumbers(coilMachine.getCoilType().getCoilTemperature() +
                                                            100L * Math.max(0, coilMachine.getTier() - GTValues.MV)) +
                                                    "K")
                                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))));
                }
            })
            .register();

    // 大型筛子
    public final static MultiblockMachineDefinition LARGE_SIEVE = REGISTRATE
            .multiblock("large_sieve", WorkableElectricMultiblockMachine::new)
            .langValue("Large Sieve")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.perfect_oc"))
            .rotationState(RotationState.ALL)
            .recipeType(SIEVE_RECIPES)
            .recipeModifiers(ELECTRIC_OVERCLOCK.apply(PERFECT_OVERCLOCK_SUBSECOND))
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_STAINLESS_CLEAN)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("#X#X#", "#X#X#", "#XXX#", "XXXXX", "#XXX#")
                    .aisle("XXXXX", "XAXAX", "XKKKX", "XKKKX", "X###X")
                    .aisle("#XXX#", "#XAX#", "XKKKX", "XKKKX", "X###X")
                    .aisle("XXXXX", "XAXAX", "XKKKX", "XKKKX", "X###X")
                    .aisle("#X#X#", "#X#X#", "#XSX#", "XXXXX", "#XXX#")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_STAINLESS_CLEAN.get()).setMinGlobalLimited(50)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('K', blocks(CASING_STAINLESS_STEEL_GEARBOX.get()))
                    .where('A', Predicates.air())
                    .where('#', Predicates.any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTSWCore.id("block/multiblock/large_sieve"))
            .register();

    // 工业化学反应
    public final static MultiblockMachineDefinition INDUSTRY_CHEMICAL_REACTOR = REGISTRATE
            .multiblock("industry_chemical_reactor", WorkableElectricMultiblockMachine::new)
            .langValue("Industry Chemical Reactor")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.perfect_oc"))
            .rotationState(RotationState.ALL)
            .recipeType(CHEMICAL_RECIPES)
            .recipeModifiers(DEFAULT_ENVIRONMENT_REQUIREMENT, GTRecipeModifiers.PARALLEL_HATCH, OC_PERFECT_SUBTICK,
                    BATCH_MODE)
            .appearanceBlock(CASING_PTFE_INERT)
            .pattern(definition -> {
                var casing = blocks(CASING_PTFE_INERT.get()).setMinGlobalLimited(40);
                var abilities = Predicates.autoAbilities(definition.getRecipeTypes())
                        .or(Predicates.autoAbilities(true, true, true));
                return FactoryBlockPattern.start()
                        .aisle("X###X", "XXXXX", "X###X", "XXXXX", "X###X")
                        .aisle("XXXXX", "XPPPX", "XCCCX", "XPPPX", "XXXXX")
                        .aisle("XXXXX", "XPPPX", "XCPCX", "XPPPX", "XXXXX")
                        .aisle("XXXXX", "XPPPX", "XCCCX", "XPPPX", "XXXXX")
                        .aisle("X###X", "SXXXX", "X###X", "XXXXX", "X###X")
                        .where('S', Predicates.controller(blocks(definition.getBlock())))
                        .where('X', casing.or(abilities))
                        .where('P', blocks(CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                        .where('C', Predicates.heatingCoils())
                        .where('#', Predicates.any())
                        .build();
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"),
                    GTSWCore.id("block/multiblock/industry_chemical_reactor"))
            .register();

    public static final MultiblockMachineDefinition MACRO_BLAST_FURNACE = REGISTRATE
            .multiblock("macro_blast_furnace", CoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.BLAST_RECIPES)
            .recipeModifiers(GTSWRecipeModifiers::macroBlastFurnaceParallel, GTSWRecipeModifiers::durationDiscount,
                    GTRecipeModifiers::ebfOverclock)
            .appearanceBlock(CASING_INVAR_HEATPROOF)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXXXXXXXXXXXX", "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG",
                            "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG",
                            "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG",
                            "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG",
                            "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG", "XXXXXXXXXXXXXXX")
                    .aisle("XXXXXXXXXXXXXXX", "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG",
                            "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG",
                            "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG",
                            "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG",
                            "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG", "XXXXXXXXXXXXXXX")
                    .aisle("XXXXXXXXXXXXXXX", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "XXXXXXXXXXXXXXX")
                    .aisle("XXXXXXXXXXXXXXX", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "XXXXXXXXXXXXXXX")
                    .aisle("XXXXXXXXXXXXXXX", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "XXXXXXXXXXXXXXX")
                    .aisle("XXXXXXXXXXXXXXX", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "XXXXXXXXXXXXXXX")
                    .aisle("XXXXXXXXXXXXXXX", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "XXXXXXXXXXXXXXX")
                    .aisle("XXXXXXXXXXXXXXX", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "XXXXXXXMXXXXXXX")
                    .aisle("XXXXXXXXXXXXXXX", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "XXXXXXXXXXXXXXX")
                    .aisle("XXXXXXXXXXXXXXX", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "XXXXXXXXXXXXXXX")
                    .aisle("XXXXXXXXXXXXXXX", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "XXXXXXXXXXXXXXX")
                    .aisle("XXXXXXXXXXXXXXX", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "XXXXXXXXXXXXXXX")
                    .aisle("XXXXXXXXXXXXXXX", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "GC           CG",
                            "GC           CG", "GC           CG", "GC           CG", "XXXXXXXXXXXXXXX")
                    .aisle("XXXXXXXXXXXXXXX", "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG",
                            "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG",
                            "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG",
                            "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG",
                            "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG", "GCCCCCCCCCCCCCG", "XXXXXXXXXXXXXXX")
                    .aisle("XXXXXXXXXXXXXXX", "GGGGGGGGGGGGGGG", "GGGGGGGSGGGGGGG", "GGGGGGGGGGGGGGG",
                            "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG",
                            "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG",
                            "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG",
                            "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG", "GGGGGGGGGGGGGGG", "XXXXXXXXXXXXXXX")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('X', blocks(CASING_INVAR_HEATPROOF.get()).setMinGlobalLimited(400)
                            .or(abilities(IMPORT_FLUIDS, EXPORT_FLUIDS))
                            .or(abilities(IMPORT_ITEMS, EXPORT_ITEMS))
                            .or(abilities(MAINTENANCE))
                            .or(abilities(PartAbility.INPUT_ENERGY, PartAbility.INPUT_LASER).setMinGlobalLimited(1)
                                    .setMaxGlobalLimited(32).setPreviewCount(1)))
                    .where('M', abilities(PartAbility.MUFFLER))
                    .where('G', blocks(CASING_TEMPERED_GLASS.get()))
                    .where('C', heatingCoils())
                    .where(' ', any())
                    .build())
            .recoveryItems(
                    () -> new ItemLike[] {
                            GTMaterialItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash).get() })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_heatproof"),
                    GTCEu.id("block/multiblock/electric_blast_furnace"))
            .tooltips(Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.0"),
                    Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.1"),
                    Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.2"),
                    Component.translatable("gtsw.tooltip.laser_hatch"),
                    Component.translatable("gtsw.machine.duration_discount"),
                    Component.translatable("gtsw.machine.macro_blast_furnace.tooltip.0"),
                    Component.translatable("gtsw.machine.macro_blast_furnace.tooltip.1"))
            .additionalDisplay((controller, components) -> {
                if (controller instanceof CoilWorkableElectricMultiblockMachine coilMachine && controller.isFormed()) {
                    components.add(Component.translatable("gtceu.multiblock.blast_furnace.max_temperature",
                            Component
                                    .translatable(
                                            FormattingUtil
                                                    .formatNumbers(coilMachine.getCoilType().getCoilTemperature() +
                                                            100L * Math.max(0, coilMachine.getTier() - GTValues.MV)) +
                                                    "K")
                                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))));
                }
            })
            .register();

    // 巨合金
    public static final MultiblockMachineDefinition MEGA_ALLOY_BLAST_SMELTER = REGISTRATE
            .multiblock("mega_alloy_blast_smelter", CoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeTypes(GCYMRecipeTypes.ALLOY_BLAST_RECIPES)
            .recipeModifiers(GTSWRecipeModifiers::macroBlastFurnaceParallel, GTSWRecipeModifiers::durationDiscount,
                    GTRecipeModifiers::ebfOverclock)
            .appearanceBlock(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("   eeeee   ", "   cbbbc   ", "   cbbbc   ", "   cbbbc   ", "   eeeee   ", "   bbbbb   ",
                            "           ", "           ", "           ", "           ", "           ", "           ",
                            "           ", "           ", "           ", "           ", "           ", "           ")
                    .aisle("  ebbbbbe  ", "  c     c  ", "  c     c  ", "  c     c  ", "  efffffe  ", "  bbbbbbb  ",
                            "   bbbbb   ", "   ccccc   ", "   ccccc   ", "   ccccc   ", "   ccccc   ", "   ccccc   ",
                            "   ccccc   ", "   ccccc   ", "   ccccc   ", "   ccccc   ", "   bbbbb   ", "           ")
                    .aisle(" ebbbbbbbe ", " cbeeeeebc ", " cbeeeeebc ", " cbeeeeebc ", " ebeeeeebe ", " bbbbbbbbb ",
                            "  baaaaab  ", "  caaaaac  ", "  caaaaac  ", "  caaaaac  ", "  caaaaac  ", "  caaaaac  ",
                            "  caaaaac  ", "  caaaaac  ", "  caaaaac  ", "  caaaaac  ", "  bbbbbbb  ", "   bbbbb   ")
                    .aisle("ebbbbbbbbbe", "c ehhhhhe c", "c eiiiiie c", "c ejjjjje c", "efeeeeeeefe", "bbbb   bbbb",
                            " baa   aab ", " caa   aac ", " caa   aac ", " caa   aac ", " caa   aac ", " caa   aac ",
                            " caa   aac ", " caa   aac ", " caa   aac ", " caa   aac ", " bbb   bbb ", "  bbbbbbb  ")
                    .aisle("ebbbbbbbbbe", "b ehhhhhe b", "b eiiiiie b", "b ejjjjje b", "efeeeeeeefe", "bbb     bbb",
                            " ba     ab ", " ca     ac ", " ca     ac ", " ca     ac ", " ca     ac ", " ca     ac ",
                            " ca     ac ", " ca     ac ", " ca     ac ", " ca     ac ", " bb     bb ", "  bbbbbbb  ")
                    .aisle("ebbbbbbbbbe", "b ehhkhhe b", "b eiikiie b", "b ejjkjje b", "efeeekeeefe", "bbb  k  bbb",
                            " ba  k  ab ", " ca  k  ac ", " ca  k  ac ", " ca  k  ac ", " ca  k  ac ", " ca  k  ac ",
                            " ca  k  ac ", " ca  k  ac ", " ca  k  ac ", " ca  k  ac ", " bb  k  bb ", "  bbbgbbb  ")
                    .aisle("ebbbbbbbbbe", "b ehhhhhe b", "b eiiiiie b", "b ejjjjje b", "efeeeeeeefe", "bbb     bbb",
                            " ba     ab ", " ca     ac ", " ca     ac ", " ca     ac ", " ca     ac ", " ca     ac ",
                            " ca     ac ", " ca     ac ", " ca     ac ", " ca     ac ", " bb     bb ", "  bbbbbbb  ")
                    .aisle("ebbbbbbbbbe", "c ehhhhhe c", "c eiiiiie c", "c ejjjjje c", "efeeeeeeefe", "bbbb   bbbb",
                            " baa   aab ", " caa   aac ", " caa   aac ", " caa   aac ", " caa   aac ", " caa   aac ",
                            " caa   aac ", " caa   aac ", " caa   aac ", " caa   aac ", " bbb   bbb ", "  bbbbbbb  ")
                    .aisle(" ebbbbbbbe ", " cbeeeeebc ", " cbeeeeebc ", " cbeeeeebc ", " ebeeeeebe ", " bbbbbbbbb ",
                            "  baaaaab  ", "  caaaaac  ", "  caaaaac  ", "  caaaaac  ", "  caaaaac  ", "  caaaaac  ",
                            "  caaaaac  ", "  caaaaac  ", "  caaaaac  ", "  caaaaac  ", "  bbbbbbb  ", "   bbbbb   ")
                    .aisle("  ebbbbbe  ", "  c     c  ", "  c     c  ", "  c     c  ", "  efffffe  ", "  bbbbbbb  ",
                            "   bbbbb   ", "   ccccc   ", "   ccccc   ", "   ccccc   ", "   ccccc   ", "   ccccc   ",
                            "   ccccc   ", "   ccccc   ", "   ccccc   ", "   ccccc   ", "   bbbbb   ", "           ")
                    .aisle("   eeeee   ", "   cbbbc   ", "   cb~bc   ", "   cbbbc   ", "   eeeee   ", "   bbbbb   ",
                            "           ", "           ", "           ", "           ", "           ", "           ",
                            "           ", "           ", "           ", "           ", "           ", "           ")
                    .where('~', Predicates.controller(blocks(definition.getBlock())))
                    .where('b', blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()).setMinGlobalLimited(280)
                            .or(Predicates.autoAbilities(true, true, true)))
                    .where('a', heatingCoils())
                    .where('g', abilities(MUFFLER))
                    .where('e', blocks(GCYMBlocks.HEAT_VENT.get()))
                    .where('c', blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                    .where('f', blocks(GTBlocks.CASING_EXTREME_ENGINE_INTAKE.get()))
                    .where('h', blocks(GTBlocks.FIREBOX_STEEL.get()))
                    .where('i', blocks(GTBlocks.FIREBOX_TITANIUM.get()))
                    .where('j', blocks(GTBlocks.FIREBOX_TUNGSTENSTEEL.get()))
                    .where('k', blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get()))
                    .where(' ', any())
                    .build())
            .recoveryItems(
                    () -> new ItemLike[] {
                            GTMaterialItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash).get() })
            .workableCasingModel(GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                    GTCEu.id("block/multiblock/gcym/blast_alloy_smelter"))
            .tooltips(Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.0"),
                    Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.1"),
                    Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.2"),
                    Component.translatable("gtsw.tooltip.laser_hatch"),
                    Component.translatable("gtsw.machine.duration_discount"),
                    Component.translatable("gtsw.machine.macro_blast_furnace.tooltip.0"),
                    Component.translatable("gtsw.machine.macro_blast_furnace.tooltip.1"))
            .additionalDisplay((controller, components) -> {
                if (controller instanceof CoilWorkableElectricMultiblockMachine coilMachine && controller.isFormed()) {
                    components.add(Component.translatable("gtceu.multiblock.blast_furnace.max_temperature",
                            Component
                                    .translatable(
                                            FormattingUtil
                                                    .formatNumbers(coilMachine.getCoilType().getCoilTemperature() +
                                                            100L * Math.max(0, coilMachine.getTier() - GTValues.MV)) +
                                                    "K")
                                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))));
                }
            })
            .register();

    public static final MultiblockMachineDefinition LARGE_FISHER = REGISTRATE
            .multiblock("large_fisher", HullWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTSWRecipeTypes.LARGE_FISHER_RECIPES)
            .appearanceBlock(CASING_STEEL_SOLID)
            .recipeModifiers(GTSWMultiMachines::leveledHullMachineRecipeModifier,
                    ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXXXXXXXX", "XXXXXXXXXXX", "XXXXXXXXXXX", "    XXX    ", "     X     ", "           ",
                            "           ", "           ")
                    .aisle("XCCCCCCCCCX", "XWWWXXXWWWX", "XWWWXXXWWWX", "    XFX    ", "    XFX    ", "    XFX    ",
                            "    XFX    ", "    XGX    ")
                    .aisle("XCCCCCCCCCX", "XWWWWWWWWWX", "XWWWWWWWWWX", "           ", "           ", "           ",
                            "           ", "     F     ")
                    .aisle("XCCCCCCCCCX", "XWWWWWWWWWX", "XWWWWWWWWWX", "           ", "           ", "           ",
                            "           ", "     F     ")
                    .aisle("XCCCCCCCCCX", "XWWWWWWWWWX", "XWWWWWWWWWX", "           ", "           ", "           ",
                            "           ", "     F     ")
                    .aisle("XCCCCCCCCCX", "XWWWWWWWWWX", "XWWWWWWWWWX", "     N     ", "     N     ", "     N     ",
                            "     N     ", "     G     ")
                    .aisle("XCCCCCCCCCX", "XWWWWWWWWWX", "XWWWWWWWWWX", "           ", "           ", "           ",
                            "           ", "           ")
                    .aisle("XCCCCCCCCCX", "XWWWWWWWWWX", "XWWWWWWWWWX", "           ", "           ", "           ",
                            "           ", "           ")
                    .aisle("XCCCCCCCCCX", "XWWWWWWWWWX", "XWWWWWWWWWX", "           ", "           ", "           ",
                            "           ", "           ")
                    .aisle("XCCCCCCCCCX", "XWWWWWWWWWX", "XWWWWWWWWWX", "           ", "           ", "           ",
                            "           ", "           ")
                    .aisle("XXXXXXXXXXX", "XXXXXSXXXXX", "XXXXXXXXXXX", "           ", "           ", "           ",
                            "           ", "           ")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('W', Predicates.blocks(WATER))
                    .where('C', leveledHulls(GTValues.LV))
                    .where('F',
                            blocks(GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.Steel).get()))
                    .where('G', Predicates.blocks(CASING_STEEL_GEARBOX.get()))
                    .where('N', Predicates.blocks(CHAIN))
                    .where('X', blocks(CASING_STEEL_SOLID.get()).setMinGlobalLimited(10)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, false)))
                    .where(' ', Predicates.any())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/large_miner"))
            .register();

    public static final MultiblockMachineDefinition LARGE_ADVANCED_STEAM_TURBINE = registerAdvancedLargeTurbine(
            "steam_advanced_large_turbine",
            HV,
            GTRecipeTypes.STEAM_TURBINE_FUELS,
            CASING_STEEL_TURBINE, CASING_STEEL_GEARBOX,
            GTCEu.id("block/casings/mechanic/machine_casing_turbine_steel"),
            GTCEu.id("block/multiblock/generator/large_steam_turbine"),
            false);

    public static final MultiblockMachineDefinition LARGE_ADVANCED_GAS_TURBINE = registerAdvancedLargeTurbine(
            "gas_advanced_large_turbine", EV,
            GTRecipeTypes.GAS_TURBINE_FUELS,
            CASING_STAINLESS_TURBINE, CASING_STAINLESS_STEEL_GEARBOX,
            GTCEu.id("block/casings/mechanic/machine_casing_turbine_stainless_steel"),
            GTCEu.id("block/multiblock/generator/large_gas_turbine"),
            true);

    public static final MultiblockMachineDefinition LARGE_ADVANCED_PLASMA_TURBINE = registerAdvancedLargeTurbine(
            "plasma_advanced_large_turbine",
            IV,
            GTRecipeTypes.PLASMA_GENERATOR_FUELS,
            CASING_TUNGSTENSTEEL_TURBINE, CASING_TUNGSTENSTEEL_GEARBOX,
            GTCEu.id("block/casings/mechanic/machine_casing_turbine_tungstensteel"),
            GTCEu.id("block/multiblock/generator/large_plasma_turbine"),
            false);

    public static final MultiblockMachineDefinition[] PROCESSING_ARRAY = registerTieredMultis("processing_array",
            ProcessingArrayMachine::new,
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Processing Array")
                    .rotationState(RotationState.ALL)
                    .blockProp(p -> p.noOcclusion().isViewBlocking((state, level, pos) -> false))
                    .shape(Shapes.box(0.001, 0.001, 0.001, 0.999, 0.999, 0.999))
                    .appearanceBlock(() -> ProcessingArrayMachine.getCasingState(tier))
                    .recipeType(DUMMY_RECIPES)
                    .recipeModifiers(ProcessingArrayMachine::recipeModifier)
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle("XXX", "CCC", "XXX")
                            .aisle("XXX", "C#C", "XXX")
                            .aisle("XSX", "CCC", "XXX")
                            .where('S', Predicates.controller(blocks(definition.getBlock())))
                            .where('X', blocks(ProcessingArrayMachine.getCasingState(tier))
                                    .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.INPUT_ENERGY, OUTPUT_ENERGY)
                                            .setMinGlobalLimited(1).setMaxGlobalLimited(1).setPreviewCount(1))
                                    .or(Predicates.autoAbilities(true, false, false)))
                            .where('C', blocks(CLEANROOM_GLASS.get()))
                            .where('#', Predicates.air())
                            .build())
                    .tooltips(Component.translatable("gtceu.universal.tooltip.parallel",
                            ProcessingArrayMachine.getMachineLimit(tier)))
                    .workableCasingModel(tier == IV ?
                            GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel") :
                            GTCEu.id("block/casings/solid/machine_casing_sturdy_hsse"),
                            GTSWCore.id("block/multiblock/processing_array"))
                    .register(),
            IV, LuV);

    public static final MultiblockMachineDefinition LARGE_COMBUSTION_SET = REGISTRATE
            .multiblock("large_combustion_set", LargeCombustionSetMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.COMBUSTION_GENERATOR_FUELS)
            .appearanceBlock(CASING_HSSE_STURDY)
            .recipeModifier(LargeCombustionSetMachine::recipeModifier, true)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXXXX", "F     F", "F     F", "F     F", "F     F", "F     F", "XXXXXXX")
                    .aisle("XXXXXXX", " XXXXX ", " XGGGX ", " XGGGX ", " XGGGX ", " XXXXX ", "XXXXXXX")
                    .aisle("XXXXXXX", " XBBBX ", " GHHHG ", " GHHHG ", " GHHHG ", " XBBBX ", "XXXXXXX")
                    .aisle("XXXXXXX", " XBBBX ", " GHHHG ", " GHHHG ", " GHHHG ", " XBBBX ", "XXXMXXX")
                    .aisle("XXXXXXX", " XBBBX ", " GHHHG ", " GHHHG ", " GHHHG ", " XBBBX ", "XXXXXXX")
                    .aisle("XXXXXXX", " XXXXX ", " XGGGX ", " XGGGX ", " XGGGX ", " XXXXX ", "XXXXXXX")
                    .aisle("XXXSXXX", "F     F", "F     F", "F     F", "F     F", "F     F", "XXXXXXX")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('F', blocks(GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.HSSE).get()))
                    .where('G', blocks(CASING_HSSE_STURDY.get())
                            .or(blocks(FUSION_GLASS.get())))
                    .where('B', blocks(CASING_TUNGSTENSTEEL_GEARBOX.get()))
                    .where('H', blocks(HIGH_POWER_CASING.get()))
                    .where('M', Predicates.abilities(MUFFLER))
                    .where('X', blocks(CASING_HSSE_STURDY.get()).setMinGlobalLimited(130)
                            .or(Predicates.abilities(IMPORT_FLUIDS).setPreviewCount(1))
                            .or(Predicates.abilities(OUTPUT_ENERGY).setPreviewCount(1))
                            .or(Predicates.abilities(MAINTENANCE).setPreviewCount(1))
                            .or(Predicates.abilities(OUTPUT_LASER).setPreviewCount(1)))
                    .build())
            .model(createWorkableCasingMachineModel(GTCEu.id("block/casings/solid/machine_casing_sturdy_hsse"),
                    GTCEu.id("block/multiblock/generator/extreme_combustion_engine"))
                    .andThen(b -> b.addDynamicRenderer(GTSWDynamicRenderHelper::createLargeCombustionRenderer)))
            .hasBER(true)
            .tooltips(Component.translatable("gtsw.tooltip.laser_hatch"))
            .register();

    public static final MultiblockMachineDefinition LARGE_GAS_COLLECTOR = REGISTRATE
            .multiblock("large_gas_collector", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTSWRecipeTypes.LARGE_GAS_COLLECTOR_RECIPES)
            .appearanceBlock(CASING_STRESS_PROOF)
            .recipeModifiers(ELECTRIC_OVERCLOCK.apply(PERFECT_OVERCLOCK_SUBSECOND))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("    XXX    ", "    XXX    ", "    XXX    ", "   XXXXX   ", "XXXXXXXXXXX", "XXXXXXXXXXX",
                            "XXXXXXXXXXX", "   XXXXX   ", "    XXX    ", "    XXX    ", "    XXX    ")
                    .aisle("    CIC    ", "    IPI    ", "    CIC    ", "           ", "CIC XXX CIC", "IPI XPX IPI",
                            "CIC XXX CIC", "           ", "    CIC    ", "    IPI    ", "    CIC    ")
                    .aisle("    CIC    ", "    IPI    ", "    CIC    ", "           ", "CIC XXX CIC", "IPI XPX IPI",
                            "CIC XXX CIC", "           ", "    CIC    ", "    IPI    ", "    CIC    ")
                    .aisle("    CIC    ", "    IPI    ", "    CIC    ", "           ", "CIC XXX CIC", "IPI XPX IPI",
                            "CIC XXX CIC", "           ", "    CIC    ", "    IPI    ", "    CIC    ")
                    .aisle("           ", "     P     ", "           ", "           ", "    XXX    ", " P  XPX  P ",
                            "    XXX    ", "           ", "           ", "     P     ", "           ")
                    .aisle("           ", "     P     ", "     P     ", "     P     ", "    XPX    ", " PPPPPPPPP ",
                            "    XPX    ", "     P     ", "     P     ", "     P     ", "           ")
                    .aisle("           ", "           ", "           ", "           ", "    XXX    ", "    XSX    ",
                            "    XXX    ", "           ", "           ", "           ", "           ")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('P', blocks(CASING_TUNGSTENSTEEL_PIPE.get()))
                    .where('I', blocks(CASING_EXTREME_ENGINE_INTAKE.get()))
                    .where('C', blocks(CASING_TUNGSTENSTEEL_ROBUST.get()))
                    .where('X', blocks(CASING_STRESS_PROOF.get()).setMinGlobalLimited(90)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.abilities(MAINTENANCE).setPreviewCount(1)))
                    .where('#', Predicates.air())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/stress_proof_casing"),
                    GTSWCore.id("block/multiblock/large_gas_collector"))
            .tooltips(Component.translatable("gtceu.machine.perfect_oc"))
            .register();

    //
    public static final MultiblockMachineDefinition INDUSTRIAL_CRACKER = REGISTRATE
            .multiblock("industrial_cracker", CoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.CRACKING_RECIPES)
            .recipeModifier(GTSWRecipeModifiers::industrialCrackerOverclock)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_TUNGSTENSTEEL_ROBUST)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("HCHCH", "HCHCH", "HCHCH")
                    .aisle("HCHCH", "HPPPH", "HCHCH")
                    .aisle("HCHCH", "HCOCH", "HCHCH")
                    .where('O', Predicates.controller(blocks(definition.get())))
                    .where('P', blocks(CASING_TUNGSTENSTEEL_PIPE.get()))
                    .where('H', blocks(CASING_TUNGSTENSTEEL_ROBUST.get()).setMinGlobalLimited(12)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, true, true)))
                    .where('#', Predicates.air())
                    .where('C', Predicates.heatingCoils())
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
                    GTCEu.id("block/multiblock/cracking_unit"))
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.cracker.tooltip.1"))
            .tooltips(Component.translatable("gtceu.machine.perfect_oc"))
            .additionalDisplay((controller, components) -> {
                if (controller instanceof CoilWorkableElectricMultiblockMachine coilMachine && controller.isFormed()) {
                    components.add(Component.translatable("gtceu.multiblock.cracking_unit.energy",
                            100 - 10 * coilMachine.getCoilTier()));
                }
            })
            .register();

    public static final MultiblockMachineDefinition[] ADVANCED_FUSION_REACTOR = registerTieredMultis(
            "advanced_fusion_reactor",
            AdvancedFusionReactorMachine::new, (tier, builder) -> builder
                    .rotationState(RotationState.ALL)
                    .recipeType(GTRecipeTypes.FUSION_RECIPES)
                    .recipeModifiers(GTRecipeModifiers.DEFAULT_ENVIRONMENT_REQUIREMENT,
                            AdvancedFusionReactorMachine::recipeModifier)
                    .tooltips(
                            Component.translatable("gtceu.machine.fusion_reactor.capacity",
                                    FusionReactorMachine.calculateEnergyStorageFactor(tier, 16) / 1000000L),
                            Component.translatable("gtsw.machine.advanced_fusion_reactor.0"),
                            Component.translatable("gtceu.multiblock.%s_fusion_reactor.description"
                                    .formatted(VN[tier].toLowerCase(Locale.ROOT))))
                    .appearanceBlock(() -> FusionReactorMachine.getCasingState(tier))
                    .pattern((definition) -> {
                        var casing = blocks(FusionReactorMachine.getCasingState(tier));
                        return FactoryBlockPattern.start()
                                .aisle("                                               ",
                                        "                                               ",
                                        "                    FCCCCCF                    ",
                                        "                    FCIBICF                    ",
                                        "                    FCCCCCF                    ",
                                        "                                               ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "                    FCBBBCF                    ",
                                        "                   CC#####CC                   ",
                                        "                   CC#####CC                   ",
                                        "                   CC#####CC                   ",
                                        "                    FCBBBCF                    ",
                                        "                                               ")
                                .aisle("                    FCBBBCF                    ",
                                        "                   CC#####CC                   ",
                                        "                CCCCC#####CCCCC                ",
                                        "                CCCHHHHHHHHHCCC                ",
                                        "                CCCCC#####CCCCC                ",
                                        "                   CC#####CC                   ",
                                        "                    FCBBBCF                    ")
                                .aisle("                    FCIBICF                    ",
                                        "                CCCCC#####CCCCC                ",
                                        "              CCCCCHHHHHHHHHCCCCC              ",
                                        "              CCHHHHHHHHHHHHHHHCC              ",
                                        "              CCCCCHHHHHHHHHCCCCC              ",
                                        "                CCCCC#####CCCCC                ",
                                        "                    FCIBICF                    ")
                                .aisle("                    FCBBBCF                    ",
                                        "              CCCCCCC#####CCCCCCC              ",
                                        "            CCCCHHHCC#####CCHHHCCCC            ",
                                        "            CCHHHHHHHHHHHHHHHHHHHCC            ",
                                        "            CCCCHHHCC#####CCHHHCCCC            ",
                                        "              CCCCCCC#####CCCCCCC              ",
                                        "                    FCBBBCF                    ")
                                .aisle("                                               ",
                                        "            CCCCCCC FCBBBCF CCCCCCC            ",
                                        "           CCCHHCCCCC#####CCCCCHHCCC           ",
                                        "           CHHHHHHHCC#####CCHHHHHHHC           ",
                                        "           CCCHHCCCCC#####CCCCCHHCCC           ",
                                        "            CCCCCCC FCBBBCF CCCCCCC            ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "           CCCCC               CCCCC           ",
                                        "          ECHHCCCCC FCCCCCF CCCCCHHCE          ",
                                        "          CHHHHHCCC FCIBICF CCCHHHHHC          ",
                                        "          ECHHCCCCC FCCCCCF CCCCCHHCE          ",
                                        "           CCCCC               CCCCC           ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "          CCCC                   CCCC          ",
                                        "         CCHCCCC               CCCCHCC         ",
                                        "         CHHHHCC               CCHHHHC         ",
                                        "         CCHCCCC               CCCCHCC         ",
                                        "          CCCC                   CCCC          ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "         CCC                       CCC         ",
                                        "        CCHCCC                   CCCHCC        ",
                                        "        CHHHCC                   CCHHHC        ",
                                        "        CCHCCC                   CCCHCC        ",
                                        "         CCC                       CCC         ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "        CCC                         CCC        ",
                                        "       CCHCE                       ECHCC       ",
                                        "       CHHHC                       CHHHC       ",
                                        "       CCHCE                       ECHCC       ",
                                        "        CCC                         CCC        ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "       CCC                           CCC       ",
                                        "      ECHCC                         CCHCE      ",
                                        "      CHHHC                         CHHHC      ",
                                        "      ECHCC                         CCHCE      ",
                                        "       CCC                           CCC       ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "      CCC                             CCC      ",
                                        "     CCHCE                           ECHCC     ",
                                        "     CHHHC                           CHHHC     ",
                                        "     CCHCE                           ECHCC     ",
                                        "      CCC                             CCC      ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "     CCC                               CCC     ",
                                        "    CCHCC                             CCHCC    ",
                                        "    CHHHC                             CHHHC    ",
                                        "    CCHCC                             CCHCC    ",
                                        "     CCC                               CCC     ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "     CCC                               CCC     ",
                                        "    CCHCC                             CCHCC    ",
                                        "    CHHHC                             CHHHC    ",
                                        "    CCHCC                             CCHCC    ",
                                        "     CCC                               CCC     ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "    CCC                                 CCC    ",
                                        "   CCHCC                               CCHCC   ",
                                        "   CHHHC                               CHHHC   ",
                                        "   CCHCC                               CCHCC   ",
                                        "    CCC                                 CCC    ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "    CCC                                 CCC    ",
                                        "   CCHCC                               CCHCC   ",
                                        "   CHHHC                               CHHHC   ",
                                        "   CCHCC                               CCHCC   ",
                                        "    CCC                                 CCC    ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "   CCC                                   CCC   ",
                                        "  CCHCC                                 CCHCC  ",
                                        "  CHHHC                                 CHHHC  ",
                                        "  CCHCC                                 CCHCC  ",
                                        "   CCC                                   CCC   ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "   CCC                                   CCC   ",
                                        "  CCHCC                                 CCHCC  ",
                                        "  CHHHC                                 CHHHC  ",
                                        "  CCHCC                                 CCHCC  ",
                                        "   CCC                                   CCC   ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "   CCC                                   CCC   ",
                                        "  CCHCC                                 CCHCC  ",
                                        "  CHHHC                                 CHHHC  ",
                                        "  CCHCC                                 CCHCC  ",
                                        "   CCC                                   CCC   ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "  CCC                                     CCC  ",
                                        " CCHCC                                   CCHCC ",
                                        " CHHHC                                   CHHHC ",
                                        " CCHCC                                   CCHCC ",
                                        "  CCC                                     CCC  ",
                                        "                                               ")
                                .aisle("  FFF                                     FFF  ",
                                        " FCCCF                                   FCCCF ",
                                        "FCCHCCF                                 FCCHCCF",
                                        "FCHHHCF                                 FCHHHCF",
                                        "FCCHCCF                                 FCCHCCF",
                                        " FCCCF                                   FCCCF ",
                                        "  FFF                                     FFF  ")
                                .aisle("  CCC                                     CCC  ",
                                        " C###C                                   C###C ",
                                        "C##H##C                                 C##H##C",
                                        "C#HHH#C                                 C#HHH#C",
                                        "C##H##C                                 C##H##C",
                                        " C###C                                   C###C ",
                                        "  CCC                                     CCC  ")
                                .aisle("  CIC                                     CIC  ",
                                        " B###B                                   B###B ",
                                        "C##H##C                                 C##H##C",
                                        "I#HHH#I                                 I#HHH#I",
                                        "C##H##C                                 C##H##C",
                                        " B###B                                   B###B ",
                                        "  CIC                                     CIC  ")
                                .aisle("  CBC                                     CBC  ",
                                        " B###B                                   B###B ",
                                        "C##H##C                                 C##H##C",
                                        "B#HHH#B                                 B#HHH#B",
                                        "C##H##C                                 C##H##C",
                                        " B###B                                   B###B ",
                                        "  CBC                                     CBC  ")
                                .aisle("  CIC                                     CIC  ",
                                        " B###B                                   B###B ",
                                        "C##H##C                                 C##H##C",
                                        "I#HHH#I                                 I#HHH#I",
                                        "C##H##C                                 C##H##C",
                                        " B###B                                   B###B ",
                                        "  CIC                                     CIC  ")
                                .aisle("  CCC                                     CCC  ",
                                        " C###C                                   C###C ",
                                        "C##H##C                                 C##H##C",
                                        "C#HHH#C                                 C#HHH#C",
                                        "C##H##C                                 C##H##C",
                                        " C###C                                   C###C ",
                                        "  CCC                                     CCC  ")
                                .aisle("  FFF                                     FFF  ",
                                        " FCCCF                                   FCCCF ",
                                        "FCCHCCF                                 FCCHCCF",
                                        "FCHHHCF                                 FCHHHCF",
                                        "FCCHCCF                                 FCCHCCF",
                                        " FCCCF                                   FCCCF ",
                                        "  FFF                                     FFF  ")
                                .aisle("                                               ",
                                        "  CCC                                     CCC  ",
                                        " CCHCC                                   CCHCC ",
                                        " CHHHC                                   CHHHC ",
                                        " CCHCC                                   CCHCC ",
                                        "  CCC                                     CCC  ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "   CCC                                   CCC   ",
                                        "  CCHCC                                 CCHCC  ",
                                        "  CHHHC                                 CHHHC  ",
                                        "  CCHCC                                 CCHCC  ",
                                        "   CCC                                   CCC   ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "   CCC                                   CCC   ",
                                        "  CCHCC                                 CCHCC  ",
                                        "  CHHHC                                 CHHHC  ",
                                        "  CCHCC                                 CCHCC  ",
                                        "   CCC                                   CCC   ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "   CCC                                   CCC   ",
                                        "  CCHCC                                 CCHCC  ",
                                        "  CHHHC                                 CHHHC  ",
                                        "  CCHCC                                 CCHCC  ",
                                        "   CCC                                   CCC   ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "    CCC                                 CCC    ",
                                        "   CCHCC                               CCHCC   ",
                                        "   CHHHC                               CHHHC   ",
                                        "   CCHCC                               CCHCC   ",
                                        "    CCC                                 CCC    ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "    CCC                                 CCC    ",
                                        "   CCHCC                               CCHCC   ",
                                        "   CHHHC                               CHHHC   ",
                                        "   CCHCC                               CCHCC   ",
                                        "    CCC                                 CCC    ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "     CCC                               CCC     ",
                                        "    CCHCC                             CCHCC    ",
                                        "    CHHHC                             CHHHC    ",
                                        "    CCHCC                             CCHCC    ",
                                        "     CCC                               CCC     ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "     CCC                               CCC     ",
                                        "    CCHCC                             CCHCC    ",
                                        "    CHHHC                             CHHHC    ",
                                        "    CCHCC                             CCHCC    ",
                                        "     CCC                               CCC     ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "      CCC                             CCC      ",
                                        "     CCHCE                           ECHCC     ",
                                        "     CHHHC                           CHHHC     ",
                                        "     CCHCE                           ECHCC     ",
                                        "      CCC                             CCC      ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "       CCC                           CCC       ",
                                        "      ECHCC                         CCHCE      ",
                                        "      CHHHC                         CHHHC      ",
                                        "      ECHCC                         CCHCE      ",
                                        "       CCC                           CCC       ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "        CCC                         CCC        ",
                                        "       CCHCE                       ECHCC       ",
                                        "       CHHHC                       CHHHC       ",
                                        "       CCHCE                       ECHCC       ",
                                        "        CCC                         CCC        ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "         CCC                       CCC         ",
                                        "        CCHCCC                   CCCHCC        ",
                                        "        CHHHCC                   CCHHHC        ",
                                        "        CCHCCC                   CCCHCC        ",
                                        "         CCC                       CCC         ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "          CCCC                   CCCC          ",
                                        "         CCHCCCC               CCCCHCC         ",
                                        "         CHHHHCC               CCHHHHC         ",
                                        "         CCHCCCC               CCCCHCC         ",
                                        "          CCCC                   CCCC          ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "           CCCCC               CCCCC           ",
                                        "          ECHHCCCCC FCCCCCF CCCCCHHCE          ",
                                        "          CHHHHHCCC FCIBICF CCCHHHHHC          ",
                                        "          ECHHCCCCC FCCCCCF CCCCCHHCE          ",
                                        "           CCCCC               CCCCC           ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "            CCCCCCC FCBBBCF CCCCCCC            ",
                                        "           CCCHHCCCCC#####CCCCCHHCCC           ",
                                        "           CHHHHHHHCC#####CCHHHHHHHC           ",
                                        "           CCCHHCCCCC#####CCCCCHHCCC           ",
                                        "            CCCCCCC FCBBBCF CCCCCCC            ",
                                        "                                               ")
                                .aisle("                    FCBBBCF                    ",
                                        "              CCCCCCC#####CCCCCCC              ",
                                        "            CCCCHHHCC#####CCHHHCCCC            ",
                                        "            CCHHHHHHHHHHHHHHHHHHHCC            ",
                                        "            CCCCHHHCC#####CCHHHCCCC            ",
                                        "              CCCCCCC#####CCCCCCC              ",
                                        "                    FCBBBCF                    ")
                                .aisle("                    FCIBICF                    ",
                                        "                CCCCC#####CCCCC                ",
                                        "              CCCCCHHHHHHHHHCCCCC              ",
                                        "              CCHHHHHHHHHHHHHHHCC              ",
                                        "              CCCCCHHHHHHHHHCCCCC              ",
                                        "                CCCCC#####CCCCC                ",
                                        "                    FCIBICF                    ")
                                .aisle("                    FCBBBCF                    ",
                                        "                   CC#####CC                   ",
                                        "                CCCCC#####CCCCC                ",
                                        "                CCCHHHHHHHHHCCC                ",
                                        "                CCCCC#####CCCCC                ",
                                        "                   CC#####CC                   ",
                                        "                    FCBBBCF                    ")
                                .aisle("                                               ",
                                        "                    FCBBBCF                    ",
                                        "                   CC#####CC                   ",
                                        "                   CC#####CC                   ",
                                        "                   CC#####CC                   ",
                                        "                    FCBBBCF                    ",
                                        "                                               ")
                                .aisle("                                               ",
                                        "                                               ",
                                        "                    FCCCCCF                    ",
                                        "                    FCISICF                    ",
                                        "                    FCCCCCF                    ",
                                        "                                               ",
                                        "                                               ")
                                .where('S', controller(blocks(definition.get())))
                                .where('B', blocks(FUSION_GLASS.get()).or(casing))
                                .where('C', casing)
                                .where('I', casing
                                        .or(abilities(PartAbility.EXPORT_FLUIDS))
                                        .or(abilities(PartAbility.IMPORT_FLUIDS)))
                                .where('F',
                                        Predicates.blocks(
                                                ChemicalHelper.getBlock(TagPrefix.frameGt, getCasingMaterial(tier))))
                                .where('H', blocks(FusionReactorMachine.getCoilState(tier)))
                                .where('E', casing.or(
                                        blocks(PartAbility.INPUT_ENERGY.getBlockRange(tier, UV).toArray(Block[]::new))
                                                .setMinGlobalLimited(1).setPreviewCount(32))
                                        .or(blocks(INPUT_LASER.getBlockRange(tier, UV).toArray(Block[]::new))
                                                .setMinGlobalLimited(1).setPreviewCount(8)))
                                .where('#', air())
                                .where(' ', any())
                                .build();
                    })
                    .model(createWorkableCasingMachineModel(FusionReactorMachine.getCasingType(tier).getTexture(),
                            GTCEu.id("block/multiblock/fusion_reactor"))
                            .andThen(b -> b.addDynamicRenderer(DynamicRenderHelper::createFusionRingRender)))
                    .hasBER(true)
                    .register(),
            LuV, ZPM, UV);
    public static final MultiblockMachineDefinition PERSONAL_BEACON = REGISTRATE
            .multiblock("personal_beacon", PersonalBeaconMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(CASING_STEEL_SOLID)
            .recipeType(DUMMY_RECIPES)
            .tooltips(Component.translatable("gtsw.machine.personal_beacon.tooltip.0"),
                    Component.translatable("gtsw.machine.personal_beacon.tooltip.1"))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXXXXXXX", "F       F", "F       F", "F       F", "F       F", "G       G", "         ")
                    .aisle("XXXXXXXXX", " XXXXXXX ", "         ", "         ", "         ", "         ", "         ")
                    .aisle("XXXXXXXXX", " XXXXXXX ", "  XXXXX  ", "         ", "         ", "         ", "         ")
                    .aisle("XXXXXXXXX", " XXXXXXX ", "  XXXXX  ", "   XXX   ", "         ", "         ", "         ")
                    .aisle("XXXXXXXXX", " XXXXXXX ", "  XXXXX  ", "   XXX   ", "    X    ", "         ", "         ")
                    .aisle("XXXXXXXXX", " XXXXXXX ", "  XXXXX  ", "   XXX   ", "         ", "         ", "         ")
                    .aisle("XXXXXXXXX", " XXXXXXX ", "  XXXXX  ", "         ", "         ", "         ", "         ")
                    .aisle("XXXXXXXXX", " XXXXXXX ", "         ", "         ", "         ", "         ", "         ")
                    .aisle("XXXXSXXXX", "F       F", "F       F", "F       F", "F       F", "G       G", "         ")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('F',
                            blocks(GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.Steel).get()))
                    .where('G', blocks(CASING_TEMPERED_GLASS.get()))
                    .where('X', blocks(CASING_STEEL_SOLID.get()).setMinGlobalLimited(10)
                            .or(abilities(IMPORT_FLUIDS).setMinGlobalLimited(1, 1))
                            .or(abilities(MAINTENANCE).setExactLimit(1))
                            .or(abilities(INPUT_ENERGY).setMinGlobalLimited(1, 1).setMaxGlobalLimited(2)))
                    .where('#', Predicates.air())
                    .build())
            .modelProperty(RecipeLogic.STATUS_PROPERTY, RecipeLogic.Status.IDLE)
            .model(createWorkableCasingMachineModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
                    .andThen(b -> b.addDynamicRenderer(GTSWDynamicRenderHelper::createPersonalBeaconRenderer)))
            .hasBER(true)
            .register();

    public static Material getCasingMaterial(int tier) {
        if (tier == LuV) return GTMaterials.BlackSteel;
        if (tier == ZPM) return GTMaterials.Tritanium;
        return GTMaterials.Neutronium;
    }

    public static MultiblockMachineDefinition[] registerTieredMultis(String name,
                                                                     BiFunction<IMachineBlockEntity, Integer, MultiblockControllerMachine> factory,
                                                                     BiFunction<Integer, MultiblockMachineBuilder<?, ?>, MultiblockMachineDefinition> builder,
                                                                     int... tiers) {
        MultiblockMachineDefinition[] definitions = new MultiblockMachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = REGISTRATE
                    .multiblock(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static Component[] workableTiered(int tier, long voltage, long energyCapacity, GTRecipeType recipeType,
                                             long tankCapacity, boolean input) {
        List<Component> tooltipComponents = new ArrayList<>();
        tooltipComponents
                .add(input ? Component.translatable("gtceu.universal.tooltip.voltage_in", voltage, GTValues.VNF[tier]) :
                        Component.translatable("gtceu.universal.tooltip.voltage_out", voltage, GTValues.VNF[tier]));
        tooltipComponents
                .add(Component.translatable("gtceu.universal.tooltip.energy_storage_capacity", energyCapacity));
        if (recipeType.getMaxInputs(FluidRecipeCapability.CAP) > 0 ||
                recipeType.getMaxOutputs(FluidRecipeCapability.CAP) > 0)
            tooltipComponents
                    .add(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", tankCapacity));
        return tooltipComponents.toArray(Component[]::new);
    }

    public static ModifierFunction industrialSteamMachineRecipeModifier(@NotNull MetaMachine machine,
                                                                        @NotNull GTRecipe recipe) {
        if (machine instanceof IndustrialSteamParallelMultiblockMachine) {
            if (RecipeHelper.getRecipeEUtTier(recipe) > GTValues.HV) {
                return ModifierFunction.NULL;
            } else {
                var realEUt = RecipeHelper.getRealEUt(recipe);
                long eut = realEUt.voltage();
                int parallelAmount = ParallelLogic.getParallelAmount(machine, recipe,
                        GTSWConfig.INSTANCE.server.industrialSteamMachineMaxParallels);
                double eutMultiplier = (double) eut * 0.8888 * (double) parallelAmount <= 32.0 ?
                        0.8888 * (double) parallelAmount : 32.0 / (double) eut;
                return ModifierFunction.builder().inputModifier(ContentModifier.multiplier(parallelAmount))
                        .outputModifier(ContentModifier.multiplier(parallelAmount)).durationMultiplier(1.5)
                        .eutMultiplier(eutMultiplier).parallels(parallelAmount).build();
            }
        } else {
            return RecipeModifier.nullWrongType(IndustrialSteamParallelMultiblockMachine.class, machine);
        }
    }

    public static ModifierFunction steamVoidMinerMachineRecipeModifier(@NotNull MetaMachine machine,
                                                                       @NotNull GTRecipe recipe) {
        if (machine instanceof SteamParallelMultiblockMachine) {
            if (RecipeHelper.getRecipeEUtTier(recipe) > GTValues.HV) {
                return ModifierFunction.NULL;
            } else {
                return ModifierFunction.IDENTITY;
            }
        } else {
            return RecipeModifier.nullWrongType(SteamParallelMultiblockMachine.class, machine);
        }
    }

    public static ModifierFunction leveledHullMachineRecipeModifier(@NotNull MetaMachine machine,
                                                                    @NotNull GTRecipe recipe) {
        if (machine instanceof HullWorkableElectricMultiblockMachine hullMachine) {
            int hullTier = hullMachine.getHullTier();
            double power = Math.pow(2, hullTier - GTValues.LV);
            int parallels = Math.max((int) Math.min(Integer.MAX_VALUE, power), 1);

            if (parallels == 1) return ModifierFunction.IDENTITY;
            return ModifierFunction.builder()
                    .modifyAllContents(ContentModifier.multiplier(parallels))
                    .eutMultiplier(parallels)
                    .parallels(parallels)
                    .build();
        } else {
            return RecipeModifier.nullWrongType(SteamParallelMultiblockMachine.class, machine);
        }
    }

    public static TraceabilityPredicate leveledHulls(int lowerTier) {
        final int maxTier = GTCEuAPI.isHighTier() ? GTValues.MAX : GTValues.UHV;
        if (lowerTier < GTValues.ULV || lowerTier > maxTier) {
            throw new IllegalArgumentException("Lower tier must be ULV or higher and not higher than max tier");
        }

        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();

            for (int tier = lowerTier; tier <= maxTier; tier++) {
                var entry = MACHINE_CASING_ALL[tier];
                if (blockState.is(entry.get())) {
                    Object currentCasing = blockWorldState.getMatchContext().getOrPut("HullEntry", entry);
                    if (!currentCasing.equals(entry)) {
                        blockWorldState.setError(new PatternStringError("gtsw.multiblock.pattern.error.hulls"));
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }, () -> {
            var info = new BlockInfo[MACHINE_CASING_ALL.length - lowerTier];
            for (int i = 0; i + lowerTier < MACHINE_CASING_ALL.length; i++) {
                var entry = MACHINE_CASING_ALL[i + lowerTier];
                info[i] = BlockInfo.fromBlockState(entry.get().defaultBlockState());
            }
            return info;
        });
    }
}
