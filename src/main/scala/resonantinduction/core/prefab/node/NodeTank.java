package resonantinduction.core.prefab.node;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;
import resonant.engine.ResonantEngine;
import resonant.lib.network.discriminator.PacketTile;
import resonant.lib.network.discriminator.PacketType;
import resonant.lib.network.handle.IPacketIDReceiver;
import resonant.lib.prefab.fluid.LimitedTank;
import resonant.lib.prefab.fluid.NodeFluidHandler;
import resonant.lib.utility.WorldUtility;
import universalelectricity.api.core.grid.INodeProvider;
import universalelectricity.api.core.grid.ISave;
import universalelectricity.core.grid.node.NodeConnector;

/**
 * Simple tank node designed to be implemented by any machine that can connect to other fluid based machines.
 *
 * @author Darkguardsman
 */
public class NodeTank extends NodeFluidHandler implements ISave, IPacketIDReceiver
{
	static final int PACKET_DESCRIPTION = 100, PACKET_TANK = 101;
	int renderSides = 0;

	public NodeTank(INodeProvider parent)
	{
		this(parent, 1);
	}

	public NodeTank(INodeProvider parent, int buckets)
	{
		super(parent, new LimitedTank(buckets * FluidContainerRegistry.BUCKET_VOLUME));
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		getPrimaryTank().readFromNBT(nbt.getCompoundTag("tank"));
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		nbt.setTag("tank", getPrimaryTank().writeToNBT(new NBTTagCompound()));
	}

	@Override
	public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType type)
	{
		switch (id)
		{
			case PACKET_DESCRIPTION:
				this.load(ByteBufUtils.readTag(buf));
				break;
			case PACKET_TANK:
				getPrimaryTank().readFromNBT(ByteBufUtils.readTag(buf));
				break;
		}
		return false;
	}

	public void sendTank()
	{
		//ResonantEngine.instance.packetHandler.sendToAllAround(new PacketTile((int) x(), (int) y(), (int) z(), PACKET_TANK, getPrimaryTank().writeToNBT(new NBTTagCompound())), 64);
	}

	@Override
	protected void addConnection(Object obj, ForgeDirection dir)
	{
		super.addConnection(obj, dir);
		if (showConnectionsFor(obj, dir))
		{
			renderSides = WorldUtility.setEnableSide(getRenderSides(), dir, true);
		}
	}

	protected boolean showConnectionsFor(Object obj, ForgeDirection dir)
	{
		if (obj != null)
		{
			if (obj.getClass().isAssignableFrom(getParent().getClass()))
			{
				return true;
			}
		}
		return false;
	}

	public int getRenderSides()
	{
		return renderSides;
	}
}
