package louis.omoshiroikamo.common;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import louis.omoshiroikamo.common.block.ModBlocks;
import louis.omoshiroikamo.common.core.lib.LibMisc;
import louis.omoshiroikamo.common.fluid.FluidRegister;
import louis.omoshiroikamo.common.fluid.material.FluidMaterialRegister;
import louis.omoshiroikamo.common.item.ModItems;

public class OKCreativeTab extends CreativeTabs {

    public static final CreativeTabs INSTANCE = new OKCreativeTab();
    private static final Random rand = new Random();
    List<ItemStack> list;

    public OKCreativeTab() {
        super(LibMisc.MOD_ID);
    }

    @Override
    public ItemStack getIconItemStack() {
        return new ItemStack(ModItems.itemMaterial);
    }

    @Override
    public Item getTabIconItem() {
        return getIconItemStack().getItem();
    }

    @Override
    public boolean hasSearchBar() {
        return true;
    }

    @Override
    public void displayAllReleventItems(List<ItemStack> list) {
        this.list = list;

        addItem(ModItems.itemMaterial);
        addItem(ModItems.itemWireCoil);
        addItem(FluidMaterialRegister.itemBucketMaterial);
        addItem(FluidRegister.itemBucketFluid);
        addBlock(ModBlocks.blockMaterial);
    }

    private void addItem(Item item) {
        item.getSubItems(item, this, list);
    }

    private void addBlock(Block block) {
        ItemStack stack = new ItemStack(block);
        block.getSubBlocks(stack.getItem(), this, list);
    }

    private void addStack(ItemStack stack) {
        list.add(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getTabLabel() {
        return LibMisc.MOD_ID;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getTranslatedTabLabel() {
        return LibMisc.MOD_ID;
    }
}
