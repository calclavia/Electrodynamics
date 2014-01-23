package resonantinduction.core.render;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import resonantinduction.archaic.imprint.ItemBlockImprint;
import resonantinduction.core.prefab.tile.TileEntityFilterable;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.render.RenderUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Briman0094
 */
@SideOnly(Side.CLIENT)
public abstract class RenderImprintable extends TileEntitySpecialRenderer
{
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float var8)
	{
		if (tileEntity != null)
		{
			if (tileEntity instanceof TileEntityFilterable)
			{
				TileEntityFilterable tileFilterable = (TileEntityFilterable) tileEntity;

				ItemStack filter = tileFilterable.getFilter();

				if (filter != null)
				{
					EntityPlayer player = Minecraft.getMinecraft().thePlayer;
					MovingObjectPosition objectPosition = player.rayTrace(8, 1);

					if (objectPosition != null)
					{
						if (objectPosition.blockX == tileFilterable.xCoord && objectPosition.blockY == tileFilterable.yCoord && objectPosition.blockZ == tileFilterable.zCoord)
						{
							Set<ItemStack> filters = ItemBlockImprint.getFilters(filter);

							int i = 0;
							for (ItemStack filterStack : filters)
							{
								if (((TileEntityFilterable) tileEntity).isInverted())
								{
									RenderUtility.renderFloatingText(filterStack.getTooltip(player, Minecraft.getMinecraft().gameSettings.advancedItemTooltips).get(0).toString(), new Vector3(x, y, z).translate(0.5, i * 0.25f - 1f, z + 0.5f), 0xFF8888);
								}
								else
								{
									RenderUtility.renderFloatingText(filterStack.getTooltip(player, Minecraft.getMinecraft().gameSettings.advancedItemTooltips).get(0).toString(), new Vector3(x, y, z).translate(0.5, i * 0.25f - 1f, z + 0.5f), 0x88FF88);
								}
								i++;
							}
						}
					}
				}
			}
		}
	}
}
