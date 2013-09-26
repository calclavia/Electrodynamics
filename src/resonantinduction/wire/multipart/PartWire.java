package resonantinduction.wire.multipart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import resonantinduction.ResonantInduction;
import resonantinduction.render.RenderPartWire;
import resonantinduction.wire.EnumWireMaterial;
import resonantinduction.wire.IInsulatedMaterial;
import resonantinduction.wire.IInsulation;
import resonantinduction.wire.IWireMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;

import universalelectricity.compatibility.Compatibility;
import universalelectricity.core.block.IConductor;
import universalelectricity.core.block.INetworkProvider;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import buildcraft.api.power.PowerHandler;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.lighting.LazyLightMatrix;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.IconTransformation;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Translation;
import codechicken.microblock.IHollowConnect;
import codechicken.multipart.IconHitEffects;
import codechicken.multipart.JIconHitEffects;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TSlottedPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartWire extends PartUniversalConductor implements TSlottedPart, JNormalOcclusion, IHollowConnect, JIconHitEffects, IInsulatedMaterial, IBlockableConnection
{
	public static final int DEFAULT_COLOR = 16;
	public int dyeID = DEFAULT_COLOR;
	public boolean isInsulated = false;
	public static RenderPartWire renderer = new RenderPartWire();
	public static IndexedCuboid6[] sides = new IndexedCuboid6[7];
	public static IndexedCuboid6[] insulatedSides = new IndexedCuboid6[7];
	public EnumWireMaterial material = EnumWireMaterial.COPPER;

	/** Client Side Connection Check */
	private ForgeDirection testingSide;

	static {
		sides[0] = new IndexedCuboid6(0, new Cuboid6(0.36, 0.000, 0.36, 0.64, 0.36, 0.64));
		sides[1] = new IndexedCuboid6(1, new Cuboid6(0.36, 0.64, 0.36, 0.64, 1.000, 0.64));
		sides[2] = new IndexedCuboid6(2, new Cuboid6(0.36, 0.36, 0.000, 0.64, 0.64, 0.36));
		sides[3] = new IndexedCuboid6(3, new Cuboid6(0.36, 0.36, 0.64, 0.64, 0.64, 1.000));
		sides[4] = new IndexedCuboid6(4, new Cuboid6(0.000, 0.36, 0.36, 0.36, 0.64, 0.64));
		sides[5] = new IndexedCuboid6(5, new Cuboid6(0.64, 0.36, 0.36, 1.000, 0.64, 0.64));
		sides[6] = new IndexedCuboid6(6, new Cuboid6(0.36, 0.36, 0.36, 0.64, 0.64, 0.64));
		insulatedSides[0] = new IndexedCuboid6(0, new Cuboid6(0.3, 0.0, 0.3, 0.7, 0.3, 0.7));
		insulatedSides[1] = new IndexedCuboid6(1, new Cuboid6(0.3, 0.7, 0.3, 0.7, 1.0, 0.7));
		insulatedSides[2] = new IndexedCuboid6(2, new Cuboid6(0.3, 0.3, 0.0, 0.7, 0.7, 0.3));
		insulatedSides[3] = new IndexedCuboid6(3, new Cuboid6(0.3, 0.3, 0.7, 0.7, 0.7, 1.0));
		insulatedSides[4] = new IndexedCuboid6(4, new Cuboid6(0.0, 0.3, 0.3, 0.3, 0.7, 0.7));
		insulatedSides[5] = new IndexedCuboid6(5, new Cuboid6(0.7, 0.3, 0.3, 1.0, 0.7, 0.7));
		insulatedSides[6] = new IndexedCuboid6(6, new Cuboid6(0.3, 0.3, 0.3, 0.7, 0.7, 0.7));
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
	
	public PartWire()
	{
		super();
	}
	
	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		if (this.world().isBlockIndirectlyGettingPowered(this.x(), this.y(), this.z()))
		{
			return false;
		}

		return super.canConnect(direction);
	}
	
	public boolean connectionPrevented(TileEntity tile, ForgeDirection side)
	{
		if (tile instanceof IWireMaterial)
		{
			IWireMaterial wireTile = (IWireMaterial) tile;
			
			if (wireTile.getMaterial() != this.getMaterial())
				return true;
		}

		if (this.isInsulated() && tile instanceof IInsulation)
		{
			IInsulation insulatedTile = (IInsulation) tile;

			if ((insulatedTile.isInsulated() && insulatedTile.getInsulationColor() != this.getInsulationColor() && this.getInsulationColor() != DEFAULT_COLOR && insulatedTile.getInsulationColor() != DEFAULT_COLOR))
				return true;
		}
		
		return (this.isBlockedOnSide(side) || tile instanceof IBlockableConnection && ((IBlockableConnection)tile).isBlockedOnSide(side.getOpposite()));
	}
	
	public byte getPossibleConnections()
	{
		if (this.world().isBlockIndirectlyGettingPowered(this.x(), this.y(), this.z()))
		{
			return 0x00;
		}
		return super.getPossibleConnections();
	}

	@Override
	public void refresh()
	{
		if (!this.world().isRemote)
		{
			this.adjacentConnections = null;
			byte possibleConnections = getPossibleConnections();
			if (possibleConnections != currentConnections)
			{
				byte or = (byte) (possibleConnections | currentConnections);
				if (or != possibleConnections) //Connections have been removed
				{
					this.getNetwork().split((IConductor) tile());
					this.setNetwork(null);
				}
				
				for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
				{
					byte tester = (byte) (1 << side.ordinal());
					if ((possibleConnections & tester) > 0)
					{
						TileEntity tileEntity = VectorHelper.getConnectorFromSide(this.world(), new Vector3(tile()), side);

						if (tileEntity instanceof INetworkProvider)
						{
							this.getNetwork().merge(((INetworkProvider) tileEntity).getNetwork());
						}
					}
				}

				currentConnections = possibleConnections;
				
			}
			this.sendDescUpdate();
			this.getNetwork().refresh();		
						
		}
		tile().markRender();
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
		IndexedCuboid6[] currentSides = this.isInsulated() ? insulatedSides : sides;
		if(tile() != null)
		{
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				int ord = side.ordinal();
				if(connectionMapContainsSide(currentConnections, side) || side == this.testingSide) subParts.add(currentSides[ord]);
			}
		}
		subParts.add(currentSides[6]);
		return subParts;
	}
	
	public boolean connectionMapContainsSide(byte connections, ForgeDirection side)
	{
		byte tester = (byte) (1 << side.ordinal());
		return ((connections & tester) > 0);
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
	public void renderStatic(codechicken.lib.vec.Vector3 pos, LazyLightMatrix olm, int pass)
	{
		if (pass == 0)
			renderer.renderStatic(this);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(codechicken.lib.vec.Vector3 pos, float frame, int pass)
	{
		if (ResonantInduction.SHINY_SILVER && this.getMaterial() == EnumWireMaterial.SILVER)
		renderer.renderShine(this, pos.x, pos.y, pos.z, frame);
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
		this.currentConnections = packet.readByte();
		if (tile() != null) tile().markRender();		
	}
	
	@Override
	public void writeDesc(MCDataOutput packet)
	{
		packet.writeInt(this.getTypeID());
		packet.writeInt(this.dyeID);
		packet.writeBoolean(this.isInsulated);
		packet.writeByte(this.currentConnections);
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
	public ItemStack pickItem(MovingObjectPosition hit)
	{
		return new ItemStack(ResonantInduction.itemPartWire, 1, this.getTypeID());
	}
	
	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
	{
		if (item != null)
		{
			if (item.itemID == Item.dyePowder.itemID && isInsulated())
			{
				setDye(item.getItemDamage());
				return true;
			}
			else if (item.itemID == Block.cloth.blockID)
			{
				if (isInsulated() && !world().isRemote)
				{
					tile().dropItems(Collections.singletonList(new ItemStack(Block.cloth, 1, BlockColored.getBlockFromDye(dyeID))));
				}
				setInsulated(BlockColored.getDyeFromBlock(item.getItemDamage()));
				player.inventory.decrStackSize(player.inventory.currentItem, 1);
				return true;
			}
			else if ((item.itemID == Item.shears.itemID || item.getItem() instanceof ItemShears) && this.isInsulated())
			{
				if (!world().isRemote)
					tile().dropItems(Collections.singletonList(new ItemStack(Block.cloth, 1, BlockColored.getBlockFromDye(dyeID))));
				setInsulated(false);
				return true;
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
		this.dyeID = DEFAULT_COLOR;
		this.refresh();
		this.world().markBlockForUpdate(this.x(), this.y(), this.z());
		((TileMultipart)this.tile()).notifyPartChange(this);
	}

	public void setInsulated(int dyeColour)
	{
		this.isInsulated = true;
		this.dyeID = dyeColour;
		this.refresh();
		this.world().markBlockForUpdate(this.x(), this.y(), this.z());
		((TileMultipart)this.tile()).notifyPartChange(this);
	}

	public void setInsulated()
	{
		setInsulated(true);
	}

	@Override
	public Cuboid6 getBounds()
	{
		return new Cuboid6(0.375, 0.375, 0.375, 0.625, 0.625, 0.625);
	}

	@Override
	public Icon getBreakingIcon(Object subPart, int side)
	{
		return RenderPartWire.breakIcon;
	}

	@Override
	public Icon getBrokenIcon(int side)
	{
		return RenderPartWire.breakIcon;
	}
	
    @Override
    public void addHitEffects(MovingObjectPosition hit, EffectRenderer effectRenderer)
    {
        IconHitEffects.addHitEffects(this, hit, effectRenderer);
    }
    
    @Override
    public void addDestroyEffects(EffectRenderer effectRenderer)
    {
        IconHitEffects.addDestroyEffects(this, effectRenderer, false);
    }

	@Override
	public boolean isBlockedOnSide(ForgeDirection side)
	{
		TMultiPart blocker = tile().partMap(side.ordinal());
		this.testingSide = side;
		boolean expandable = NormalOcclusionTest.apply(this, blocker);
		this.testingSide = null;
		return !expandable;
	}
	
	@Override
	public void onPartChanged(TMultiPart part)
	{
		refresh();
	}
    

}
