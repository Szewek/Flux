package szewek.flux.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.ItemHandlerHelper;
import szewek.flux.F;
import szewek.flux.FluxCfg;
import szewek.flux.network.FluxPackets;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class Gifts implements IFutureReloadListener {
	private static final ITextComponent RECEIVED_GIFT = new TranslationTextComponent("flux.gift.received");
	private static final Set<ResourceLocation> GIFT_LOOT_TABLES = new HashSet<>();
	private static final ResourceLocation GIFTS_LIST = F.loc("gifts/global_list.json");

	Gifts() {
	}

	@Override
	public CompletableFuture<Void> reload(IStage stage, IResourceManager rm, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor bgExec, Executor gameExec) {
		return collectGiftLootTables(rm, bgExec)
				.thenCompose(stage::wait)
				.thenAcceptAsync(Gifts::saveGiftLootTables, gameExec);
	}

	private static CompletableFuture<Set<ResourceLocation>> collectGiftLootTables(IResourceManager rm, Executor exec) {
		return FluxData.collectFromResources(HashSet::new, rm, GIFTS_LIST, exec, (set, json) -> {
			JsonArray entries = JSONUtils.getAsJsonArray(json, "entries");
			for (JsonElement el : entries) {
				set.add(new ResourceLocation(el.getAsString()));
			}
		});
	}

	private static void saveGiftLootTables(Set<ResourceLocation> locSet) {
		GIFT_LOOT_TABLES.clear();
		GIFT_LOOT_TABLES.addAll(locSet);
	}

	public static int giftColors(ItemStack stack, int pass) {
		CompoundNBT tag = stack.getTag();
		if (tag == null) {
			return 0x808080;
		}
		String name = pass == 0 ? "Box" : "Ribbon";
		return tag.contains(name) ? tag.getInt(name) : 0x404040;
	}

	public static void makeGiftsForPlayer(ServerPlayerEntity player) {
		if (!FluxCfg.COMMON.gifts.get()) {
			return;
		}
		CompoundNBT data = player.getPersistentData();
		ListNBT received = data.getList("receivedGifts", 8);
		boolean change = false;
		LootContext lootCtx = new LootContext.Builder((ServerWorld) player.level)
				.withParameter(LootParameters.ORIGIN, player.position())
				.withParameter(LootParameters.THIS_ENTITY, player)
				.create(LootParameterSets.GIFT);
		for (ResourceLocation loc : GIFT_LOOT_TABLES) {
			final StringNBT locNBT = StringNBT.valueOf(loc.toString());
			if (!received.contains(locNBT)) {
				produceGifts(player, lootCtx, loc);
				change = true;
				received.add(locNBT);
			}
		}
		if (change) {
			FluxPackets.sendGiftReceived(player);
			data.put("receivedGifts", received);
			player.sendMessage(RECEIVED_GIFT, Util.NIL_UUID);
		}
	}

	public static void produceGifts(ServerPlayerEntity player, LootContext lootCtx, ResourceLocation loc) {
		LootTable lootTable = player.server.getLootTables().get(loc);
		for (ItemStack stack : lootTable.getRandomItems(lootCtx)) {
			ItemHandlerHelper.giveItemToPlayer(player, stack);
		}
	}
}
