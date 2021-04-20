package szewek.flux.tile;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import szewek.fl.type.FluxContainerType;
import szewek.flux.container.Machine2For1Container;
import szewek.flux.recipe.AbstractMachineRecipe;
import szewek.flux.util.inventory.IOSize;

import javax.annotation.Nullable;
import java.util.function.Function;

public final class Machine2For1Tile extends AbstractMachineTile {
	private static final IOSize IO_SIZE = new IOSize(2, 1);
	private static final int[] SLOTS_TOP_BOTTOM = {0, 2};
	private static final int[] SLOTS_SIDE = {1, 2};
	private final String titleId;
	private final ContainerType<Machine2For1Container> containerType;
	private final LazyOptional<? extends IItemHandler>[] sideHandlers = SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);

	public Machine2For1Tile(TileEntityType<?> typeIn, IRecipeType<? extends AbstractMachineRecipe> recipeTypeIn, ContainerType<Machine2For1Container> ctype, String titleId) {
		super(typeIn, recipeTypeIn, IO_SIZE);
		this.titleId = titleId;
		containerType = ctype;
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		switch (side) {
			case UP:
			case DOWN:
				return SLOTS_TOP_BOTTOM;
			default:
				return SLOTS_SIDE;
		}
	}

	@Override
	protected Container createMenu(int id, PlayerInventory player) {
		return new Machine2For1Container(containerType, recipeType, id, player, this, machineData);
	}

	@Override
	public ITextComponent getDefaultName() {
		return new TranslationTextComponent(this.titleId);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (!remove && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (side == Direction.UP) {
				return sideHandlers[0].cast();
			} else if (side == Direction.DOWN) {
				return sideHandlers[1].cast();
			} else {
				return sideHandlers[2].cast();
			}
		}
		return super.getCapability(cap, side);
	}

	public static Function<TileEntityType<Machine2For1Tile>, Machine2For1Tile> make(final IRecipeType<? extends AbstractMachineRecipe> recipeType, final FluxContainerType<Machine2For1Container> ctype, String titleName) {
		final String titleId = "container.flux." + titleName;
		return type -> new Machine2For1Tile(type, recipeType, ctype, titleId);
	}
}
