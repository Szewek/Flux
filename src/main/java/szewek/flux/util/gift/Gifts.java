package szewek.flux.util.gift;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Gifts {
	private static final Int2ObjectMap<GiftData> GIFT_MAP = new Int2ObjectOpenHashMap<>();

	private static void add(int day, int month, int boxColor, int ribbonColor, List<ItemStack> stacks) {
		GIFT_MAP.put(month * 32 + day, new GiftData(boxColor, ribbonColor, stacks));
	}

	public static GiftData get(int xday) {
		return GIFT_MAP.get(xday);
	}

	public static int colorByGift(ItemStack stack, int pass) {
		CompoundNBT tag = stack.getTag();
		if (tag == null) {
			return 0x808080;
		} else {
			int xday = tag.getInt("xDay");
			GiftData gd = GIFT_MAP.get(xday);
			if (gd == null) {
				return 0x404040;
			} else {
				return pass == 0 ? gd.boxColor : gd.ribbonColor;
			}
		}
	}

	static {
		add(1, 1, 0x2020F0, 0xF0F020, Arrays.asList(
			new ItemStack(Items.NETHER_STAR),
			new ItemStack(Items.FIREWORK_ROCKET, 16)
		));
		add(14, 2, 0xF01010, 0xF88080, Collections.singletonList(
				new ItemStack(Items.HEART_OF_THE_SEA, 2)
		));
		add(14, 3, 0x808080, 0x2020DF, Collections.singletonList(
				new ItemStack(Items.PUMPKIN_PIE)
		));
		add(22, 3, 0x0808F8, 0x3232F0, Collections.singletonList(
				new ItemStack(Items.WATER_BUCKET)
		));
	}

	private Gifts() {}
}
