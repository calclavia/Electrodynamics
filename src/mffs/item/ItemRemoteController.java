package mffs.item;

import java.util.List;

import mffs.MFFSHelper;
import mffs.ModularForceFieldSystem;
import mffs.api.card.ICardLink;
import mffs.base.ItemBase;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import universalelectricity.core.vector.Vector3;

public class ItemRemoteController extends ItemBase implements ICardLink
{
	public ItemRemoteController(int id)
	{
		super(id, "remoteController");
		this.setMaxStackSize(1);
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean b)
	{
		super.addInformation(itemStack, player, list, b);

		Vector3 position = this.getLink(itemStack);

		if (position != null)
		{
			int blockId = position.getBlockID(player.worldObj);

			if (Block.blocksList[blockId] != null)
			{
				list.add("Linked with: " + Block.blocksList[blockId].getLocalizedName());
				list.add(position.intX() + ", " + position.intY() + ", " + position.intZ());
				return;
			}
		}

		list.add("Not linked.");
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer entityPlayer, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
	{
		if (entityPlayer.isSneaking())
		{
			if (!world.isRemote)
			{
				Vector3 vector = new Vector3(x, y, z);

				this.setLink(itemStack, vector);

				if (Block.blocksList[vector.getBlockID(world)] != null)
				{
					entityPlayer.addChatMessage("Linked remote to position: " + x + ", " + y + ", " + z + " with block: " + Block.blocksList[vector.getBlockID(world)].getLocalizedName());
				}

			}

			return true;
		}

		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer)
	{
		if (!entityPlayer.isSneaking())
		{
			Vector3 position = this.getLink(itemStack);

			if (position != null)
			{
				int blockId = position.getBlockID(world);

				if (Block.blocksList[blockId] != null)
				{
					int requiredEnergy = (int) Vector3.distance(new Vector3(entityPlayer), position) * (LiquidContainerRegistry.BUCKET_VOLUME / 10);

					Chunk chunk = world.getChunkFromBlockCoords(position.intX(), position.intZ());

					if (chunk != null && chunk.isChunkLoaded)
					{
						try
						{
							Block.blocksList[blockId].onBlockActivated(world, position.intX(), position.intY(), position.intZ(), entityPlayer, 0, 0, 0, 0);

							if (!world.isRemote)
							{
								ModularForceFieldSystem.proxy.renderBeam(world, new Vector3(entityPlayer).add(new Vector3(0, entityPlayer.getEyeHeight(), 0)), position.add(0.5), 0.6f, 0.6f, 1, 20);
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}

		return itemStack;
	}

	@Override
	public void setLink(ItemStack itemStack, Vector3 position)
	{
		NBTTagCompound nbt = MFFSHelper.getNBTTagCompound(itemStack);
		nbt.setCompoundTag("position", position.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public Vector3 getLink(ItemStack itemStack)
	{
		NBTTagCompound nbt = MFFSHelper.getNBTTagCompound(itemStack);
		return Vector3.readFromNBT(nbt.getCompoundTag("position"));
	}
}
