package szewek.flux.tile.cable;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import szewek.flux.energy.EnergyCache;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

public final class EnergyCableTile extends AbstractCableTile<IEnergyStorage> {
	private final AtomicInteger energy = new AtomicInteger();
	private final EnergyCache energyCache = new EnergyCache(this);

	public EnergyCableTile(TileEntityType<EnergyCableTile> type) {
		super(type, CapabilityEnergy.ENERGY);
		for(int i = 0; i < 6; i++) {
			sides[i] = new EnergyCableTile.Side(i, sideFlag, energy);
		}
	}

	@Override
	public void load(BlockState blockState, CompoundNBT compound) {
		super.load(blockState, compound);
		energy.set(MathHelper.clamp(compound.getInt("E"), 0, 50000));
	}

	@Override
	public CompoundNBT save(CompoundNBT compound) {
		super.save(compound);
		compound.putInt("E", energy.get());
		return compound;
	}

	@Override
	protected void updateSide(Direction dir) {
		try {
			IEnergyStorage ie = energyCache.getCached(dir);
			if (ie != null) {
				int r;
				int n = energy.get();
				if (ie instanceof Side) {
					r = (n - ie.getEnergyStored()) / 2;
					if (r != 0) {
						energy.addAndGet(-r);
						((Side) ie).syncEnergy(r);
					}
				} else if (ie.canReceive()) {
					r = 10000;
					if (r >= n) {
						r = n;
					}
					r = ie.receiveEnergy(r, true);
					if (r > 0) {
						energy.addAndGet(-r);
						ie.receiveEnergy(r, false);
					}
				}
			}
		} catch (Exception ignored) {
			// Keep garbage "integrations" away from my precious Energy Cable!
			energyCache.clear();
			// A good mod developer ALWAYS invalidates LazyOptional instances!
		}
	}

	@Override
	public void remove() {
		super.remove();
		energyCache.clear();
	}

	public static final class Side extends AbstractSide<IEnergyStorage> implements IEnergyStorage {
		private final AtomicInteger energy;

		private Side(int i, AtomicInteger sf, AtomicInteger energy) {
			super(i, sf);
			this.energy = energy;
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			int r = maxReceive;
			if (r > 0) {
				int n = 50000 - energy.get();
				if (r > n) {
					r = n;
				}
				if (!simulate) {
					energy.addAndGet(r);
					update();
				}
			}
			return r;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			int r = maxExtract;
			if (r > 0) {
				int n = energy.get();
				if (r > n) {
					r = n;
				}
				if (!simulate) {
					energy.addAndGet(-r);
					update();
				}
			}
			return r;
		}

		private void syncEnergy(int diff) {
			energy.addAndGet(diff);
			update();
		}

		@Override
		public int getEnergyStored() {
			return energy.get();
		}

		@Override
		public int getMaxEnergyStored() {
			return 50000;
		}

		@Override
		public boolean canExtract() {
			return true;
		}

		@Override
		public boolean canReceive() {
			return true;
		}

		@Nonnull
		@Override
		public IEnergyStorage get() {
			return this;
		}
	}
}
