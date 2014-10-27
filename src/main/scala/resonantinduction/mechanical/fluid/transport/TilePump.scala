package resonantinduction.mechanical.fluid.transport

import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{Fluid, FluidStack, FluidTankInfo, IFluidHandler}
import resonant.api.IRotatable
import resonantinduction.mechanical.mech.TileMechanical
import resonant.api.grid.INode

class TilePump extends TileMechanical(Material.iron) with IRotatable with IFluidHandler
{
    var pressureNode : PumpNode = null

    //Constructor
    normalRender(false)
    isOpaqueCube(false)
    customItemRender(true)
    setTextureName("material_steel")
    pressureNode = new PumpNode(this)


    override def start
    {
        pressureNode.reconstruct
        super.start
    }

    override def invalidate
    {
        super.invalidate
        pressureNode.deconstruct
    }

    override def update
    {
        super.update
        if (!worldObj.isRemote && mechanicalNode.getPower > 0)
        {
            val tileIn: TileEntity = asVector3.add(getDirection.getOpposite).getTileEntity(this.worldObj)
            if (tileIn.isInstanceOf[IFluidHandler])
            {
                val drain: FluidStack = (tileIn.asInstanceOf[IFluidHandler]).drain(getDirection, pressureNode.getCapacity, false)
                if (drain != null)
                {
                    (tileIn.asInstanceOf[IFluidHandler]).drain(getDirection, fill(getDirection.getOpposite, drain, true), true)
                }
            }
        }
    }

    def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
    {
        if (from eq getDirection.getOpposite)
        {
            val tileOut: TileEntity = asVector3.add(from.getOpposite).getTileEntity(this.worldObj)
            if (tileOut.isInstanceOf[IFluidHandler]) return (tileOut.asInstanceOf[IFluidHandler]).fill(from, resource, doFill)
        }
        return 0
    }

    def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack =
    {
        return null
    }

    def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack =
    {
        return null
    }

    def canFill(from: ForgeDirection, fluid: Fluid): Boolean =
    {
        return from eq this.getDirection.getOpposite
    }

    def canDrain(from: ForgeDirection, fluid: Fluid): Boolean =
    {
        return from eq this.getDirection
    }

    def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] =
    {
        return null
    }

    override def getNode(nodeType: Class[_ <: INode], from: ForgeDirection): INode =
    {
        if (nodeType.isAssignableFrom(pressureNode.getClass)) return pressureNode
        return super.getNode(nodeType, from)
    }

    override def getDirection: ForgeDirection =
    {
        return null
    }

    override def setDirection(direction: ForgeDirection)
    {
    }
}