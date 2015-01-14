package edx.basic.process

import edx.core.Reference
import edx.core.resource.content.ItemRubble
import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import org.lwjgl.opengl.GL11
import resonant.lib.content.prefab.TInventory
import resonant.lib.network.discriminator.PacketType
import resonant.lib.network.handle.{TPacketReceiver, TPacketSender}
import resonant.lib.prefab.tile.spatial.SpatialTile
import resonant.lib.render.RenderUtility
import resonant.lib.transform.vector.Vector3
import resonant.lib.wrapper.ByteBufWrapper._

object TileSieve
{
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "sieve.tcn"))
}

class TileSieve extends SpatialTile(Material.wood) with TInventory with TPacketSender with TPacketReceiver
{
  //Constructor
  setTextureName("material_wood_top")

  override def canUpdate: Boolean = false

  override def getSizeInventory: Int = 1

  override def use(player: EntityPlayer, hitSide: Int, hit: Vector3): Boolean =
  {
    val currentStack = player.inventory.getCurrentItem

    if (currentStack != null && currentStack.getItem.isInstanceOf[ItemRubble])
    {
      interactCurrentItem(this, 0, player)
    }
    else
    {
      extractItem(this, 0, player)
    }

    return true
  }

  override def renderDynamic(pos: Vector3, frame: Float, pass: Int): Unit =
  {
    GL11.glPushMatrix()
    GL11.glTranslated(pos.x, pos.y, pos.z)
    RenderUtility.bind(Reference.domain, Reference.modelPath + "sieve.png")
    TileSieve.model.renderAll()
    GL11.glPopMatrix()
  }

  /**
   * Packets
   */
  override def write(buf: ByteBuf, id: Int)
  {
    super.write(buf, id)
    buf <<<< writeToNBT
  }

  override def read(buf: ByteBuf, id: Int, packetType: PacketType)
  {
    super.read(buf, id, packetType)
    buf >>>> readFromNBT
  }
}