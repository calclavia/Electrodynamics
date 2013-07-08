package mffs.item;

import java.util.List;
import java.util.Set;

import mffs.MFFSHelper;
import mffs.ModularForceFieldSystem;
import mffs.api.card.ICardLink;
import mffs.api.fortron.IFortronFrequency;
import mffs.api.security.Permission;
import mffs.fortron.FrequencyGrid;
import mffs.item.card.ItemCardFrequency;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fluids.FluidContainerRegistry;
import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import universalelectricity.core.vector.Vector3;

public class ItemRemoteController extends ItemCardFrequency implements ICardLink
{
	public ItemRemoteController(int id)
	{
		super("remoteController", id);
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
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if (player.isSneaking())
		{
			if (!world.isRemote)
			{
				Vector3 vector = new Vector3(x, y, z);

				this.setLink(itemStack, vector);

				if (Block.blocksList[vector.getBlockID(world)] != null)
				{
					player.addChatMessage("Linked remote to position: " + x + ", " + y + ", " + z + " with block: " + Block.blocksList[vector.getBlockID(world)].getLocalizedName());
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
					Chunk chunk = world.getChunkFromBlockCoords(position.intX(), position.intZ());

					if (chunk != null && chunk.isChunkLoaded && (MFFSHelper.hasPermission(world, position, Action.RIGHT_CLICK_BLOCK, entityPlayer) || MFFSHelper.hasPermission(world, position, Permission.REMOTE_CONTROL, entityPlayer)))
					{
						double requiredEnergy = Vector3.distance(new Vector3(entityPlayer), position) * (FluidContainerRegistry.BUCKET_VOLUME / 100);
						int receivedEnergy = 0;

						Set<IFortronFrequency> fortronTiles = FrequencyGrid.instance().getFortronTiles(world, new Vector3(entityPlayer), 50, this.getFrequency(itemStack));

						for (IFortronFrequency fortronTile : fortronTiles)
						{
							int consumedEnergy = fortronTile.requestFortron((int) Math.ceil(requiredEnergy / fortronTiles.size()), true);

							if (consumedEnergy > 0)
							{
								if (world.isRemote)
								{
									ModularForceFieldSystem.proxy.renderBeam(world, new Vector3(entityPlayer).add(new Vector3(0, entityPlayer.getEyeHeight() - 0.2, 0)), new Vector3((TileEntity) fortronTile).add(0.5), 0.6f, 0.6f, 1, 20);
								}

								receivedEnergy += consumedEnergy;
							}

							if (receivedEnergy >= requiredEnergy)
							{
								try
								{
									Block.blocksList[blockId].onBlockActivated(world, position.intX(), position.intY(), position.intZ(), entityPlayer, 0, 0, 0, 0);
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}

								return itemStack;
							}
						}

						if (!world.isRemote)
						{
							entityPlayer.addChatMessage("Unable to harness " + ElectricityDisplay.getDisplay(requiredEnergy, ElectricUnit.JOULES) + " from the Fortron field.");
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
