package szewek.flux.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import szewek.flux.F;

public final class WashingRecipe extends AbstractMachineRecipe {
	public WashingRecipe(ResourceLocation idIn, String groupIn, MachineRecipeSerializer.Builder builder) {
		super(F.Recipes.WASHING, idIn, groupIn, builder);
	}

	public ItemStack getIcon() {
		return new ItemStack(F.Blocks.WASHER);
	}

	public IRecipeSerializer<?> getSerializer() {
		return F.Recipes.WASHING_SERIALIZER;
	}


}
