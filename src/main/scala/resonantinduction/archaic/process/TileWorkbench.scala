package resonantinduction.archaic.process

import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.{Item, ItemBlock, ItemStack}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.oredict.OreDictionary
import org.lwjgl.opengl.GL11
import resonant.api.recipe.MachineRecipes
import resonant.lib.content.prefab.TInventory
import resonant.lib.factory.resources.RecipeType
import resonant.lib.network.discriminator.PacketType
import resonant.lib.network.handle.{TPacketReceiver, TPacketSender}
import resonant.lib.prefab.tile.spatial.SpatialTile
import resonant.lib.render.{RenderItemOverlayUtility, RenderUtility}
import resonant.lib.transform.vector.Vector3
import resonant.lib.utility.inventory.InventoryUtility
import resonant.lib.wrapper.ByteBufWrapper._
import resonantinduction.archaic.engineering.ItemHammer
import resonantinduction.core.{Reference, ResonantInduction}

/**
 * The workbench is meant for manual ore and wood processing.
 * It is also the core block in Resonant Induction that leads the player to all aspect of the mod.
 *
 * There are two states: 0 - wooden, 1 - stone
 *
 * Functions:
 * Crush ores -> rubble
 * Grind rubble -> dust
 *
 * Cut logs -> slabs
 * Glue slabs -> wood
 *
 * @author Calclavia
 */
object TileWorkbench
{
  val model = Array(AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "workbench_0.obj")), AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "workbench_1.obj")))
}

class TileWorkbench extends SpatialTile(Material.rock) with TInventory with TPacketSender with TPacketReceiver
{
  //Constructor
  setTextureName(Reference.prefix + "material_wood_surface")
  normalRender = false
  isOpaqueCube = false

  override def getSizeInventory = 1

  override def use(player: EntityPlayer, hitSide: Int, hit: Vector3): Boolean =
  {
    if (player.getCurrentEquippedItem != null)
    {
      if (metadata == 1)
      {
        //The player is holding a hammer. Attempt to crush the item on the bench
        if (player.getCurrentEquippedItem.getItem.isInstanceOf[ItemHammer])
        {
          val inputStack = getStackInSlot(0)

          if (inputStack != null)
          {
            val oreName = OreDictionary.getOreName(OreDictionary.getOreID(inputStack))

            if (oreName != null && oreName != "Unknown")
            {
              if (!world.isRemote)
              {
                //Try output resource
                def tryOutput(name: String, probability: Float): Boolean =
                {
                  val outputs = MachineRecipes.instance.getOutput(name, oreName)

                  if (outputs.length > 0 && world.rand.nextFloat < probability)
                  {
                    outputs.map(_.getItemStack.copy()).foreach(s => InventoryUtility.dropItemStack(world, new Vector3(player), s, 0))
                    inputStack.stackSize -= 1
                    setInventorySlotContents(0, if (inputStack.stackSize <= 0) null else inputStack)
                    world.spawnParticle("crit", x + 0.5, y + 0.5, z + 0.5, 0, 0, 0)
                    return true
                  }
                  return false
                }

                if (!tryOutput(RecipeType.CRUSHER.name, 0.1f))
                  tryOutput(RecipeType.GRINDER.name, 0.05f)
              }

              ResonantInduction.proxy.renderBlockParticle(world, new Vector3(x + 0.5, y + 0.5, z + 0.5), new Vector3((Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3), Item.getIdFromItem(inputStack.getItem), 1)
              world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, Reference.prefix + "hammer", 0.5f, 0.8f + (0.2f * world.rand.nextFloat))
              player.addExhaustion(0.1f)
              player.getCurrentEquippedItem.damageItem(1, player)
            }
          }

          return true
        }
      }
      else
      {
        if (player.getCurrentEquippedItem.getItem.isInstanceOf[ItemBlock])
        {
          if (player.getCurrentEquippedItem.getItem.asInstanceOf[ItemBlock].field_150939_a == Blocks.stone)
          {
            setMeta(1)
            player.getCurrentEquippedItem.stackSize -= 1
            if (player.getCurrentEquippedItem.stackSize <= 0)
              player.inventory.setInventorySlotContents(player.inventory.currentItem, null)
            return true
          }
        }
      }
    }

    //Try putting the current item in.
    interactCurrentItem(this, 0, player)
    onInventoryChanged()
    return true
  }

  /** Called each time the inventory changes */
  override def onInventoryChanged()
  {
    super.onInventoryChanged()

    if (!world.isRemote)
      sendDescPacket()
  }

  override def isItemValidForSlot(i: Int, itemStack: ItemStack): Boolean =
  {
    return true
  }

  override def read(buf: ByteBuf, id: Int, packetType: PacketType)
  {
    super.read(buf, id, packetType)
    buf >>> getInventory
  }

  override def write(buf: ByteBuf, id: Int)
  {
    super.write(buf, id)
    buf <<< getInventory
  }

  /**
   * Packets
   */

  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    GL11.glPushMatrix()
    RenderItemOverlayUtility.renderTopOverlay(this, Array[ItemStack](getStackInSlot(0)), null, 1, 1, pos.x, pos.y - (if (metadata == 1) 0.2 else 0.5), pos.z, 1.8f)
    GL11.glPopMatrix()
    GL11.glPushMatrix()
    GL11.glColor4f(1, 1, 1, 1)
    GL11.glTranslated(pos.x, pos.y, pos.z)
    RenderUtility.bind(Reference.domain, Reference.modelPath + "workbench_" + metadata + ".png")
    TileWorkbench.model(metadata).renderAll()
    GL11.glPopMatrix()

  }
}
