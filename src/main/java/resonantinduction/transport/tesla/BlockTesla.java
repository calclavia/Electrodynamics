/**
 * 
 */
package resonantinduction.transport.tesla;

import calclavia.lib.prefab.TranslationHelper;
import calclavia.lib.prefab.item.ItemCoordLink;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.ResonantInduction;
import resonantinduction.Utility;
import resonantinduction.core.base.BlockBase;
import resonantinduction.core.render.BlockRenderingHandler;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
public class BlockTesla extends BlockBase implements ITileEntityProvider
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
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
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
					entityPlayer.addChatMessage(TranslationHelper.getLocal("message.tesla.toggleAttack").replace("%v", status + ""));
				}

				return true;
			}
			else if (entityPlayer.getCurrentEquippedItem().getItem() instanceof ItemCoordLink)
			{
				if (tileEntity.linked == null)
				{
					ItemCoordLink link = ((ItemCoordLink) entityPlayer.getCurrentEquippedItem().getItem());
					VectorWorld linkVec = link.getLink(entityPlayer.getCurrentEquippedItem());

					if (linkVec != null)
					{
						if (!world.isRemote)
						{
							World otherWorld = linkVec.world;

							if (linkVec.getTileEntity(otherWorld) instanceof TileTesla)
							{
								tileEntity.setLink(new Vector3(((TileTesla) linkVec.getTileEntity(otherWorld)).getTopTelsa()), linkVec.world.provider.dimensionId, true);

								entityPlayer.addChatMessage(TranslationHelper.getLocal("message.tesla.pair").replace("%v0", this.getLocalizedName()).replace("%v1", linkVec.x + "").replace("%v2", linkVec.y + "").replace("%v3", linkVec.z + ""));

								link.clearLink(entityPlayer.getCurrentEquippedItem());
								world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "ambient.weather.thunder", 5, 1);

								return true;
							}
						}
					}
				}
				else
				{
					tileEntity.setLink(null, world.provider.dimensionId, true);

					if (!world.isRemote)
					{
						entityPlayer.addChatMessage("Unlinked Tesla.");
					}

					return true;
				}
			}
		}
		else
		{
			boolean receiveMode = tileEntity.toggleReceive();

			if (world.isRemote)
			{
				entityPlayer.addChatMessage(TranslationHelper.getLocal("message.tesla.mode").replace("%v", receiveMode + ""));
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
