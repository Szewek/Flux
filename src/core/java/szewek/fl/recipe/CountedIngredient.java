package szewek.fl.recipe;

import com.google.gson.JsonObject;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.Stream;

/**
 * Ingredient with set count for an item stack
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CountedIngredient extends Ingredient {
	private final int count;
	private boolean counted;

	CountedIngredient(Stream<? extends IItemList> stream, int count) {
		super(stream);
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	@Override
	public ItemStack[] getItems() {
		ItemStack[] stacks = super.getItems();
		if (!counted) {
			for(ItemStack stack : stacks) {
				stack.setCount(count);
			}
			counted = true;
		}
		return stacks;
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public boolean test(@Nullable ItemStack stack) {
		return super.test(stack) && stack.getCount() >= count;
	}

	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return Serializer.INSTANCE;
	}

	public static class Serializer implements IIngredientSerializer<CountedIngredient> {
		public static final Serializer INSTANCE = new Serializer();

		@Override
		public CountedIngredient parse(PacketBuffer buffer) {
			int count = buffer.readVarInt();
			return new CountedIngredient(Stream.generate(() -> new Ingredient.SingleItemList(buffer.readItem())).limit(buffer.readVarInt()), count);
		}

		@Override
		public CountedIngredient parse(JsonObject json) {
			int count = JSONUtils.getAsInt(json, "count", 1);
			return new CountedIngredient(Stream.of(Ingredient.valueFromJson(json)), count);
		}

		@Override
		public void write(PacketBuffer buffer, CountedIngredient ingredient) {
			buffer.writeVarInt(ingredient.count);
			ItemStack[] items = ingredient.getItems();
			buffer.writeVarInt(items.length);

			for (ItemStack stack : items) {
				buffer.writeItem(stack);
			}
		}
	}
}
