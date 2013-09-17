package resonantinduction.wire.multipart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import resonantinduction.base.IPacketReceiver;
import resonantinduction.render.RenderWirePart;
import resonantinduction.wire.EnumWireMaterial;
import resonantinduction.wire.TileEntityWire;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

import universalelectricity.compatibility.Compatibility;
import universalelectricity.core.block.INetworkProvider;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import buildcraft.api.power.PowerHandler;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.Cuboid6;
import codechicken.microblock.IHollowConnect;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TNormalOcclusion;
import codechicken.multipart.TSlottedPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartWire extends PartUniversalConductor implements IPacketReceiver, TSlottedPart, JNormalOcclusion, IHollowConnect
{
	public static final int DEFAULT_COLOR = 16;
	public int dyeID = DEFAULT_COLOR;
	public boolean isInsulated = false;
	public static RenderWirePart renderer = new RenderWirePart();
	public static IndexedCuboid6[] sides = new IndexedCuboid6[7];

	/** Client Side Connection Check */
	public boolean isTick = false;

	static {
		sides[0] = new IndexedCuboid6(0, new Cuboid6(0.3, 0.0, 0.3, 0.7, 0.3, 0.7));
		sides[1] = new IndexedCuboid6(1, new Cuboid6(0.3, 0.7, 0.3, 0.7, 1.0, 0.7));
		sides[2] = new IndexedCuboid6(2, new Cuboid6(0.3, 0.3, 0.0, 0.7, 0.7, 0.3));
		sides[3] = new IndexedCuboid6(3, new Cuboid6(0.3, 0.3, 0.7, 0.7, 0.7, 1.0));
		sides[4] = new IndexedCuboid6(4, new Cuboid6(0.0, 0.3, 0.3, 0.3, 0.7, 0.7));
		sides[5] = new IndexedCuboid6(5, new Cuboid6(0.7, 0.3, 0.3, 1.0, 0.7, 0.7));
		sides[6] = new IndexedCuboid6(6, new Cuboid6(0.3, 0.3, 0.3, 0.7, 0.7, 0.7));
	}
	
	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		if (this.world().isBlockIndirectlyGettingPowered(this.x(), this.y(), this.z()))
		{
			return false;
		}

		Vector3 connectPos = new Vector3(tile()).modifyPositionFromSide(direction);

		if (connectPos.getTileEntity(this.world()) instanceof TileEntityWire)
		{
			TileEntityWire tileWire = (TileEntityWire) connectPos.getTileEntity(this.world());

			if ((tileWire.isInsulated && this.isInsulated && tileWire.dyeID != this.dyeID && this.dyeID != DEFAULT_COLOR && tileWire.dyeID != DEFAULT_COLOR) || connectPos.getBlockMetadata(this.world()) != this.getTypeID())
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public void refresh()
	{
		if (!this.world().isRemote)
		{
			this.adjacentConnections = null;

			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				if (this.canConnect(side.getOpposite()))
				{
					TileEntity tileEntity = VectorHelper.getConnectorFromSide(this.world(), new Vector3(tile()), side);

					if (tileEntity != null)
					{
						if (tileEntity.getClass().isInstance(this) && tileEntity instanceof INetworkProvider)
						{
							this.getNetwork().merge(((INetworkProvider) tileEntity).getNetwork());
						}
					}
				}
			}

			this.getNetwork().refresh();
		}
	}

	@Override
	public float getResistance()
	{
		return getMaterial().resistance;
	}

	@Override
	public float getCurrentCapacity()
	{
		return getMaterial().maxAmps;
	}

	public EnumWireMaterial getMaterial()
	{
		return EnumWireMaterial.values()[this.getTypeID()];
	}

	public int getTypeID()
	{
		return this.world().getBlockMetadata(this.x(), this.y(), this.z());
	}

	/**
	 * @param dyeID
	 */
	public void setDye(int dyeID)
	{
		this.dyeID = dyeID;
		this.refresh();
		this.world().markBlockForUpdate(this.x(), this.y(), this.z());
	}

	public void setInsulated()
	{
		this.isInsulated = true;
		this.refresh();
		this.world().markBlockForUpdate(this.x(), this.y(), this.z());
		((TileMultipart)this.getTile()).notifyPartChange(this);
	}
/*
	@Override
	public Packet getDescriptionPacket()
	{
		return PacketHandler.getTileEntityPacket(tile(), this.isInsulated, this.dyeID, this instanceof TileEntityTickWire);
	}
*/
	@Override
	public void handle(ByteArrayDataInput input)
	{
		try
		{
			this.isInsulated = input.readBoolean();
			this.dyeID = input.readInt();
			this.isTick = input.readBoolean();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);
		this.dyeID = nbt.getInteger("dyeID");
		this.isInsulated = nbt.getBoolean("isInsulated");
	}

	/**
	 * Furnace connection for tick wires
	 */
	@Override
	public TileEntity[] getAdjacentConnections()
	{
		super.getAdjacentConnections();

		if (this.isTick)
		{
			for (byte i = 0; i < 6; i++)
			{
				ForgeDirection side = ForgeDirection.getOrientation(i);
				TileEntity tileEntity = VectorHelper.getTileEntityFromSide(this.world(), new Vector3(tile()), side);

				if (tileEntity instanceof TileEntityFurnace)
				{
					this.adjacentConnections[i] = tileEntity;
				}
			}
		}
		return this.adjacentConnections;
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);
		nbt.setInteger("dyeID", this.dyeID);
		nbt.setBoolean("isInsulated", this.isInsulated);
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		return null;
	}

	@Override
	public void doWork(PowerHandler workProvider)
	{
		this.buildcraftBuffer = Compatibility.BC3_RATIO * 25 * this.getMaterial().maxAmps;
		this.powerHandler.configure(0, this.buildcraftBuffer, this.buildcraftBuffer, this.buildcraftBuffer * 2);
		super.doWork(workProvider);
	}
	
	@Override
	public Iterable<IndexedCuboid6> getSubParts()
	{
		Set<IndexedCuboid6> subParts = new HashSet<IndexedCuboid6>();
		if(getTile() != null)
		{
			TileEntity[] connections = getAdjacentConnections();
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				int ord = side.ordinal();
				if(connections[ord] != null) subParts.add(sides[ord]);
			}
		}
		subParts.add(sides[6]);
		return subParts;
	}
	
	@Override
	public Iterable<Cuboid6> getCollisionBoxes()
	{
		Set<Cuboid6> collisionBoxes = new HashSet<Cuboid6>();
		collisionBoxes.addAll((Collection<? extends Cuboid6>) getSubParts());
		return collisionBoxes;
	}

	@Override
	public String getType()
	{
		return "resonant_induction_wire";
	}
	
	@Override
	public Iterable<ItemStack> getDrops()
	{
		return null;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(codechicken.lib.vec.Vector3 pos, float frame, int pass)
	{
		renderer.renderWirePartAt(this, pos.x, pos.y, pos.z, frame);
	}

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes()
	{
		return getCollisionBoxes();
	}
	
	@Override
	public boolean occlusionTest(TMultiPart other)
	{
		return NormalOcclusionTest.apply(this, other);
	}

	@Override
	public int getSlotMask()
	{
		return PartMap.CENTER.mask;
	}

	@Override
	public int getHollowSize()
	{
		return this.isInsulated ? 8 : 6;
	}
	
	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
	{
		if (item != null)
		{
			if (item.itemID == Item.dyePowder.itemID)
			{
				setDye(item.getItemDamage());
				return true;
			}
			else if (item.itemID == Block.cloth.blockID && !isInsulated)
			{
				setInsulated();
				setDye(BlockColored.getDyeFromBlock(item.getItemDamage()));
				player.inventory.decrStackSize(player.inventory.currentItem, 1);
				return true;
			}
		}
		return false;
	}
}
