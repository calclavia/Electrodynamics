package resonantinduction.electrical.wire.framed;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyTile;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import resonant.lib.prefab.damage.ElectricalDamage;
import resonantinduction.core.prefab.part.PartFramedConnection;
import resonantinduction.electrical.Electrical;
import resonantinduction.electrical.wire.EnumWireMaterial;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.UniversalClass;
import universalelectricity.api.electricity.IElectricalNetwork;
import universalelectricity.api.energy.EnergyNetworkLoader;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyNetwork;
import codechicken.lib.lighting.LazyLightMatrix;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.IconTransformation;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Translation;
import codechicken.microblock.IHollowConnect;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TSlottedPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@UniversalClass
public class PartFramedWire extends PartFramedConnection<EnumWireMaterial, IConductor, IEnergyNetwork> implements IConductor, TSlottedPart, JNormalOcclusion, IHollowConnect
{
    public PartFramedWire()
    {
        super(Electrical.itemInsulation);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            breakIcon = RenderFramedWire.breakIcon;
    }

    public PartFramedWire(EnumWireMaterial type)
    {
        this();
        material = type;
    }

    public PartFramedWire(int typeID)
    {
        this(EnumWireMaterial.values()[typeID]);
    }

    @Override
    public String getType()
    {
        return "resonant_induction_wire";
    }

    /** IC2 Functions */
    @Override
    public void onWorldJoin()
    {
        if (tile() instanceof IEnergyTile && !world().isRemote)
        {
            // Check if there's another part that's an IEnergyTile
            boolean foundAnotherPart = false;

            for (int i = 0; i < tile().partList().size(); i++)
            {
                TMultiPart part = tile().partMap(i);

                if (part instanceof IEnergyTile && part != this)
                {
                    foundAnotherPart = true;
                    break;
                }
            }

            if (!foundAnotherPart)
            {
                MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent((IEnergyTile) tile()));
            }
        }

        super.onWorldJoin();
    }

    @Override
    public void preRemove()
    {
        if (!world().isRemote)
        {
            this.getNetwork().split(this);

            if (tile() instanceof IEnergyTile)
            {
                // Check if there's another part that's an IEnergyTile
                boolean foundAnotherPart = false;

                for (int i = 0; i < tile().partList().size(); i++)
                {
                    TMultiPart part = tile().partMap(i);

                    if (part instanceof IEnergyTile && part != this)
                    {
                        foundAnotherPart = true;
                        break;
                    }
                }

                if (!foundAnotherPart)
                {
                    MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent((IEnergyTile) tile()));
                }
            }
        }

        super.preRemove();
    }

    @Override
    public boolean doesTick()
    {
        return false;
    }

    @Override
    public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
    {
        if (!world().isRemote)
            System.out.println(getNetwork());

        if (item != null)
        {
            if (item.getItem().itemID == Block.lever.blockID)
            {
                TileMultipart tile = tile();
                World w = world();

                if (!w.isRemote)
                {
                    PartFramedSwitchWire wire = (PartFramedSwitchWire) MultiPartRegistry.createPart("resonant_induction_switch_wire", false);
                    wire.copyFrom(this);

                    if (tile.canReplacePart(this, wire))
                    {
                        tile.remPart(this);
                        TileMultipart.addPart(w, new BlockCoord(tile), wire);

                        if (!player.capabilities.isCreativeMode)
                        {
                            player.inventory.decrStackSize(player.inventory.currentItem, 1);
                        }
                    }
                }
                return true;
            }
        }

        return super.activate(player, part, item);
    }

    @Override
    public void preparePlacement(int meta)
    {
        this.setMaterial(meta);
    }

    @Override
    public Iterable<Cuboid6> getCollisionBoxes()
    {
        Set<Cuboid6> collisionBoxes = new HashSet<Cuboid6>();
        collisionBoxes.addAll((Collection<? extends Cuboid6>) getSubParts());
        return collisionBoxes;
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
        {
            RenderFramedWire.INSTANCE.renderStatic(this);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(codechicken.lib.vec.Vector3 pos, float frame, int pass)
    {
        if (getMaterial() == EnumWireMaterial.SILVER)
        {
            RenderFramedWire.INSTANCE.renderShine(this, pos.x, pos.y, pos.z, frame);
        }
    }

    @Override
    public void drawBreaking(RenderBlocks renderBlocks)
    {
        CCRenderState.reset();
        RenderUtils.renderBlock(sides[6], 0, new Translation(x(), y(), z()), new IconTransformation(renderBlocks.overrideBlockTexture), null);
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
        return isInsulated ? 8 : 6;
    }

    @Override
    protected boolean canConnectTo(TileEntity tile, ForgeDirection side)
    {
        Object obj = tile instanceof TileMultipart ? ((TileMultipart) tile).partMap(ForgeDirection.UNKNOWN.ordinal()) : tile;
        return canConnect(side, obj);
    }

    @Override
    public IConductor getConnector(TileEntity tile)
    {
        if (tile instanceof IConductor)
            return (IConductor) ((IConductor) tile).getInstance(ForgeDirection.UNKNOWN);

        return null;
    }

    /** Shouldn't need to be overridden. Override connectionPrevented instead */
    @Override
    public boolean canConnect(ForgeDirection from, Object obj)
    {
        if (isBlockedOnSide(from))
            return false;

        if (obj instanceof PartFramedWire)
        {
            if (world().isBlockIndirectlyGettingPowered(x(), y(), z()))
            {
                return false;
            }

            PartFramedWire wire = (PartFramedWire) obj;

            if (this.getMaterial() == wire.getMaterial())
            {
                if (isInsulated() && wire.isInsulated())
                {
                    return getColor() == wire.getColor() || (getColor() == DEFAULT_COLOR || wire.getColor() == DEFAULT_COLOR);
                }

                return true;
            }

            return false;
        }

        return CompatibilityModule.canConnect(obj, from.getOpposite(), this);
    }

    @Override
    public float getResistance()
    {
        return this.getMaterial().resistance;
    }

    public void copyFrom(PartFramedWire otherCable)
    {
        this.isInsulated = otherCable.isInsulated;
        this.color = otherCable.color;
        this.connections = otherCable.connections;
        this.material = otherCable.material;
        this.currentWireConnections = otherCable.currentWireConnections;
        this.currentAcceptorConnections = otherCable.currentAcceptorConnections;
        this.setNetwork(otherCable.getNetwork());
        this.getNetwork().setBufferFor(this, otherCable.getInstance(ForgeDirection.UNKNOWN).getNetwork().getBufferOf(otherCable));
    }

    @Override
    public IEnergyNetwork getNetwork()
    {
        if (network == null)
        {
            setNetwork(EnergyNetworkLoader.getNewNetwork(this));
        }

        return network;
    }

    @Override
    public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
    {
        return this.getNetwork().produce(this, from.getOpposite(), receive, doReceive);
    }

    @Override
    public long onExtractEnergy(ForgeDirection from, long request, boolean doExtract)
    {
        return 0;
    }

    @Override
    public long getCurrentCapacity()
    {
        return this.getMaterial().maxAmps;
    }

    @Override
    public void setMaterial(int i)
    {
        setMaterial(EnumWireMaterial.values()[i]);
    }

    @Override
    protected ItemStack getItem()
    {
        return new ItemStack(Electrical.itemWire, 1, getMaterialID());
    }

    @Override
    public void onEntityCollision(Entity entity)
    {
        if (!this.isInsulated() && this.getNetwork() instanceof IElectricalNetwork)
            ElectricalDamage.handleElectrocution(entity, this, (IElectricalNetwork) this.getNetwork());
    }

    @Override
    public String toString()
    {
        return "[PartFramedWire]" + x() + "x " + y() + "y " + z() + "z " + getSlotMask() + "s ";
    }

}