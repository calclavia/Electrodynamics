package mffs.field.mode

import java.io.File
import java.util
import java.util.{Set => JSet}

import mffs.Settings
import mffs.content.Content
import mffs.field.module.ItemModuleArray
import mffs.util.CacheHandler

class ItemShapeCustom extends ItemShape with CacheHandler
{
  private final val NBT_ID: String = "id"
  private final val NBT_MODE: String = "mode"
  private final val NBT_POINT_1: String = "point1"
  private final val NBT_POINT_2: String = "point2"
  private final val NBT_FIELD_BLOCK_LIST: String = "fieldPoints"
  private final val NBT_FIELD_BLOCK_NAME: String = "blockID"
  private final val NBT_FIELD_BLOCK_METADATA: String = "blockMetadata"
  private final val NBT_FIELD_SIZE: String = "fieldSize"
  private final val NBT_FILE_SAVE_PREFIX: String = "custom_mode_"

  val modes = Array(Content.modeCube, Content.modeSphere, Content.modeTube, Content.modePyramid)

	override def addInformation(Item: Item, par2EntityPlayer: EntityPlayer, list: util.List[_], par4: Boolean)
  {
	  val nbt: NBTTagCompound = NBTUtility.getNBTTagCompound(Item)
	  list.add(Game.instance.get.languageManager.getLocal("info.modeCustom.mode") + " " + (if (nbt.getBoolean(NBT_MODE)) Game.instance.get.languageManager.getLocal("info.modeCustom.substraction") else Game.instance.get.languageManager.getLocal("info.modeCustom.additive")))
	  val point1: Vector3d = new Vector3d(nbt.getCompoundTag(NBT_POINT_1))
	  list.add(Game.instance.get.languageManager.getLocal("info.modeCustom.point1") + " " + point1.xi + ", " + point1.yi + ", " + point1.zi)
	  val point2: Vector3d = new Vector3d(nbt.getCompoundTag(NBT_POINT_2))
	  list.add(Game.instance.get.languageManager.getLocal("info.modeCustom.point2") + " " + point2.xi + ", " + point2.yi + ", " + point2.zi)
    val modeID: Int = nbt.getInteger(NBT_ID)
    if (modeID > 0)
    {
		list.add(Game.instance.get.languageManager.getLocal("info.modeCustom.modeID") + " " + modeID)
      val fieldSize: Int = nbt.getInteger(NBT_FIELD_SIZE)
      if (fieldSize > 0)
      {
		  list.add(Game.instance.get.languageManager.getLocal("info.modeCustom.fieldSize") + " " + fieldSize)
      }
      else
      {
		  list.add(Game.instance.get.languageManager.getLocal("info.modeCustom.notSaved"))
      }
    }
    if (GuiScreen.isShiftKeyDown)
    {
		super.addInformation(Item, par2EntityPlayer, list, par4)
    }
    else
    {
		list.add(Game.instance.get.languageManager.getLocal("info.modeCustom.shift"))
    }
  }

	override def onItemRightClick(Item: Item, world: World, entityPlayer: EntityPlayer): Item =
  {
	  if (Game.instance.networkManager.isServer)
    {
      if (entityPlayer.isSneaking)
      {
		  val nbt: NBTTagCompound = NBTUtility.getNBTTagCompound(Item)
        if (nbt != null)
        {
			val point1 = new Vector3d(nbt.getCompoundTag(NBT_POINT_1))
			val point2 = new Vector3d(nbt.getCompoundTag(NBT_POINT_2))

          if (nbt.hasKey(NBT_POINT_1) && nbt.hasKey(NBT_POINT_2) && !(point1 == point2))
          {
            if (point1.distance(point2) < Settings.maxForceFieldScale)
            {
              nbt.removeTag(NBT_POINT_1)
              nbt.removeTag(NBT_POINT_2)
              var midPoint = point1.midpoint(point2).floor
              point1 -= midPoint
              point2 -= midPoint
              val minPoint = point1.min(point2)
              val maxPoint = point1.max(point2)

				var saveNBT = NBTUtility.loadData(getSaveDirectory, NBT_FILE_SAVE_PREFIX + getModeID(Item))

              if (saveNBT == null)
              {
                saveNBT = new NBTTagCompound
              }

              var list: NBTTagList = null
              if (saveNBT.hasKey(NBT_FIELD_BLOCK_LIST))
              {
                list = saveNBT.getTag(NBT_FIELD_BLOCK_LIST).asInstanceOf[NBTTagList]
              }
              else
              {
                list = new NBTTagList
              }

              for (x <- minPoint.xi to maxPoint.xi; y <- minPoint.yi to maxPoint.yi; z <- minPoint.zi to maxPoint.zi)
              {
				  val position = new Vector3d(x, y, z)
                val targetCheck = midPoint + position
                val block = targetCheck.getBlock(world)

                if (!block.isAir(world, targetCheck.xi, targetCheck.yi, targetCheck.zi))
                {
                  /**
                   * Additive and Subtractive modes
                   */
                  if (!nbt.getBoolean(NBT_MODE))
                  {
                    val vectorTag = position.toNBT
                    vectorTag.setString(NBT_FIELD_BLOCK_NAME, Block.blockRegistry.getNameForObject(block))
                    vectorTag.setInteger(NBT_FIELD_BLOCK_METADATA, targetCheck.getBlockMetadata(world))
                    list.appendTag(vectorTag)
                  }
                  else
                  {
					  (0 until list.tagCount) filter (i => new Vector3d(list.getCompoundTagAt(i)).equals(position)) foreach (list.removeTag(_))
                  }
                }
              }

              saveNBT.setTag(NBT_FIELD_BLOCK_LIST, list)
              nbt.setInteger(NBT_FIELD_SIZE, list.tagCount)
				NBTUtility.saveData(getSaveDirectory, NBT_FILE_SAVE_PREFIX + getModeID(Item), saveNBT)
              clearCache()
				entityPlayer.addChatMessage(new ChatComponentText(Game.instance.get.languageManager.getLocal("message.modeCustom.saved")))
            }
          }
        }
      }
      else
      {
		  val nbt = NBTUtility.getNBTTagCompound(Item)

        if (nbt != null)
        {
          nbt.setBoolean(NBT_MODE, !nbt.getBoolean(NBT_MODE))
			entityPlayer.addChatMessage(new
					ChatComponentText(Game.instance.get.languageManager.getLocal("message.modeCustom.modeChange").replaceAll("#p", (if (nbt.getBoolean(NBT_MODE)) Game.instance.get.languageManager.getLocal("info.modeCustom.substraction") else Game.instance.get.languageManager.getLocal("info.modeCustom.additive")))))
        }
      }
    }
	  return Item
  }

	override def onItemUse(Item: Item, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, par7: Int, par8: Float, par9: Float, par10: Float): Boolean =
  {
	  if (Game.instance.networkManager.isServer)
    {
		val nbt: NBTTagCompound = NBTUtility.getNBTTagCompound(Item)
      if (nbt != null)
      {
		  val point1: Vector3d = new Vector3d(nbt.getCompoundTag(NBT_POINT_1))
		  if (!nbt.hasKey(NBT_POINT_1) || (point1 == new Vector3d(0, 0, 0)))
        {
			nbt.setTag(NBT_POINT_1, new Vector3d(x, y, z).toNBT)
          player.addChatMessage(new ChatComponentText("Set point 1: " + x + ", " + y + ", " + z + "."))
        }
        else
        {
			nbt.setTag(NBT_POINT_2, new Vector3d(x, y, z).toNBT)
          player.addChatMessage(new ChatComponentText("Set point 2: " + x + ", " + y + ", " + z + "."))
        }
      }
    }
    return true
  }

	def getFieldBlockMap(projector: IFieldMatrix, Item: Item): mutable.Map[Vector3d, (Block, Int)] =
  {
	  val cacheID = "Item_" + Item.hashCode

	  if (hasCache(classOf[mutable.Map[Vector3d, (Block, Int)]], cacheID)) return getCache(classOf[mutable.Map[Vector3d, (Block, Int)]], cacheID)

	  val fieldMap = getFieldBlockMapClean(projector, Item)

    if (projector.getModuleCount(Content.moduleArray) > 0)
    {
      val longestDirectional = (Content.moduleArray.asInstanceOf[ItemModuleArray]).getDirectionWidthMap(fieldMap.keySet)

      for (direction <- ForgeDirection.VALID_DIRECTIONS)
      {
        val copyAmount = projector.getSidedModuleCount(Content.moduleArray, direction)
        val directionalDisplacement = (Math.abs(longestDirectional(direction)) + Math.abs(longestDirectional(direction.getOpposite))) + 1

        (0 until copyAmount) foreach (i =>
        {
          val directionalDisplacementScale = directionalDisplacement * (i + 1)

			getFieldBlocks(projector, Item) foreach (originalVec =>
          {
			  val newFieldBlock = originalVec.clone + new Vector3d(direction) * directionalDisplacementScale
            fieldMap.put(newFieldBlock, fieldMap(originalVec))
          })
        })
      }
    }

    cache(cacheID, fieldMap)

    return fieldMap
  }

	override def getInteriorPoints(projector: IFieldMatrix): JSet[Vector3d] =
  {
    return this.getExteriorPoints(projector)
  }

	override def getExteriorPoints(projector: IFieldMatrix): JSet[Vector3d] =
  {
    return this.getFieldBlocks(projector, projector.getModeStack)
  }

	def getFieldBlocks(projector: IFieldMatrix, Item: Item): Set[Vector3d] =
  {
	  return getFieldBlockMapClean(projector, Item).keySet.toSet
  }

	def getFieldBlockMapClean(projector: IFieldMatrix, Item: Item): mutable.Map[Vector3d, (Block, Int)] =
  {
    val scale = (projector.getModuleCount(Content.moduleScale) / 3f + 1)
	  val fieldBlocks = mutable.Map.empty[Vector3d, (Block, Int)]

    if (getSaveDirectory != null)
    {
		val nbt = NBTUtility.loadData(this.getSaveDirectory, NBT_FILE_SAVE_PREFIX + getModeID(Item))

      if (nbt != null)
      {
        val nbtTagList = nbt.getTagList(NBT_FIELD_BLOCK_LIST, 10)

        (0 until nbtTagList.tagCount)
          .map(i => nbtTagList.getCompoundTagAt(i))
			.foreach(vectorTag => fieldBlocks.put(new
				Vector3d(vectorTag) * scale, (Block.blockRegistry.getObject(vectorTag.getString(NBT_FIELD_BLOCK_NAME)).asInstanceOf[Block], vectorTag.getInteger(NBT_FIELD_BLOCK_METADATA))))
      }
    }
    return fieldBlocks
  }

	def getModeID(Item: Item): Int =
  {
	  val nbt = NBTUtility.getNBTTagCompound(Item)
    var id: Int = nbt.getInteger(NBT_ID)
    if (id <= 0)
    {
      nbt.setInteger(NBT_ID, getNextAvaliableID)
      id = nbt.getInteger(NBT_ID)
    }
    return id
  }

  def getNextAvaliableID: Int =
  {
    var i: Int = 1
    for (fileEntry <- this.getSaveDirectory.listFiles)
    {
      i += 1
    }
    return i
  }

  def getSaveDirectory: File =
  {
    val saveDirectory: File = NBTUtility.getSaveDirectory(MinecraftServer.getServer.getFolderName)
    if (!saveDirectory.exists)
    {
      saveDirectory.mkdir
    }
    val file: File = new File(saveDirectory, "mffs")
    if (!file.exists)
    {
      file.mkdir
    }
    return file
  }

	override def isInField(projector: IFieldMatrix, position: Vector3d): Boolean =
  {
    return false
  }

  @SideOnly(Side.CLIENT)
  override def render(projector: IProjector, x: Double, y: Double, z: Double, f: Float, ticks: Long)
  {
    modes((projector.asInstanceOf[TileEntity]).getWorldObj().rand.nextInt(modes.length - 1)).render(projector, x, y, z, f, ticks)
  }

  override def getFortronCost(amplifier: Float): Float =
  {
    return super.getFortronCost(amplifier) * amplifier
  }
}