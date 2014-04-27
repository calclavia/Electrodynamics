package resonantinduction.electrical.wire.flat;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.MultipartUtility;
import resonantinduction.electrical.wire.EnumWireMaterial;
import resonantinduction.electrical.wire.PartAdvancedWire;
import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourARGB;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.lighting.LazyLightMatrix;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.PartMap;
import codechicken.multipart.TFacePart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** This is the base class for all wire types. It can be used for any sub type, as it contains the
 * base calculations necessary to create a working wire. This calculates all possible connections to
 * sides, around corners, and inside corners, while checking for microblock obstructions.
 * 
 * @author Modified by Calclavia, MrTJP */
public class PartFlatWire extends PartAdvancedWire implements TFacePart, JNormalOcclusion
{
    public static Cuboid6[][] selectionBounds = new Cuboid6[3][6];
    public static Cuboid6[][] occlusionBounds = new Cuboid6[3][6];

    static
    {
        for (int t = 0; t < 3; t++)
        {
            // Subtract the box a little because we'd like things like posts to get first hit
            Cuboid6 selection = new Cuboid6(0, 0, 0, 1, (t + 2) / 16D, 1).expand(-0.005);
            Cuboid6 occlusion = new Cuboid6(2 / 8D, 0, 2 / 8D, 6 / 8D, (t + 2) / 16D, 6 / 8D);
            for (int s = 0; s < 6; s++)
            {
                selectionBounds[t][s] = selection.copy().apply(Rotation.sideRotations[s].at(Vector3.center));
                occlusionBounds[t][s] = occlusion.copy().apply(Rotation.sideRotations[s].at(Vector3.center));
            }
        }
    }

    public byte side;

    /** A map of the corners.
     * 
     * 
     * Currently split into 4 nybbles (from lowest) 0 = Corner connections (this wire should connect
     * around a corner to something external) 1 = Straight connections (this wire should connect to
     * something external) 2 = Internal connections (this wire should connect to something internal)
     * 3 = Internal open connections (this wire is not blocked by a cover/edge part and *could*
     * connect through side) bit 16 = connection to the centerpart 5 = Render corner connections.
     * Like corner connections but set to low if the other wire part is smaller than this (they
     * render to us not us to them) */
    public int connMap;

    public PartFlatWire()
    {

    }

    public PartFlatWire(int typeID)
    {
        this(EnumWireMaterial.values()[typeID]);
    }

    public PartFlatWire(EnumWireMaterial type)
    {
        material = type;
    }

    public void preparePlacement(int side, int meta)
    {
        this.side = (byte) (side ^ 1);
        this.setMaterial(meta);
    }

    /** PACKET and NBT Methods */
    @Override
    public void load(NBTTagCompound tag)
    {
        super.load(tag);
        this.side = tag.getByte("side");
        this.connMap = tag.getInteger("connMap");
    }

    @Override
    public void save(NBTTagCompound tag)
    {
        super.save(tag);
        tag.setByte("side", this.side);
        tag.setInteger("connMap", this.connMap);
    }

    @Override
    public void readDesc(MCDataInput packet)
    {
        super.readDesc(packet);
        this.side = packet.readByte();
        this.connMap = packet.readInt();
    }

    @Override
    public void writeDesc(MCDataOutput packet)
    {
        super.writeDesc(packet);
        packet.writeByte(this.side);
        packet.writeInt(this.connMap);
    }

    @Override
    public void read(MCDataInput packet)
    {
        read(packet, packet.readUByte());
    }

    @Override
    public void read(MCDataInput packet, int packetID)
    {
        if (packetID == 0)
        {
            this.connMap = packet.readInt();
            tile().markRender();
        }
        else
        {
            super.read(packet, packetID);
        }
    }

    public void sendConnUpdate()
    {
        tile().getWriteStream(this).writeByte(0).writeInt(this.connMap);
    }

    /* WORLD EVENTS */
    @Override
    public void onRemoved()
    {
        super.onRemoved();

        if (!world().isRemote)
        {
            for (int r = 0; r < 4; r++)
            {
                if (maskConnects(r))
                {
                    if ((connMap & 1 << r) != 0)
                    {
                        notifyCornerChange(r);
                    }
                    else if ((connMap & 0x10 << r) != 0)
                    {
                        notifyStraightChange(r);
                    }
                }
            }
        }
    }

    @Override
    public void onChunkLoad()
    {
        if ((connMap & 0x80000000) != 0) // compat with converters, recalc connections
        {
            if (dropIfCantStay())
            {
                return;
            }

            connMap = 0;

            updateInternalConnections();

            if (updateOpenConnections())
            {
                updateExternalConnections();
            }

            tile().markDirty();
        }

        this.recalculateConnections();

        super.onChunkLoad();
    }

    @Override
    public void onAdded()
    {
        super.onAdded();

        if (!world().isRemote)
        {
            updateOpenConnections();
            boolean changed = updateInternalConnections();
            // don't use || because it's fail fast
            changed |= updateExternalConnections();

            if (changed)
            {
                sendConnUpdate();
            }

            this.recalculateConnections();
        }
    }

    @Override
    public void onPartChanged(TMultiPart part)
    {
        if (!world().isRemote)
        {
            boolean changed = updateInternalConnections();

            if (updateOpenConnections())
            {
                changed |= updateExternalConnections();
            }

            if (changed)
            {
                sendConnUpdate();
            }

            this.recalculateConnections();
        }

        super.onPartChanged(part);
    }

    @Override
    public void onNeighborChanged()
    {
        if (!world().isRemote)
        {
            if (dropIfCantStay())
            {
                return;
            }

            if (updateExternalConnections())
            {
                sendConnUpdate();
            }

            this.recalculateConnections();
        }
        super.onNeighborChanged();
    }

    @Override
    public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
    {
        if (item != null)
        {
            if (item.getItem().itemID == Block.lever.blockID)
            {
                TileMultipart tile = tile();
                World w = world();

                if (!w.isRemote)
                {
                    PartFlatSwitchWire wire = (PartFlatSwitchWire) MultiPartRegistry.createPart("resonant_induction_flat_switch_wire", false);
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
    public void recalculateConnections()
    {
        this.updateOpenConnections();

        boolean[] calculatedSides = new boolean[6];

        /** Check external connections. */
        for (byte r = 0; r < 4; r++)
        {
            if (maskOpen(r))
            {
                int absDir = Rotation.rotateSide(this.side, r);

                // Check direct connection.
                if (this.setExternalConnection(r, absDir))
                {
                    calculatedSides[absDir] = true;
                }

                // Check Corner Connection
                BlockCoord cornerPos = new BlockCoord(tile());
                cornerPos.offset(absDir);

                if (canConnectThroughCorner(cornerPos, absDir ^ 1, this.side))
                {
                    cornerPos.offset(this.side);
                    TileMultipart tpCorner = MultipartUtility.getMultipartTile(world(), cornerPos);

                    if (tpCorner != null)
                    {
                        TMultiPart tp = tpCorner.partMap(absDir ^ 1);

                        if (this.canConnectTo(tp, ForgeDirection.getOrientation(absDir)))
                        {
                            this.connections[absDir] = tp;

                            if (tp instanceof PartFlatWire)
                            {
                                // We found a wire, merge networks!
                                this.getNetwork().merge(((PartFlatWire) tp).getNetwork());
                            }

                            calculatedSides[absDir] = true;
                            continue;
                        }
                    }
                }

                if (!calculatedSides[absDir])
                {
                    this.disconnect(absDir);
                }
            }
        }

        /** Check internal connections. */
        for (byte r = 0; r < 4; r++)
        {
            int absDir = Rotation.rotateSide(this.side, r);

            /** Look for an internal straight connection. */
            if (tile().partMap(PartMap.edgeBetween(absDir, this.side)) == null)
            {
                TMultiPart tp = tile().partMap(absDir);

                if (this.canConnectTo(tp))
                {
                    // We found a wire! Merge networks!
                    this.connections[absDir] = tp;

                    if (tp instanceof PartFlatWire)
                    {
                        this.getNetwork().merge(((PartFlatWire) tp).getNetwork());
                    }
                    continue;
                }
            }

            if (!calculatedSides[absDir])
            {
                this.disconnect(absDir);
            }
        }

        // Connect to the face of the block the wire is placed on.
        this.setExternalConnection(-1, this.side);

        this.getNetwork().reconstruct();
    }

    public boolean setExternalConnection(int r, int absSide)
    {
        BlockCoord pos = new BlockCoord(tile()).offset(absSide);

        /** Look for an external wire connection. */
        TileMultipart tileMultiPart = MultipartUtility.getMultipartTile(world(), pos);

        if (tileMultiPart != null && r != -1)
        {
            TMultiPart tp = tileMultiPart.partMap(this.side);

            if (this.canConnectTo(tp, ForgeDirection.getOrientation(absSide)))
            {
                // Check the wire we are connecting to and see if THAT block can accept this one.
                int otherR = (r + 2) % 4;

                if (tp instanceof PartFlatWire && ((PartFlatWire) tp).canConnectTo(this, ForgeDirection.getOrientation(absSide).getOpposite()) && ((PartFlatWire) tp).maskOpen(otherR))
                {
                    // We found a wire! Merge connection.
                    connections[absSide] = tp;
                    getNetwork().merge(((PartFlatWire) tp).getNetwork());
                    return true;
                }

                /** Check for a micro-energy block */
                if (canConnectTo(tp))
                {
                    connections[absSide] = tp;
                    return true;
                }
            }

            this.disconnect(absSide);
        }

        /** Look for an external energy handler. */
        TileEntity tileEntity = world().getBlockTileEntity(pos.x, pos.y, pos.z);

        if (this.canConnectTo(tileEntity, ForgeDirection.getOrientation(absSide)))
        {
            this.connections[absSide] = tileEntity;
            return true;
        }

        this.disconnect(absSide);

        return false;
    }

    private synchronized void disconnect(int i)
    {
        if (!this.world().isRemote)
        {
            if (this.connections[i] != null)
            {
                if (this.connections[i] instanceof PartFlatWire)
                {
                    PartFlatWire wire = (PartFlatWire) this.connections[i];
                    this.connections[i] = null;
                    this.getNetwork().split(this, wire);
                }
                else
                {
                    this.connections[i] = null;
                }
            }
        }
    }

    @Override
    public Object[] getConnections()
    {
        return this.connections;
    }

    public boolean canStay()
    {
        BlockCoord pos = new BlockCoord(tile()).offset(side);
        return MultipartUtility.canPlaceWireOnSide(world(), pos.x, pos.y, pos.z, ForgeDirection.getOrientation(side ^ 1), false);
    }

    public boolean dropIfCantStay()
    {
        if (!canStay())
        {
            drop();
            return true;
        }
        return false;
    }

    public void drop()
    {
        TileMultipart.dropItem(getItem(), world(), Vector3.fromTileEntityCenter(tile()));
        tile().remPart(this);
    }

    /** Recalculates connections to blocks outside this space
     * 
     * @return true if a new connection was added or one was removed */
    protected boolean updateExternalConnections()
    {
        int newConn = 0;

        for (int r = 0; r < 4; r++)
        {
            if (!maskOpen(r))
            {
                continue;
            }

            if (connectStraight(r))
            {
                newConn |= 0x10 << r;
            }
            else
            {
                int cnrMode = connectCorner(r);

                if (cnrMode != 0)
                {
                    newConn |= 1 << r;

                    if (cnrMode == 2)
                    {
                        newConn |= 0x100000 << r;// render flag
                    }
                }
            }
        }

        if (newConn != (connMap & 0xF000FF))
        {
            int diff = connMap ^ newConn;
            connMap = connMap & ~0xF000FF | newConn;

            // Notify corner disconnections
            for (int r = 0; r < 4; r++)
            {
                if ((diff & 1 << r) != 0)
                {
                    notifyCornerChange(r);
                }
            }

            return true;
        }

        return false;
    }

    /** Recalculates connections to other parts within this space
     * 
     * @return true if a new connection was added or one was removed */
    protected boolean updateInternalConnections()
    {
        int newConn = 0;
        for (int r = 0; r < 4; r++)
        {
            if (connectInternal(r))
            {
                newConn |= 0x100 << r;
            }
        }

        if (connectCenter())
        {
            newConn |= 0x10000;
        }

        if (newConn != (connMap & 0x10F00))
        {
            connMap = connMap & ~0x10F00 | newConn;
            return true;
        }
        return false;
    }

    /** Recalculates connections that can be made to other parts outside of this space
     * 
     * @return true if external connections should be recalculated */
    protected boolean updateOpenConnections()
    {
        int newConn = 0;
        for (int r = 0; r < 4; r++)
            if (connectionOpen(r))
                newConn |= 0x1000 << r;

        if (newConn != (connMap & 0xF000))
        {
            connMap = connMap & ~0xF000 | newConn;
            return true;
        }
        return false;
    }

    public boolean connectionOpen(int r)
    {
        int absDir = Rotation.rotateSide(side, r);
        TMultiPart facePart = tile().partMap(absDir);
        if (facePart != null && (!(facePart instanceof PartFlatWire) || !canConnectTo(facePart, ForgeDirection.getOrientation(absDir))))
            return false;

        if (tile().partMap(PartMap.edgeBetween(side, absDir)) != null)
            return false;

        return true;
    }

    /** Return a corner connection state. 0 = No connection 1 = Physical connection 2 = Render
     * connection */
    public int connectCorner(int r)
    {
        int absDir = Rotation.rotateSide(side, r);

        BlockCoord pos = new BlockCoord(tile());
        pos.offset(absDir);

        if (!canConnectThroughCorner(pos, absDir ^ 1, side))
            return 0;

        pos.offset(side);
        TileMultipart t = MultipartUtility.getMultipartTile(world(), pos);
        if (t != null)
        {
            TMultiPart tp = t.partMap(absDir ^ 1);

            if (canConnectTo(tp, ForgeDirection.getOrientation(absDir)))
            {
                if (tp instanceof PartFlatWire)
                {
                    boolean b = ((PartFlatWire) tp).connectCorner(this, Rotation.rotationTo(absDir ^ 1, side ^ 1));

                    if (b)
                    {
                        // let them connect to us
                        if (!renderThisCorner((PartFlatWire) tp))
                        {
                            return 1;
                        }

                        return 2;
                    }
                }

                return 2;
            }
        }
        return 0;
    }

    public boolean canConnectThroughCorner(BlockCoord pos, int side1, int side2)
    {
        if (world().isAirBlock(pos.x, pos.y, pos.z))
        {
            return true;
        }

        TileMultipart t = MultipartUtility.getMultipartTile(world(), pos);
        if (t != null)
        {
            return t.partMap(side1) == null && t.partMap(side2) == null && t.partMap(PartMap.edgeBetween(side1, side2)) == null;
        }

        return false;
    }

    public boolean connectStraight(int r)
    {
        int absDir = Rotation.rotateSide(side, r);

        BlockCoord pos = new BlockCoord(tile()).offset(absDir);

        TileMultipart t = MultipartUtility.getMultipartTile(world(), pos);
        if (t != null)
        {
            TMultiPart tp = t.partMap(side);

            if (this.canConnectTo(tp, ForgeDirection.getOrientation(absDir)))
            {
                if (tp instanceof PartFlatWire)
                {
                    return ((PartFlatWire) tp).connectStraight(this, (r + 2) % 4);
                }

                return true;
            }
        }
        else
        {
            TileEntity tileEntity = world().getBlockTileEntity(pos.x, pos.y, pos.z);
            return this.canConnectTo(tileEntity, ForgeDirection.getOrientation(absDir));
        }

        return false;
    }

    public boolean connectInternal(int r)
    {
        int absDir = Rotation.rotateSide(side, r);

        if (tile().partMap(PartMap.edgeBetween(absDir, side)) != null)
            return false;

        TMultiPart tp = tile().partMap(absDir);

        if (this.canConnectTo(tp, ForgeDirection.getOrientation(absDir)))
        {
            return ((PartFlatWire) tp).connectInternal(this, Rotation.rotationTo(absDir, side));
        }

        return connectInternalOverride(tp, r);
    }

    public boolean connectInternalOverride(TMultiPart p, int r)
    {
        return false;
    }

    public boolean connectCenter()
    {
        TMultiPart tp = tile().partMap(6);

        if (this.canConnectTo(tp))
        {
            if (tp instanceof PartFlatWire)
            {
                return ((PartFlatWire) tp).connectInternal(this, side);
            }

            return true;
        }

        return false;
    }

    public boolean renderThisCorner(PartFlatWire part)
    {
        if (!(part instanceof PartFlatWire))
            return false;

        PartFlatWire wire = part;
        if (wire.getThickness() == getThickness())
            return side < wire.side;

        return wire.getThickness() > getThickness();
    }

    public boolean connectCorner(PartFlatWire wire, int r)
    {
        int absDir = Rotation.rotateSide(side, r);

        if (this.canConnectTo(wire, ForgeDirection.getOrientation(absDir)) && maskOpen(r))
        {
            int oldConn = connMap;
            connMap |= 0x1 << r;
            if (renderThisCorner(wire))// render connection
                connMap |= 0x100000 << r;

            if (oldConn != connMap)
                sendConnUpdate();
            return true;
        }
        return false;
    }

    public boolean connectStraight(PartFlatWire wire, int r)
    {
        int absDir = Rotation.rotateSide(side, r);

        if (this.canConnectTo(wire, ForgeDirection.getOrientation(absDir)) && maskOpen(r))
        {
            int oldConn = connMap;
            connMap |= 0x10 << r;
            if (oldConn != connMap)
                sendConnUpdate();
            return true;
        }
        return false;
    }

    public boolean connectInternal(PartFlatWire wire, int r)
    {
        int absDir = Rotation.rotateSide(side, r);

        if (this.canConnectTo(wire, ForgeDirection.getOrientation(absDir)))
        {
            int oldConn = connMap;
            connMap |= 0x100 << r;
            if (oldConn != connMap)
                sendConnUpdate();
            return true;
        }
        return false;
    }

    public boolean canConnectCorner(int r)
    {
        return true;
    }

    public void notifyCornerChange(int r)
    {
        int absDir = Rotation.rotateSide(side, r);

        BlockCoord pos = new BlockCoord(tile()).offset(absDir).offset(side);
        world().notifyBlockOfNeighborChange(pos.x, pos.y, pos.z, tile().getBlockType().blockID);
    }

    public void notifyStraightChange(int r)
    {
        int absDir = Rotation.rotateSide(side, r);

        BlockCoord pos = new BlockCoord(tile()).offset(absDir);
        world().notifyBlockOfNeighborChange(pos.x, pos.y, pos.z, tile().getBlockType().blockID);
    }

    public boolean maskConnects(int r)
    {
        return (connMap & 0x111 << r) != 0;
    }

    public boolean maskOpen(int r)
    {
        return (connMap & 0x1000 << r) != 0;
    }

    /** START TILEMULTIPART INTERACTIONS **/
    @Override
    public float getStrength(MovingObjectPosition hit, EntityPlayer player)
    {
        return 4;
    }

    @Override
    public int getSlotMask()
    {
        return 1 << this.side;
    }

    @Override
    public Iterable<IndexedCuboid6> getSubParts()
    {
        return Arrays.asList(new IndexedCuboid6(0, selectionBounds[getThickness()][side]));
    }

    @Override
    public boolean occlusionTest(TMultiPart npart)
    {
        return NormalOcclusionTest.apply(this, npart);
    }

    @Override
    public Iterable<Cuboid6> getOcclusionBoxes()
    {
        return Arrays.asList(occlusionBounds[getThickness()][side]);
    }

    public int getThickness()
    {
        return this.isInsulated ? 2 : 1;
    }

    @Override
    public int redstoneConductionMap()
    {
        return 0;
    }

    @Override
    public boolean solid(int arg0)
    {
        return false;
    }

    @Override
    public String getType()
    {
        return "resonant_induction_flat_wire";
    }

    /** RENDERING */
    @SideOnly(Side.CLIENT)
    public Icon getIcon()
    {
        return RenderFlatWire.flatWireTexture;
    }

    public Colour getColour()
    {
        if (isInsulated)
        {
            Colour color = new ColourARGB(ItemDye.dyeColors[this.color]);
            color.a = (byte) 255;
            return color;
        }

        return getMaterial().color;
    }

    public boolean useStaticRenderer()
    {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatic(Vector3 pos, LazyLightMatrix olm, int pass)
    {
        if (pass == 0 && useStaticRenderer())
        {
            CCRenderState.setBrightness(world(), x(), y(), z());
            RenderFlatWire.render(this, pos);
            CCRenderState.setColour(-1);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(Vector3 pos, float frame, int pass)
    {
        if (pass == 0 && !useStaticRenderer())
        {
            GL11.glDisable(GL11.GL_LIGHTING);
            TextureUtils.bindAtlas(0);
            CCRenderState.useModelColours(true);
            CCRenderState.startDrawing(7);
            RenderFlatWire.render(this, pos);
            CCRenderState.draw();
            CCRenderState.setColour(-1);
            GL11.glEnable(GL11.GL_LIGHTING);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawBreaking(RenderBlocks renderBlocks)
    {
        CCRenderState.reset();
        RenderFlatWire.renderBreakingOverlay(renderBlocks.overrideBlockTexture, this);
    }

    /** Utility method to aid in initializing this or subclasses, usually when you need to change the
     * wire to another type
     * 
     * @param otherCable the wire to copy from */
    public void copyFrom(PartFlatWire otherCable)
    {
        this.isInsulated = otherCable.isInsulated;
        this.color = otherCable.color;
        this.connections = otherCable.connections;
        this.material = otherCable.material;
        this.side = otherCable.side;
        this.connMap = otherCable.connMap;
        this.setNetwork(otherCable.getNetwork());
        this.getNetwork().setBufferFor(this, otherCable.getNetwork().getBufferOf(otherCable));
    }

    @Override
    public String toString()
    {
        return "[PartFlatWire]" + x() + "x " + y() + "y " + z() + "z " + getSlotMask() + "s ";
    }
}