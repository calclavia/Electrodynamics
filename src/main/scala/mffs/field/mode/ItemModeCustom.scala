package mffs.field.mode

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import mffs.util.TCache
import mffs.{ModularForceFieldSystem, Settings}
import mffs.field.module.ItemModuleArray
import net.minecraft.block.Block
import net.minecraft.client.gui.GuiScreen
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.server.MinecraftServer
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.mffs.IFieldInteraction
import resonant.api.mffs.IProjector
import resonant.api.mffs.modules.IProjectorMode
import resonant.lib.utility.LanguageUtility
import resonant.lib.utility.nbt.NBTUtility
import universalelectricity.core.transform.vector.Vector3
import java.io.File
import java.util.HashMap
import java.util.List
import java.util.Set


class ItemModeCustom extends ItemMode with TCache
{
  private final val NBT_ID: String = "id"
  private final val NBT_MODE: String = "mode"
  private final val NBT_POINT_1: String = "point1"
  private final val NBT_POINT_2: String = "point2"
  private final val NBT_FIELD_BLOCK_LIST: String = "fieldPoints"
  private final val NBT_FIELD_BLOCK_ID: String = "blockID"
  private final val NBT_FIELD_BLOCK_METADATA: String = "blockMetadata"
  private final val NBT_FIELD_SIZE: String = "fieldSize"
  private final val NBT_FILE_SAVE_PREFIX: String = "custom_mode_"

  override def addInformation(itemStack: ItemStack, par2EntityPlayer: EntityPlayer, list: List[_], par4: Boolean)
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
          val point1: Vector3 = new Vector3(nbt.getCompoundTag(NBT_POINT_1))
          val point2: Vector3 = new Vector3(nbt.getCompoundTag(NBT_POINT_2))
          if (nbt.hasKey(NBT_POINT_1) && nbt.hasKey(NBT_POINT_2) && !(point1 == point2))
          {
            if (point1.distance(point2) < Settings.MAX_FORCE_FIELD_SCALE)
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
                                val position: Vector3 = new Vector3(x, y, z)
                                val targetCheck: Vector3 = midPoint.clone.translate(position)
                                val blockID: Int = targetCheck.getBlockID(world)
                                if (blockID > 0)
                                {
                                  if (!nbt.getBoolean(NBT_MODE))
                                  {
                                    val vectorTag: NBTTagCompound = new NBTTagCompound
                                    position.writeToNBT(vectorTag)
                                    vectorTag.setInteger(NBT_FIELD_BLOCK_ID, blockID)
                                    vectorTag.setInteger(NBT_FIELD_BLOCK_METADATA, targetCheck.getBlockMetadata(world))
                                    list.appendTag(vectorTag)
                                  }
                                  else
                                  {
                                    {
                                      var i: Int = 0
                                      while (i < list.tagCount)
                                      {
                                        {
                                          val vector: Vector3 = new Vector3(list.tagAt(i).asInstanceOf[NBTTagCompound])
                                          if (vector == position)
                                          {
                                            list.removeTag(i)
                                          }
                                        }
                                        ({
                                          i += 1; i - 1
                                        })
                                      }
                                    }
                                  }
                                }
                              }
                              ({
                                z += 1; z - 1
                              })
                            }
                          }
                        }
                        ({
                          y += 1; y - 1
                        })
                      }
                    }
                  }
                  ({
                    x += 1; x - 1
                  })
                }
              }
              saveNBT.setTag(NBT_FIELD_BLOCK_LIST, list)
              nbt.setInteger(NBT_FIELD_SIZE, list.tagCount)
              NBTUtility.saveData(getSaveDirectory, NBT_FILE_SAVE_PREFIX + getModeID(itemStack), saveNBT)
              this.clearCache
              entityPlayer.addChatMessage(LanguageUtility.getLocal("message.modeCustom.saved"))
            }
          }
        }
      }
      else
      {
        val nbt: NBTTagCompound = NBTUtility.getNBTTagCompound(itemStack)
        if (nbt != null)
        {
          nbt.setBoolean(NBT_MODE, !nbt.getBoolean(NBT_MODE))
          entityPlayer.addChatMessage(LanguageUtility.getLocal("message.modeCustom.modeChange").replaceAll("%p", (if (nbt.getBoolean(NBT_MODE)) LanguageUtility.getLocal("info.modeCustom.substraction") else LanguageUtility.getLocal("info.modeCustom.additive"))))
        }
      }
    }
    return itemStack
  }

  override def onItemUse(itemStack: ItemStack, entityPlayer: EntityPlayer, world: World, x: Int, y: Int, z: Int, par7: Int, par8: Float, par9: Float, par10: Float): Boolean =
  {
    if (!world.isRemote)
    {
      val nbt: NBTTagCompound = NBTUtility.getNBTTagCompound(itemStack)
      if (nbt != null)
      {
        val point1: Vector3 = new Vector3(nbt.getCompoundTag(NBT_POINT_1))
        if (!nbt.hasKey(NBT_POINT_1) || (point1 == new Vector3(0, 0, 0)))
        {
          nbt.setCompoundTag(NBT_POINT_1, new Vector3(x, y, z).writeToNBT(new NBTTagCompound))
          entityPlayer.addChatMessage("Set point 1: " + x + ", " + y + ", " + z + ".")
        }
        else
        {
          nbt.setCompoundTag(NBT_POINT_2, new Vector3(x, y, z).writeToNBT(new NBTTagCompound))
          entityPlayer.addChatMessage("Set point 2: " + x + ", " + y + ", " + z + ".")
        }
      }
    }
    return true
  }

  def getModeID(itemStack: ItemStack): Int =
  {
    val nbt: NBTTagCompound = NBTUtility.getNBTTagCompound(itemStack)
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

  def getFieldBlocks(projector: IFieldInteraction, itemStack: ItemStack): Set[Vector3] =
  {
    return this.getFieldBlockMapClean(projector, itemStack).keySet
  }

  def getFieldBlockMap(projector: IFieldInteraction, itemStack: ItemStack): Map[Vector3, (Block, Int)] =
  {
    val cacheID: String = "itemStack_" + itemStack.hashCode

    if (Settings.useCache)
    {
      if (this.cache.containsKey(cacheID))
      {
        if (this.cache.get(cacheID).isInstanceOf[HashMap[_, _]])
        {
          return this.cache.get(cacheID).asInstanceOf[HashMap[Vector3, Array[Int]]]
        }
      }
    }

    val fieldMap: HashMap[Vector3, (Block, Int)] = this.getFieldBlockMapClean(projector, itemStack)

    if (projector.getModuleCount(ModularForceFieldSystem.itemModuleArray) > 0)
    {
      val longestDirectional: HashMap[ForgeDirection, Integer] = (ModularForceFieldSystem.itemModuleArray.asInstanceOf[ItemModuleArray]).getDirectionWidthMap(fieldMap.keySet)
      for (direction <- ForgeDirection.VALID_DIRECTIONS)
      {
        val copyAmount: Int = projector.getSidedModuleCount(ModularForceFieldSystem.itemModuleArray, direction)
        val directionalDisplacement: Int = (Math.abs(longestDirectional.get(direction)) + Math.abs(longestDirectional.get(direction.getOpposite))) + 1
        {
          var i: Int = 0
          while (i < copyAmount)
          {

              val directionalDisplacementScale: Int = directionalDisplacement * (i + 1)
              import scala.collection.JavaConversions._
              for (originalFieldBlock <- this.getFieldBlocks(projector, itemStack))
              {
                val newFieldBlock: Vector3 = originalFieldBlock.clone.translate(new Vector3(direction).scale(directionalDisplacementScale))
                fieldMap.put(newFieldBlock, fieldMap.get(originalFieldBlock))
              }

              i += 1
          }
        }
      }
    }

    if (Settings.useCache)
    {
      cache.put(cacheID, fieldMap)
    }

    return fieldMap
  }

  def getFieldBlockMapClean(projector: IFieldInteraction, itemStack: ItemStack): Map[Vector3, (Block, Int)] =
  {
    val scale: Float = projector.getModuleCount(ModularForceFieldSystem.itemModuleScale).asInstanceOf[Float] / 3
    val fieldBlocks =Map[Vector3, (Block, Int)]

    if (this.getSaveDirectory != null)
    {
      val nbt: NBTTagCompound = NBTUtility.loadData(this.getSaveDirectory, NBT_FILE_SAVE_PREFIX + getModeID(itemStack))
      if (nbt != null)
      {
        val nbtTagList: NBTTagList = nbt.getTagList(NBT_FIELD_BLOCK_LIST)
        {
          var i: Int = 0
          while (i < nbtTagList.tagCount)
          {

              val vectorTag: NBTTagCompound = nbtTagList.tagAt(i).asInstanceOf[NBTTagCompound]
              val position: Vector3 = new Vector3(vectorTag)
              if (scale > 0)
              {
                position.scale(scale)
              }

              if (position != null)
              {
                fieldBlocks.put(position, (vectorTag.getInteger(NBT_FIELD_BLOCK_ID), vectorTag.getInteger(NBT_FIELD_BLOCK_METADATA)))
              }

              i += 1;
          }
        }
      }
    }
    return fieldBlocks
  }

  def getCache(cacheID: String): AnyRef =
  {
    return this.cache.get(cacheID)
  }

  def clearCache(cacheID: String)
  {
    this.cache.remove(cacheID)
  }

  def clearCache
  {
    this.cache.clear
  }

  def getExteriorPoints(projector: IFieldInteraction): Set[Vector3] =
  {
    return this.getFieldBlocks(projector, projector.getModeStack)
  }

  def getInteriorPoints(projector: IFieldInteraction): Set[Vector3] =
  {
    return this.getExteriorPoints(projector)
  }

  override def isInField(projector: IFieldInteraction, position: Vector3): Boolean =
  {
    return false
  }

  @SideOnly(Side.CLIENT) override def render(projector: IProjector, x: Double, y: Double, z: Double, f: Float, ticks: Long)
  {
    val modes: Array[IProjectorMode] = Array[IProjectorMode](ModularForceFieldSystem.itemModeCube, ModularForceFieldSystem.itemModeSphere, ModularForceFieldSystem.itemModeTube, ModularForceFieldSystem.itemModePyramid)
    modes((projector.asInstanceOf[TileEntity]).getWorldObj().rand.nextInt(modes.length - 1)).render(projector, x, y, z, f, ticks)
  }

  override def getFortronCost(amplifier: Float): Float =
  {
    return super.getFortronCost(amplifier) * amplifier
  }

  private final val cache: HashMap[String, AnyRef] = new HashMap[_, _]
}