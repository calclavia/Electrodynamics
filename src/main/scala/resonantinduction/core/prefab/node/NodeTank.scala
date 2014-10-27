package resonantinduction.core.prefab.node

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._
import resonant.api.ISave
import resonant.lib.prefab.fluid.{LimitedTank, NodeFluidHandler}
import resonant.lib.utility.WorldUtility
import resonant.api.grid.{INodeProvider, INode}

/**
 * Simple tank node designed to be implemented by any machine that can connect to other fluid based machines.
 *
 * @author Darkguardsman
 */
class NodeTank(parent: INodeProvider, buckets: Int)  extends NodeFluidHandler(parent, new LimitedTank(buckets * FluidContainerRegistry.BUCKET_VOLUME)) with ISave with INode {


  def load(nbt: NBTTagCompound)
  {
    getPrimaryTank.readFromNBT(nbt.getCompoundTag("tank"))
  }

  def save(nbt: NBTTagCompound)
  {
    nbt.setTag("tank", getPrimaryTank.writeToNBT(new NBTTagCompound))
  }

  protected override def addConnection(obj: AnyRef, dir: ForgeDirection)
  {
    super.addConnection(obj, dir)
    if (showConnectionsFor(obj, dir))
    {
      renderSides = WorldUtility.setEnableSide(getRenderSides, dir, true)
    }
  }

  protected def showConnectionsFor(obj: AnyRef, dir: ForgeDirection): Boolean = {
    if (obj != null) {
      if (obj.getClass.isAssignableFrom(getParent.getClass)) {
        return true
      }
    }
    return false
  }

  def getRenderSides: Int = {
    return renderSides
  }

  private[node] var renderSides: Int = 0
}