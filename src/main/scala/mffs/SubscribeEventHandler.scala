package mffs

import java.util.HashMap

import cpw.mods.fml.common.eventhandler.{Event, SubscribeEvent}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import mffs.base.TileFortron
import mffs.field.TileElectromagnetProjector
import mffs.fortron.FortronHelper
import net.minecraft.block.{Block, BlockSkull}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemSkull
import net.minecraft.tileentity.{TileEntity, TileEntitySkull}
import net.minecraft.util.IIcon
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.event.entity.living.LivingSpawnEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action
import resonant.api.mffs.security.{IInterdictionMatrix, Permission}
import resonant.api.mffs.{EventForceManipulate, EventStabilize}
import resonant.lib.event.ChunkModifiedEvent

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
      registerIcon(Reference.PREFIX + "fortron", event)
    }
  }

  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  def textureHook(event: TextureStitchEvent.Post)
  {
    FortronHelper.FLUID_FORTRON.setIcons(fluidIconMap.get(Reference.PREFIX + "fortron"))
  }

  @SubscribeEvent
  def eventPreForceManipulate(evt: EventForceManipulate.EventPreForceManipulate)
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
      val tileentity: TileEntity = evt.world.getBlockTileEntity(evt.x, evt.y, evt.z)
      if (tileentity.isInstanceOf[TileEntitySkull])
      {
        var s: String = ""
        if (evt.itemStack.hasTagCompound && evt.itemStack.getTagCompound.hasKey("SkullOwner"))
        {
          s = evt.itemStack.getTagCompound.getString("SkullOwner")
        }
        (tileentity.asInstanceOf[TileEntitySkull]).setSkullType(evt.itemStack.getItemDamage, s)
        (Block.skull.asInstanceOf[BlockSkull]).makeWither(evt.world, evt.x, evt.y, evt.z, tileentity.asInstanceOf[TileEntitySkull])
      }
      evt.itemStack.stackSize -= 1
      evt.setCanceled(true)
    }
  }

  @SubscribeEvent
  def playerInteractEvent(evt: PlayerInteractEvent)
  {
    if (evt.action eq Action.RIGHT_CLICK_BLOCK || evt.action eq Action.LEFT_CLICK_BLOCK)
    {
      if (evt.action eq Action.LEFT_CLICK_BLOCK && evt.entityPlayer.worldObj.getBlockId(evt.x, evt.y, evt.z) eq ModularForceFieldSystem.blockForceField.blockID)
      {
        evt.setCanceled(true)
        return
      }
      if (evt.entityPlayer.capabilities.isCreativeMode)
      {
        return
      }
      val position: Nothing = new Nothing(evt.x, evt.y, evt.z)
      val interdictionMatrix: IInterdictionMatrix = MFFSHelper.getNearestInterdictionMatrix(evt.entityPlayer.worldObj, position)
      if (interdictionMatrix != null)
      {
        val blockID: Int = position.getBlockID(evt.entityPlayer.worldObj)
        if (ModularForceFieldSystem.blockBiometricIdentifier.blockID eq blockID && MFFSHelper.isPermittedByInterdictionMatrix(interdictionMatrix, evt.entityPlayer.username, Permission.SECURITY_CENTER_CONFIGURE))
        {
          return
        }
        val hasPermission: Boolean = MFFSHelper.hasPermission(evt.entityPlayer.worldObj, new Nothing(evt.x, evt.y, evt.z), interdictionMatrix, evt.action, evt.entityPlayer)
        if (!hasPermission)
        {
          evt.entityPlayer.addChatMessage("[" + ModularForceFieldSystem.blockInterdictionMatrix.getLocalizedName + "] You have no permission to do that!")
          evt.setCanceled(true)
        }
      }
    }
  }

  /**
   * When a block breaks, mark force field projectors for an update.
   *
   * @param evt
   */
  @SubscribeEvent
  def chunkModifyEvent(evt: ChunkModifiedEvent.ChunkSetBlockEvent)
  {
    if (!evt.world.isRemote && evt.blockID == 0)
    {
      for (fortronFrequency <- FrequencyGrid.instance.getFortronTiles(evt.world))
      {
        if (fortronFrequency.isInstanceOf[TileElectromagnetProjector])
        {
          val projector: TileElectromagnetProjector = fortronFrequency.asInstanceOf[TileElectromagnetProjector]
          if (projector.getCalculatedField != null)
          {
            if (projector.getCalculatedField.contains(new Nothing(evt.x, evt.y, evt.z)))
            {
              projector.markFieldUpdate = true
            }
          }
        }
      }
    }
  }

  @SubscribeEvent
  def livingSpawnEvent(evt: LivingSpawnEvent)
  {
    val interdictionMatrix: IInterdictionMatrix = MFFSHelper.getNearestInterdictionMatrix(evt.world, new Nothing(evt.entityLiving))
    if (interdictionMatrix != null && !(evt.entity.isInstanceOf[EntityPlayer]))
    {
      if (interdictionMatrix.getModuleCount(ModularForceFieldSystem.itemModuleAntiSpawn) > 0)
      {
        evt.setResult(Event.Result.DENY)
      }
    }
  }
}