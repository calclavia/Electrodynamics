/**
 * 
 */
package resonantinduction.core.handler;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import universalelectricity.api.vector.VectorWorld;
import calclavia.components.event.MultitoolEvent;
import codechicken.multipart.ControlKeyModifer;

/**
 * @author Calclavia
 */
public class LinkEventHandler
{
	@ForgeSubscribe
	public void linkEvent(MultitoolEvent evt)
	{
		if (ControlKeyModifer.isControlDown(evt.player))
		{
			TileEntity tile = evt.world.getBlockTileEntity(evt.x, evt.y, evt.z);

			if (tile instanceof ILinkable && this.hasLink(evt.toolStack))
			{
				if (!evt.world.isRemote)
				{
					if (((ILinkable) tile).onLink(evt.player, this.getLink(evt.toolStack)))
					{
						this.clearLink(evt.toolStack);
						evt.player.addChatMessage("Link cleared.");
					}
				}
				evt.setResult(Result.DENY);
			}
			else
			{
				if (!evt.world.isRemote)
				{
					evt.player.addChatMessage("Set link to block [" + evt.x + ", " + evt.y + ", " + evt.z + "], Dimension: '" + evt.world.provider.getDimensionName() + "'");
					this.setLink(evt.toolStack, new VectorWorld(evt.world, evt.x, evt.y, evt.z));
				}
			}

			evt.setCanceled(true);
		}
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
