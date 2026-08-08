package com.moakiee.ae2lt.registry;

import com.moakiee.ae2lt.AE2LightningTech;
import com.moakiee.ae2lt.block.AtmosphericIonizerBlock;
import com.moakiee.ae2lt.block.BuddingOverloadCrystalBlock;
import com.moakiee.ae2lt.block.CrystalCatalyzerBlock;
import com.moakiee.ae2lt.block.FirmamentConversionCoreBlock;
import com.moakiee.ae2lt.block.LightningAssemblyChamberBlock;
import com.moakiee.ae2lt.block.LightningCollectorBlock;
import com.moakiee.ae2lt.block.LightningSimulationChamberBlock;
import com.moakiee.ae2lt.block.MatrixCasingBlock;
import com.moakiee.ae2lt.block.MatrixControllerBlock;
import com.moakiee.ae2lt.block.MatrixGlassBlock;
import com.moakiee.ae2lt.block.MatrixMultiblockSimpleBlock;
import com.moakiee.ae2lt.block.MatrixPatternStorageBlock;
import com.moakiee.ae2lt.block.MatrixPortBlock;
import com.moakiee.ae2lt.logic.craft.MatrixMultiblockComponent;
import com.moakiee.ae2lt.block.OverloadProcessingFactoryBlock;
import com.moakiee.ae2lt.block.OverloadTntBlock;
import com.moakiee.ae2lt.block.OverloadCrystalClusterBlock;
import com.moakiee.ae2lt.block.OverloadDeviceWorkbenchBlock;
import com.moakiee.ae2lt.block.OverloadedControllerBlock;
import com.moakiee.ae2lt.block.OverloadedInterfaceBlock;
import com.moakiee.ae2lt.block.OverloadedPatternProviderBlock;
import com.moakiee.ae2lt.block.OverloadedPowerSupplyBlock;
import com.moakiee.ae2lt.block.TeslaCoilBlock;
import com.moakiee.ae2lt.block.AdvancedWirelessOverloadedControllerBlock;
import com.moakiee.ae2lt.block.WirelessOverloadedControllerBlock;
import com.moakiee.ae2lt.block.WirelessReceiverBlock;
import com.moakiee.ae2lt.blockentity.ExtendedOverloadedPatternProviderBlockEntity;
import com.moakiee.ae2lt.blockentity.OverloadedPatternProviderBlockEntity;
import java.util.function.Supplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    private static final String APPFLUX_MODID = "appflux";

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AE2LightningTech.MODID);

    private static final BlockBehaviour.Properties BUDDING_PROPERTIES = BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_CYAN)
            .strength(3.0F, 5.0F)
            .sound(SoundType.AMETHYST)
            .randomTicks()
            .requiresCorrectToolForDrops();

    private static final BlockBehaviour.Properties CLUSTER_PROPERTIES = BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_CYAN)
            .strength(1.5F)
            .sound(SoundType.AMETHYST_CLUSTER)
            .forceSolidOn()
            .requiresCorrectToolForDrops();

    private static final BlockBehaviour.Properties OVERLOAD_CRYSTAL_BLOCK_PROPERTIES = BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_CYAN)
            .strength(3.0F, 5.0F)
            .sound(SoundType.STONE)
            .forceSolidOn()
            .requiresCorrectToolForDrops();

    private static final BlockBehaviour.Properties SILICON_BLOCK_PROPERTIES = BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(5.0F, 6.0F)
            .sound(SoundType.METAL)
            .forceSolidOn()
            .requiresCorrectToolForDrops();

    private static final BlockBehaviour.Properties OVERLOAD_MACHINE_FRAME_PROPERTIES = BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(5.0F, 6.0F)
            .sound(SoundType.METAL)
            .forceSolidOn()
            .requiresCorrectToolForDrops();

    private static final BlockBehaviour.Properties FIRMAMENT_CONVERSION_CORE_PROPERTIES = BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_CYAN)
            .strength(-1.0F, 3600000.0F)
            .sound(SoundType.METAL)
            .forceSolidOn()
            .pushReaction(PushReaction.BLOCK)
            .noLootTable();

    private static final BlockBehaviour.Properties MATRIX_PROPERTIES = BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(6.0F, 6.0F)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops();

    private static final BlockBehaviour.Properties MATRIX_GLASS_PROPERTIES = BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(6.0F, 6.0F)
            .sound(SoundType.GLASS)
            .noOcclusion()
            .requiresCorrectToolForDrops();

    public static final RegistryObject<Block> OVERLOAD_CRYSTAL_BLOCK =
            registerBlock("overload_crystal_block", () -> new Block(OVERLOAD_CRYSTAL_BLOCK_PROPERTIES));

    public static final RegistryObject<Block> SILICON_BLOCK =
            registerBlock("silicon_block", () -> new Block(SILICON_BLOCK_PROPERTIES));

    public static final RegistryObject<Block> OVERLOAD_MACHINE_FRAME =
            registerBlock("overload_machine_frame", () -> new Block(OVERLOAD_MACHINE_FRAME_PROPERTIES));

    public static final RegistryObject<FirmamentConversionCoreBlock> FIRMAMENT_CONVERSION_CORE =
            registerBlock(
                    "firmament_conversion_core",
                    () -> new FirmamentConversionCoreBlock(FIRMAMENT_CONVERSION_CORE_PROPERTIES));

    public static final RegistryObject<OverloadTntBlock> OVERLOAD_TNT =
            registerBlock("overload_tnt", () -> new OverloadTntBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.TNT)));

    public static final RegistryObject<LightningCollectorBlock> LIGHTNING_COLLECTOR =
            registerBlock("lightning_collector", LightningCollectorBlock::new);

    public static final RegistryObject<LightningSimulationChamberBlock> LIGHTNING_SIMULATION_CHAMBER =
            registerBlock("lightning_simulation_room", LightningSimulationChamberBlock::new);

    public static final RegistryObject<LightningAssemblyChamberBlock> LIGHTNING_ASSEMBLY_CHAMBER =
            registerBlock("lightning_assembly_chamber", LightningAssemblyChamberBlock::new);

    public static final RegistryObject<OverloadProcessingFactoryBlock> OVERLOAD_PROCESSING_FACTORY =
            registerBlock("overload_processing_factory", OverloadProcessingFactoryBlock::new);

    public static final RegistryObject<TeslaCoilBlock> TESLA_COIL =
            registerBlock("tesla_coil", TeslaCoilBlock::new);

    public static final RegistryObject<AtmosphericIonizerBlock> ATMOSPHERIC_IONIZER =
            registerBlock("atmospheric_ionizer", AtmosphericIonizerBlock::new);

    public static final RegistryObject<CrystalCatalyzerBlock> CRYSTAL_CATALYZER =
            registerBlock("crystal_catalyzer", CrystalCatalyzerBlock::new);

    public static final RegistryObject<OverloadedControllerBlock> OVERLOADED_CONTROLLER =
            registerBlock("overloaded_controller", OverloadedControllerBlock::new);

    public static final RegistryObject<BuddingOverloadCrystalBlock> FLAWLESS_BUDDING_OVERLOAD_CRYSTAL =
            registerBlock("flawless_budding_overload_crystal", () ->
                    new BuddingOverloadCrystalBlock(BUDDING_PROPERTIES));

    public static final RegistryObject<BuddingOverloadCrystalBlock> FLAWED_BUDDING_OVERLOAD_CRYSTAL =
            registerBlock("flawed_budding_overload_crystal", () ->
                    new BuddingOverloadCrystalBlock(BUDDING_PROPERTIES));

    public static final RegistryObject<BuddingOverloadCrystalBlock> CRACKED_BUDDING_OVERLOAD_CRYSTAL =
            registerBlock("cracked_budding_overload_crystal", () ->
                    new BuddingOverloadCrystalBlock(BUDDING_PROPERTIES));

    public static final RegistryObject<BuddingOverloadCrystalBlock> DAMAGED_BUDDING_OVERLOAD_CRYSTAL =
            registerBlock("damaged_budding_overload_crystal", () ->
                    new BuddingOverloadCrystalBlock(BUDDING_PROPERTIES));

    public static final RegistryObject<OverloadCrystalClusterBlock> SMALL_OVERLOAD_CRYSTAL_BUD =
            registerBlock("small_overload_crystal_bud", () ->
                    new OverloadCrystalClusterBlock(3, 4, CLUSTER_PROPERTIES.sound(SoundType.SMALL_AMETHYST_BUD).lightLevel(s -> 1)));

    public static final RegistryObject<OverloadCrystalClusterBlock> MEDIUM_OVERLOAD_CRYSTAL_BUD =
            registerBlock("medium_overload_crystal_bud", () ->
                    new OverloadCrystalClusterBlock(4, 3, CLUSTER_PROPERTIES.sound(SoundType.MEDIUM_AMETHYST_BUD).lightLevel(s -> 2)));

    public static final RegistryObject<OverloadCrystalClusterBlock> LARGE_OVERLOAD_CRYSTAL_BUD =
            registerBlock("large_overload_crystal_bud", () ->
                    new OverloadCrystalClusterBlock(5, 3, CLUSTER_PROPERTIES.sound(SoundType.LARGE_AMETHYST_BUD).lightLevel(s -> 4)));

    public static final RegistryObject<OverloadCrystalClusterBlock> OVERLOAD_CRYSTAL_CLUSTER =
            registerBlock("overload_crystal_cluster", () ->
                    new OverloadCrystalClusterBlock(7, 3, CLUSTER_PROPERTIES.sound(SoundType.AMETHYST_CLUSTER).lightLevel(s -> 5)));

    public static final RegistryObject<OverloadedPatternProviderBlock<OverloadedPatternProviderBlockEntity>>
            OVERLOADED_PATTERN_PROVIDER =
            registerBlock("overloaded_pattern_provider", OverloadedPatternProviderBlock::new);

    public static final RegistryObject<OverloadedPatternProviderBlock<ExtendedOverloadedPatternProviderBlockEntity>>
            EXTENDED_OVERLOADED_PATTERN_PROVIDER =
            registerBlock("extended_overloaded_pattern_provider", OverloadedPatternProviderBlock::new);

    public static final RegistryObject<OverloadedInterfaceBlock> OVERLOADED_INTERFACE =
            registerBlock("overloaded_interface", OverloadedInterfaceBlock::new);

    public static final RegistryObject<OverloadedPowerSupplyBlock> OVERLOADED_POWER_SUPPLY =
            registerBlock(
                    "overloaded_power_supply",
                    OverloadedPowerSupplyBlock::new,
                    ModBlocks::isAppFluxLoaded);

    public static final RegistryObject<WirelessReceiverBlock> WIRELESS_RECEIVER =
            registerBlock("wireless_receiver", WirelessReceiverBlock::new);

    public static final RegistryObject<WirelessOverloadedControllerBlock> WIRELESS_OVERLOADED_CONTROLLER =
            registerBlock("wireless_overloaded_controller", WirelessOverloadedControllerBlock::new);

    public static final RegistryObject<AdvancedWirelessOverloadedControllerBlock> ADVANCED_WIRELESS_OVERLOADED_CONTROLLER =
            registerBlock("advanced_wireless_overloaded_controller", AdvancedWirelessOverloadedControllerBlock::new);

    public static final RegistryObject<OverloadDeviceWorkbenchBlock> OVERLOAD_DEVICE_WORKBENCH =
            registerBlock("overload_device_workbench", OverloadDeviceWorkbenchBlock::new);

    // Matrix blocks are registered as normal Forge blocks; formation is owned
    // by the controller block entity and does not replace AE2's controller.
    public static final RegistryObject<MatrixCasingBlock> MATTER_WARPING_MATRIX_CASING =
            registerBlock("matter_warping_matrix_casing", () -> new MatrixCasingBlock(MATRIX_PROPERTIES));

    public static final RegistryObject<MatrixMultiblockSimpleBlock> MATTER_WARPING_MATRIX_CONSTRAINT_FRAME =
            registerBlock("matter_warping_matrix_constraint_frame",
                    () -> new MatrixMultiblockSimpleBlock(MATRIX_PROPERTIES,
                            MatrixMultiblockComponent.MATRIX_CONSTRAINT_FRAME));

    public static final RegistryObject<MatrixGlassBlock> MATTER_WARPING_MATRIX_GLASS =
            registerBlock("matter_warping_matrix_glass", () -> new MatrixGlassBlock(MATRIX_GLASS_PROPERTIES));

    public static final RegistryObject<MatrixControllerBlock> MATTER_WARPING_MATRIX_CONTROLLER =
            registerBlock("matter_warping_matrix_controller", () -> new MatrixControllerBlock(MATRIX_PROPERTIES));

    public static final RegistryObject<MatrixPortBlock> MATTER_WARPING_MATRIX_PORT =
            registerBlock("matter_warping_matrix_port", () -> new MatrixPortBlock(MATRIX_PROPERTIES));

    public static final RegistryObject<MatrixMultiblockSimpleBlock> MATTER_WARPING_MATRIX_STABLE_MAIN_CORE =
            registerBlock("matter_warping_matrix_stable_main_core", () -> new MatrixMultiblockSimpleBlock(
                    MATRIX_PROPERTIES, MatrixMultiblockComponent.STABLE_MAIN_CORE));

    public static final RegistryObject<MatrixMultiblockSimpleBlock> MATTER_WARPING_MATRIX_QUANTUM_MAIN_CORE =
            registerBlock("matter_warping_matrix_quantum_main_core", () -> new MatrixMultiblockSimpleBlock(
                    MATRIX_PROPERTIES, MatrixMultiblockComponent.QUANTUM_MAIN_CORE));

    public static final RegistryObject<MatrixMultiblockSimpleBlock> MATTER_WARPING_MATRIX_OVERLOAD_MAIN_CORE =
            registerBlock("matter_warping_matrix_overload_main_core", () -> new MatrixMultiblockSimpleBlock(
                    MATRIX_PROPERTIES, MatrixMultiblockComponent.OVERLOAD_MAIN_CORE));

    public static final RegistryObject<MatrixMultiblockSimpleBlock> MATTER_WARPING_MATRIX_MULTIDIMENSIONAL_MAIN_CORE =
            registerBlock("matter_warping_matrix_multidimensional_main_core", () -> new MatrixMultiblockSimpleBlock(
                    MATRIX_PROPERTIES, MatrixMultiblockComponent.MULTIDIMENSIONAL_MAIN_CORE));

    public static final RegistryObject<MatrixMultiblockSimpleBlock> MATTER_WARPING_MATRIX_THREAD_UNIT_T1 =
            registerBlock("matter_warping_matrix_thread_unit_t1", () -> new MatrixMultiblockSimpleBlock(
                    MATRIX_PROPERTIES, MatrixMultiblockComponent.THREAD_UNIT_T1));

    public static final RegistryObject<MatrixMultiblockSimpleBlock> MATTER_WARPING_MATRIX_THREAD_UNIT_T2 =
            registerBlock("matter_warping_matrix_thread_unit_t2", () -> new MatrixMultiblockSimpleBlock(
                    MATRIX_PROPERTIES, MatrixMultiblockComponent.THREAD_UNIT_T2));

    public static final RegistryObject<MatrixMultiblockSimpleBlock> MATTER_WARPING_MATRIX_THERMAL_CONTROL_UNIT_T1 =
            registerBlock("matter_warping_matrix_thermal_control_unit_t1", () -> new MatrixMultiblockSimpleBlock(
                    MATRIX_PROPERTIES, MatrixMultiblockComponent.THERMAL_CONTROL_UNIT_T1));

    public static final RegistryObject<MatrixMultiblockSimpleBlock> MATTER_WARPING_MATRIX_THERMAL_CONTROL_UNIT_T2 =
            registerBlock("matter_warping_matrix_thermal_control_unit_t2", () -> new MatrixMultiblockSimpleBlock(
                    MATRIX_PROPERTIES, MatrixMultiblockComponent.THERMAL_CONTROL_UNIT_T2));

    public static final RegistryObject<MatrixPatternStorageBlock> MATTER_WARPING_MATRIX_PATTERN_STORAGE_T1 =
            registerBlock("matter_warping_matrix_pattern_storage_t1", () -> new MatrixPatternStorageBlock(
                    MATRIX_PROPERTIES, MatrixMultiblockComponent.PATTERN_STORAGE_T1));

    public static final RegistryObject<MatrixPatternStorageBlock> MATTER_WARPING_MATRIX_PATTERN_STORAGE_T2 =
            registerBlock("matter_warping_matrix_pattern_storage_t2", () -> new MatrixPatternStorageBlock(
                    MATRIX_PROPERTIES, MatrixMultiblockComponent.PATTERN_STORAGE_T2));

    private ModBlocks() {
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> blockFactory) {
        return registerBlock(name, blockFactory, () -> true);
    }

    private static <T extends Block> RegistryObject<T> registerBlock(
            String name,
            Supplier<T> blockFactory,
            Supplier<Boolean> shouldRegisterItem) {
        return registerBlock(name, blockFactory, shouldRegisterItem, shouldRegisterItem);
    }

    private static <T extends Block> RegistryObject<T> registerBlock(
            String name,
            Supplier<T> blockFactory,
            Supplier<Boolean> shouldRegisterBlock,
            Supplier<Boolean> shouldRegisterItem) {
        if (!shouldRegisterBlock.get()) {
            return null;
        }

        var registered = BLOCKS.register(name, blockFactory);
        if (shouldRegisterItem.get()) {
            ModItems.ITEMS.register(name, () -> new BlockItem(registered.get(), new Item.Properties()));
        }
        return registered;
    }

    public static boolean hasOverloadedPowerSupply() {
        return OVERLOADED_POWER_SUPPLY != null;
    }

    private static boolean isAppFluxLoaded() {
        return ModList.get().isLoaded(APPFLUX_MODID);
    }
}

