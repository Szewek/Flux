package szewek.flux.tile.part;

public class MachineEnergy extends EnergyPart {

    public MachineEnergy(int max) {
        super(max);
    }

    public boolean use(int amount) {
        boolean b = stored >= amount;
        if (b) {
            stored -= amount;
        }
        return b;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (maxReceive <= 0) return 0;
        int r = Math.min(maxReceive, max - stored);
        if (r > 0 && !simulate) {
            stored += r;
        }
        return r;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }
}
