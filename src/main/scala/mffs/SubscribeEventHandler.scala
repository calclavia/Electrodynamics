package mffs

import java.util.UUID

import mffs.content.Content

object SubscribeEventHandler
{


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
      tileEntity.asInstanceOf[TileFortron].markSendFortron = false
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
	  if (evt.Item.getItem.isInstanceOf[ItemSkull])
    {
		evt.world.setBlock(evt.x, evt.y, evt.z, Blocks.skull, evt.Item.getItemDamage, 2)
      val tile = evt.world.getTileEntity(evt.x, evt.y, evt.z)

      if (tile.isInstanceOf[TileEntitySkull])
      {
		  val nbt = evt.Item.getTagCompound

        if (nbt != null)
        {
          var gameProfile: GameProfile = null

          if (nbt.hasKey("SkullOwner", 10))
          {
            gameProfile = NBTUtil.func_152459_a(nbt.getCompoundTag("SkullOwner"))
          }
          else if (nbt.hasKey("SkullOwner", 8) && nbt.getString("SkullOwner").length > 0)
          {
            gameProfile = new GameProfile(null.asInstanceOf[UUID], nbt.getString("SkullOwner"))
          }

			if (gameProfile != null)
				tile.asInstanceOf[TileEntitySkull].func_152106_a(gameProfile)
			else {
				tile.asInstanceOf[TileEntitySkull].func_152107_a(evt.Item.getItemDamage)
			}

          Blocks.skull.asInstanceOf[BlockSkull].func_149965_a(evt.world, evt.x, evt.y, evt.z, tile.asInstanceOf[TileEntitySkull])
        }
      }

		evt.Item.stackSize -= 1
      evt.setCanceled(true)
    }
  }

  @SubscribeEvent
  def playerInteractEvent(evt: PlayerInteractEvent)
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

	  val position = new Vector3d(evt.x, evt.y, evt.z)

    val relevantProjectors = MFFSUtility.getRelevantProjectors(evt.entityPlayer.worldObj, position)

    //Check if we can sync this block (activate). If not, we cancel the event.
	  if (!relevantProjectors.forall(x => x.isAccessGranted(evt.entityPlayer.worldObj, new Vector3d(evt.x, evt.y, evt.z), evt.entityPlayer, evt.action)))
    {
      //Check if player has permission
      evt.entityPlayer.addChatMessage(new ChatComponentText("[" + Reference.name + "] You have no permission to do that!"))
      evt.setCanceled(true)
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
		val vec = new Vector3d(evt.x, evt.y, evt.z)

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
    if (!evt.entity.isInstanceOf[EntityPlayer])
    {
		if (MFFSUtility.getRelevantProjectors(evt.world, new Vector3d(evt.entityLiving)).exists(_.getModuleCount(Content.moduleAntiSpawn) > 0))
      {
        evt.setResult(Event.Result.DENY)
      }
    }
  }
}