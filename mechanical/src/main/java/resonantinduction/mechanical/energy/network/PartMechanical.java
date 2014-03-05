package resonantinduction.mechanical.energy.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TFacePart;
import codechicken.multipart.TMultiPart;

/**
 * We assume all the force acting on the gear is 90 degrees.
 * 
 * @author Calclavia
 * 
 */
public abstract class PartMechanical extends JCuboidPart implements JNormalOcclusion, TFacePart, IMechanicalNodeProvider
{
	public MechanicalNode node;
	protected double prevAngularVelocity;

	/**
	 * Packets
	 */
	int ticks = 0;
	boolean markPacketUpdate = false;

	/** Side of the block this is placed on */
	public ForgeDirection placementSide = ForgeDirection.UNKNOWN;

	public int tier;

	public void preparePlacement(int side, int itemDamage)
	{
		this.placementSide = ForgeDirection.getOrientation((byte) (side));
		this.tier = itemDamage;
	}

	@Override
	public void update()
	{
		ticks++;

		if (!world().isRemote)
			checkClientUpdate();

		super.update();
	}

	public void checkClientUpdate()
	{
		if (Math.abs(prevAngularVelocity - node.angularVelocity) > 0.05f || (prevAngularVelocity != node.angularVelocity && (prevAngularVelocity == 0 || node.angularVelocity == 0)))
		{
			prevAngularVelocity = node.angularVelocity;
			markPacketUpdate = true;
		}

		if (markPacketUpdate && ticks % 10 == 0)
		{
			sendRotationPacket();
			markPacketUpdate = false;
		}
	}

	public MechanicalNode getNode(ForgeDirection dir)
	{
		return node;
	}

	@Override
	public void onWorldJoin()
	{
		node.reconstruct();
	}

	@Override
	public void onWorldSeparate()
	{
		node.deconstruct();
	}

	/** Packet Code. */
	public void sendRotationPacket()
	{
		if (world() != null && !world().isRemote)
		{
			sendDescUpdate();
			// TODO: Make packets more efficient.
			// getWriteStream().writeByte(1).writeFloat(angularVelocity);
		}
	}

	public void read(MCDataInput packet, int packetID)
	{
		switch (packetID)
		{
			case 0:
				readDesc(packet);
				break;
			case 1:
				node.angularVelocity = packet.readFloat();
				break;
		}
	}

	/** Packet Code. */
	@Override
	public void readDesc(MCDataInput packet)
	{
		load(packet.readNBTTagCompound());
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		// packet.writeByte(0);
		NBTTagCompound nbt = new NBTTagCompound();
		save(nbt);
		packet.writeNBTTagCompound(nbt);
	}

	@Override
	public void read(MCDataInput packet)
	{
		super.read(packet);
		// read(packet, packet.readUByte());
	}

	@Override
	public int redstoneConductionMap()
	{
		return 0;
	}

	@Override
	public boolean solid(int arg0)
	{
		return true;
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		placementSide = ForgeDirection.getOrientation(nbt.getByte("side"));
		tier = nbt.getByte("tier");
		node.load(nbt);
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		nbt.setByte("side", (byte) placementSide.ordinal());
		nbt.setByte("tier", (byte) tier);
		node.save(nbt);
	}

	protected abstract ItemStack getItem();

	@Override
	public Iterable<ItemStack> getDrops()
	{
		List<ItemStack> drops = new ArrayList<ItemStack>();
		drops.add(getItem());
		return drops;
	}

	@Override
	public ItemStack pickItem(MovingObjectPosition hit)
	{
		return getItem();
	}

	public universalelectricity.api.vector.Vector3 getPosition()
	{
		return new universalelectricity.api.vector.Vector3(x(), y(), z());
	}
}