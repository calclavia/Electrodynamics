package resonantinduction.electrical.wire.framed

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.lib.render.CCRenderState
import codechicken.lib.vec.Vector3
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.wrapper.BitmaskWrapper._
import resonantinduction.core.prefab.node.TMultipartNode
import resonantinduction.core.prefab.part.connector.PartFramedNode
import resonantinduction.electrical.wire.base.TWire
import universalelectricity.core.grid.node.DCNode

/**
 * Fluid transport pipe
 *
 * @author Calclavia
 */
class PartFramedWire extends PartFramedNode with TWire
{
  override lazy val node = new DCNode(this) with TMultipartNode
  {
    override def reconstruct()
    {
      val prevCon = connectionMask
      connectionMask = 0x00

     super.reconstruct()

      if (connectionMask != prevCon)
        sendConnectionUpdate()
    }

    override protected def addConnection(obj: AnyRef, dir: ForgeDirection)
    {
      super.addConnection(obj, dir)
      connectionMask = connectionMask.openMask(dir)
    }
  }

  override def preparePlacement(side: Int, meta: Int)
  {
    setMaterial(meta)
    node.resistance_=(material.resistance)
  }

  /**
   * Packet Methods
   */
  override def writeDesc(packet: MCDataOutput)
  {
    super[PartFramedNode].writeDesc(packet)
    super[TWire].writeDesc(packet)
  }

  override def readDesc(packet: MCDataInput)
  {
    super[PartFramedNode].readDesc(packet)
    super[TWire].readDesc(packet)
  }

  override def read(packet: MCDataInput, packetID: Int)
  {
    super[PartFramedNode].read(packet, packetID)
    super[TWire].read(packet, packetID)
  }

  /**
   * NBT Methods
   */
  override def load(nbt: NBTTagCompound)
  {
    super[PartFramedNode].load(nbt)
    super[TWire].load(nbt)
  }

  override def save(nbt: NBTTagCompound)
  {
    super[PartFramedNode].save(nbt)
    super[TWire].save(nbt)
  }

  @SideOnly(Side.CLIENT)
  override def renderStatic(pos: Vector3, pass: Int): Boolean =
  {
    RenderFramedWire.renderStatic(this)
    return true
  }

  override def drawBreaking(renderBlocks: RenderBlocks)
  {
    CCRenderState.reset()
  }
}