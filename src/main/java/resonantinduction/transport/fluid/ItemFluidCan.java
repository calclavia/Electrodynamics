package resonantinduction.transport.fluid;

import java.util.List;

import resonantinduction.AssemblyLine;
import resonantinduction.ResonantInductionTabs;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ItemFluidContainer;

import com.builtbroken.minecraft.DarkCore;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Small fluid can that is designed to store up to one bucket of fluid. Doesn't work like a bucket
 * as it is sealed with a pressure cap. This can is designed to work with tools or machines only.
 * 
 * @author DarkGuardsman */
public class ItemFluidCan extends ItemFluidContainer
{
    public static final String FLUID_NBT = "FluidStack";

    @SideOnly(Side.CLIENT)
    public Icon[] icons;

    public ItemFluidCan()
    {
        super(ResonantInductionTransport.CONFIGURATION.getItem("FluidCan", DarkCore.getNextItemId()).getInt());
        this.setUnlocalizedName("FluidCan");
        this.setCreativeTab(ResonantInductionTabs.tabHydraulic());
        this.setMaxStackSize(1);
        this.setMaxDamage(100);
        this.setNoRepair();
        this.capacity = FluidContainerRegistry.BUCKET_VOLUME * 2;
        this.setContainerItem(this);
    }

    @Override
    public boolean doesContainerItemLeaveCraftingGrid(ItemStack par1ItemStack)
    {
        FluidStack fluidStack = this.drain(par1ItemStack, Integer.MAX_VALUE, false);
        if (fluidStack != null)
        {
            return false;
        }
        return true;
    }

    @Override
    public String getItemDisplayName(ItemStack par1ItemStack)
    {
        String fluid = "";
        FluidStack fluidStack = this.drain(par1ItemStack, Integer.MAX_VALUE, false);
        if (fluidStack != null)
        {
            fluid = fluidStack.getFluid().getLocalizedName();
        }
        return ("" + (fluid + " " + StatCollector.translateToLocal(this.getUnlocalizedNameInefficiently(par1ItemStack) + ".name"))).trim();
    }

    @Override
    public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
    {
        FluidStack fluidStack = this.drain(par1ItemStack, Integer.MAX_VALUE, false);
        if (fluidStack != null)
        {
            par3List.add("Volume: " + fluidStack.amount + "mb");
        }
    }

    @Override
    public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        par3List.add(new ItemStack(this));

        ItemStack waterCan = new ItemStack(this);
        this.fill(waterCan, new FluidStack(FluidRegistry.WATER, FluidContainerRegistry.BUCKET_VOLUME), true);
        par3List.add(waterCan);

        ItemStack lavaCan = new ItemStack(this);
        this.fill(lavaCan, new FluidStack(FluidRegistry.LAVA, FluidContainerRegistry.BUCKET_VOLUME), true);
        par3List.add(lavaCan);
    }
}
