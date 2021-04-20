package szewek.flux.tile;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import szewek.fl.recipe.RecipeCompat;
import szewek.flux.F;
import szewek.flux.container.CopierContainer;
import szewek.flux.util.inventory.IOSize;

import javax.annotation.Nullable;

public class CopierTile extends AbstractMachineTile {
	private static final IOSize IO_SIZE = new IOSize(2, 1);
	private static final int[] SLOTS = new int[] {0, 2};

	public CopierTile() {
		super(F.T.COPIER, F.R.COPYING, IO_SIZE);
	}

	@Override
	protected boolean canProcess() {
		if (cachedRecipe == null) {
			//noinspection ConstantConditions
			setCachedRecipe(level.getRecipeManager().getRecipeFor(F.R.COPYING, this, level).orElse(null));
		}
		if (cachedRecipe != null) {
			ItemStack result = inv.get(1);
			if (result.isEmpty()) {
				return false;
			}
			return inv.checkResult(result);
		}
		return false;
	}

	@Override
	protected void produceResult() {
		if (canProcess()) {
			ItemStack orig = inv.get(1).copy();
			if (!orig.isEmpty()) {
				ItemStack copied = orig.copy();
				ItemStack output = inv.get(2);
				if (output.isEmpty()) {
					inv.set(2, copied);
				} else if (ItemStack.isSame(copied, output)) {
					output.grow(1);
				}
				RecipeCompat.getRecipeItemsConsumer(cachedRecipe).accept(inv.subList(0, 1));
				setCachedRecipe(null);
			}
		}
	}

	@Override
	protected ITextComponent getDefaultName() {
		return new TranslationTextComponent("container.flux.copier");
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		return index == 0;
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return SLOTS;
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
		return index == 2;
	}

	@Override
	public void setRecipeUsed(@Nullable IRecipe<?> recipe) {
	}

	@Override
	protected Container createMenu(int id, PlayerInventory player) {
		return new CopierContainer(id, player, this, machineData);
	}
}
