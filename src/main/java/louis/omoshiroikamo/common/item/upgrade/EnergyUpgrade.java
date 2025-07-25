package louis.omoshiroikamo.common.item.upgrade;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import louis.omoshiroikamo.api.client.SpecialTooltipHandler;
import louis.omoshiroikamo.api.energy.PowerDisplayUtil;
import louis.omoshiroikamo.api.mana.IManaItem;
import louis.omoshiroikamo.common.config.Config;
import louis.omoshiroikamo.common.core.lib.LibMisc;

public class EnergyUpgrade extends AbstractUpgrade {

    public static final AbstractUpgrade EMPOWERED = new EnergyUpgrade(
        LibMisc.MOD_ID + ".mana.upgrade.empowered_one",
        Config.manaUpgradeDiamondCost,
        new ItemStack(Items.diamond),
        Config.manaPowerStorageBase,
        Config.manaPowerStorageBase / 100);

    private static final String UPGRADE_NAME = "energyUpgrade";
    private static final String KEY_CAPACITY = "capacity";
    private static final String KEY_ENERGY = "energy";
    private static final String KEY_ABS_WITH_POWER = "absDamWithPower";
    private static final String KEY_MAX_IN = "maxInput";
    private static final String KEY_MAX_OUT = "maxOuput";

    private static final Random RANDOM = new Random();
    protected int capacity;
    protected int energy;
    protected int maxInRF;
    protected int maxOutRF;

    public EnergyUpgrade(String name, int levels, ItemStack upgradeItem, int capcity, int maxReceiveIO) {
        super(UPGRADE_NAME, name, upgradeItem, levels);
        capacity = capcity;
        energy = 0;
        maxInRF = maxReceiveIO;
        maxOutRF = maxReceiveIO;
    }

    public EnergyUpgrade(NBTTagCompound tag) {
        super(UPGRADE_NAME, tag);
        capacity = tag.getInteger(KEY_CAPACITY);
        energy = tag.getInteger(KEY_ENERGY);
        maxInRF = tag.getInteger(KEY_MAX_IN);
        maxOutRF = tag.getInteger(KEY_MAX_OUT);
    }

    public static EnergyUpgrade loadFromItem(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        if (stack.stackTagCompound == null) {
            return null;
        }
        if (!stack.stackTagCompound.hasKey(KEY_UPGRADE_PREFIX + UPGRADE_NAME)) {
            return null;
        }
        return new EnergyUpgrade((NBTTagCompound) stack.stackTagCompound.getTag(KEY_UPGRADE_PREFIX + UPGRADE_NAME));
    }

    public static boolean itemHasAnyPowerUpgrade(ItemStack itemstack) {
        return loadFromItem(itemstack) != null;
    }

    public static AbstractUpgrade next(AbstractUpgrade upgrade) {
        if (upgrade == null) {
            return EMPOWERED;
        }
        return null;
    }

    public static int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
        EnergyUpgrade eu = EnergyUpgrade.loadFromItem(container);
        if (eu == null) {
            return 0;
        }
        int res = eu.extractEnergy(maxExtract, simulate);
        if (!simulate && res > 0) {
            eu.writeToItem(container);
        }
        return res;
    }

    public static int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
        EnergyUpgrade eu = EnergyUpgrade.loadFromItem(container);
        if (eu == null) {
            return 0;
        }
        int res = eu.receiveEnergy(maxReceive, simulate);
        if (!simulate && res > 0) {
            eu.writeToItem(container);
        }
        return res;
    }

    public static void setPowerLevel(ItemStack item, int amount) {
        if (item == null || !itemHasAnyPowerUpgrade(item)) {
            return;
        }
        amount = Math.min(amount, getMaxEnergyStored(item));
        EnergyUpgrade eu = loadFromItem(item);
        eu.setEnergy(amount);
        eu.writeToItem(item);
    }

    public static void setPowerFull(ItemStack item) {
        if (item == null || !itemHasAnyPowerUpgrade(item)) {
            return;
        }
        EnergyUpgrade eu = loadFromItem(item);
        eu.setEnergy(eu.getCapacity());
        eu.writeToItem(item);
    }

    public static String getStoredEnergyString(ItemStack itemstack) {
        EnergyUpgrade up = loadFromItem(itemstack);
        if (up == null) {
            return null;
        }
        return PowerDisplayUtil.formatStoredPower(up.energy, up.capacity);
    }

    public static int getEnergyStored(ItemStack container) {
        EnergyUpgrade eu = EnergyUpgrade.loadFromItem(container);
        if (eu == null) {
            return 0;
        }
        return eu.getEnergy();
    }

    public static int getMaxEnergyStored(ItemStack container) {
        EnergyUpgrade eu = EnergyUpgrade.loadFromItem(container);
        if (eu == null) {
            return 0;
        }
        return eu.getCapacity();
    }

    @Override
    public boolean hasUpgrade(ItemStack stack) {
        if (!super.hasUpgrade(stack)) {
            return false;
        }
        EnergyUpgrade up = loadFromItem(stack);
        if (up == null) {
            return false;
        }
        return up.unlocName.equals(unlocName);
    }

    @Override
    public boolean canAddToItem(ItemStack stack) {
        if (stack == null || stack.getItem() == null || !(stack.getItem() instanceof IManaItem)) {
            return false;
        }
        AbstractUpgrade up = next(loadFromItem(stack));
        if (up == null) {
            return false;
        }

        return up.unlocName.equals(unlocName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addDetailedEntries(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag) {

        List<String> upgradeStr = new ArrayList<String>();
        upgradeStr.add(EnumChatFormatting.DARK_AQUA + LibMisc.lang.localizeExact(getUnlocalizedName() + ".name"));
        SpecialTooltipHandler.addDetailedTooltipFromResources(upgradeStr, getUnlocalizedName());

        // String percDamage = (int) Math.round(getAbsorptionRatio(itemstack) * 100) + "";
        String capString = PowerDisplayUtil.formatPower(capacity) + " " + PowerDisplayUtil.abrevation();
        for (int i = 0; i < upgradeStr.size(); i++) {
            String str = upgradeStr.get(i);
            str = str.replaceAll("\\$P", capString);
            // str = str.replaceAll("\\$D", percDamage);
            upgradeStr.set(i, str);
        }
        list.addAll(upgradeStr);
    }

    @Override
    public void writeUpgradeToNBT(NBTTagCompound upgradeRoot) {
        upgradeRoot.setInteger(KEY_CAPACITY, capacity);
        upgradeRoot.setInteger(KEY_ENERGY, energy);

        upgradeRoot.setInteger(KEY_MAX_IN, maxInRF);
        upgradeRoot.setInteger(KEY_MAX_OUT, maxOutRF);
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int receiveEnergy(int maxRF, boolean simulate) {

        int energyReceived = Math.min(capacity - energy, Math.min(maxInRF, maxRF));
        if (!simulate) {
            energy += energyReceived;
        }
        return energyReceived;
    }

    public int extractEnergy(int maxExtract, boolean simulate) {
        int energyExtracted = Math.min(energy, Math.min(maxOutRF, maxExtract));
        if (!simulate) {
            energy -= energyExtracted;
        }
        return energyExtracted;
    }

    public int getCapacity() {
        return capacity;
    }
}
