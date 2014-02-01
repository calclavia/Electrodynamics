/**
 * 
 */
package resonantinduction.core.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.api.vector.VectorWorld;
import calclavia.components.tool.ToolMode;
import codechicken.multipart.ControlKeyModifer;

/**
 * @author Calclavia
 */
public class ToolModeLink extends ToolMode
{
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (ControlKeyModifer.isControlDown(player))
		{
			if (tile instanceof ILinkable)
			{
				if (!world.isRemote)
				{
					if (((ILinkable) tile).onLink(player, this.getLink(stack)))
					{
						clearLink(stack);
						player.addChatMessage("Link cleared.");
						return true;
					}
				}
			}
		}

		if (!world.isRemote)
		{
			player.addChatMessage("Set link to block [" + x + ", " + y + ", " + z + "], Dimension: '" + world.provider.getDimensionName() + "'");
			setLink(stack, new VectorWorld(world, x, y, z));

			if (tile instanceof ILinkable)
			{
				if (!world.isRemote)
				{
					((ILinkable) tile).onLink(player, this.getLink(stack));
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public String getName()
	{
		return "toolmode.link.name";
	}

	public boolean hasLink(ItemStack itemStack)
	{
		return getLink(itemStack) != null;
	}

	public VectorWorld getLink(ItemStack itemStack)
	{
		if (itemStack.stackTagCompound == null || !itemStack.getTagCompound().hasKey("link"))
		{
			return null;
		}

		return new VectorWorld(itemStack.getTagCompound().getCompoundTag("link"));
	}

	public void setLink(ItemStack itemStack, VectorWorld vec)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.getTagCompound().setCompoundTag("link", vec.writeToNBT(new NBTTagCompound()));
	}

	public void clearLink(ItemStack itemStack)
	{
		itemStack.getTagCompound().removeTag("link");
	}

}
