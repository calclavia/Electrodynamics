package mffs.item;

import resonant.api.mffs.EventForceManipulate.EventPostForceManipulate;
import resonant.api.mffs.EventForceManipulate.EventPreForceManipulate;
import resonant.api.mffs.card.ICoordLink;
import resonant.api.mffs.fortron.FrequencyGrid;
import resonant.api.mffs.fortron.IFortronFrequency;
import resonant.api.mffs.security.Permission;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.MFFSHelper;
import mffs.ModularForceFieldSystem;
import mffs.item.card.ItemCardFrequency;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fluids.FluidContainerRegistry;
import resonant.lib.utility.LanguageUtility;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import universalelectricity.core.transform.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemRemoteController extends ItemCardFrequency implements ICoordLink
{
	private final Set<ItemStack> remotesCached = new HashSet<ItemStack>();
	private final Set<ItemStack> temporaryRemoteBlacklist = new HashSet<ItemStack>();

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
				list.add(LanguageUtility.getLocal("info.item.linkedWith") + " " + Block.blocksList[blockId].getLocalizedName());
			}

			list.add(vec.xi() + ", " + vec.yi() + ", " + vec.zi());
			list.add(LanguageUtility.getLocal("info.item.dimension") + " '" + vec.world.provider.getDimensionName() + "'");
		}
		else
		{
			list.add(LanguageUtility.getLocal("info.item.notLinked"));
		}
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
	{
		if (!world.isRemote && player.isSneaking())
		{
			VectorWorld vector = new VectorWorld(world, x, y, z);
			this.setLink(itemStack, vector);

			if (Block.blocksList[vector.getBlockID(world)] != null)
			{
				player.addChatMessage(LanguageUtility.getLocal("message.remoteController.linked").replaceAll("%p", x + ", " + y + ", " + z).replaceAll("%q", Block.blocksList[vector.getBlockID(world)].getLocalizedName()));
			}
		}

		return true;
	}

	public boolean hasLink(ItemStack itemStack)
	{
		return getLink(itemStack) != null;
	}

	@Override
	public VectorWorld getLink(ItemStack itemStack)
	{
		if (itemStack.stackTagCompound == null || !itemStack.getTagCompound().hasKey("link"))
		{
			return null;
		}

		return new VectorWorld(itemStack.getTagCompound().getCompoundTag("link"));
	}

	@Override
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
					Chunk chunk = world.getChunkFromBlockCoords(position.xi(), position.zi());

					if (chunk != null && chunk.isChunkLoaded && (MFFSHelper.hasPermission(world, position, Action.RIGHT_CLICK_BLOCK, entityPlayer) || MFFSHelper.hasPermission(world, position, Permission.REMOTE_CONTROL, entityPlayer)))
					{
						float requiredEnergy = (float) Vector3.distance(new Vector3(entityPlayer), position) * (FluidContainerRegistry.BUCKET_VOLUME / 100);
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
									Block.blocksList[blockId].onBlockActivated(world, position.xi(), position.yi(), position.zi(), entityPlayer, 0, 0, 0, 0);
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
							entityPlayer.addChatMessage(LanguageUtility.getLocal("message.remoteController.fail").replaceAll("%p", UnitDisplay.getDisplay(requiredEnergy, Unit.JOULES)));
						}
					}
				}
			}
		}

		return itemStack;
	}

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
