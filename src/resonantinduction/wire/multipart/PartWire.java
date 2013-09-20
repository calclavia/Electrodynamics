package resonantinduction.wire.multipart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import resonantinduction.PacketHandler;
import resonantinduction.ResonantInduction;
import resonantinduction.base.IPacketReceiver;
import resonantinduction.render.RenderWirePart;
import resonantinduction.wire.EnumWireMaterial;
import resonantinduction.wire.IInsulatedMaterial;
import resonantinduction.wire.IInsulation;
import resonantinduction.wire.IWireMaterial;
import resonantinduction.wire.TileEntityWire;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

import universalelectricity.compatibility.Compatibility;
import universalelectricity.core.block.IConductor;
import universalelectricity.core.block.INetworkProvider;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import buildcraft.api.power.PowerHandler;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.IconTransformation;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Translation;
import codechicken.microblock.IHollowConnect;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TNormalOcclusion;
import codechicken.multipart.TSlottedPart;
import codechicken.multipart.TileMultipart;
import codechicken.multipart.handler.MultipartProxy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartWire extends PartUniversalConductor implements IPacketReceiver, TSlottedPart, JNormalOcclusion, IHollowConnect, IInsulatedMaterial
{
	public static final int DEFAULT_COLOR = 16;
	public int dyeID = DEFAULT_COLOR;
	public boolean isInsulated = false;
	public static RenderWirePart renderer = new RenderWirePart();
	public static IndexedCuboid6[] sides = new IndexedCuboid6[7];
	public EnumWireMaterial material = EnumWireMaterial.COPPER;
	public byte currentConnections;

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
	
	public PartWire(int typeID)
	{
		this(EnumWireMaterial.values()[typeID]);
	}
	
	public PartWire(EnumWireMaterial type)
	{
		super();
		this.material = type;
	}
	
	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		if (this.world().isBlockIndirectlyGettingPowered(this.x(), this.y(), this.z()))
		{
			return false;
		}

		Vector3 connectPos = new Vector3(tile()).modifyPositionFromSide(direction);
		TileEntity connectTile = connectPos.getTileEntity(this.world());
		if (connectTile instanceof IWireMaterial)
		{
			IWireMaterial wireTile = (IWireMaterial) connectTile;
			
			if (wireTile.getMaterial() != this.getMaterial())
			{
				return false;
			}
		}

		if (this.isInsulated() && connectTile instanceof IInsulation)
		{
			IInsulation insulatedTile = (IInsulation) connectTile;

			if ((insulatedTile.isInsulated() && insulatedTile.getInsulationColor() != this.getInsulationColor() && this.getInsulationColor() != DEFAULT_COLOR && insulatedTile.getInsulationColor() != DEFAULT_COLOR))
			{
				return false;
			}
		}

		return true;
	}

	public byte getPreventedConnections()
	{
		byte map = 0x00;
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tileEntity = VectorHelper.getTileEntityFromSide(this.world(), new Vector3(tile()), side);
			if (tileEntity instanceof INetworkProvider && !canConnect(side))
				map |= 1 << side.ordinal();
		}
		return map;
		
	}
	
	@Override
	public void refresh()
	{
		if (!this.world().isRemote)
		{
			if (isInsulated() || this.world().isBlockIndirectlyGettingPowered(this.x(), this.y(), this.z()))
			{
				byte preventedConnections = getPreventedConnections();
				for (int i = 0; i < 6; i++)
				{
					int sideConnected = currentConnections & (1 << i);
					int sidePrevented = preventedConnections & (1 << i);
					if (sideConnected == 1 << i && sidePrevented == 1 << i)
					{
						currentConnections = 0x00;
						getNetwork().split((IConductor) tile());
						setNetwork(null);
						break;
					}
				}
			}
			
			this.adjacentConnections = null;
			
			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				if (this.canConnect(side))
				{
					TileEntity tileEntity = VectorHelper.getConnectorFromSide(this.world(), new Vector3(tile()), side);

					if (tileEntity != null)
					{
						if (tileEntity instanceof INetworkProvider)
						{
							this.getNetwork().merge(((INetworkProvider) tileEntity).getNetwork());
							currentConnections |= (1 << side.ordinal());
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
		return material;
	}
	
	public int getTypeID()
	{
		return material.ordinal();
	}

	public void setDye(int dyeID)
	{
		this.dyeID = dyeID;
		this.refresh();
		this.world().markBlockForUpdate(this.x(), this.y(), this.z());
	}
	
	public void setMaterialFromID(int id)
	{
		this.material = EnumWireMaterial.values()[id];
	}

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
	public String getType()
	{
		return "resonant_induction_wire";
	}
	
	@Override
	public boolean occlusionTest(TMultiPart other)
	{
		return NormalOcclusionTest.apply(this, other);
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
	public Iterable<ItemStack> getDrops()
	{
		List<ItemStack> drops = new ArrayList<ItemStack>();
		drops.add(pickItem(null));
		if(isInsulated)
			drops.add(new ItemStack(Block.cloth, 1, BlockColored.getBlockFromDye(dyeID)));
		return drops;
	}
	
	@Override
	public float getStrength(MovingObjectPosition hit, EntityPlayer player)
	{
		return 10F;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(codechicken.lib.vec.Vector3 pos, float frame, int pass)
	{
		renderer.renderModelAt(this, pos.x, pos.y, pos.z, frame);
	}
	
	@Override
	public void drawBreaking(RenderBlocks renderBlocks)
	{
        CCRenderState.reset();
        RenderUtils.renderBlock(sides[6], 0, new Translation(x(), y(), z()), new IconTransformation(renderBlocks.overrideBlockTexture), null);
	}

	@Override
	public void readDesc(MCDataInput packet) 
	{
		this.setMaterialFromID(packet.readInt());
		this.dyeID = packet.readInt();
		this.isInsulated = packet.readBoolean();
		this.isTick = packet.readBoolean();
		
	}
	
	@Override
	public void writeDesc(MCDataOutput packet)
	{
		packet.writeInt(this.getTypeID());
		packet.writeInt(this.dyeID);
		packet.writeBoolean(this.isInsulated);
		packet.writeBoolean(this.isTick);
	}
	
	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);
		nbt.setInteger("typeID", this.getTypeID());
		nbt.setInteger("dyeID", this.dyeID);
		nbt.setBoolean("isInsulated", this.isInsulated);
	}
	
	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);
		this.setMaterialFromID(nbt.getInteger("typeID"));
		this.dyeID = nbt.getInteger("dyeID");
		this.isInsulated = nbt.getBoolean("isInsulated");
	}
	
	@Override
	public void onAdded()
	{
		super.onAdded();
		refresh();
	}

	@Override
	public boolean doesTick()
	{
		return this.isTick;
	}
	
	@Override
	public ItemStack pickItem(MovingObjectPosition hit)
	{
		return new ItemStack(ResonantInduction.itemPartWire, 1, this.getTypeID());
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
			else if (item.itemID == Item.shears.itemID || item.getItem() instanceof ItemShears)
			{
				if (!world().isRemote)
					tile().dropItems(Collections.singletonList(new ItemStack(Block.cloth, 1, BlockColored.getBlockFromDye(dyeID))));
				setInsulated(false);
				setDye(DEFAULT_COLOR);
			}
		}
		if (!world().isRemote)player.addChatMessage(getNetwork().toString());
		return false;
	}
	
	@Override
	public Iterable<Cuboid6> getOcclusionBoxes()
	{
		return getCollisionBoxes();
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
	public boolean isInsulated()
	{
		return isInsulated;
	}

	@Override
	public int getInsulationColor()
	{
		return isInsulated ? dyeID : -1;
	}

	@Override
	public void setInsulationColor(int dyeID)
	{
		this.dyeID = dyeID;
		this.refresh();
		this.world().markBlockForUpdate(this.x(), this.y(), this.z());
	}

	@Override
	public void setInsulated(boolean insulated)
	{
		this.isInsulated = insulated;
		this.refresh();
		this.world().markBlockForUpdate(this.x(), this.y(), this.z());
		((TileMultipart)this.getTile()).notifyPartChange(this);
	}

	public void setInsulated()
	{
		setInsulated(true);
	}
}
