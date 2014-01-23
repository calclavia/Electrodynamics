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
import resonantinduction.core.Reference;
import resonantinduction.core.Settings;
import resonantinduction.core.prefab.block.BlockIOBase;
import resonantinduction.core.render.RIBlockRenderingHandler;
import universalelectricity.api.CompatibilityModule;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A block that detects power.
 * 
 * @author Calclavia
 * 
 */
public class BlockBattery extends BlockIOBase implements ITileEntityProvider
{
	public BlockBattery()
	{
		super("battery", Settings.getNextBlockID());
		this.setTextureName(Reference.PREFIX + "machine");
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		if (!world.isRemote)
		{
			TileBattery battery = (TileBattery) world.getBlockTileEntity(x, y, z);
			battery.updateStructure();
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemStack)
	{
		if (!world.isRemote && itemStack.getItem() instanceof ItemBlockBattery)
		{
			ItemBlockBattery itemBlock = (ItemBlockBattery) itemStack.getItem();
			TileBattery battery = (TileBattery) world.getBlockTileEntity(x, y, z);
			battery.energy.setCapacity(TileBattery.getEnergyForTier(itemBlock.getTier(itemStack)));
			battery.energy.setEnergy(itemBlock.getEnergy(itemStack));
			battery.updateStructure();
			world.setBlockMetadataWithNotify(x, y, z, itemBlock.getTier(itemStack), 3);
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int id)
	{
		if (!world.isRemote)
		{
			if (id == blockID)
			{
				TileBattery battery = (TileBattery) world.getBlockTileEntity(x, y, z);
				battery.updateStructure();
			}
		}
	}

	@Override
	public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
			dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
			world.setBlock(x, y, z, 0);
		}

		return true;
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
			itemBlock.setTier(itemStack, (byte) metadata);
			itemBlock.setEnergy(itemStack, battery.energy.getEnergy());
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
		return RIBlockRenderingHandler.INSTANCE.getRenderId();
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
		return CompatibilityModule.getItemWithCharge(new ItemStack(id, 1, getDamageValue(world, x, y, z)), battery.getEnergy(null));
	}
}
