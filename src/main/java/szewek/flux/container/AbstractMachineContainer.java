package szewek.flux.container;

import net.minecraft.client.util.RecipeBookCategories;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IRecipeHelperPopulator;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeBookCategory;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import szewek.fl.util.ConsumerUtil;
import szewek.flux.item.ChipItem;
import szewek.flux.util.ServerRecipePlacerMachine;
import szewek.flux.util.inventory.IOSize;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractMachineContainer extends RecipeBookContainer<IInventory> {
	protected final IInventory machineInventory;
	private final IIntArray data;
	protected final World world;
	public final IRecipeType<?> recipeType;
	private final IOSize ioSize;

	protected AbstractMachineContainer(ContainerType ctype, IRecipeType<?> rtype, int id, PlayerInventory playerInv, IOSize ioSize) {
		this(ctype, rtype, id, playerInv, ioSize, new Inventory(ioSize.all + 1), new IntArray(7));
	}

	protected AbstractMachineContainer(ContainerType ctype, IRecipeType<?> rtype, int id, PlayerInventory playerInv, IOSize ioSize, IInventory machineInv, IIntArray data) {
		super(ctype, id);
		recipeType = rtype;
		this.ioSize = ioSize;
		Container.checkContainerSize(machineInv, ioSize.all + 1);
		Container.checkContainerDataCount(data, 7);
		machineInventory = machineInv;
		this.data = data;
		world = playerInv.player.level;
		initSlots(playerInv);

		ConsumerUtil.addPlayerSlotsAt(playerInv, 8, 84, this::addSlot);
		addDataSlots(data);
	}

	protected abstract void initSlots(PlayerInventory playerInventory);

	@Override
	public void fillCraftSlotsStackedContents(RecipeItemHelper helper) {
		if (machineInventory instanceof IRecipeHelperPopulator) {
			((IRecipeHelperPopulator) machineInventory).fillStackedContents(helper);
		}
	}

	@Override
	public void clearCraftingContent() {
		machineInventory.clearContent();
	}

	@Override
	public void handlePlacement(boolean placeAll, IRecipe<?> recipe, ServerPlayerEntity player) {
		//noinspection unchecked
		new ServerRecipePlacerMachine<>(this, ioSize).recipeClicked(player, (IRecipe<IInventory>) recipe, placeAll);
	}

	@Override
	public boolean recipeMatches(IRecipe<? super IInventory> recipeIn) {
		return recipeIn.getType() == recipeType && recipeIn.matches(machineInventory, world);
	}

	@Override
	public int getResultSlotIndex() {
		return ioSize.in;
	}

	@Override
	public int getGridWidth() {
		return ioSize.in;
	}

	@Override
	public int getGridHeight() {
		return 1;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public int getSize() {
		return ioSize.in + 1;
	}

	@Override
	public boolean stillValid(PlayerEntity playerIn) {
		return machineInventory.stillValid(playerIn);
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack slotStack = slot.getItem();
			stack = slotStack.copy();
			int s = ioSize.all + 1;
			int e = s + 36;
			if (index >= s) {
				if (slotStack.getItem() instanceof ChipItem) {
					s = ioSize.all;
					e = s + 1;
				} else {
					e = s;
					s = 0;
				}
			}

			if (!moveItemStackTo(slotStack, s, e, false)) {
				return ItemStack.EMPTY;
			}

			if (index >= ioSize.in && index < ioSize.all + 1) {
				slot.onQuickCraft(slotStack, stack);
			}

			if (slotStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (slotStack.getCount() == stack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, slotStack);
		}
		return stack;
	}

	@Override
	// TODO Specify better catrgory
	public RecipeBookCategory getRecipeBookType() {
		return RecipeBookCategory.CRAFTING;
	}

	@Override
	public List<RecipeBookCategories> getRecipeBookCategories() {
		return Arrays.asList(RecipeBookCategories.CRAFTING_SEARCH, RecipeBookCategories.CRAFTING_EQUIPMENT, RecipeBookCategories.CRAFTING_BUILDING_BLOCKS, RecipeBookCategories.CRAFTING_MISC, RecipeBookCategories.CRAFTING_REDSTONE);
	}

	@OnlyIn(Dist.CLIENT)
	public final int processScaled() {
		int i = data.get(2);
		int j = data.get(3);
		return j == 0 || i == 0 ? 0 : i * 24 / j;
	}

	public final boolean isCompatRecipe() {
		return data.get(6) > 0;
	}

	@OnlyIn(Dist.CLIENT)
	public final int energyScaled() {
		return getEnergy() * 54 / 1000000;
	}

	@OnlyIn(Dist.CLIENT)
	public final List<ITextComponent> energyText() {
		return Arrays.asList(
				new StringTextComponent(getEnergy() + " / " + 1000000 + " F"),
				new TranslationTextComponent("flux.usage", data.get(4))
		);
	}

	private int getEnergy() {
		return (data.get(0) << 16) + data.get(1);
	}

}
