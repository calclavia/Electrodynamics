package mffs.item

import java.util.{HashSet, List, Set}

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.relauncher.{Side, SideOnly}
import mffs.ModularForceFieldSystem
import mffs.item.card.ItemCardFrequency
import mffs.security.access.MFFSPermissions
import mffs.util.MFFSUtility
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ChatComponentText
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action
import net.minecraftforge.fluids.FluidContainerRegistry
import resonant.api.mffs.EventForceManipulate
import resonant.api.mffs.card.ICoordLink
import resonant.api.mffs.fortron.{FrequencyGridRegistry, IFortronFrequency}
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.WrapList._
import universalelectricity.api.UnitDisplay
import universalelectricity.core.transform.vector.{Vector3, VectorWorld}

import scala.collection.JavaConversions._

class ItemRemoteController extends ItemCardFrequency with ICoordLink
{
  private final val remotesCached = new HashSet[ItemStack]
  private final val temporaryRemoteBlacklist = new HashSet[ItemStack]

  @SideOnly(Side.CLIENT)
  override def addInformation(itemstack: ItemStack, entityplayer: EntityPlayer, list: List[_], flag: Boolean)
  {
    super.addInformation(itemstack, entityplayer, list, flag)
    if (hasLink(itemstack))
    {
      val vec: VectorWorld = getLink(itemstack)
      val block: Block = vec.getBlock(entityplayer.worldObj)
      if (block ne Blocks.air)
      {
        list.add(LanguageUtility.getLocal("info.item.linkedWith") + " " + block.getLocalizedName)
      }
      list.add(vec.xi + ", " + vec.yi + ", " + vec.zi)
      list.add(LanguageUtility.getLocal("info.item.dimension") + " '" + vec.world.provider.getDimensionName + "'")
    }
    else
    {
      list.add(LanguageUtility.getLocal("info.item.notLinked"))
    }
  }

  override def onItemUse(itemStack: ItemStack, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, par7: Int, par8: Float, par9: Float, par10: Float): Boolean =
  {
    if (!world.isRemote && player.isSneaking)
    {
      val vector: VectorWorld = new VectorWorld(world, x, y, z)
      setLink(itemStack, vector)
      val block = vector.getBlock

      if (block != null)
      {
        player.addChatMessage(new ChatComponentText(LanguageUtility.getLocal("message.remoteController.linked").replaceAll("%p", x + ", " + y + ", " + z).replaceAll("%q", block.getLocalizedName)))
      }
    }
    return true
  }

  def hasLink(itemStack: ItemStack): Boolean =
  {
    return getLink(itemStack) != null
  }

  def getLink(itemStack: ItemStack): VectorWorld =
  {
    if (itemStack.stackTagCompound == null || !itemStack.getTagCompound.hasKey("link"))
    {
      return null
    }
    return new VectorWorld(itemStack.getTagCompound.getCompoundTag("link"))
  }

  def setLink(itemStack: ItemStack, vec: VectorWorld)
  {
    if (itemStack.getTagCompound == null)
    {
      itemStack.setTagCompound(new NBTTagCompound)
    }
    itemStack.getTagCompound.setTag("link", vec.toNBT)
  }

  def clearLink(itemStack: ItemStack)
  {
    itemStack.getTagCompound.removeTag("link")
  }

  override def onItemRightClick(itemStack: ItemStack, world: World, entityPlayer: EntityPlayer): ItemStack =
  {
    if (!entityPlayer.isSneaking)
    {
      val position: Vector3 = this.getLink(itemStack)
      if (position != null)
      {
        val block: Block = position.getBlock(world)
        if (block ne Blocks.air)
        {
          val chunk: Chunk = world.getChunkFromBlockCoords(position.xi, position.zi)
          if (chunk != null && chunk.isChunkLoaded && (MFFSUtility.hasPermission(world, position, Action.RIGHT_CLICK_BLOCK, entityPlayer) || MFFSUtility.hasPermission(world, position, MFFSPermissions.remoteControl, entityPlayer)))
          {
            val requiredEnergy = new Vector3(entityPlayer).distance(position) * (FluidContainerRegistry.BUCKET_VOLUME / 100)
            var receivedEnergy = 0
            val fortronTiles: Set[IFortronFrequency] = FrequencyGridRegistry.instance.getNodes(classOf[IFortronFrequency], world, new Vector3(entityPlayer), 50, this.getFrequency(itemStack))

            for (fortronTile <- fortronTiles)
            {
              val consumedEnergy: Int = fortronTile.requestFortron(Math.ceil(requiredEnergy / fortronTiles.size).asInstanceOf[Int], true)
              if (consumedEnergy > 0)
              {
                if (world.isRemote)
                {
                  ModularForceFieldSystem.proxy.renderBeam(world, new Vector3(entityPlayer).add(new Vector3(0, entityPlayer.getEyeHeight - 0.2, 0)), new Vector3(fortronTile.asInstanceOf[TileEntity]).add(0.5), 0.6f, 0.6f, 1, 20)
                }
                receivedEnergy += consumedEnergy
              }
              if (receivedEnergy >= requiredEnergy)
              {
                try
                {
                  block.onBlockActivated(world, position.xi, position.yi, position.zi, entityPlayer, 0, 0, 0, 0)
                }
                catch
                  {
                    case e: Exception =>
                    {
                      e.printStackTrace
                    }
                  }
                return itemStack
              }
            }
            if (!world.isRemote)
            {
              entityPlayer.addChatMessage(new ChatComponentText(LanguageUtility.getLocal("message.remoteController.fail").replaceAll("%p", new UnitDisplay(UnitDisplay.Unit.JOULES, requiredEnergy).toString)))
            }
          }
        }
      }
    }
    return itemStack
  }

  @SubscribeEvent def preMove(evt: EventForceManipulate.EventPreForceManipulate)
  {
    this.temporaryRemoteBlacklist.clear
  }

  /**
   * Moves the coordinates of the link if the Force Manipulator moved a block that is linked by
   * the remote.
   *
   * @param evt
   */
  @SubscribeEvent def onMove(evt: EventForceManipulate.EventPostForceManipulate)
  {
    if (!evt.world.isRemote)
    {
      import scala.collection.JavaConversions._
      for (itemStack <- this.remotesCached)
      {
        if (!temporaryRemoteBlacklist.contains(itemStack) && (new Vector3(evt.beforeX, evt.beforeY, evt.beforeZ) == this.getLink(itemStack)))
        {
          this.setLink(itemStack, new VectorWorld(evt.world, evt.afterX, evt.afterY, evt.afterZ))
          temporaryRemoteBlacklist.add(itemStack)
        }
      }
    }
  }

}