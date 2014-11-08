package resonantinduction.atomic.machine.reactor

import java.util.{ArrayList, HashSet, List, Set}

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{IIcon, MathHelper}
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._
import resonant.content.prefab.java.TileAdvanced
import resonant.content.spatial.block.SpatialBlock
import resonant.lib.path.{IPathCallBack, Pathfinder}
import resonant.lib.transform.vector.Vector3

/**
 * Reactor Drain
 *
 * @author Calclavia
 */
class TileReactorDrain extends TileAdvanced(Material.iron) with IFluidHandler
{
    private final val tanks: Set[IFluidTank] = new HashSet[IFluidTank]
    private var lastFindTime: Long = -1

    def find
    {
        this.tanks.clear
        val world: World = this.worldObj
        val position: Vector3 = toVector3
        val finder: Pathfinder = new Pathfinder(new IPathCallBack
        {
            def getConnectedNodes(finder: Pathfinder, currentNode: Vector3): Set[Vector3] =
            {
                val neighbors: Set[Vector3] = new HashSet[Vector3]

                for (i <- 0 to 6)
                {
                    val direction: ForgeDirection = ForgeDirection.getOrientation(i)
                    val position: Vector3 = currentNode.clone.add(direction)
                    val block: Block = position.getBlock(world)
                    if (block == null || block.isInstanceOf[IFluidBlock] || position.getTileEntity(world).isInstanceOf[TileReactorCell])
                    {
                        neighbors.add(position)
                    }
                }
                return neighbors
            }

            def onSearch(finder: Pathfinder, start: Vector3, node: Vector3): Boolean =
            {
                if (node.getTileEntity(world).isInstanceOf[TileReactorCell])
                {
                    finder.results.add(node)
                }
                if (node.distance(position) > 6)
                {
                    return true
                }
                return false
            }
        }).init(toVector3.add(ForgeDirection.getOrientation(this.getBlockMetadata).getOpposite))
        import scala.collection.JavaConversions._
        for (node <- finder.results)
        {
            val tileEntity: TileEntity = node.getTileEntity(this.worldObj)
            if (tileEntity.isInstanceOf[TileReactorCell])
            {
                this.tanks.add((tileEntity.asInstanceOf[TileReactorCell]).tank)
            }
        }
        this.lastFindTime = this.worldObj.getWorldTime
    }

    def getOptimalTank: IFluidTank =
    {
        if (this.lastFindTime == -1 || this.worldObj.getWorldTime - this.lastFindTime > 20)
        {
            this.find
        }
        if (this.tanks.size > 0)
        {
            var optimalTank: IFluidTank = null
            import scala.collection.JavaConversions._
            for (tank <- this.tanks)
            {
                if (tank != null)
                {
                    if (optimalTank == null || (optimalTank != null && getFluidSafe(tank.getFluid) > getFluidSafe(optimalTank.getFluid)))
                    {
                        optimalTank = tank
                    }
                }
            }
            return optimalTank
        }
        return null
    }

    def getFluidSafe(stack: FluidStack): Int =
    {
        if (stack != null)
        {
            return stack.amount
        }
        return 0
    }

    override def canUpdate: Boolean =
    {
        return false
    }

    def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
    {
        return 0
    }

    def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack =
    {
        if (this.getOptimalTank != null)
        {
            return this.getOptimalTank.drain(maxDrain, doDrain)
        }
        return null
    }

    def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack =
    {
        return null
    }

    def canFill(from: ForgeDirection, fluid: Fluid): Boolean =
    {
        return false
    }

    def canDrain(from: ForgeDirection, fluid: Fluid): Boolean =
    {
        return true
    }

    def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] =
    {
        val tankInfoList: List[FluidTankInfo] = new ArrayList[FluidTankInfo]
        this.getOptimalTank
        import scala.collection.JavaConversions._
        for (tank <- this.tanks)
        {
            tankInfoList.add(tank.getInfo)
        }
        return tankInfoList.toArray(new Array[FluidTankInfo](0))
    }

    @SideOnly(Side.CLIENT) override def registerIcons(iconRegister: IIconRegister)
    {
        super.registerIcons(iconRegister)
        SpatialBlock.icon.put("ReactorDrain_front", iconRegister.registerIcon("ReactorDrain_front"))
    }

    override def getIcon(side: Int, metadata: Int): IIcon =
    {
        if (side == metadata)
        {
            return SpatialBlock.icon.get("ReactorDrain_front")
        }
        return super.getIcon(side, metadata)
    }

    override def onPlaced(entityLiving: EntityLivingBase, itemStack: ItemStack)
    {
        if (MathHelper.abs(entityLiving.posX.asInstanceOf[Float] - xi) < 2.0F && MathHelper.abs(entityLiving.posZ.asInstanceOf[Float] - zi) < 2.0F)
        {
            val d0: Double = entityLiving.posY + 1.82D - entityLiving.yOffset
            if (d0 - y > 2.0D)
            {
                world.setBlockMetadataWithNotify(xi, yi, zi, 1, 3)
                return
            }
            if (y - d0 > 0.0D)
            {
                world.setBlockMetadataWithNotify(xi, yi, zi, 0, 3)
                return
            }
        }
        super.onPlaced(entityLiving, itemStack)
    }
}