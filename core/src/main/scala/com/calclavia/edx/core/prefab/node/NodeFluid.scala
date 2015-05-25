package com.calclavia.edx.core.prefab.node

import net.minecraft.nbt.NBTTagCompound
import nova.core.util.Direction
import net.minecraftforge.fluids._
import resonantengine.api.graph.INodeProvider
import resonantengine.api.graph.node.INode
import resonantengine.api.misc.ISave
import resonantengine.lib.grid.core.{NodeConnector, TTileConnector}
import resonantengine.prefab.fluid.{TFluidHandler, TFluidTank}

/**
 * A node that handles fluid interactions
 *
 * @param parent Parent(TileEntity or Multipart) that contains this node
 * @param volume Amount of fluid in liters
 * @author Calclavia
 */
class NodeFluid(parent: INodeProvider, volume: Int = FluidContainerRegistry.BUCKET_VOLUME) extends NodeConnector[IFluidHandler](parent) with ISave with TFluidHandler with TFluidTank with TTileConnector[IFluidHandler]
{
  var onFluidChanged: () => Unit = () => ()
  /** Internal tank */
  private var tank = new FluidTank(volume)

  override def fill(from: Direction, resource: FluidStack, doFill: Boolean): Int =
  {
    tank synchronized
    {
      if (canConnect(from))
      {
        val ret = super.fill(from, resource, doFill)
        onFluidChanged()
        return ret
      }
      return 0
    }
  }

  override def drain(from: Direction, resource: FluidStack, doDrain: Boolean): FluidStack =
  {
    tank synchronized
    {
      if (canConnect(from))
      {
        val ret = super.drain(from, resource, doDrain)
        onFluidChanged()
        return ret
      }
      return null
    }
  }

  override def drain(from: Direction, maxDrain: Int, doDrain: Boolean): FluidStack =
  {
    tank synchronized
    {
      if (canConnect(from))
      {
        val ret = super.drain(from, maxDrain, doDrain)
        onFluidChanged()
        return ret
      }

      return null
    }
  }

  override def canFill(from: Direction, fluid: Fluid): Boolean = canConnect(from)

  override def canDrain(from: Direction, fluid: Fluid): Boolean = canConnect(from)

  override def load(nbt: NBTTagCompound)
  {
    getTank.readFromNBT(nbt.getCompoundTag("tank"))
  }

  override def getTank: FluidTank = tank

  override def save(nbt: NBTTagCompound)
  {
    nbt.setTag("tank", getTank.writeToNBT(new NBTTagCompound))
  }

  /**
   * Sets the primary tank (not checked)
   * @param t - The new tank
   */
  def setPrimaryTank(t: FluidTank) = tank = t

  /**
   * The class used to compare when making connections
   */
  override protected def getCompareClass: Class[_ <: NodeFluid with INode] = classOf[NodeFluid]

  protected def showConnectionsFor(obj: AnyRef, dir: Direction): Boolean =
  {
    if (obj != null)
    {
      if (obj.getClass.isAssignableFrom(getParent.getClass))
      {
        return true
      }
    }
    return false
  }
}
