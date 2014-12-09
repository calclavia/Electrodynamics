package resonantinduction.atomic.machine

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IIcon
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._
import resonant.api.tile.IBoilHandler
import resonant.content.prefab.java.TileAdvanced

/**
 * Funnel for gas.
 */
object TileFunnel
{
    private var iconTop: IIcon = null
}

class TileFunnel extends TileAdvanced(Material.iron) with IBoilHandler
{
    private final val tank: FluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 16)

    override def getIcon(side: Int, meta: Int): IIcon =
    {
        return if (side == 1 || side == 0) TileFunnel.iconTop else super.getIcon(side, meta)
    }

    @SideOnly(Side.CLIENT) override def registerIcons(iconRegister: IIconRegister)
    {
        super.registerIcons(iconRegister)
        TileFunnel.iconTop = iconRegister.registerIcon(getTextureName + "_top")
    }

    override def update
    {
        super.update
        if (tank.getFluidAmount > 0)
        {
            val tileEntity: TileEntity = this.worldObj.getTileEntity(this.xCoord, this.yCoord + 1, this.zCoord)
            if (tileEntity.isInstanceOf[IFluidHandler])
            {
                val handler: IFluidHandler = tileEntity.asInstanceOf[IFluidHandler]
                if (handler.canFill(ForgeDirection.DOWN, tank.getFluid.getFluid))
                {
                    val drainedStack: FluidStack = tank.drain(tank.getCapacity, false)
                    if (drainedStack != null)
                    {
                        tank.drain(handler.fill(ForgeDirection.DOWN, drainedStack, true), true)
                    }
                }
            }
        }
    }

    override def readFromNBT(tag: NBTTagCompound)
    {
        super.readFromNBT(tag)
        tank.writeToNBT(tag)
    }

    override def writeToNBT(tag: NBTTagCompound)
    {
        super.writeToNBT(tag)
        tank.readFromNBT(tag)
    }

    /**
     * Tank Methods
     */
    def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
    {
        return tank.fill(resource, doFill)
    }

    def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack =
    {
        return this.tank.drain(maxDrain, doDrain)
    }

    def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack =
    {
        if (resource == null || !resource.isFluidEqual(tank.getFluid))
        {
            return null
        }
        return tank.drain(resource.amount, doDrain)
    }

    def canFill(from: ForgeDirection, fluid: Fluid): Boolean =
    {
        if (fluid.isGaseous && from == ForgeDirection.DOWN)
        {
            return true
        }
        return false
    }

    def canDrain(from: ForgeDirection, fluid: Fluid): Boolean =
    {
        if (fluid.isGaseous && from == ForgeDirection.UP)
        {
            return true
        }
        return false
    }

    def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] =
    {
        return Array[FluidTankInfo](tank.getInfo)
    }
}