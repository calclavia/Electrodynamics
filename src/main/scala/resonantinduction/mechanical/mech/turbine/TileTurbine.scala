package resonantinduction.mechanical.mech.turbine

import java.util.{HashSet, Set}

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.AxisAlignedBB
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.multiblock.reference.IMultiBlockStructure
import resonant.lib.network.handle.IPacketIDReceiver
import resonantinduction.mechanical.mech.TileMechanical
import resonantinduction.mechanical.mech.gear.ItemHandCrank
import resonant.api.grid.INodeProvider
import resonant.lib.transform.vector.Vector3

/** Reduced version of the main turbine class */
class TileTurbine extends TileMechanical(Material.wood) with IMultiBlockStructure[TileTurbine] with INodeProvider with IPacketIDReceiver
{
    /** Tier of the tile */
    var tier: Int = 0
    /** Radius of large turbine? */
    var multiBlockRadius: Int = 1
    /** Max power in watts. */
    protected var maxPower: Long = 0L
    /** The power of the turbine this tick. In joules/tick */
    var power: Double = 0
    protected final val defaultTorque: Long = 5000
    /** MutliBlock methods. */
    private var multiBlock: TurbineMBlockHandler = null

    //Constructor
    normalRender(false)
    isOpaqueCube(false)
    setTextureName("material_wood_surface")
    mechanicalNode = new TurbineNode(this)
    rotationMask = 63


    override def onRemove(block: Block, par1: Int)
    {
        super.onRemove(block, par1)
        getMultiBlock.deconstruct
    }

    override def update
    {
        super.update
        getMultiBlock.update
        if (getMultiBlock.isPrimary)
        {
            if (!worldObj.isRemote)
            {
                mechanicalNode.angularVelocity = (power.asInstanceOf[Double] / mechanicalNode.torque).asInstanceOf[Float]
            }
            if (mechanicalNode.angularVelocity != 0)
            {
                playSound
            }
        }
        if (!worldObj.isRemote) power = 0
    }

    protected def getMaxPower: Long =
    {
        if (this.getMultiBlock.isConstructed)
        {
            return (maxPower * getArea).asInstanceOf[Long]
        }
        return maxPower
    }

    def getArea: Int =
    {
        return (((multiBlockRadius + 0.5) * 2) * ((multiBlockRadius + 0.5) * 2)).asInstanceOf[Int]
    }

    /** Called to play sound effects */
    def playSound
    {
    }

    /** Reads a tile entity from NBT. */
    override def readFromNBT(nbt: NBTTagCompound)
    {
        super.readFromNBT(nbt)
        multiBlockRadius = nbt.getInteger("multiBlockRadius")
        tier = nbt.getInteger("tier")
        getMultiBlock.load(nbt)
    }

    /** Writes a tile entity to NBT. */
    override def writeToNBT(nbt: NBTTagCompound)
    {
        super.writeToNBT(nbt)
        nbt.setInteger("multiBlockRadius", multiBlockRadius)
        nbt.setInteger("tier", tier)
        getMultiBlock.save(nbt)
    }

    @SideOnly(Side.CLIENT) override def getRenderBoundingBox: AxisAlignedBB =
    {
        return AxisAlignedBB.getBoundingBox(this.xCoord - multiBlockRadius, this.yCoord - multiBlockRadius, this.zCoord - multiBlockRadius, this.xCoord + 1 + multiBlockRadius, this.yCoord + 1 + multiBlockRadius, this.zCoord + 1 + multiBlockRadius)
    }

    def getMultiBlockVectors: java.lang.Iterable[Vector3] =
    {
        val vectors: Set[Vector3] = new HashSet[Vector3]
        val dir: ForgeDirection = getDirection
        val xMulti: Int = if (dir.offsetX != 0) 0 else 1
        val yMulti: Int = if (dir.offsetY != 0) 0 else 1
        val zMulti: Int = if (dir.offsetZ != 0) 0 else 1

        for (x : Int  <- -multiBlockRadius to multiBlockRadius)
        {
            for (y : Int  <- -multiBlockRadius to multiBlockRadius)
            {

                for (z : Int <- -multiBlockRadius to multiBlockRadius)
                {
                    vectors.add(new Vector3(x * xMulti, y * yMulti, z * zMulti))
                }
            }
        }

        return vectors
    }

    def getPosition: Vector3 =
    {
        return asVector3
    }

    def getMultiBlock: TurbineMBlockHandler =
    {
        if (multiBlock == null) multiBlock = new TurbineMBlockHandler(this)
        return multiBlock
    }

    def onMultiBlockChanged
    {
        worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, if (getBlockType != null) getBlockType else null)
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
    }

    def getWorld: World =
    {
        return worldObj
    }

    override def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
    {
        if (player.getCurrentEquippedItem != null && player.getCurrentEquippedItem.getItem.isInstanceOf[ItemHandCrank])
        {
            if (!world.isRemote)
            {
                mechanicalNode.torque = -mechanicalNode.torque
                mechanicalNode.angularVelocity = -mechanicalNode.angularVelocity
            }
            return true
        }
        return super.use(player, side, hit)
    }

    override def configure(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
    {
        if (!player.isSneaking)
        {
            if (getMultiBlock.isConstructed)
            {
                getMultiBlock.deconstruct
                multiBlockRadius += 1
                if (!getMultiBlock.construct)
                {
                    multiBlockRadius = 1
                }
                return true
            }
            else
            {
                if (!getMultiBlock.construct)
                {
                    multiBlockRadius = 1
                    getMultiBlock.construct
                }
            }
        }
        else
        {
            val toFlip: Set[TileTurbine] = new HashSet[TileTurbine]
            if (!getMultiBlock.isConstructed)
            {
                toFlip.add(this)
            }
            else
            {
                val str: Set[TileTurbine] = getMultiBlock.getPrimary.getMultiBlock.getStructure
                if (str != null) toFlip.addAll(str)
            }
            import scala.collection.JavaConversions._
            for (turbine <- toFlip)
            {
                if (side == turbine.getDirection.ordinal) world.setBlockMetadataWithNotify(turbine.xCoord, turbine.yCoord, turbine.zCoord, side ^ 1, 3)
                else world.setBlockMetadataWithNotify(turbine.xCoord, turbine.yCoord, turbine.zCoord, side, 3)
            }
        }
        return true
    }
}