package resonantinduction.archaic.process

import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.oredict.OreDictionary
import resonant.api.recipe.{MachineRecipes, RecipeResource}
import resonant.lib.factory.resources.RecipeType
import resonant.lib.network.handle.{TPacketReceiver, TPacketSender}
import resonant.lib.prefab.tile.TileInventory
import resonant.lib.render.RenderUtility
import resonant.lib.transform.vector.Vector3
import resonant.lib.utility.inventory.InventoryUtility
import resonantinduction.archaic.engineering.ItemHammer
import resonantinduction.core.{Reference, ResonantInduction}

/**
 *
 * @author Calclavia
 */
object TileWorkbench
{
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "workbench.obj"))
}

class TileWorkbench extends TileInventory(Material.rock) with TPacketSender with TPacketReceiver
{
  //Constructor
  maxSlots = 1
  setTextureName(Reference.prefix + "material_metal_side")
  normalRender = false
  isOpaqueCube = false

  override def use(player: EntityPlayer, hitSide: Int, hit: Vector3): Boolean =
  {
    //The player is holding a hammer. Attempt to crush the item on the bench
    if (player.getCurrentEquippedItem != null)
    {
      if (player.getCurrentEquippedItem.getItem.isInstanceOf[ItemHammer])
      {
        val inputStack = getStackInSlot(0)

        if (inputStack != null)
        {
          val oreName = OreDictionary.getOreName(OreDictionary.getOreID(inputStack))

          if (oreName != null && oreName != "Unknown")
          {
            val outputs: Array[RecipeResource] = MachineRecipes.INSTANCE.getOutput(RecipeType.CRUSHER.name, oreName)
            if (outputs != null && outputs.length > 0)
            {
              if (!world.isRemote && world.rand.nextFloat < 0.2)
              {
                for (resource <- outputs)
                {
                  val outputStack: ItemStack = resource.getItemStack.copy
                  if (outputStack != null)
                  {
                    InventoryUtility.dropItemStack(world, new Vector3(player), outputStack, 0)
                    inputStack.stackSize -= 1
                    setInventorySlotContents(0, if (inputStack.stackSize <= 0) null else inputStack)
                  }
                }
              }
              ResonantInduction.proxy.renderBlockParticle(world, new Vector3(x + 0.5, y + 0.5, z + 0.5), new Vector3((Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3), Item.getIdFromItem(inputStack.getItem), 1)
              world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, Reference.prefix + "hammer", 0.5f, 0.8f + (0.2f * world.rand.nextFloat))
              player.addExhaustion(0.1f)
              player.getCurrentEquippedItem.damageItem(1, player)
              return true
            }
          }
        }
      }
      else
      {
        return interactCurrentItem(this, 0, player)
      }
    }

    return false
  }

  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    RenderUtility.bind(Reference.domain, Reference.modelPath + "workbench.png")
    TileWorkbench.model.renderAll()
  }
}
