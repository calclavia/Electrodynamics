/**
 * 
 */
package resonantinduction.electrical.battery;

import java.util.ArrayList;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import resonant.lib.prefab.block.BlockSidedIO;
import resonant.lib.render.block.BlockRenderingHandler;
import resonant.lib.utility.inventory.InventoryUtility;
import resonantinduction.core.Reference;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.UniversalElectricity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A block that detects power.
 * 
 * @author Calclavia
 */
public class BlockBattery extends BlockSidedIO implements ITileEntityProvider
{
	public BlockBattery(int id)
	{
		super(id, UniversalElectricity.machine);
		setTextureName(Reference.PREFIX + "material_metal_side");
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		if (!world.isRemote)
		{
			TileEnergyDistribution distribution = (TileEnergyDistribution) world.getBlockTileEntity(x, y, z);
			distribution.updateStructure();
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int id)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (!world.isRemote && tileEntity instanceof TileEnergyDistribution)
		{
			TileEnergyDistribution distribution = (TileEnergyDistribution) tileEntity;
			distribution.updateStructure();
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemStack)
	{
		if (!world.isRemote && itemStack.getItem() instanceof ItemBlockBattery)
		{
			ItemBlockBattery itemBlock = (ItemBlockBattery) itemStack.getItem();
			TileBattery battery = (TileBattery) world.getBlockTileEntity(x, y, z);
			battery.getEnergyHandler().setCapacity(TileBattery.getEnergyForTier(ItemBlockBattery.getTier(itemStack)));
			battery.getEnergyHandler().setEnergy(itemBlock.getEnergy(itemStack));
			battery.updateStructure();
			world.setBlockMetadataWithNotify(x, y, z, ItemBlockBattery.getTier(itemStack), 3);
		}
	}

	@Override
	public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			InventoryUtility.dropBlockAsItem(world, x, y, z, true);
		}
		return true;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6)
	{
		super.breakBlock(world, x, y, z, par5, par6);
	}

	@Override
	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune)
	{
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		ItemStack itemStack = new ItemStack(this, 1);

		if (world.getBlockTileEntity(x, y, z) instanceof TileBattery)
		{
			TileBattery battery = (TileBattery) world.getBlockTileEntity(x, y, z);
			ItemBlockBattery itemBlock = (ItemBlockBattery) itemStack.getItem();
			ItemBlockBattery.setTier(itemStack, (byte) world.getBlockMetadata(x, y, z));
			itemBlock.setEnergy(itemStack, battery.getEnergyHandler().getEnergy());
		}
		ret.add(itemStack);
		return ret;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderType()
	{
		return BlockRenderingHandler.INSTANCE.getRenderId();
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileBattery();
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		int id = idPicked(world, x, y, z);

		if (id == 0)
		{
			return null;
		}

		Item item = Item.itemsList[id];
		if (item == null)
		{
			return null;
		}

		TileBattery battery = (TileBattery) world.getBlockTileEntity(x, y, z);
		return CompatibilityModule.getItemWithCharge(ItemBlockBattery.setTier(new ItemStack(id, 1, 0), (byte) world.getBlockMetadata(x, y, z)), battery.getEnergyHandler().getEnergy());
	}
}
