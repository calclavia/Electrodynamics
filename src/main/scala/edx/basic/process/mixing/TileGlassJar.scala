package edx.basic.process.mixing

import java.awt.Color
import java.util.ArrayList

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.basic.BasicContent
import edx.core.Reference
import edx.core.resource.alloy.{Alloy, AlloyUtility}
import edx.core.resource.content.{ItemDust, ItemRefinedDust}
import io.netty.buffer.ByteBuf
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer.ItemRenderType
import net.minecraftforge.client.model.AdvancedModelLoader
import org.lwjgl.opengl.GL11
import resonantengine.api.item.ISimpleItemRenderer
import resonantengine.core.network.discriminator.PacketType
import resonantengine.lib.factory.resources.ResourceFactory
import resonantengine.lib.factory.resources.item.TItemResource
import resonantengine.lib.modcontent.block.ResonantTile
import resonantengine.lib.render.RenderUtility
import resonantengine.lib.render.model.ModelCube
import resonantengine.lib.transform.region.Cuboid
import resonantengine.lib.transform.vector.{Vector3, VectorWorld}
import resonantengine.lib.utility.inventory.InventoryUtility
import resonantengine.lib.utility.nbt.NBTUtility
import resonantengine.lib.wrapper.ByteBufWrapper._
import resonantengine.prefab.block.itemblock.ItemBlockSaved
import resonantengine.prefab.network.{TPacketReceiver, TPacketSender}

/**
 * A glass jar for mixing different dusts/refined together.
 * @author Calclavia
 */
object TileGlassJar
{
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "glassJar.tcn"))
  val dustMaterialTexture = new ResourceLocation(Reference.domain, Reference.blockTextureDirectory + "material_sand.png")
}

class TileGlassJar extends ResonantTile(Material.wood) with TPacketReceiver with TPacketSender with ISimpleItemRenderer
{
  var alloy = new Alloy(8)
  var mixed = false

  setTextureName("glass")
  bounds = new Cuboid(0.2, 0, 0.2, 0.8, 1, 0.8)
  normalRender = false
  isOpaqueCube = false
  itemBlock = classOf[ItemGlassJar]

  override def canUpdate: Boolean = false

  /**
   * Override this method
   * Be sure to super this method or manually write the id into the packet when sending
   */
  override def write(buf: ByteBuf, id: Int)
  {
    super.write(buf, id)
    buf <<<< writeToNBT
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setBoolean("mixed", mixed)
    alloy.save(nbt)
  }

  override def read(buf: ByteBuf, id: Int, packetType: PacketType)
  {
    super.read(buf, id, packetType)
    buf >>>> readFromNBT
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    mixed = nbt.getBoolean("mixed")
    alloy.load(nbt)
  }

  override def getDrops(metadata: Int, fortune: Int): ArrayList[ItemStack] = new ArrayList[ItemStack]

  override def onRemove(block: Block, par6: Int)
  {
    val stack: ItemStack = ItemBlockSaved.getItemStackWithNBT(block, world, x, y, z)
    InventoryUtility.dropItemStack(world, center, stack)
  }

  override def renderInventoryItem(`type`: ItemRenderType, itemStack: ItemStack, data: AnyRef*): Unit =
  {
    renderInventory(itemStack)
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

  def renderMixture(itemStack: ItemStack = null)
  {
    val alloy: Alloy =
      if (itemStack != null)
        new Alloy(NBTUtility.getNBTTagCompound(itemStack))
      else
        this.alloy

    val mixed =
      if (itemStack != null)
        NBTUtility.getNBTTagCompound(itemStack).getBoolean("mixed")
      else
        this.mixed

    if (alloy != null && alloy.size > 0)
    {
      GL11.glPushMatrix()
      RenderUtility.bind(TileGlassJar.dustMaterialTexture)

      if (mixed)
      {
        val color = new Color(alloy.color).darker.darker.darker
        GL11.glTranslated(0, -0.5 + 0.75f / 2 * alloy.percentage, 0)
        GL11.glScalef(0.4f, 0.75f * alloy.percentage, 0.4f)
        GL11.glColor4f(color.getRed / 255f, color.getGreen / 255f, color.getBlue / 255f, 1)
        ModelCube.INSTNACE.render()
      }
      else
      {
        val size = alloy.size
        val height = 0.72f / 8f

        //Translate to bottom of the jar
        GL11.glTranslatef(0, -0.45f, 0)

        alloy.content.foreach
        {
          case (material, materialCount) =>
          {
            val color = new Color(ResourceFactory.getColor(material)).darker.darker.darker
            GL11.glPushMatrix()
            GL11.glTranslated(0, (height * materialCount) / 2, 0)
            GL11.glScalef(0.4f, height * materialCount, 0.4f)
            GL11.glColor4f(color.getRed / 255f, color.getGreen / 255f, color.getBlue / 255f, 1)
            ModelCube.INSTNACE.render()
            GL11.glPopMatrix()

            //Translate each section of the jar upwards
            GL11.glTranslatef(0, height * materialCount, 0)
          }
        }
      }

      GL11.glPopMatrix()
    }
  }

  def renderJar()
  {
    RenderUtility.enableBlending()
    GL11.glScalef(1.6f, 1.6f, 1.6f)
    GL11.glColor4f(1, 1, 1, 1)
    RenderUtility.bind(Reference.domain, Reference.modelPath + "glassJar.png")
    TileGlassJar.model.renderAll()
    RenderUtility.disableBlending()
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

  @SideOnly(Side.CLIENT)
  override protected def getTextureName: String = textureName

  override protected def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (!world.isRemote)
    {
      if (player.getCurrentEquippedItem != null)
      {
        val item = player.getCurrentEquippedItem.getItem

        if (item.isInstanceOf[ItemDust] || item.isInstanceOf[ItemRefinedDust])
        {
          if (alloy.mix(item.asInstanceOf[TItemResource].material))
          {
            player.getCurrentEquippedItem.splitStack(1)
            sendDescPacket()
            return true
          }
        }
      }
      else if (mixed)
      {
        //Eject dust
        InventoryUtility.dropItemStack(new VectorWorld(player), AlloyUtility.setAlloy(new ItemStack(BasicContent.itemAlloyDust, alloy.size), alloy))
        alloy = new Alloy(8)
        mixed = false
        sendDescPacket()
      }
    }

    return true
  }

}
