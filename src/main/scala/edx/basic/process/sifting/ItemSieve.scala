package edx.basic.process.sifting

import java.util

import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{EnumAction, ItemStack}
import net.minecraft.world.World
import resonantengine.api.edx.recipe.{MachineRecipes, RecipeType}
import resonantengine.lib.render.EnumColor
import resonantengine.lib.transform.vector.Vector3
import resonantengine.lib.utility.inventory.{ExternalInventory, InventoryUtility}
import resonantengine.lib.utility.nbt.NBTUtility
import resonantengine.lib.wrapper.CollectionWrapper._
import resonantengine.lib.wrapper.StringWrapper._
import resonantengine.prefab.block.itemblock.ItemBlockSaved

/**
 * The ItemBlock for the glass jar
 * @author Calclavia
 */
class ItemSieve(block: Block) extends ItemBlockSaved(block)
{
  var lastLook: Vector3 = null

  override def addInformation(itemStack: ItemStack, player: EntityPlayer, list: util.List[_], par4: Boolean)
  {
    super.addInformation(itemStack, player, list, par4)

    val nbt = NBTUtility.getNBTTagCompound(itemStack)
    val inventory = new ExternalInventory(null, 1)
    inventory.load(nbt)
    val rubbleStack = inventory.getStackInSlot(0)
    if (rubbleStack != null)
      list.add(EnumColor.ORANGE + rubbleStack.getDisplayName + EnumColor.DARK_RED + " x" + rubbleStack.stackSize)
    else
      list.add("tooltip.empty".getLocal)
  }

  override def onItemUseFirst(stack: ItemStack, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float): Boolean =
  {
    if (player.isSneaking)
      return true

    return false
  }

  override def onItemRightClick(stack: ItemStack, world: World, player: EntityPlayer): ItemStack =
  {
    val nbt = NBTUtility.getNBTTagCompound(stack)
    val inventory = new ExternalInventory(null, 1)
    inventory.load(nbt)
    val rubbleStack = inventory.getStackInSlot(0)

    if (rubbleStack != null)
      player.setItemInUse(stack, getMaxItemUseDuration(stack))

    return stack
  }

  override def getMaxItemUseDuration(stack: ItemStack): Int = 20 * 5

  override def onUsingTick(stack: ItemStack, player: EntityPlayer, count: Int)
  {
    player.swingItem()
  }

  override def onEaten(stack: ItemStack, world: World, player: EntityPlayer): ItemStack =
  {
    val nbt = NBTUtility.getNBTTagCompound(stack)
    val inventory = new ExternalInventory(null, 1)
    inventory.load(nbt)
    val rubbleStack = inventory.getStackInSlot(0)

    if (rubbleStack != null)
    {
      val outputs = MachineRecipes.instance.getOutput(RecipeType.SIFTER.name, rubbleStack)

      if (outputs.length > 0)
      {
        outputs.map(_.getItemStack.copy()).foreach(s => InventoryUtility.dropItemStack(world, new Vector3(player), s, 0))
        rubbleStack.stackSize -= 1

        if (rubbleStack.stackSize > 0)
          inventory.setInventorySlotContents(0, rubbleStack)
        else
          inventory.setInventorySlotContents(0, null)
      }
    }

    inventory.save(nbt)
    return stack
  }

  override def getItemUseAction(stack: ItemStack) = EnumAction.none
}