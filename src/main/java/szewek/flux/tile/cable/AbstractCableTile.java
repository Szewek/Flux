package szewek.flux.tile.cable;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractCableTile<T> extends TileEntity implements ITickableTileEntity {
	private int cooldown;
	private final Capability<T> cap;
	protected final AtomicInteger sideFlag = new AtomicInteger();
	@SuppressWarnings({"unchecked"})
	protected final AbstractSide<T>[] sides = (AbstractSide<T>[]) new AbstractSide[6];

	public AbstractCableTile(TileEntityType<?> type, Capability<T> cap) {
		super(type);
		this.cap = cap;
	}

	@Override
	public void tick() {
		assert level != null;
		if (!level.isClientSide) {
			if (--cooldown > 0) {
				return;
			}
			cooldown = 4;
			byte sf = (byte) (sideFlag.getAndSet(0) ^ 63);
			int i = 0;
			final Direction[] dirs = Direction.values();
			while (i < 6 && sf != 0) {
				if ((sf & 1) != 0) {
					updateSide(dirs[i]);
				}
				sf >>= 1;
				i++;
			}
		}
	}

	public LazyOptional<T> getSide(Direction dir) {
		return sides[dir.get3DDataValue()].lazyCast();
	}

	protected abstract void updateSide(Direction dir);

	@Nonnull
	@Override
	public <X> LazyOptional<X> getCapability(@Nonnull Capability<X> cap, @Nullable Direction side) {
		if (!remove && cap == this.cap && side != null) {
			return sides[side.get3DDataValue()].lazyCast();
		} else {
			return super.getCapability(cap, side);
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		for (AbstractSide<T> s : sides) {
			s.invalidate();
		}
	}
}
