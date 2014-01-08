package resonantinduction.energy.battery;

import java.util.List;
import java.util.Set;

import resonantinduction.AssemblyLine;
import resonantinduction.CommonProxy;
import resonantinduction.ResonantInductionTabs;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.common.Pair;
import com.builtbroken.minecraft.helpers.HelperMethods;
import com.builtbroken.minecraft.prefab.BlockMachine;

/** Block for energy storage devices
 * 
 * @author Rseifert */
public class BlockEnergyStorage extends BlockMachine
{
    public BlockEnergyStorage()
    {
        super(ResonantInductionTransport.CONFIGURATION, "DMEnergyStorage", UniversalElectricity.machine);
        this.setCreativeTab(ResonantInductionTabs.tabIndustrial());
    }

    @Override
    public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
        Vector3 vec = new Vector3(x, y, z);
        int meta = vec.getBlockMetadata(world);
        if (side == (meta))
        {
            return this.iconOutput;
        }
        return vec.clone().modifyPositionFromSide(ForgeDirection.getOrientation(side)).getTileEntity(world) instanceof IConductor ? this.iconInput : this.blockIcon;

    }

    @Override
    public boolean onMachineActivated(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        int metadata = par1World.getBlockMetadata(x, y, z);

        if (!par1World.isRemote)
        {
            par5EntityPlayer.openGui(ResonantInductionTransport.instance, CommonProxy.GUI_BATTERY_BOX, par1World, x, y, z);

        }
        return true;
    }

    @Override
    public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote)
        {
            int metadata = world.getBlockMetadata(x, y, z);
            if (metadata >= 5)
            {
                world.setBlockMetadataWithNotify(x, y, z, 0, 3);
            }
            else
            {
                world.setBlockMetadataWithNotify(x, y, z, metadata + 1, 3);
            }
        }
        return true;
    }

    @Override
    public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote)
        {
            world.setBlockMetadataWithNotify(x, y, z, side, 3);
        }
        return true;
    }

    @Override
    public boolean onSneakMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote)
        {
            ItemStack batBoxStack = ItemBlockEnergyStorage.getWrenchedBatteryBox(world, new Vector3(x, y, z));
            if (entityPlayer.getHeldItem() == null)
            {
                entityPlayer.inventory.setInventorySlotContents(entityPlayer.inventory.currentItem, batBoxStack);
            }
            else
            {
                HelperMethods.dropItemStack(world, new Vector3(x, y, z), batBoxStack, false);
            }
            world.setBlockToAir(x, y, z);
        }
        return true;
    }

    @Override
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        list.add(new Pair<String, Class<? extends TileEntity>>("DCTileBatBox", TileEntityBatteryBox.class));

    }

    @Override
    public TileEntity createTileEntity(World world, int metadata)
    {
        return new TileEntityBatteryBox();
    }

    @Override
    public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        par3List.add(new ItemStack(par1, 1, 0));
    }

    @Override
    public int damageDropped(int metadata)
    {
        return 0;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
    {
        return new ItemStack(this, 1, 0);
    }

}
