package szewek.flux;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.IForgeRegistry;
import szewek.fl.recipe.RecipeCompat;
import szewek.fl.type.FluxContainerType;
import szewek.fl.type.FluxRecipeType;
import szewek.fl.type.FluxTileType;
import szewek.fl.util.FluxItemTier;
import szewek.flux.block.*;
import szewek.flux.container.*;
import szewek.flux.gui.FluxGenScreen;
import szewek.flux.gui.MachineScreen;
import szewek.flux.gui.SignalControllerScreen;
import szewek.flux.item.*;
import szewek.flux.recipe.*;
import szewek.flux.tile.*;
import szewek.flux.util.ChipUpgradeTrade;
import szewek.flux.util.Gifts;
import szewek.flux.util.Toolset;
import szewek.flux.util.metals.Metal;
import szewek.flux.util.metals.Metals;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static szewek.flux.FluxMod.MODID;

public final class F {
	public static final ItemGroup FLUX_GROUP = new ItemGroup("flux.items") {
		@Override public ItemStack createIcon() {
			return new ItemStack(B.FLUXGEN);
		}
	};

	@SubscribeEvent
	public static void blocks(final RegistryEvent.Register<Block> re) {
		final IForgeRegistry<Block> reg = re.getRegistry();
		for (FluxOreBlock ore : B.ORES.values()) {
			reg.register(ore);
		}
		for (MetalBlock metalBlock : B.METAL_BLOCKS.values()) {
			reg.register(metalBlock);
		}
		reg.registerAll(
				B.FLUXGEN.setRegistryName(MODID, "fluxgen"),
				B.ENERGY_CABLE.setRegistryName(MODID, "energy_cable"),
				B.DIGGER.setRegistryName(MODID, "digger"),
				B.FARMER.setRegistryName(MODID, "farmer"),
				B.BUTCHER.setRegistryName(MODID, "butcher"),
				B.MOB_POUNDER.setRegistryName(MODID, "mob_pounder"),
				B.ITEM_ABSORBER.setRegistryName(MODID, "item_absorber"),
				B.RR_TABLE.setRegistryName(MODID, "rrtable"),
				B.ONLINE_MARKET.setRegistryName(MODID, "online_market"),
				B.GRINDING_MILL.setRegistryName(MODID, "grinding_mill"),
				B.ALLOY_CASTER.setRegistryName(MODID, "alloy_caster"),
				B.WASHER.setRegistryName(MODID, "washer"),
				B.COMPACTOR.setRegistryName(MODID, "compactor"),
				B.INTERACTOR_RAIL.setRegistryName(MODID, "interactor_rail"),
				B.SIGNAL_CONTROLLER.setRegistryName(MODID, "signal_controller"),
				B.COPIER.setRegistryName(MODID, "copier")
		);
	}

	@SubscribeEvent
	public static void items(final RegistryEvent.Register<Item> re) {
		final IForgeRegistry<Item> reg = re.getRegistry();
		for (MetalItem grit : I.GRITS.values()) {
			reg.register(grit);
		}
		for (MetalItem dust : I.DUSTS.values()) {
			reg.register(dust);
		}
		for (MetalItem ingot : I.INGOTS.values()) {
			reg.register(ingot);
		}
		for (MetalItem nugget : I.NUGGETS.values()) {
			reg.register(nugget);
		}
		for (MetalItem gear : I.GEARS.values()) {
			reg.register(gear);
		}
		for (MetalItem plate : I.PLATES.values()) {
			reg.register(plate);
		}
		for (Map.Entry<Metal, FluxOreBlock> e : B.ORES.entrySet()) {
			Metal key = e.getKey();
			FluxOreBlock value = e.getValue();
			reg.register(fromBlock(value, key.metalName + "_ore"));
		}
		for (Map.Entry<Metal, MetalBlock> e : B.METAL_BLOCKS.entrySet()) {
			Metal name = e.getKey();
			MetalBlock b = e.getValue();
			reg.register(fromBlock(b, name.metalName + "_block"));
		}
		reg.registerAll(
				I.FLUXTOOL, I.GIFT, I.MACHINE_BASE, I.CHIP,
				I.SEAL, I.GLUE, I.PASTE,
				fromBlock(B.FLUXGEN, "fluxgen"),
				fromBlock(B.GRINDING_MILL, "grinding_mill"),
				fromBlock(B.ALLOY_CASTER, "alloy_caster"),
				fromBlock(B.WASHER, "washer"),
				fromBlock(B.COMPACTOR, "compactor"),
				fromBlock(B.ENERGY_CABLE, "energy_cable"),
				fromBlock(B.DIGGER, "digger"),
				fromBlock(B.FARMER, "farmer"),
				fromBlock(B.BUTCHER, "butcher"),
				fromBlock(B.MOB_POUNDER, "mob_pounder"),
				fromBlock(B.ITEM_ABSORBER, "item_absorber"),
				fromBlock(B.RR_TABLE, "rrtable"),
				fromBlock(B.ONLINE_MARKET, "online_market"),
				fromBlock(B.INTERACTOR_RAIL, "interactor_rail"),
				fromBlock(B.SIGNAL_CONTROLLER, "signal_controller"),
				fromBlock(B.COPIER, "copier")
		);
		I.BRONZE_TOOLS.registerTools(reg);
		I.STEEL_TOOLS.registerTools(reg);
	}

	@SubscribeEvent
	public static void tiles(final RegistryEvent.Register<TileEntityType<?>> re) {
		re.getRegistry().registerAll(
				T.FLUXGEN, T.ENERGY_CABLE, T.DIGGER, T.FARMER, T.BUTCHER, T.MOB_POUNDER, T.ITEM_ABSORBER,
				T.GRINDING_MILL, T.ALLOY_CASTER, T.WASHER, T.COMPACTOR, T.RR_TABLE, T.ONLINE_MARKET,
				T.INTERACTOR_RAIL, T.SIGNAL_CONTROLLER, T.COPIER
		);
	}

	@SubscribeEvent
	public static void containers(final RegistryEvent.Register<ContainerType<?>> re) {
		re.getRegistry().registerAll(
				C.FLUXGEN.setRegistryName(MODID, "fluxgen"),
				C.GRINDING_MILL.setRegistryName(MODID, "grinding_mill"),
				C.ALLOY_CASTER.setRegistryName(MODID, "alloy_caster"),
				C.WASHER.setRegistryName(MODID, "washer"),
				C.COMPACTOR.setRegistryName(MODID, "compactor"),
				C.SIGNAL_CONTROLLER.setRegistryName(MODID, "signal_controller"),
				C.COPIER.setRegistryName(MODID, "copier")
		);
	}

	@SubscribeEvent
	public static void recipes(final RegistryEvent.Register<IRecipeSerializer<?>> re) {
		re.getRegistry().registerAll(
				R.GRINDING.serializer,
				R.ALLOYING.serializer,
				R.WASHING.serializer,
				R.COMPACTING.serializer,
				CopyingRecipe.SERIALIZER
		);
		@SuppressWarnings("unchecked")
		final List<String> blacklist = (List<String>) FluxCfg.COMMON.blacklistCompatRecipes.get();
		final Predicate<String> filterBlacklist = s -> !blacklist.contains(s);

		recipeCompat(R.GRINDING, filterBlacklist,
				"pattysmorestuff:crushing",
				"silents_mechanisms:crushing",
				"usefulmachinery:crushing"
		);
		recipeCompat(R.ALLOYING, filterBlacklist,
				"blue_power:alloy_smelting"
		);
		recipeCompat(R.COMPACTING, filterBlacklist, "wtbw_machines:compressing");
	}

	@SubscribeEvent
	public static void professions(final RegistryEvent.Register<VillagerProfession> re) {
		re.getRegistry().register(V.FLUX_ENGINEER.setRegistryName(MODID, "flux_engineer"));
		Int2ObjectMap<VillagerTrades.ITrade[]> lvlTrades = new Int2ObjectOpenHashMap<>();
		lvlTrades.put(1, new VillagerTrades.ITrade[]{
				new VillagerTrades.EmeraldForItemsTrade(I.INGOTS.get(Metals.COPPER), 6, 10, 4),
				new ChipUpgradeTrade(-1, 5)
		});
		lvlTrades.put(2, new VillagerTrades.ITrade[]{
				new VillagerTrades.EmeraldForItemsTrade(I.INGOTS.get(Metals.TIN), 4, 8, 4),
				new ChipUpgradeTrade(-3, 10)
		});
		lvlTrades.put(3, new VillagerTrades.ITrade[]{
				new ChipUpgradeTrade(-5, 20)
		});
		lvlTrades.put(4, new VillagerTrades.ITrade[]{
				new ChipUpgradeTrade(-9, 50)
		});
		lvlTrades.put(5, new VillagerTrades.ITrade[]{
				new ChipUpgradeTrade(-9, 100)
		});
		VillagerTrades.VILLAGER_DEFAULT_TRADES.put(V.FLUX_ENGINEER, lvlTrades);
	}

	@SubscribeEvent
	public static void pointsOfInterest(final RegistryEvent.Register<PointOfInterestType> re) {
		re.getRegistry().register(V.FLUX_ENGINEER_POI);
	}

	@OnlyIn(Dist.CLIENT)
	static void client(final Minecraft mc) {
		final Item[] arr = new Item[0];
		final ItemColors ic = mc.getItemColors();
		ic.register(Gifts::colorByGift, I.GIFT);
		ic.register(Metals::gritColors, I.GRITS.values().toArray(arr));
		ic.register(Metals::itemColors, I.DUSTS.values().toArray(arr));
		ic.register(Metals::ingotColors, I.INGOTS.values().toArray(arr));
		ic.register(Metals::itemColors, I.NUGGETS.values().toArray(arr));
		ic.register(Metals::itemColors, I.GEARS.values().toArray(arr));
		ic.register(Metals::itemColors, I.PLATES.values().toArray(arr));
		I.BRONZE_TOOLS.registerToolColors(Metals.BRONZE, ic);
		I.STEEL_TOOLS.registerToolColors(Metals.STEEL, ic);

		RenderTypeLookup.setRenderLayer(B.INTERACTOR_RAIL, RenderType.getCutout());

		ScreenManager.registerFactory(C.FLUXGEN, FluxGenScreen::new);
		ScreenManager.registerFactory(C.SIGNAL_CONTROLLER, SignalControllerScreen::new);
		ScreenManager.registerFactory(C.GRINDING_MILL, MachineScreen.make("grindable", "grinding_mill"));
		ScreenManager.registerFactory(C.ALLOY_CASTER, MachineScreen.make("alloyable", "alloy_caster"));
		ScreenManager.registerFactory(C.WASHER, MachineScreen.make("washable", "washer"));
		ScreenManager.registerFactory(C.COMPACTOR, MachineScreen.make("compactable", "compactor"));
		ScreenManager.registerFactory(C.COPIER, MachineScreen.make("copyable", "copier"));
	}

	public static final class B {
		public static final Map<Metal, FluxOreBlock> ORES = makeOres();
		public static final Map<Metal, MetalBlock> METAL_BLOCKS = makeBlocks();
		public static final FluxGenBlock FLUXGEN = new FluxGenBlock();
		public static final EnergyCableBlock ENERGY_CABLE = new EnergyCableBlock();
		public static final RRTableBlock RR_TABLE = new RRTableBlock();
		public static final OnlineMarketBlock ONLINE_MARKET = new OnlineMarketBlock();
		public static final InteractorRailBlock INTERACTOR_RAIL = new InteractorRailBlock();
		public static final SignalControllerBlock SIGNAL_CONTROLLER = new SignalControllerBlock();
		public static final ActiveTileBlock
				DIGGER = new ActiveTileBlock(),
				FARMER = new ActiveTileBlock(),
				BUTCHER = new ActiveTileBlock(),
				MOB_POUNDER = new ActiveTileBlock(),
				ITEM_ABSORBER = new ActiveTileBlock();
		public static final MachineBlock
				GRINDING_MILL = new MachineBlock(),
				ALLOY_CASTER = new MachineBlock(),
				WASHER = new MachineBlock(),
				COMPACTOR = new MachineBlock(),
				COPIER = new MachineBlock();
	}

	public static final class I {
		public static final Map<Metal, MetalItem>
				GRITS = metalMap("grit", Metal::nonAlloy),
				DUSTS = metalMap("dust", null),
				INGOTS = metalMap("ingot", Metal::nonVanilla),
				NUGGETS = metalMap("nugget", Metal::nonVanilla),
				GEARS = metalMap("gear", null),
				PLATES = metalMap("plate", null);
		public static final FluxToolItem FLUXTOOL = item(FluxToolItem::new, "mftool", new Item.Properties().maxStackSize(1));
		public static final GiftItem GIFT = item(GiftItem::new, "gift", new Item.Properties().maxStackSize(1));
		public static final Item MACHINE_BASE = item(Item::new, "machine_base", new Item.Properties());
		public static final ChipItem CHIP = item(ChipItem::new, "chip", new Item.Properties());
		public static final FluxAdhesiveItem
				SEAL = item(FluxAdhesiveItem::new, "seal", new Item.Properties()),
				GLUE = item(FluxAdhesiveItem::new, "glue", new Item.Properties()),
				PASTE = item(FluxAdhesiveItem::new, "paste", new Item.Properties());

		public static final FluxItemTier
				BRONZE_TIER = new FluxItemTier(2, 500, 7f, 2.5f, 20, "ingots/bronze", INGOTS.get(Metals.BRONZE)),
				STEEL_TIER = new FluxItemTier(3, 1500, 8.5f, 3f, 22, "ingots/steel", INGOTS.get(Metals.STEEL));
		public static final Toolset
				BRONZE_TOOLS = new Toolset(BRONZE_TIER, "bronze"),
				STEEL_TOOLS = new Toolset(STEEL_TIER, "steel");

	}

	public static final class T {
		public static final TileEntityType<FluxGenTile> FLUXGEN;
		public static final TileEntityType<EnergyCableTile> ENERGY_CABLE;
		public static final TileEntityType<DiggerTile> DIGGER;
		public static final TileEntityType<FarmerTile> FARMER;
		public static final TileEntityType<ButcherTile> BUTCHER;
		public static final TileEntityType<MobPounderTile> MOB_POUNDER;
		public static final TileEntityType<ItemAbsorberTile> ITEM_ABSORBER;
		public static final TileEntityType<RRTableTile> RR_TABLE;
		public static final TileEntityType<OnlineMarketTile> ONLINE_MARKET;
		public static final TileEntityType<InteractorRailTile> INTERACTOR_RAIL;
		public static final TileEntityType<SignalControllerTile> SIGNAL_CONTROLLER;
		public static final TileEntityType<CopierTile> COPIER;
		public static final FluxTileType<?>
				GRINDING_MILL,
				ALLOY_CASTER,
				WASHER,
				COMPACTOR;

		static {
			FLUXGEN = tile(FluxGenTile::new, "fluxgen", B.FLUXGEN);
			ENERGY_CABLE = tile(EnergyCableTile::new, "energy_cable", B.ENERGY_CABLE);
			DIGGER = tile(DiggerTile::new, "digger", B.DIGGER);
			FARMER = tile(FarmerTile::new, "farmer", B.FARMER);
			BUTCHER = tile(ButcherTile::new, "butcher", B.BUTCHER);
			MOB_POUNDER = tile(MobPounderTile::new, "mob_pounder", B.MOB_POUNDER);
			ITEM_ABSORBER = tile(ItemAbsorberTile::new, "item_absorber", B.ITEM_ABSORBER);
			RR_TABLE = tile(RRTableTile::new, "rrtable", B.RR_TABLE);
			ONLINE_MARKET = tile(OnlineMarketTile::new, "online_market", B.ONLINE_MARKET);
			INTERACTOR_RAIL = tile(InteractorRailTile::new, "interactor_rail", B.INTERACTOR_RAIL);
			SIGNAL_CONTROLLER = tile(SignalControllerTile::new, "signal_controller", B.SIGNAL_CONTROLLER);
			COPIER = tile(CopierTile::new, "copier", B.COPIER);
			GRINDING_MILL = tile(Machine2For1Tile.make(R.GRINDING, C.GRINDING_MILL, "grinding_mill"), "grinding_mill", B.GRINDING_MILL);
			ALLOY_CASTER = tile(Machine2For1Tile.make(R.ALLOYING, C.ALLOY_CASTER, "alloy_caster"), "alloy_caster", B.ALLOY_CASTER);
			WASHER = tile(Machine2For1Tile.make(R.WASHING, C.WASHER, "washer"), "washer", B.WASHER);
			COMPACTOR = tile(Machine2For1Tile.make(R.COMPACTING, C.COMPACTOR, "compactor"), "compactor", B.COMPACTOR);
		}
	}

	public static final class C {
		public static final ContainerType<FluxGenContainer> FLUXGEN;
		public static final ContainerType<SignalControllerContainer> SIGNAL_CONTROLLER;
		public static final ContainerType<CopierContainer> COPIER;
		public static final FluxContainerType<Machine2For1Container>
				GRINDING_MILL, ALLOY_CASTER, WASHER, COMPACTOR;

		static {
			FLUXGEN = container(FluxGenContainer::new);
			SIGNAL_CONTROLLER = container(SignalControllerContainer::new);
			COPIER = container(CopierContainer::new);
			GRINDING_MILL = containerFlux(Machine2For1Container.make(R.GRINDING));
			ALLOY_CASTER = containerFlux(Machine2For1Container.make(R.ALLOYING));
			WASHER = containerFlux(Machine2For1Container.make(R.WASHING));
			COMPACTOR = containerFlux(Machine2For1Container.make(R.COMPACTING));
		}
	}

	public static final class R {
		public static final FluxRecipeType<GrindingRecipe> GRINDING = recipe("grinding", serializer(GrindingRecipe::new, "grinding"));
		public static final FluxRecipeType<AlloyingRecipe> ALLOYING = recipe("alloying", serializer(AlloyingRecipe::new, "alloying"));
		public static final FluxRecipeType<WashingRecipe> WASHING = recipe("washing", serializer(WashingRecipe::new, "washing"));
		public static final FluxRecipeType<CompactingRecipe> COMPACTING = recipe("compacting", serializer(CompactingRecipe::new, "compacting"));
		public static final FluxRecipeType<CopyingRecipe> COPYING = recipe("copying", CopyingRecipe.SERIALIZER);
	}

	public static final class V {
		public static final PointOfInterestType
				FLUX_ENGINEER_POI = poi("flux_engineer", B.FLUXGEN);
		public static final VillagerProfession FLUX_ENGINEER = new VillagerProfession("flux:flux_engineer", FLUX_ENGINEER_POI, ImmutableSet.of(), ImmutableSet.of(), null);
	}

	public static final class Tags {
		public static final Tag<Block> DIGGER_SKIP = blockTag("digger_skip");
	}

	private static Map<Metal, FluxOreBlock> makeOres() {
		Map<Metal, FluxOreBlock> m = new HashMap<>();
		for (Metal metal : Metals.all()) {
			if (metal.notVanillaOrAlloy()) {
				FluxOreBlock b = new FluxOreBlock(metal);
				b.setRegistryName("flux", metal.metalName + "_ore");
				m.put(metal, b);
			}
		}
		return m;
	}

	private static Map<Metal, MetalBlock> makeBlocks() {
		Map<Metal, MetalBlock> m = new HashMap<>();
		for (Metal metal : Metals.all()) {
			if (metal.nonVanilla()) {
				MetalBlock b = new MetalBlock(metal);
				b.setRegistryName("flux", metal.metalName + "_block");
				m.put(metal, b);
			}
		}
		return m;
	}

	private static <T extends Item> T item(Function<Item.Properties, T> f, String name, Item.Properties props) {
		T item = f.apply(props.group(FLUX_GROUP));
		item.setRegistryName(MODID, name);
		return item;
	}

	private static BlockItem fromBlock(Block b, String name) {
		BlockItem item = new BlockItem(b, new Item.Properties().group(FLUX_GROUP));
		item.setRegistryName(MODID, name);
		return item;
	}

	private static Map<Metal, MetalItem> metalMap(String type, Predicate<Metal> filter) {
		Map<Metal, MetalItem> m = new HashMap<>();
		Item.Properties props = new Item.Properties();
		for (Metal met : Metals.all()) {
			if (filter == null || filter.test(met)) {
				m.put(met, item(p -> new MetalItem(p, met), met.metalName + '_' + type, props));
			}
		}
		return m;
	}

	private static <T extends TileEntity> TileEntityType<T> tile(Supplier<T> f, String name, Block b) {
		//noinspection ConstantConditions
		TileEntityType<T> type = new TileEntityType<>(f, Collections.singleton(b), null);
		type.setRegistryName(MODID, name);
		return type;
	}

	private static <T extends TileEntity> FluxTileType<T> tile(Function<FluxTileType<T>, T> f, String name, Block b) {
		//noinspection ConstantConditions
		FluxTileType<T> type = new FluxTileType<>(f, Collections.singleton(b), null);
		type.setRegistryName(MODID, name);
		return type;
	}

	private static <C extends Container> ContainerType<C> container(IContainerFactory<C> factory) {
		return new ContainerType<>(factory);
	}

	private static <C extends AbstractMachineContainer> FluxContainerType<C> containerFlux(FluxContainerType.IContainerBuilder<C> cb) {
		return new FluxContainerType<>(cb);
	}

	private static <T extends IRecipe<?>> FluxRecipeType<T> recipe(String key, IRecipeSerializer<T> ser) {
		return Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(MODID, key), new FluxRecipeType<>(key, ser));
	}

	private static <T extends AbstractMachineRecipe> MachineRecipeSerializer<T> serializer(MachineRecipeSerializer.IFactory<T> factory, String key) {
		MachineRecipeSerializer<T> mrs = new MachineRecipeSerializer<>(factory, 200);
		mrs.setRegistryName(MODID, key);
		return mrs;
	}

	private static PointOfInterestType poi(String name, Block b) {
		return PointOfInterestType.func_221052_a(
				new PointOfInterestType(MODID + ":" + name, ImmutableSet.copyOf(b.getStateContainer().getValidStates()), 1, 1)
				.setRegistryName(MODID, name)
		);
	}

	private static void recipeCompat(IRecipeType<?> rtype, Predicate<String> filter, String... compats) {
		RecipeCompat.registerCompatRecipeTypes(rtype, Arrays.stream(compats).filter(filter).collect(Collectors.toSet()));
	}

	private static Tag<Block> blockTag(String name) {
		return new BlockTags.Wrapper(new ResourceLocation(MODID, name));
	}

	private F() {}
}
