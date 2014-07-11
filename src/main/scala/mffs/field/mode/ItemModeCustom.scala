package mffs.field.mode

import java.io.File
import java.util
import java.util.{Set => JSet}

import cpw.mods.fml.relauncher.{Side, SideOnly}
import mffs.field.module.ItemModuleArray
import mffs.util.TCache
import mffs.{Content, ModularForceFieldSystem, Settings}
import net.minecraft.block.Block
import net.minecraft.client.gui.GuiScreen
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.server.MinecraftServer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ChatComponentText
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.mffs.{IFieldMatrix, IProjector}
import resonant.lib.utility.LanguageUtility
import resonant.lib.utility.nbt.NBTUtility
import resonant.lib.wrapper.WrapList._
import universalelectricity.core.transform.vector.Vector3

import scala.collection.JavaConversions._

class ItemModeCustom extends ItemMode with TCache
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

  override def addInformation(itemStack: ItemStack, par2EntityPlayer: EntityPlayer, list: util.List[_], par4: Boolean)
  {
    val nbt: NBTTagCompound = NBTUtility.getNBTTagCompound(itemStack)
    list.add(LanguageUtility.getLocal("info.modeCustom.mode") + " " + (if (nbt.getBoolean(NBT_MODE)) LanguageUtility.getLocal("info.modeCustom.substraction") else LanguageUtility.getLocal("info.modeCustom.additive")))
    val point1: Vector3 = new Vector3(nbt.getCompoundTag(NBT_POINT_1))
    list.add(LanguageUtility.getLocal("info.modeCustom.point1") + " " + point1.xi + ", " + point1.yi + ", " + point1.zi)
    val point2: Vector3 = new Vector3(nbt.getCompoundTag(NBT_POINT_2))
    list.add(LanguageUtility.getLocal("info.modeCustom.point2") + " " + point2.xi + ", " + point2.yi + ", " + point2.zi)
    val modeID: Int = nbt.getInteger(NBT_ID)
    if (modeID > 0)
    {
      list.add(LanguageUtility.getLocal("info.modeCustom.modeID") + " " + modeID)
      val fieldSize: Int = nbt.getInteger(NBT_FIELD_SIZE)
      if (fieldSize > 0)
      {
        list.add(LanguageUtility.getLocal("info.modeCustom.fieldSize") + " " + fieldSize)
      }
      else
      {
        list.add(LanguageUtility.getLocal("info.modeCustom.notSaved"))
      }
    }
    if (GuiScreen.isShiftKeyDown)
    {
      super.addInformation(itemStack, par2EntityPlayer, list, par4)
    }
    else
    {
      list.add(LanguageUtility.getLocal("info.modeCustom.shift"))
    }
  }

  override def onItemRightClick(itemStack: ItemStack, world: World, entityPlayer: EntityPlayer): ItemStack =
  {
    if (!world.isRemote)
    {
      if (entityPlayer.isSneaking)
      {
        val nbt: NBTTagCompound = NBTUtility.getNBTTagCompound(itemStack)
        if (nbt != null)
        {
          val point1 = new Vector3(nbt.getCompoundTag(NBT_POINT_1))
          val point2 = new Vector3(nbt.getCompoundTag(NBT_POINT_2))

          if (nbt.hasKey(NBT_POINT_1) && nbt.hasKey(NBT_POINT_2) && !(point1 == point2))
          {
            if (point1.distance(point2) < Settings.maxForceFieldScale)
            {
              nbt.removeTag(NBT_POINT_1)
              nbt.removeTag(NBT_POINT_2)
              var midPoint: Vector3 = new Vector3
              midPoint.x = (point1.x + point2.x) / 2
              midPoint.y = (point1.y + point2.y) / 2
              midPoint.z = (point1.z + point2.z) / 2
              midPoint = midPoint.floor
              point1.subtract(midPoint)
              point2.subtract(midPoint)
              val minPoint: Vector3 = new Vector3(Math.min(point1.x, point2.x), Math.min(point1.y, point2.y), Math.min(point1.z, point2.z))
              val maxPoint: Vector3 = new Vector3(Math.max(point1.x, point2.x), Math.max(point1.y, point2.y), Math.max(point1.z, point2.z))
              var saveNBT: NBTTagCompound = NBTUtility.loadData(this.getSaveDirectory, NBT_FILE_SAVE_PREFIX + getModeID(itemStack))
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
              {
                var x: Int = minPoint.xi
                while (x <= maxPoint.xi)
                {
                  {
                    {
                      var y: Int = minPoint.yi
                      while (y <= maxPoint.yi)
                      {
                        {
                          {
                            var z: Int = minPoint.zi
                            while (z <= maxPoint.zi)
                            {
                              {
                                val position = new Vector3(x, y, z)
                                val targetCheck = midPoint.clone + position
                                val block = targetCheck.getBlock(world)
                                if (block != Blocks.air)
                                {
                                  if (!nbt.getBoolean(NBT_MODE))
                                  {
                                    val vectorTag = position.toNBT
                                    vectorTag.setString(NBT_FIELD_BLOCK_NAME, Block.blockRegistry.getNameForObject(block))
                                    vectorTag.setInteger(NBT_FIELD_BLOCK_METADATA, targetCheck.getBlockMetadata(world))
                                    list.appendTag(vectorTag)
                                  }
                                  else
                                  {
                                    {
                                      var i: Int = 0
                                      while (i < list.tagCount)
                                      {
                                        val vector = new Vector3(list.getCompoundTagAt(i))

                                        if (vector == position)
                                        {
                                          list.removeTag(i)
                                        }

                                        i += 1
                                      }
                                    }
                                  }
                                }
                              }
                              z += 1
                            }
                          }
                        }
                        y += 1
                      }
                    }
                  }
                  x += 1
                }
              }
              saveNBT.setTag(NBT_FIELD_BLOCK_LIST, list)
              nbt.setInteger(NBT_FIELD_SIZE, list.tagCount)
              NBTUtility.saveData(getSaveDirectory, NBT_FILE_SAVE_PREFIX + getModeID(itemStack), saveNBT)
              clearCache()
              entityPlayer.addChatMessage(new ChatComponentText(LanguageUtility.getLocal("message.modeCustom.saved")))
            }
          }
        }
      }
      else
      {
        val nbt = NBTUtility.getNBTTagCompound(itemStack)

        if (nbt != null)
        {
          nbt.setBoolean(NBT_MODE, !nbt.getBoolean(NBT_MODE))
          entityPlayer.addChatMessage(new ChatComponentText(LanguageUtility.getLocal("message.modeCustom.modeChange").replaceAll("%p", (if (nbt.getBoolean(NBT_MODE)) LanguageUtility.getLocal("info.modeCustom.substraction") else LanguageUtility.getLocal("info.modeCustom.additive")))))
        }
      }
    }
    return itemStack
  }

  override def onItemUse(itemStack: ItemStack, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, par7: Int, par8: Float, par9: Float, par10: Float): Boolean =
  {
    if (!world.isRemote)
    {
      val nbt: NBTTagCompound = NBTUtility.getNBTTagCompound(itemStack)
      if (nbt != null)
      {
        val point1: Vector3 = new Vector3(nbt.getCompoundTag(NBT_POINT_1))
        if (!nbt.hasKey(NBT_POINT_1) || (point1 == new Vector3(0, 0, 0)))
        {
          nbt.setTag(NBT_POINT_1, new Vector3(x, y, z).toNBT)
          player.addChatMessage(new ChatComponentText("Set point 1: " + x + ", " + y + ", " + z + "."))
        }
        else
        {
          nbt.setTag(NBT_POINT_2, new Vector3(x, y, z).toNBT)
          player.addChatMessage(new ChatComponentText("Set point 2: " + x + ", " + y + ", " + z + "."))
        }
      }
    }
    return true
  }

  def getModeID(itemStack: ItemStack): Int =
  {
    val nbt = NBTUtility.getNBTTagCompound(itemStack)
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

  def getFieldBlocks(projector: IFieldMatrix, itemStack: ItemStack): Set[Vector3] =
  {
    return getFieldBlockMapClean(projector, itemStack).keySet
  }

  def getFieldBlockMap(projector: IFieldMatrix, itemStack: ItemStack): Map[Vector3, (Block, Int)] =
  {
    val cacheID = "itemStack_" + itemStack.hashCode

    if (hasCache(classOf[Map[Vector3, (Block, Int)]], cacheID)) return getCache(classOf[Map[Vector3, (Block, Int)]], cacheID)

    val fieldMap = getFieldBlockMapClean(projector, itemStack)

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

          getFieldBlocks(projector, itemStack) foreach (originalVec =>
          {
            val newFieldBlock = originalVec.clone + new Vector3(direction) * directionalDisplacementScale
            fieldMap.put(newFieldBlock, fieldMap(originalVec))
          })
        })
      }
    }

    cache(cacheID, fieldMap)

    return fieldMap
  }

  def getFieldBlockMapClean(projector: IFieldMatrix, itemStack: ItemStack): Map[Vector3, (Block, Int)] =
  {
    val scale = projector.getModuleCount(Content.moduleScale) / 3f
    val fieldBlocks = Map[Vector3, (Block, Int)]()

    if (getSaveDirectory != null)
    {
      val nbt: NBTTagCompound = NBTUtility.loadData(this.getSaveDirectory, NBT_FILE_SAVE_PREFIX + getModeID(itemStack))

      if (nbt != null)
      {
        val nbtTagList = nbt.getTagList(NBT_FIELD_BLOCK_LIST, 10)

        (0 until nbtTagList.tagCount) map (i => nbtTagList.getCompoundTagAt(i)) foreach (vectorTag => fieldBlocks.put(new Vector3(vectorTag) * scale, (Block.blockRegistry.getObject(vectorTag.getString(NBT_FIELD_BLOCK_NAME)).asInstanceOf[Block], vectorTag.getInteger(NBT_FIELD_BLOCK_METADATA))))
      }
    }
    return fieldBlocks
  }

  override def getExteriorPoints(projector: IFieldMatrix): JSet[Vector3] =
  {
    return this.getFieldBlocks(projector, projector.getModeStack)
  }

  override def getInteriorPoints(projector: IFieldMatrix): JSet[Vector3] =
  {
    return this.getExteriorPoints(projector)
  }

  override def isInField(projector: IFieldMatrix, position: Vector3): Boolean =
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