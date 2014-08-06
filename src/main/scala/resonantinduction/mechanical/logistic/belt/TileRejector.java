package resonantinduction.mechanical.logistic.belt;

import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.api.IEntityConveyor;
import resonant.engine.ResonantEngine;
import resonant.lib.network.discriminator.PacketTile;
import resonant.lib.network.discriminator.PacketType;
import resonant.lib.network.handle.IPacketIDReceiver;
import resonantinduction.archaic.filter.imprint.TileFilterable;
import universalelectricity.core.transform.vector.Vector3;

/** @author Darkguardsman */
public class TileRejector extends TileFilterable implements IPacketIDReceiver
{
	/** should the piston fire, or be extended */
	public boolean firePiston = false;

    public TileRejector()
    {
        super(Material.circuits);
        this.isOpaqueCube(false);
        this.normalRender(false);
    }

	@Override
	public void update()
	{
		super.update();
		/** Has to update a bit faster than a conveyer belt */
		if (this.ticks() % 5 == 0)
		{
			this.firePiston = false;

			Vector3 searchPosition = new Vector3(this);
			searchPosition.add(this.getDirection());
			TileEntity tileEntity = searchPosition.getTileEntity(this.worldObj);

			try
			{
				if (this.isFunctioning())
				{
					/**
					 * Find all entities in the position in which this block is facing and attempt
					 * to push it out of the way.
					 */
					AxisAlignedBB bounds = AxisAlignedBB.getBoundingBox(searchPosition.x(), searchPosition.y(), searchPosition.z(), searchPosition.x() + 1, searchPosition.y() + 1, searchPosition.z() + 1);
					List<Entity> entitiesInFront = this.worldObj.getEntitiesWithinAABB(Entity.class, bounds);

					for (Entity entity : entitiesInFront)
					{
						if (this.canEntityBeThrow(entity))
						{
							this.throwItem(tileEntity, this.getDirection(), entity);
						}
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/** Pushs an entity in the direction in which the rejector is facing */
	public void throwItem(TileEntity tileEntity, ForgeDirection side, Entity entity)
	{
		this.firePiston = true;
		// TODO add config to adjust the motion magnitude per rejector
		entity.posX += side.offsetX;
		// entity.motionY += 0.10000000298023224D;
		entity.posZ += side.offsetZ;

		if (!this.worldObj.isRemote && tileEntity instanceof IEntityConveyor)
		{
			((IEntityConveyor) tileEntity).ignoreEntity(entity);
		}
	}

	/** Checks to see if the rejector can push the entity in the facing direction */
	public boolean canEntityBeThrow(Entity entity)
	{
		// TODO Add other things than items
		if (entity instanceof EntityItem)
		{
			EntityItem entityItem = (EntityItem) entity;
			ItemStack itemStack = entityItem.getEntityItem();

			return this.isFiltering(itemStack);
		}

		return false;
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return ResonantEngine.instance.packetHandler.toMCPacket(new PacketTile(this, 0, this.isInverted(), this.firePiston));
	}

	@Override
	public boolean read(ByteBuf data, int id, EntityPlayer player, PacketType type)
	{
		try
		{
			if (this.worldObj.isRemote)
			{
				if (id == 0)
				{
					this.setInverted(data.readBoolean());
					this.firePiston = data.readBoolean();
					return true;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

}
