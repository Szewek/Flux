package szewek.flux.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import szewek.flux.F;

public final class CompactorContainer extends Machine2For1Container {
	public CompactorContainer(int id, PlayerInventory playerInventoryIn, PacketBuffer data) {
		super(F.Containers.COMPACTOR, F.Recipes.COMPACTING, id, playerInventoryIn, 2, 1);
	}

	public CompactorContainer(int id, PlayerInventory playerInventoryIn, IInventory machineInventoryIn, IIntArray dataIn) {
		super(F.Containers.COMPACTOR, F.Recipes.COMPACTING, id, playerInventoryIn, 2, 1, machineInventoryIn, dataIn);
	}
}
