package resonantinduction.archaic.firebox

import java.util.{ArrayList, List}

import cpw.mods.fml.relauncher.{Side, SideOnly}
import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.IIcon
import net.minecraft.world.IBlockAccess
import resonant.content.spatial.block.SpatialBlock
import resonant.lib.content.prefab.java.TileInventory
import resonant.lib.network.ByteBufWrapper._
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.network.handle.{TPacketReceiver, TPacketSender}
import resonant.lib.transform.region.Cuboid
import resonant.lib.transform.vector.{Vector2, Vector3}
import resonantinduction.core.Reference

/**
 * For smelting items.
 *
 * @author Calclavia
 */
object TileHotPlate
{
  final val MAX_SMELT_TIME: Int = 200
}

class TileHotPlate extends TileInventory(Material.iron) with TPacketSender with TPacketReceiver
{
  final val smeltTime: Array[Int] = Array[Int](0, 0, 0, 0)
  final val stackSizeCache: Array[Int] = Array[Int](0, 0, 0, 0)

  //Constructor
  setSizeInventory(4)
  bounds = new Cuboid(0, 0, 0, 1, 0.2f, 1)
  forceItemToRenderAsBlock = true
  isOpaqueCube = false

  override def update()
  {
    super.update()

    if (canRun)
    {
      var didSmelt = false

      for (i <- 0 until getSizeInventory)
      {
        if (canSmelt(this.getStackInSlot(i)))
        {
          if (smeltTime(i) <= 0)
          {
            stackSizeCache(i) = this.getStackInSlot(i).stackSize
            smeltTime(i) = TileHotPlate.MAX_SMELT_TIME * stackSizeCache(i)
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
          }
          else if (smeltTime(i) > 0)
          {
            smeltTime(i) -= 1
            if (smeltTime(i) == 0)
            {
              if (!worldObj.isRemote)
              {
                val outputStack: ItemStack = FurnaceRecipes.smelting.getSmeltingResult(getStackInSlot(i)).copy
                outputStack.stackSize = stackSizeCache(i)
                setInventorySlotContents(i, outputStack)
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
              }
            }
          }
          didSmelt = true
        }
        else
        {
          smeltTime(i) = 0
        }
      }
    }
  }

  override def onInventoryChanged()
  {
    for (i <- 0 until getSizeInventory)
    {
      if (getStackInSlot(i) != null)
      {
        if (stackSizeCache(i) != getStackInSlot(i).stackSize)
        {
          if (smeltTime(i) > 0)
          {
            smeltTime(i) += (getStackInSlot(i).stackSize - stackSizeCache(i)) * TileHotPlate.MAX_SMELT_TIME
          }
          stackSizeCache(i) = getStackInSlot(i).stackSize
        }
      }
      else
      {
        stackSizeCache(i) = 0
      }
    }
    if (worldObj != null)
    {
      worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
    }
  }

  def canRun: Boolean =
  {
    val tileEntity = worldObj.getTileEntity(xCoord, yCoord - 1, zCoord)

    if (tileEntity.isInstanceOf[TileFirebox])
    {
      if ((tileEntity.asInstanceOf[TileFirebox]).isBurning)
      {
        return true
      }
    }
    return false
  }

  def canSmelt(stack: ItemStack): Boolean =
  {
    return stack != null && FurnaceRecipes.smelting.getSmeltingResult(stack) != null
  }

  def isSmelting: Boolean =
  {

    for (i <- 0 until getSizeInventory)
    {
      if (getSmeltTime(i) > 0)
      {
        return true
      }

    }

    return false
  }

  def getSmeltTime(i: Int): Int =
  {
    return smeltTime(i)
  }

  override def isItemValidForSlot(i: Int, itemStack: ItemStack): Boolean =
  {
    return i < getSizeInventory && canSmelt(itemStack)
  }

  override def write(buf: ByteBuf, id: Int)
  {
    super.write(buf, id)
    val nbt = new NBTTagCompound
    writeToNBT(nbt)
    buf <<< nbt
  }

  override def read(buf: ByteBuf, id: Int, packetType: PacketType)
  {
    super.read(buf, id, packetType)

    readFromNBT(buf.readTag())
    markRender()
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    for (i <- 0 until getSizeInventory)
    {
      smeltTime(i) = nbt.getInteger("smeltTime" + i)
    }
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)

    for (i <- 0 until getSizeInventory)
    {
      nbt.setInteger("smeltTime" + i, smeltTime(i))
    }
  }

  @SideOnly(Side.CLIENT)
  override def registerIcons(iconReg: IIconRegister)
  {
    super.registerIcons(iconReg)
    SpatialBlock.icon.put("electricHotPlate", iconReg.registerIcon(Reference.prefix + "electricHotPlate"))
    SpatialBlock.icon.put("hotPlate_on", iconReg.registerIcon(Reference.prefix + "hotPlate_on"))
  }

  /**
   * Called in the world.
   */
  override def getIcon(access: IBlockAccess, side: Int): IIcon =
  {
    return if (access.getBlockMetadata(xi, yi, zi) == 1) SpatialBlock.icon.get("electricHotPlate") else (if (canRun) SpatialBlock.icon.get("hotPlate_on") else SpatialBlock.icon.get(getTextureName))
  }

  @SideOnly(Side.CLIENT)
  override def getIcon(side: Int, meta: Int): IIcon =
  {
    return if (meta == 1) SpatialBlock.icon.get("electricHotPlate") else SpatialBlock.icon.get(getTextureName)
  }

  override def click(player: EntityPlayer)
  {
    if (server)
    {
      extractItem(this, 0, player)
    }
  }

  override def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (server)
    {
      val hitVector: Vector2 = new Vector2(hit.x, hit.z)
      val regionLength: Double = 1d / 2d
      var j: Int = 0
      for (j <- 0 until 2)
      {
        for (k <- 0 until 2)
        {
          val check = new Vector2(j, k) * regionLength
          if (check.distance(hitVector) < regionLength)
          {
            val slotID = j * 2 + k
            interactCurrentItem(this, slotID, player)
            onInventoryChanged()
            return true
          }
        }
      }
      return false
    }
    return true
  }

}