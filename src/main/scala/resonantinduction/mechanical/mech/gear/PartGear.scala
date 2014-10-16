package resonantinduction.mechanical.mech.gear

import java.util

import codechicken.lib.vec.{Cuboid6, Rotation, Transformation, Vector3}
import codechicken.microblock.FaceMicroClass
import codechicken.multipart.ControlKeyModifer
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.MovingObjectPosition
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.multiblock.reference.IMultiBlockStructure
import resonant.lib.utility.WrenchUtility
import resonantinduction.core.Reference
import resonantinduction.mechanical.MechanicalContent
import resonantinduction.mechanical.mech.PartMechanical
import universalelectricity.api.core.grid.INode
import universalelectricity.core.transform.vector.VectorWorld

/** We assume all the force acting on the gear is 90 degrees.
  *
  * @author Calclavia */
object PartGear
{
    var oBoxes: Array[Array[Cuboid6]] = new Array[Array[Cuboid6]](6)

    oBoxes(0)(0) = new Cuboid6(1 / 8D, 0, 0, 7 / 8D, 1 / 8D, 1)
    oBoxes(0)(1) = new Cuboid6(0, 0, 1 / 8D, 1, 1 / 8D, 7 / 8D)

    for(s <- 1 until 6)
    {
        val t: Transformation = Rotation.sideRotations(s).at(Vector3.center)
        oBoxes(s)(0) = oBoxes(0)(0).copy.apply(t)
        oBoxes(s)(1) = oBoxes(0)(1).copy.apply(t)

    }
}

class PartGear extends PartMechanical with IMultiBlockStructure[PartGear]
{
    var isClockwiseCrank: Boolean = true
    var manualCrankTime: Int = 0
    var multiBlockRadius: Int = 1
    /** Multiblock */
    var multiBlock: GearMultiBlockHandler = null

    //Constructor
    node = new GearNode(this)

    override def update
    {
        super.update
        if (!this.world.isRemote)
        {
            if (manualCrankTime > 0)
            {
                node.apply(this, if (isClockwiseCrank) 15 else -15, if (isClockwiseCrank) 0.025f else -0.025f)
                manualCrankTime -= 1
            }
        }
        getMultiBlock.update
    }

    override def checkClientUpdate
    {
        if (getMultiBlock.isPrimary) super.checkClientUpdate
    }

    override def activate(player: EntityPlayer, hit: MovingObjectPosition, itemStack: ItemStack): Boolean =
    {
        if (itemStack != null && itemStack.getItem.isInstanceOf[ItemHandCrank])
        {
            if (!world.isRemote && ControlKeyModifer.isControlDown(player))
            {
                getMultiBlock.get.node.torque = -getMultiBlock.get.node.torque
                getMultiBlock.get.node.angularVelocity = -getMultiBlock.get.node.angularVelocity
                return true
            }
            isClockwiseCrank = player.isSneaking
            //TODO fix;
            // getMultiBlock.get.manualCrankTime = 20
            world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, Reference.prefix + "gearCrank", 0.5f, 0.9f + world.rand.nextFloat * 0.2f)
            player.addExhaustion(0.01f)
            return true
        }
        if (WrenchUtility.isWrench(itemStack))
        {
            getMultiBlock.toggleConstruct
            return true
        }
        return super.activate(player, hit, itemStack)
    }

    override def preRemove
    {
        super.preRemove
        getMultiBlock.deconstruct
    }

    /** Is this gear block the one in the center-edge of the multiblock that can interact with other
      * gears?
      *
      * @return */
    def isCenterMultiBlock: Boolean =
    {
        if (!getMultiBlock.isConstructed)
        {
            return true
        }
        val primaryPos: VectorWorld = getMultiBlock.getPrimary.getPosition
        if (primaryPos.xi == x && placementSide.offsetX == 0)
        {
            return true
        }
        if (primaryPos.yi == y && placementSide.offsetY == 0)
        {
            return true
        }
        if (primaryPos.zi == z && placementSide.offsetZ == 0)
        {
            return true
        }
        return false
    }

    protected def getItem: ItemStack =
    {
        return new ItemStack(MechanicalContent.itemGear, 1, tier)
    }

    @SideOnly(Side.CLIENT) override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
    {
        if (pass == 0)
        {
            RenderGear.INSTANCE.renderDynamic(this, pos.x, pos.y, pos.z, tier)
        }
    }

    override def load(nbt: NBTTagCompound)
    {
        super.load(nbt)
        getMultiBlock.load(nbt)
    }

    override def save(nbt: NBTTagCompound)
    {
        super.save(nbt)
        getMultiBlock.save(nbt)
    }

    override def getMultiBlockVectors: java.util.List[universalelectricity.core.transform.vector.Vector3] =
    {
        val vec = new universalelectricity.core.transform.vector.Vector3(this.x, this.y, this.z)
        var array: java.util.List[universalelectricity.core.transform.vector.Vector3] = vec.getAround(this.world, placementSide, 1)
        return array
    }

    def getWorld: World =
    {
        return world
    }

    def onMultiBlockChanged
    {
        if (world != null)
        {
            tile.notifyPartChange(this)
            if (!world.isRemote)
            {
                sendDescUpdate
            }
        }
    }

    def getMultiBlock: GearMultiBlockHandler =
    {
        if (multiBlock == null) multiBlock = new GearMultiBlockHandler(this)
        return multiBlock
    }

    override def getNode(nodeType: Class[_ <: INode], from: ForgeDirection): INode =
    {
        if (nodeType.isAssignableFrom(node.getClass)) return getMultiBlock.get.node
        return null
    }

    /** Multipart Bounds */
    def getOcclusionBoxes: java.lang.Iterable[Cuboid6] =
    {
        val list: java.util.List[Cuboid6] = new util.ArrayList[Cuboid6];
        for (v <- PartGear.oBoxes(this.placementSide.ordinal))
        {
            list.add(v)
        }
        return list
    }

    def getSlotMask: Int =
    {
        return 1 << this.placementSide.ordinal
    }

    def getBounds: Cuboid6 =
    {
        return FaceMicroClass.aBounds(0x10 | this.placementSide.ordinal)
    }

    @SideOnly(Side.CLIENT) override def getRenderBounds: Cuboid6 =
    {
        return Cuboid6.full.copy.expand(multiBlockRadius)
    }

    override def toString: java.lang.String =
    {
        return "[PartGear]" + x + "x " + y + "y " + z + "z " + getSlotMask + "s "
    }
}