package szewek.flux.tile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import szewek.fl.signal.ISignalHandler;
import szewek.fl.signal.SignalCapability;
import szewek.flux.F;
import szewek.flux.block.SignalControllerBlock;
import szewek.flux.container.SignalControllerContainer;

import static szewek.flux.block.SignalControllerBlock.POWERED;

public class SignalControllerTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
	private int cooldown, keepPower;
	private short currentChannel;
	private byte mode;
	private final IIntArray data = new IIntArray() {
		@Override
		public int get(int index) {
			switch (index) {
				case 0:
					return mode;
				case 1:
					return currentChannel;
			}
			return 0;
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case 0:
					mode = (byte) (value % 4);
					break;
				case 1:
					currentChannel = (short) MathHelper.clamp(value, 0, 256);
			}
		}

		@Override
		public int getCount() {
			return 2;
		}
	};

	public SignalControllerTile() {
		super(F.T.SIGNAL_CONTROLLER);
	}

	@Override
	public Container createMenu(int id, PlayerInventory pinv, PlayerEntity player) {
		return new SignalControllerContainer(id, pinv, data);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent("container.flux.signal_controller");
	}

	@Override
	public void load(BlockState blockState, CompoundNBT compound) {
		super.load(blockState, compound);
		currentChannel = (short) MathHelper.clamp(compound.getShort("Channel"), 0, 255);
		mode = compound.getByte("Mode");
		keepPower = compound.getInt("KeepPower");
	}

	@Override
	public CompoundNBT save(CompoundNBT compound) {
		super.save(compound);
		compound.putShort("Channel", currentChannel);
		compound.putByte("Mode", mode);
		compound.putInt("KeepPower", keepPower);
		return compound;
	}

	public void updateData(byte m, short ch) {
		mode = m;
		currentChannel = (short) MathHelper.clamp(ch, 0, 255);
		if (mode != 0) {
			BlockState state = getBlockState();
			if (state.getValue(POWERED)) {
				level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, false));
			}
		}
	}

	@Override
	public void tick() {
		if (!level.isClientSide) {
			if (cooldown > 0) {
				--cooldown;
			} else {
				updateState();
			}
			if (keepPower > 0) {
				--keepPower;
			} else {
				level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERED, false));
			}
		}
	}

	public void updateState() {
		cooldown = 10;
		BlockState state = getBlockState();
		Direction dir = state.getValue(SignalControllerBlock.FACING);
		TileEntity tile = level.getBlockEntity(worldPosition.relative(dir));
		boolean sb = state.getValue(POWERED);
		if (tile != null) {
			LazyOptional<ISignalHandler> lazyOpt = tile.getCapability(SignalCapability.SIGNAL_CAP, dir.getOpposite());
			if (lazyOpt.isPresent()) {
				ISignalHandler sh = lazyOpt.orElse(null);
				boolean b;
				switch (mode) {
					case 0:
						if (sh.allowsSignalOutput(currentChannel)) {
							b = sh.getSignal(currentChannel);
							if (b) {
								keepPower = 15;
								if (!sb) {
									level.setBlockAndUpdate(worldPosition, state.setValue(POWERED, true));
								}
							}
						}
						break;
					case 1:
						if (sh.allowsSignalInput(currentChannel)) {
							sh.setSignal(currentChannel);
						}
						break;
					case 2:
						if (sh.allowsSignalInput(currentChannel)) {
							sh.clearSignal(currentChannel);
						}
						break;
					case 3:
						if (sh.allowsSignalInput(currentChannel)) {
							b = level.hasNeighborSignal(worldPosition);
							sh.putSignal(currentChannel, b);
						}
				}
				cooldown = 4;
			}
		}
	}
}
