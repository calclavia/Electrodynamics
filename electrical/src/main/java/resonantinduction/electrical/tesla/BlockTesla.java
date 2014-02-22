/**
 * 
 */
package resonantinduction.electrical.tesla;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.MultipartUtility;
import resonantinduction.core.Reference;
import resonantinduction.core.render.RIBlockRenderingHandler;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.vector.VectorWorld;
import calclavia.components.tool.ToolModeLink;
import calclavia.lib.prefab.block.BlockSidedIO;
import calclavia.lib.utility.LanguageUtility;
import calclavia.lib.utility.WrenchUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
public class BlockTesla extends BlockSidedIO implements ITileEntityProvider
{
	public BlockTesla(int id)
	{
		super(id, UniversalElectricity.machine);
		setTextureName(Reference.PREFIX + "material_metal_side");
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
		TileTesla tileEntity = ((TileTesla) t).getMultiBlock().get();

		if (entityPlayer.getCurrentEquippedItem() != null)
		{
			int dyeColor = MultipartUtility.isDye(entityPlayer.getCurrentEquippedItem());

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

			if (!world.isRemote)
			{
				entityPlayer.addChatMessage(LanguageUtility.getLocal("message.tesla.mode").replace("%v", receiveMode + ""));
			}

			return true;

		}

		return false;
	}

	@Override
	public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (tile instanceof TileTesla)
		{
			ItemStack itemStack = player.getCurrentEquippedItem();

			if (WrenchUtility.isWrench(itemStack))
			{
				if (((TileTesla)tile).tryLink(ToolModeLink.getLink(itemStack)))
				{
					if (world.isRemote)
						player.addChatMessage("Successfully linked devices.");
					ToolModeLink.clearLink(itemStack);
				}
				else
				{
					if (world.isRemote)
						player.addChatMessage("Marked link for device.");

					ToolModeLink.setLink(itemStack, new VectorWorld(world, x, y, z));
				}

				return true;
			}
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
		return RIBlockRenderingHandler.INSTANCE.getRenderId();
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
