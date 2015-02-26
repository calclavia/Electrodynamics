package mffs.item

import java.util.{HashSet, List, Set}

import mffs.ModularForceFieldSystem
import mffs.api.card.CoordLink
import mffs.item.card.ItemCardFrequency
import mffs.particle.FieldColor
import mffs.security.MFFSPermissions
import mffs.util.MFFSUtility
import nova.core.item.Item

class ItemRemoteController extends ItemCardFrequency with CoordLink
{
	private final val remotesCached = new HashSet[Item]
	private final val temporaryRemoteBlacklist = new HashSet[Item]

  @SideOnly(Side.CLIENT)
  override def addInformation(Item: Item, entityplayer: EntityPlayer, list: List[_], flag: Boolean)
  {
	  super.addInformation(Item, entityplayer, list, flag)

	  if (hasLink(Item))
    {
		val vec: VectorWorld = getLink(Item)
      val block: Block = vec.getBlock(entityplayer.worldObj)
      if (block ne Blocks.air)
      {
		  list.add(Game.instance.get.languageManager.getLocal("info.item.linkedWith") + " " + block.getLocalizedName)
      }
      list.add(vec.xi + ", " + vec.yi + ", " + vec.zi)
		list.add(Game.instance.get.languageManager.getLocal("info.item.dimension") + " '" + vec.world.provider.getDimensionName + "'")
    }
    else
    {
		list.add(Game.instance.get.languageManager.getLocal("info.item.notLinked"))
    }
  }


	def hasLink(Item: Item): Boolean =
  {
	  return getLink(Item) != null
  }

	override def onItemUse(Item: Item, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, par7: Int, par8: Float, par9: Float, par10: Float): Boolean =
  {
	  if (Game.instance.networkManager.isServer && player.isSneaking)
    {
      val vector: VectorWorld = new VectorWorld(world, x, y, z)
		setLink(Item, vector)
      val block = vector.getBlock

      if (block != null)
      {
		  player.addChatMessage(new
				  ChatComponentText(Game.instance.get.languageManager.getLocal("message.remoteController.linked").replaceAll("#p", x + ", " + y + ", " + z).replaceAll("#q", block.getLocalizedName)))
      }
    }
    return true
  }

	def clearLink(Item: Item)
  {
	  Item.getTagCompound.removeTag("link")
  }

	override def onItemRightClick(Item: Item, world: World, entityPlayer: EntityPlayer): Item =
  {
    if (!entityPlayer.isSneaking)
    {
		val position: Vector3d = this.getLink(Item)
      if (position != null)
      {
        val block: Block = position.getBlock(world)
        if (block ne Blocks.air)
        {
          val chunk: Chunk = world.getChunkFromBlockCoords(position.xi, position.zi)
          if (chunk != null && chunk.isChunkLoaded && (MFFSUtility.hasPermission(world, position, Action.RIGHT_CLICK_BLOCK, entityPlayer) || MFFSUtility.hasPermission(world, position, MFFSPermissions.remoteControl, entityPlayer)))
          {
			  val requiredEnergy = new Vector3d(entityPlayer).distance(position) * (FluidContainerRegistry.BUCKET_VOLUME / 100)
            var receivedEnergy = 0
			  val fortronTiles: Set[IFortronFrequency] = FrequencyGridRegistry.instance.getNodes(classOf[IFortronFrequency], world, new Vector3d(entityPlayer), 50, this.getFrequency(Item))

            for (fortronTile <- fortronTiles)
            {
              val consumedEnergy: Int = fortronTile.requestFortron(Math.ceil(requiredEnergy / fortronTiles.size).asInstanceOf[Int], true)
              if (consumedEnergy > 0)
              {
                if (world.isRemote)
                {
					ModularForceFieldSystem.proxy.renderBeam(world, new Vector3d(entityPlayer).add(new Vector3d(0, entityPlayer.getEyeHeight - 0.2, 0)), new
							Vector3d(fortronTile.asInstanceOf[TileEntity]).add(0.5), FieldColor.blue, 20)
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
				  return Item
              }
            }
			  if (Game.instance.networkManager.isServer)
            {
				entityPlayer.addChatMessage(new ChatComponentText(Game.instance.get.languageManager.getLocal("message.remoteController.fail").replaceAll("#p", new
						UnitDisplay(UnitDisplay.Unit.JOULES, requiredEnergy).toString)))
            }
          }
        }
      }
    }
    else
    {
		super.onItemRightClick(Item, world, entityPlayer)
    }

	  return Item
  }

	def getLink(Item: Item): VectorWorld = {
		if (Item.stackTagCompound == null || !Item.getTagCompound.hasKey("link")) {
			return null
		}
		return new VectorWorld(Item.getTagCompound.getCompoundTag("link"))
	}

  @SubscribeEvent def preMove(evt: EventForceMobilize.EventPreForceManipulate)
  {
    this.temporaryRemoteBlacklist.clear
  }

  /**
   * Moves the coordinates of the link if the Force Manipulator moved a block that is linked by
   * the remote.
   *
   * @param evt
   */
  @SubscribeEvent def onMove(evt: EventForceMobilize.EventPostForceManipulate)
  {
    if (!evt.world.isRemote)
    {
		for (Item <- this.remotesCached)
      {
		  if (!temporaryRemoteBlacklist.contains(Item) && (new Vector3d(evt.beforeX, evt.beforeY, evt.beforeZ) == this.getLink(Item)))
        {
			this.setLink(Item, new VectorWorld(evt.world, evt.afterX, evt.afterY, evt.afterZ))
			temporaryRemoteBlacklist.add(Item)
        }
      }
    }
  }

	def setLink(Item: Item, vec: VectorWorld) {
		if (Item.getTagCompound == null) {
			Item.setTagCompound(new NBTTagCompound)
		}
		Item.getTagCompound.setTag("link", vec.toNBT)
	}

}