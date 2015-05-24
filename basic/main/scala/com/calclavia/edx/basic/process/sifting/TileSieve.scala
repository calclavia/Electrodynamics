package com.calclavia.edx.basic.process.sifting

import java.util.ArrayList

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import edx.core.resource.content.ItemRubble
import io.netty.buffer.ByteBuf
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer.ItemRenderType
import net.minecraftforge.client.model.AdvancedModelLoader
import org.lwjgl.opengl.GL11
import resonantengine.api.item.ISimpleItemRenderer
import resonantengine.core.network.discriminator.PacketType
import resonantengine.lib.content.prefab.TInventory
import resonantengine.lib.modcontent.block.ResonantTile
import resonantengine.lib.render.{RenderItemOverlayUtility, RenderUtility}
import resonantengine.lib.transform.region.Cuboid
import resonantengine.lib.transform.vector.Vector3
import resonantengine.lib.utility.inventory.InventoryUtility
import resonantengine.lib.wrapper.ByteBufWrapper._
import resonantengine.prefab.block.itemblock.ItemBlockSaved
import resonantengine.prefab.network.{TPacketReceiver, TPacketSender}

object TileSieve
{
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "sieve.tcn"))
}

class TileSieve extends ResonantTile(Material.wood) with TInventory with TPacketSender with TPacketReceiver with ISimpleItemRenderer
{
  //Constructor
  setTextureName("material_wood_top")
  bounds = new Cuboid(0, 0, 0, 1, 0.25, 1)
  normalRender = false
  isOpaqueCube = false
  itemBlock = classOf[ItemSieve]

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

  override def isItemValidForSlot(i: Int, itemStack: ItemStack): Boolean = itemStack.getItem.isInstanceOf[ItemRubble]

  override def renderInventoryItem(renderType: ItemRenderType, itemStack: ItemStack, data: AnyRef*): Unit =
  {
    /*
    GL11.glPushMatrix()

    val nbt = NBTUtility.getNBTTagCompound(itemStack)
    val inv = new ExternalInventory(null, 1)
    inv.load(nbt)

    if (inv.getStackInSlot(0) != null)
      RenderItemOverlayUtility.renderTopOverlay(this, Array[ItemStack](inv.getStackInSlot(0)), null, 1, 1, 0,  - 1, 0, 1f)
    GL11.glPopMatrix()
*/

    GL11.glPushMatrix()
    GL11.glTranslatef(0.5f, 1f, 0.5f)
    GL11.glScalef(1.4f, 1.4f, 1.4f)
    RenderUtility.bind(Reference.domain, Reference.modelPath + "sieve.png")
    TileSieve.model.renderAll()
    GL11.glPopMatrix()
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    GL11.glPushMatrix()

    if (getStackInSlot(0) != null)
      RenderItemOverlayUtility.renderTopOverlay(this, Array[ItemStack](getStackInSlot(0)), null, 1, 1, pos.x, pos.y - 1, pos.z, 1f)
    GL11.glPopMatrix()

    GL11.glPushMatrix()
    GL11.glTranslated(pos.x, pos.y, pos.z)

    GL11.glTranslatef(0.5f, 0.65f, 0.5f)
    GL11.glScalef(1.4f, 1.4f, 1.4f)
    RenderUtility.bind(Reference.domain, Reference.modelPath + "sieve.png")
    TileSieve.model.renderAll()
    GL11.glPopMatrix()
  }

  override def getDrops(metadata: Int, fortune: Int): ArrayList[ItemStack] = new ArrayList[ItemStack]

  override def onRemove(block: Block, par6: Int)
  {
    val stack: ItemStack = ItemBlockSaved.getItemStackWithNBT(block, world, x, y, z)
    InventoryUtility.dropItemStack(world, center, stack)
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