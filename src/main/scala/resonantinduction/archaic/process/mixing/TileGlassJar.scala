package resonantinduction.archaic.process.mixing

import java.awt.Color

import cpw.mods.fml.relauncher.{Side, SideOnly}
import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import org.lwjgl.opengl.GL11
import resonant.lib.factory.resources.item.TItemResource
import resonant.lib.network.discriminator.PacketType
import resonant.lib.network.handle.{TPacketReceiver, TPacketSender}
import resonant.lib.prefab.tile.spatial.SpatialTile
import resonant.lib.render.RenderUtility
import resonant.lib.render.model.ModelCube
import resonant.lib.transform.region.Cuboid
import resonant.lib.transform.vector.Vector3
import resonant.lib.wrapper.ByteBufWrapper._
import resonant.lib.wrapper.NBTWrapper._
import resonantinduction.core.Reference
import resonantinduction.core.resource.AlloyUtility
import resonantinduction.core.resource.content.{ItemDust, ItemRefinedDust}

/**
 * A glass jar for mixing different dusts/refined together.
 * @author Calclavia
 */
object TileGlassJar
{
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "glassJar.tcn"))
  val dustMaterialTexture = new ResourceLocation(Reference.domain, Reference.blockTextureDirectory + "material_sand.png")
}

class TileGlassJar extends SpatialTile(Material.wood) with TPacketReceiver with TPacketSender
{
  val jarCapacity = 64
  var mixed = false
  var mixture = Map.empty[String, Int]

  setTextureName("glass")
  bounds = new Cuboid(0.2, 0, 0.2, 0.8, 1, 0.8)
  normalRender = false
  isOpaqueCube = false
  itemBlock = classOf[ItemGlassJar]

  def percentage(material: String): Float = mixture(material) / mixtureSize.toFloat

  /**
   * Override this method
   * Be sure to super this method or manually write the id into the packet when sending
   */
  override def write(buf: ByteBuf, id: Int)
  {
    super.write(buf, id)
    buf <<< mixed
    buf <<<< writeToNBT
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setBoolean("mixed", mixed)
    nbt.setMap("mixture", mixture)
  }

  override def read(buf: ByteBuf, id: Int, packetType: PacketType)
  {
    super.read(buf, id, packetType)
    mixed = buf.readBoolean()
    buf >>>> readFromNBT
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    mixed = nbt.getBoolean("mixed")
    mixture = nbt.getMap("mixture")
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(itemStack: ItemStack): Unit =
  {
    GL11.glPushMatrix()
    renderMixture(itemStack)
    GL11.glPopMatrix()

    GL11.glPushMatrix()
    GL11.glTranslated(0, 0.3, 0)
    renderJar()
    GL11.glPopMatrix()
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    GL11.glPushMatrix()
    GL11.glTranslated(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
    renderMixture()
    GL11.glPopMatrix()

    GL11.glPushMatrix()
    GL11.glTranslated(pos.x + 0.5, pos.y + 0.8, pos.z + 0.5)
    renderJar()
    GL11.glPopMatrix()
  }

  def renderMixture(itemStack: ItemStack = null)
  {
    val mixture: Map[String, Int] =
      if (itemStack != null)
        if (itemStack.getTagCompound != null)
          itemStack.getTagCompound.getMap("mixture")
        else
          null
      else
        this.mixture

    if (mixture != null && mixture.size > 0)
    {
      GL11.glPushMatrix()
      val total = mixture.values.foldLeft(0)(_ + _)
      val res = AlloyUtility.mixedColor(mixture.map(keyVal => (keyVal._1, keyVal._2 / total.toFloat)))
      val color = new Color(res)
      GL11.glTranslated(0, -0.5 + 0.75f / 2 * percentage, 0)
      GL11.glScalef(0.4f, 0.75f * percentage, 0.4f)
      GL11.glColor4f(color.getRed / 255f, color.getGreen / 255f, color.getBlue / 255f, 1)
      RenderUtility.bind(TileGlassJar.dustMaterialTexture)
      ModelCube.INSTNACE.render()
      GL11.glPopMatrix()
    }
  }

  def percentage: Float = mixtureSize.toFloat / jarCapacity

  def mixtureSize = mixture.values.foldLeft(0)(_ + _)

  def renderJar()
  {
    RenderUtility.enableBlending()
    GL11.glScalef(1.6f, 1.6f, 1.6f)
    GL11.glColor4f(1, 1, 1, 1)
    RenderUtility.bind(Reference.domain, Reference.modelPath + "glassJar.png")
    TileGlassJar.model.renderAll()
    RenderUtility.disableBlending()
  }

  override protected def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (player.getCurrentEquippedItem != null)
    {
      val item = player.getCurrentEquippedItem.getItem

      if (item.isInstanceOf[ItemDust] || item.isInstanceOf[ItemRefinedDust])
      {
        if (mix(item.asInstanceOf[TItemResource].material))
        {
          player.getCurrentEquippedItem.splitStack(1)
          return true
        }
      }
    }

    return true
  }

  /**
   * Mixes a dust material into this jar
   */
  def mix(material: String): Boolean =
  {
    if (mixtureSize < jarCapacity)
    {
      mixture += material -> (mixture.getOrElse(material, 0) + 1)
      return true
    }
    return false
  }
}
