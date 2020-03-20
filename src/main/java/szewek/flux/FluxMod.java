package szewek.flux;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import szewek.flux.energy.FurnaceEnergy;
import szewek.flux.util.FluxDataManager;
import szewek.flux.util.gift.GiftData;
import szewek.flux.util.gift.Gifts;
import szewek.flux.util.metals.Metals;

import java.util.Calendar;

@Mod(FluxMod.MODID)
public final class FluxMod {
	public static final String MODID = "flux";
	private static IModInfo modInfo;

	public FluxMod() {
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FluxCfg.commonSpec);
		modEventBus.register(FluxCfg.class);
		modEventBus.register(CommonEvents.class);
		modEventBus.register(F.class);
	}

	final static class CommonEvents {
		@SubscribeEvent
		public static void setup(final FMLCommonSetupEvent e) {
			modInfo = ModLoadingContext.get().getActiveContainer().getModInfo();

			if (!FluxCfg.COMMON.disableOres.get()) {
				ForgeRegistries.BIOMES.getValues().forEach(biome -> {
					Biome.Category cat = biome.getCategory();
					if (cat != Biome.Category.NETHER && cat != Biome.Category.THEEND) {
						biome.addFeature(
								GenerationStage.Decoration.UNDERGROUND_ORES,
								Feature.ORE.withConfiguration(new OreFeatureConfig(
										OreFeatureConfig.FillerBlockType.NATURAL_STONE,
										F.B.ORES.get(Metals.COPPER).getDefaultState(),
								7
	                            )).withPlacement(Placement.COUNT_RANGE.configure(new CountRangeConfig(20, 0, 0, 96)))
	                    );
						biome.addFeature(
								GenerationStage.Decoration.UNDERGROUND_ORES,
								Feature.ORE.withConfiguration(new OreFeatureConfig(
										OreFeatureConfig.FillerBlockType.NATURAL_STONE,
										F.B.ORES.get(Metals.TIN).getDefaultState(),
								7
	                            )).withPlacement(Placement.COUNT_RANGE.configure(new CountRangeConfig(20, 0, 0, 72)))
	                    );
					}
				});
			}
		}
	}

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	final static class ClientEvents {
		@SubscribeEvent
		public static void setupClient(final FMLClientSetupEvent e) {
			F.client(e.getMinecraftSupplier().get());
		}
	}

	@Mod.EventBusSubscriber
	final static class Events {
		private static final ResourceLocation FURNACE_CAP = new ResourceLocation(MODID, "furnace_energy");

		@SubscribeEvent
		public static void wrapTile(final AttachCapabilitiesEvent<TileEntity> e) {
			TileEntity te = e.getObject();
			if (te instanceof AbstractFurnaceTileEntity) {
				e.addCapability(FURNACE_CAP, new FurnaceEnergy((AbstractFurnaceTileEntity) te));
			}
		}

		@SubscribeEvent
		public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent pe) {
			PlayerEntity player = pe.getPlayer();
			if (!player.world.isRemote) {
				VersionChecker.CheckResult ver = VersionChecker.getResult(modInfo);
				if (ver.target != null && (ver.status == VersionChecker.Status.OUTDATED || ver.status == VersionChecker.Status.BETA_OUTDATED)) {
					player.sendMessage(new TranslationTextComponent("flux.update", ver.target.toString()));
				}
				CompoundNBT data = player.getPersistentData();
				int lastXDay = data.getInt("lastXDay");
				int lastXYear = data.getInt("lastXYear");
				Calendar calendar = Calendar.getInstance();
				int xday = (1 + calendar.get(Calendar.MONTH)) * 32 + calendar.get(Calendar.DAY_OF_MONTH);
				int xyear = calendar.get(Calendar.YEAR);
				if (lastXYear < xyear) {
					lastXDay = 0;
				}
				if (lastXDay < xday) {
					GiftData gd = Gifts.get(xday);
					if (gd != null) {
						data.putInt("lastXDay", xday);
						data.putInt("lastXYear", xyear);
						CompoundNBT itemTag = new CompoundNBT();
						itemTag.putInt("xDay", xday);
						ItemStack giftStack = new ItemStack(F.I.GIFT, 1);
						giftStack.setTag(itemTag);
						ItemHandlerHelper.giveItemToPlayer(player, giftStack, -1);
					}
				}
			}
		}

		@SubscribeEvent
		public static void serverAboutToStart(final FMLServerAboutToStartEvent e) {
			e.getServer().getResourceManager().addReloadListener(new FluxDataManager());
		}
	}
}
