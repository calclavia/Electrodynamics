package mffs

import java.util.{HashMap, UUID}

import com.mojang.authlib.GameProfile
import cpw.mods.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent
import cpw.mods.fml.common.eventhandler.{Event, SubscribeEvent}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import mffs.base.TileFortron
import mffs.field.TileElectromagneticProjector
import mffs.security.MFFSPermissions
import mffs.util.{FortronUtility, MFFSUtility}
import net.minecraft.block.BlockSkull
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemSkull
import net.minecraft.nbt.{NBTTagCompound, NBTUtil}
import net.minecraft.tileentity.{TileEntity, TileEntitySkull}
import net.minecraft.util.{ChatComponentText, IIcon}
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.event.entity.living.LivingSpawnEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action
import resonant.api.mffs.event.{EventForceMobilize, EventStabilize}
import resonant.api.mffs.fortron.FrequencyGridRegistry
import resonant.lib.config.ConfigHandler
import resonant.lib.event.ChunkModifiedEvent
import universalelectricity.core.transform.vector.Vector3

import scala.collection.JavaConversions._

object SubscribeEventHandler
{
  val fluidIconMap = new HashMap[String, IIcon]

  def registerIcon(name: String, event: TextureStitchEvent.Pre)
  {
    fluidIconMap.put(name, event.map.registerIcon(name))
  }

  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  def preTextureHook(event: TextureStitchEvent.Pre)
  {
    if (event.map.getTextureType() == 0)
    {
      registerIcon(Reference.prefix + "fortron", event)
    }
  }

  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  def textureHook(event: TextureStitchEvent.Post)
  {
    FortronUtility.FLUID_FORTRON.setIcons(fluidIconMap.get(Reference.prefix + "fortron"))
  }

  @SubscribeEvent
  def onConfigChange(evt: OnConfigChangedEvent)
  {
    if (evt.modID.equals(Reference.id))
      ConfigHandler.sync(Settings, Settings.config)
  }

  @SubscribeEvent
  def eventPreForceManipulate(evt: EventForceMobilize.EventPreForceManipulate)
  {
    val tileEntity: TileEntity = evt.world.getTileEntity(evt.beforeX, evt.beforeY, evt.beforeZ)

    if (tileEntity.isInstanceOf[TileFortron])
    {
      (tileEntity.asInstanceOf[TileFortron]).markSendFortron = false
    }
  }

  /**
   * Special stabilization cases.
   *
   * @param evt
   */
  @SubscribeEvent
  def eventStabilize(evt: EventStabilize)
  {
    if (evt.itemStack.getItem.isInstanceOf[ItemSkull])
    {
      evt.world.setBlock(evt.x, evt.y, evt.z, Blocks.skull, evt.itemStack.getItemDamage, 2)
      val tile = evt.world.getTileEntity(evt.x, evt.y, evt.z)

      if (tile.isInstanceOf[TileEntitySkull])
      {
        val nbt: NBTTagCompound = evt.itemStack.getTagCompound()

        var gameprofile: GameProfile = null

        if (nbt.hasKey("SkullOwner", 10))
        {
          gameprofile = NBTUtil.func_152459_a(nbt.getCompoundTag("SkullOwner"))
        }
        else if (nbt.hasKey("SkullOwner", 8) && nbt.getString("SkullOwner").length > 0)
        {
          gameprofile = new GameProfile(null.asInstanceOf[UUID], nbt.getString("SkullOwner"))
        }

        if (gameprofile != null)
          tile.asInstanceOf[TileEntitySkull].func_152106_a(gameprofile)
        else
          tile.asInstanceOf[TileEntitySkull].func_152107_a(evt.itemStack.getItemDamage)

        Blocks.skull.asInstanceOf[BlockSkull].func_149965_a(evt.world, evt.x, evt.y, evt.z, tile.asInstanceOf[TileEntitySkull])
      }

      evt.itemStack.stackSize -= 1
      evt.setCanceled(true)
    }
  }

  @SubscribeEvent
  def playerInteractEvent(evt: PlayerInteractEvent)
  {
    if (evt.action == Action.RIGHT_CLICK_BLOCK || evt.action == Action.LEFT_CLICK_BLOCK)
    {
      // Cancel if we click on a force field.
      if (evt.action == Action.LEFT_CLICK_BLOCK && evt.entityPlayer.worldObj.getBlock(evt.x, evt.y, evt.z) == Content.forceField)
      {
        evt.setCanceled(true)
        return
      }

      // Only check non-creative players
      if (evt.entityPlayer.capabilities.isCreativeMode)
      {
        return
      }

      val position = new Vector3(evt.x, evt.y, evt.z)

      val relevantProjectors = MFFSUtility.getRelevantProjectors(evt.entityPlayer.worldObj, position)

      //Check if we can sync this block (activate). If not, we cancel the event.
      if (!relevantProjectors.forall(x => x.isAccessGranted(evt.entityPlayer.worldObj, new Vector3(evt.x, evt.y, evt.z), evt.entityPlayer, evt.action) && x.hasPermission(evt.entityPlayer.getGameProfile, MFFSPermissions.configure)))
      {
        evt.entityPlayer.addChatMessage(new ChatComponentText("[" + Reference.name + "] You have no permission to do that!"))
        evt.setCanceled(true)
      }
    }
  }

  /**
   * When a block breaks, mark force field projectors for an update.
   */
  @SubscribeEvent
  def chunkModifyEvent(evt: ChunkModifiedEvent.ChunkSetBlockEvent)
  {
    if (!evt.world.isRemote && evt.block == Blocks.air)
    {
      val vec = new Vector3(evt.x, evt.y, evt.z)

      FrequencyGridRegistry.instance.getNodes(classOf[TileElectromagneticProjector])
        .view
        .filter(_.getWorldObj == evt.world)
        .filter(_.getCalculatedField != null)
        .filter(_.getCalculatedField.contains(vec))
        .force
        .foreach(_.markFieldUpdate = true)
    }
  }

  @SubscribeEvent
  def livingSpawnEvent(evt: LivingSpawnEvent)
  {
    if (!(evt.entity.isInstanceOf[EntityPlayer]))
    {
      if (MFFSUtility.getRelevantProjectors(evt.world, new Vector3(evt.entityLiving)).exists(_.getModuleCount(Content.moduleAntiSpawn) > 0))
      {
        evt.setResult(Event.Result.DENY)
      }
    }
  }
}