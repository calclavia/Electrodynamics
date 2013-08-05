package resonantinduction.entangler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import resonantinduction.base.Vector3;

/**
 * 
 * @author AidanBrady
 * 
 */
public class ItemQuantumEntangler extends ItemCoordLink
{
	private static boolean didBindThisTick = false;

	public ItemQuantumEntangler(int id)
	{
		super("quantumEntangler", id);
		setMaxStackSize(1);
		// TODO Handheld model, render animation, energy usage (should be easy?)
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int side, float posX, float posY, float posZ)
	{
		if (!world.isRemote)
		{
			if (world.isAirBlock(x, y + 1, z) && world.isAirBlock(x, y + 2, z))
			{
				int dimID = world.provider.dimensionId;

				entityplayer.addChatMessage("Bound Entangler to block [" + x + ", " + y + ", " + z + "], dimension '" + dimID + "'");
				setLink(itemstack, new Vector3(x, y, z), dimID);
				didBindThisTick = true;

				return true;
			}

			entityplayer.addChatMessage("Error: invalid block for binding!");
		}

		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		if (!world.isRemote && !didBindThisTick)
		{
			if (!hasLink(itemstack))
			{
				entityplayer.addChatMessage("Error: no block bound to Entangler!");
				return itemstack;
			}

			// TELEPORT //

			Vector3 vec = getLink(itemstack);
			int dimID = getLinkDim(itemstack);

			// travel to dimension if different dimID
			if (world.provider.dimensionId != dimID)
			{
				((EntityPlayerMP) entityplayer).travelToDimension(dimID);
			}

			// actually teleport to new coords
			((EntityPlayerMP) entityplayer).playerNetServerHandler.setPlayerLocation(vec.x + 0.5, vec.y + 1, vec.z + 0.5, entityplayer.rotationYaw, entityplayer.rotationPitch);

			world.playSoundAtEntity(entityplayer, "mob.endermen.portal", 1.0F, 1.0F);
		}

		didBindThisTick = false;

		return itemstack;
	}

}
