package resonantinduction.mechanical.mech.turbine

import java.lang.reflect.Method
import java.util.List

import cpw.mods.fml.relauncher.ReflectionHelper
import net.minecraft.block.{Block, BlockDynamicLiquid}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Blocks
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Vec3
import net.minecraft.world.IBlockAccess
import net.minecraftforge.common.util.ForgeDirection
import resonant.content.prefab.itemblock.ItemBlockMetadata
import resonantinduction.core.Settings
import resonantinduction.mechanical.mech.MechanicalNode
import universalelectricity.api.core.grid.INodeProvider
import universalelectricity.core.transform.vector.Vector3
import resonant.lib.wrapper.WrapList._

/**
 * The vertical water turbine collects flowing water flowing on X axis.
 * The horizontal water turbine collects flowing water on Z axis.
 *
 * @author Calclavia
 *
 */
class TileWaterTurbine extends TileTurbine
{
    var powerTicks: Int = 0

    //Constructor
    this.itemBlock_$eq(classOf[ItemBlockMetadata])
    mechanicalNode.torque = defaultTorque
    mechanicalNode = new TurbineNode((this))
    {
        override def canConnect(from: ForgeDirection, source: AnyRef): Boolean =
        {
            if (source.isInstanceOf[MechanicalNode] && !(source.isInstanceOf[TileTurbine]))
            {
                val sourceTile: TileEntity = position.add(from).getTileEntity(getWorld)
                if (sourceTile.isInstanceOf[INodeProvider])
                {
                    val sourceInstance: MechanicalNode = (sourceTile.asInstanceOf[INodeProvider]).getNode(classOf[MechanicalNode], from.getOpposite).asInstanceOf[MechanicalNode]
                    return sourceInstance == source && (from == getDirection.getOpposite || from == getDirection)
                }
            }
            return false
        }
    }
    //End Constructor

    override def update
    {
        super.update
        if (getMultiBlock.isConstructed)
        {
            mechanicalNode.torque = (defaultTorque / (1d / multiBlockRadius)).asInstanceOf[Long]
        }
        else
        {
            mechanicalNode.torque = defaultTorque / 12
        }
        if (getDirection.offsetY != 0)
        {
            maxPower = 10000
            if (powerTicks > 0)
            {
                getMultiBlock.get.power += getWaterPower
                powerTicks -= 1
            }
            if (ticks % 20 == 0)
            {
                val blockIDAbove: Block = worldObj.getBlock(xCoord, yCoord + 1, zCoord)
                val metadata: Int = worldObj.getBlockMetadata(xCoord, yCoord + 1, zCoord)
                val isWater: Boolean = (blockIDAbove == Blocks.water || blockIDAbove == Blocks.flowing_water)
                if (isWater && worldObj.isAirBlock(xCoord, yCoord - 1, zCoord) && metadata == 0)
                {
                    powerTicks = 20
                    worldObj.setBlockToAir(xCoord, yCoord + 1, zCoord)
                    worldObj.setBlock(xCoord, yCoord - 1, zCoord, Blocks.flowing_water)
                }
            }
        }
        else
        {
            maxPower = 2500
            val currentDir: ForgeDirection = getDirection
            for (dir <- ForgeDirection.VALID_DIRECTIONS)
            {
                if (dir != currentDir && dir != currentDir.getOpposite)
                {
                    val check: Vector3 = new Vector3(this).add(dir)
                    val blockID: Block = worldObj.getBlock(check.xi, check.yi, check.zi)
                    val metadata: Int = worldObj.getBlockMetadata(check.xi, check.yi, check.zi)
                    if (blockID == Blocks.water || blockID == Blocks.flowing_water)
                    {
                        try
                        {
                            val m: Method = ReflectionHelper.findMethod(classOf[BlockDynamicLiquid], null, Array[String]("getFlowVector", "func_72202_i"), classOf[IBlockAccess], Integer.TYPE, Integer.TYPE, Integer.TYPE)
                            val vector: Vector3 = new Vector3(m.invoke(Blocks.water, Array(worldObj, check.xi, check.yi, check.zi)).asInstanceOf[Vec3])
                            if ((currentDir.offsetZ > 0 && vector.x < 0) || (currentDir.offsetZ < 0 && vector.x > 0) || (currentDir.offsetX > 0 && vector.z > 0) || (currentDir.offsetX < 0 && vector.z < 0))
                            {
                                mechanicalNode.torque = -mechanicalNode.torque
                            }
                            if (getDirection.offsetX != 0)
                            {
                                getMultiBlock.get.power += Math.abs(getWaterPower * vector.z * (7 - metadata) / 7f)
                                powerTicks = 20
                            }
                            if (getDirection.offsetZ != 0)
                            {
                                getMultiBlock.get.power += Math.abs(getWaterPower * vector.x * (7 - metadata) / 7f)
                                powerTicks = 20
                            }
                        }
                        catch
                            {
                                case e: Exception =>
                                {
                                    e.printStackTrace
                                }
                            }
                    }
                }
            }
        }
    }

    /**
     * Gravitation Potential Energy:
     * PE = mgh
     */
    private def getWaterPower: Long =
    {
        return (maxPower / (2 - tier + 1)) * Settings.WATER_POWER_RATIO
    }

    override def getSubBlocks(par1: Item, par2CreativeTabs: CreativeTabs, par3List: List[_])
    {
        for (i <- 0 to 3)
        {
            par3List.add(new ItemStack(par1, 1, i))
        }
    }
}