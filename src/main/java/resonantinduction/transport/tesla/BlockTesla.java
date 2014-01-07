/**
 * 
 */
package resonantinduction.transport.tesla;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.ResonantInduction;
import resonantinduction.Utility;
import resonantinduction.core.base.BlockIOBase;
import resonantinduction.core.render.BlockRenderingHandler;
import resonantinduction.transport.levitator.TileEMLevitator;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;
import calclavia.lib.prefab.item.ItemCoordLink;
import calclavia.lib.utility.LanguageUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
public class BlockTesla extends BlockIOBase implements ITileEntityProvider
{
	public BlockTesla(int id)
	{
		super("tesla", id);
		this.setTextureName(ResonantInduction.PREFIX + "machine");
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		super.onBlockAdded(world, x, y, z);
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		((TileTesla) tileEntity).updatePositionStatus();
	}

	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity t = world.getBlockTileEntity(x, y, z);
		TileTesla tileEntity = ((TileTesla) t).getControllingTelsa();

		if (entityPlayer.getCurrentEquippedItem() != null)
		{
			int dyeColor = Utility.isDye(entityPlayer.getCurrentEquippedItem());

			if (dyeColor != -1)
			{
				tileEntity.setDye(dyeColor);

				if (!entityPlayer.capabilities.isCreativeMode)
				{
					entityPlayer.inventory.decrStackSize(entityPlayer.inventory.currentItem, 1);
				}

				return true;
			}
			else if (entityPlayer.getCurrentEquippedItem().itemID == Item.redstone.itemID)
			{
				boolean status = tileEntity.toggleEntityAttack();

				if (!entityPlayer.capabilities.isCreativeMode)
				{
					entityPlayer.inventory.decrStackSize(entityPlayer.inventory.currentItem, 1);
				}

				if (!world.isRemote)
				{
					entityPlayer.addChatMessage(LanguageUtility.getLocal("message.tesla.toggleAttack").replace("%v", status + ""));
				}

				return true;
			}
		}
		else
		{
			boolean receiveMode = tileEntity.toggleReceive();

			if (world.isRemote)
			{
				entityPlayer.addChatMessage(LanguageUtility.getLocal("message.tesla.mode").replace("%v", receiveMode + ""));
			}

			return true;

		}

		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int id)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		((TileTesla) tileEntity).updatePositionStatus();
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileTesla();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return BlockRenderingHandler.INSTANCE.getRenderId();
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

}
