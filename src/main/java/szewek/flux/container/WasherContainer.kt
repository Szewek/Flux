package szewek.flux.container

import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.container.Slot
import net.minecraft.network.PacketBuffer
import net.minecraft.util.IIntArray
import szewek.flux.FContainers
import szewek.flux.FRecipes

class WasherContainer : AbstractMachineContainer {
    constructor(id: Int, playerInventoryIn: PlayerInventory, data: PacketBuffer?) : super(FContainers.WASHER, FRecipes.WASHING, id, playerInventoryIn, 2, 1) {}
    constructor(id: Int, playerInventoryIn: PlayerInventory, machineInventoryIn: IInventory, dataIn: IIntArray) : super(FContainers.WASHER, FRecipes.WASHING, id, playerInventoryIn, 2, 1, machineInventoryIn, dataIn) {}

    override fun initSlots(playerInventory: PlayerInventory) {
        addSlot(Slot(machineInventory, 0, 56, 26))
        addSlot(Slot(machineInventory, 1, 56, 44))
        addSlot(MachineResultSlot(playerInventory.player, machineInventory, 2, 116, 35))
        initPlayerSlotsAt(playerInventory, 8, 84)
    }
}