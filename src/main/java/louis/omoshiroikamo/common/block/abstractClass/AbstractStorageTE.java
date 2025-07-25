package louis.omoshiroikamo.common.block.abstractClass;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.utils.item.ItemStackHandler;

import louis.omoshiroikamo.api.fluid.SmartTank;
import louis.omoshiroikamo.api.material.MaterialEntry;
import louis.omoshiroikamo.api.material.MaterialRegistry;
import louis.omoshiroikamo.common.block.abstractClass.machine.SlotDefinition;

public abstract class AbstractStorageTE extends AbstractTE implements ISidedInventory {

    protected final SlotDefinition slotDefinition;
    public ItemStackHandler inv;
    private final int[] allSlots;
    protected SmartTank[] fluidTanks;

    public AbstractStorageTE(SlotDefinition slotDefinition, MaterialEntry material) {
        this.slotDefinition = slotDefinition;
        this.material = material;
        inv = new ItemStackHandler(slotDefinition.getNumSlots());
        fluidTanks = new SmartTank[slotDefinition.getNumFluidSlots()];
        for (int i = 0; i < fluidTanks.length; i++) {
            if (material != null) {
                fluidTanks[i] = new SmartTank(material);
            } else {
                fluidTanks[i] = new SmartTank(8000);
            }
        }
        allSlots = new int[inv.getSlots()];
        for (int i = 0; i < allSlots.length; i++) {
            allSlots[i] = i;
        }
    }

    public AbstractStorageTE(SlotDefinition slotDefinition) {
        this(slotDefinition, MaterialRegistry.get("Iron"));
    }

    public SlotDefinition getSlotDefinition() {
        return slotDefinition;
    }

    @Override
    public void writeCommon(NBTTagCompound root) {
        root.setTag("item_inv", this.inv.serializeNBT());

        NBTTagCompound tanksTag = new NBTTagCompound();
        for (int i = 0; i < fluidTanks.length; i++) {
            NBTTagCompound tankTag = new NBTTagCompound();
            fluidTanks[i].writeToNBT(tankTag);
            tanksTag.setTag("Tank" + i, tankTag);
        }
        root.setTag("FluidTanks", tanksTag);

    }

    @Override
    public void readCommon(NBTTagCompound root) {
        this.inv.deserializeNBT(root.getCompoundTag("item_inv"));

        NBTTagCompound tanksTag = root.getCompoundTag("FluidTanks");
        for (int i = 0; i < fluidTanks.length; i++) {
            String key = "Tank" + i;
            if (tanksTag.hasKey(key)) {
                fluidTanks[i].readFromNBT(tanksTag.getCompoundTag(key));
            }
        }
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return allSlots;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemstack, int side) {
        ItemStack existing = inv.getStackInSlot(slot);
        if (existing != null) {
            return existing.isStackable() && existing.isItemEqual(itemstack);
        }
        return isItemValidForSlot(slot, itemstack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemstack, int side) {
        if (!slotDefinition.isOutputSlot(slot)) {
            return false;
        }
        ItemStack existing = inv.getStackInSlot(slot);
        if (existing == null || existing.stackSize < itemstack.stackSize) {
            return false;
        }
        return itemstack.getItem() == existing.getItem();
    }

    @Override
    public int getSizeInventory() {
        return inv.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= inv.getSlots()) {
            return null;
        }
        return inv.getStackInSlot(slot);
    }

    @Override
    public ItemStack decrStackSize(int fromSlot, int amount) {
        ItemStack fromStack = inv.getStackInSlot(fromSlot);
        if (fromStack == null) {
            return null;
        }
        if (fromStack.stackSize <= amount) {
            inv.setStackInSlot(fromSlot, null);
            return fromStack;
        }
        ItemStack result = fromStack.splitStack(amount);
        inv.setStackInSlot(fromSlot, fromStack.stackSize > 0 ? fromStack : null);
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack contents) {
        if (contents == null) {
            inv.setStackInSlot(slot, null);
        } else {
            inv.setStackInSlot(slot, contents.copy());
        }

        ItemStack stack = inv.getStackInSlot(slot);
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
            inv.setStackInSlot(slot, stack);
        }
    }

    @Override
    public String getInventoryName() {
        return getMachineName();
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return isMachineItemValidForSlot(slot, stack);
    }

    protected abstract boolean isMachineItemValidForSlot(int slot, ItemStack itemstack);
}
