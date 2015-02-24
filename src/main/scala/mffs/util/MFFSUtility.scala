package mffs.util

import mffs.content.Content
import mffs.field.BlockProjector
import mffs.field.mode.ItemModeCustom

/**
 * A class containing some general helpful functions.
 *
 * @author Calclavia
 */
object MFFSUtility
{
  /**
   * Gets the first Item that is an ItemBlock in this TileEntity or in nearby chests.
   */
  def getFirstItemBlock(tileEntity: TileEntity, Item: Item): Item =
  {
	  return getFirstItemBlock(tileEntity, Item, true)
  }

	def getFirstItemBlock(tileEntity: TileEntity, Item: Item, recur: Boolean): Item =
  {
    if (tileEntity.isInstanceOf[IProjector])
    {
      val projector = tileEntity.asInstanceOf[IProjector]

		projector.getModuleSlots().find(getFirstItemBlock(_, projector, Item) != null) match
      {
			case Some(entry) => return getFirstItemBlock(entry, projector, Item)
        case _ =>
      }
    }
    else if (tileEntity.isInstanceOf[IInventory])
    {
      val inventory = tileEntity.asInstanceOf[IInventory]

		(0 until inventory.getSizeInventory()).view map (getFirstItemBlock(_, inventory, Item)) headOption match
      {
        case Some(entry) => return entry
        case _ =>
      }
    }

    if (recur)
    {
      ForgeDirection.VALID_DIRECTIONS.foreach(
        direction =>
        {
			val vector = new Vector3d(tileEntity) + direction
          val checkTile = vector.getTileEntity(tileEntity.getWorldObj())

          if (checkTile != null)
          {
			  val checkStack: Item = getFirstItemBlock(checkTile, Item, false)

            if (checkStack != null)
            {
              return checkStack
            }
          }
        })
    }
    return null
  }

	def getFirstItemBlock(i: Int, inventory: IInventory, Item: Item): Item =
  {
	  val checkStack: Item = inventory.getStackInSlot(i)
    if (checkStack != null && checkStack.getItem.isInstanceOf[ItemBlock])
    {
		if (Item == null || checkStack.isItemEqual(Item))
      {
        return checkStack
      }
    }
    return null
  }

	def getCamoBlock(proj: IProjector, position: Vector3d): Item =
  {
	  val projector = proj.asInstanceOf[BlockProjector]
    val tile = projector.asInstanceOf[TileEntity]

    if (projector != null)
    {
      if (!tile.getWorldObj().isRemote)
      {
        if (projector.getModuleCount(Content.moduleCamouflage) > 0)
        {
			if (projector.getShape.isInstanceOf[ItemModeCustom])
          {
			  val fieldMap = (projector.getShape.asInstanceOf[ItemModeCustom]).getFieldBlockMap(projector, projector.getShape)

            if (fieldMap != null)
            {
				val fieldCenter = new Vector3d(projector.asInstanceOf[TileEntity]) + projector.getTranslation()
				var relativePosition: Vector3d = position - fieldCenter
              relativePosition = relativePosition.transform(new EulerAngle(-projector.getRotationYaw, -projector.getRotationPitch, 0))

              val blockInfo = fieldMap(relativePosition.round)

              if (blockInfo != null && !blockInfo._1.isAir(tile.getWorldObj(), position.xi, position.yi, position.zi))
              {
				  return new Item(blockInfo._1, 1, blockInfo._2)
              }
            }
          }

          projector.getFilterStacks filter (getFilterBlock(_) != null) headOption match
          {
            case Some(entry) => return entry
            case _ => return null
          }
        }
      }
    }

    return null
  }

	def getFilterBlock(Item: Item): Block =
  {
	  if (Item != null)
    {
		return getFilterBlock(Item.getItem)

    }
    return null
  }

  def getFilterBlock(item: Item): Block =
  {
    if (item.isInstanceOf[ItemBlock])
    {
      return item.asInstanceOf[ItemBlock].field_150939_a
    }

    return null
  }

	def hasPermission(world: World, position: Vector3d, permission: Permission, player: EntityPlayer): Boolean =
  {
    return hasPermission(world, position, permission, player.getGameProfile())
  }

	def hasPermission(world: World, position: Vector3d, action: PlayerInteractEvent.Action, player: EntityPlayer): Boolean = {
		return getRelevantProjectors(world, position) forall (_.isAccessGranted(world, position, player, action))
  }

  /**
   * Gets the set of projectors that have an effect in this position.
   */
  def getRelevantProjectors(world: World, position: Vector3d): mutable.Set[BlockProjector] =
  {
	  return FrequencyGridRegistry.instance.asInstanceOf[GridFrequency].getNodes(classOf[BlockProjector]) filter (_.isInField(position))
  }

	def hasPermission(world: World, position: Vector3d, permission: Permission, profile: GameProfile): Boolean = {
		return getRelevantProjectors(world, position).forall(_.hasPermission(profile, permission))
  }
}