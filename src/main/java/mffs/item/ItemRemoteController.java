package mffs.item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mffs.api.EventForceManipulate.EventPostForceManipulate;
import mffs.api.EventForceManipulate.EventPreForceManipulate;
import mffs.api.card.ICoordLink;
import mffs.item.card.ItemCardFrequency;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemRemoteController extends ItemCardFrequency implements ICoordLink
{
	private final Set<ItemStack> remotesCached = new HashSet<ItemStack>();

	public ItemRemoteController(int id)
	{
		super("remoteController", id);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		super.addInformation(itemstack, entityplayer, list, flag);

		if (hasLink(itemstack))
		{
			VectorWorld vec = getLink(itemstack);
			int blockId = vec.getBlockID(entityplayer.worldObj);

			if (Block.blocksList[blockId] != null)
			{
				list.add("Linked with: " + Block.blocksList[blockId].getLocalizedName());
			}

			list.add(vec.intX() + ", " + vec.intY() + ", " + vec.intZ());
			list.add("Dimension: '" + vec.world.provider.getDimensionName() + "'");
		}
		else
		{
			list.add("Not linked.");
		}
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
	{
		if (!world.isRemote)
		{
			VectorWorld vector = new VectorWorld(world, x, y, z);
			this.setLink(itemStack, vector);

			if (Block.blocksList[vector.getBlockID(world)] != null)
			{
				player.addChatMessage("Linked to position: " + x + ", " + y + ", " + z + " with block: " + Block.blocksList[vector.getBlockID(world)].getLocalizedName());
			}
		}

		return true;
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

	private final Set<ItemStack> temporaryRemoteBlacklist = new HashSet<ItemStack>();

	@ForgeSubscribe
	public void preMove(EventPreForceManipulate evt)
	{
		this.temporaryRemoteBlacklist.clear();
	}

	/**
	 * Moves the coordinates of the link if the Force Manipulator moved a block that is linked by
	 * the remote.
	 * 
	 * @param evt
	 */
	@ForgeSubscribe
	public void onMove(EventPostForceManipulate evt)
	{
		if (!evt.world.isRemote)
		{
			for (ItemStack itemStack : this.remotesCached)
			{
				if (!temporaryRemoteBlacklist.contains(itemStack) && new Vector3(evt.beforeX, evt.beforeY, evt.beforeZ).equals(this.getLink(itemStack)))
				{
					// TODO: Change remote to locate in other world?
					this.setLink(itemStack, new VectorWorld(evt.world, evt.afterX, evt.afterY, evt.afterZ));
					temporaryRemoteBlacklist.add(itemStack);
				}
			}
		}
	}
}
