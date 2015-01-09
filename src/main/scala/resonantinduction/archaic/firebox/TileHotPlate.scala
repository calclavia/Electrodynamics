package resonantinduction.archaic.firebox

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
import resonant.lib.content.prefab.TInventory
import resonant.lib.network.discriminator.PacketType
import resonant.lib.network.handle.{TPacketReceiver, TPacketSender}
import resonant.lib.prefab.tile.spatial.{SpatialBlock, SpatialTile}
import resonant.lib.transform.region.Cuboid
import resonant.lib.transform.vector.{Vector2, Vector3}
import resonant.lib.wrapper.ByteBufWrapper._
import resonant.lib.wrapper.RandomWrapper._
import resonantinduction.core.Reference

/**
 * For smelting items.
 *
 * @author Calclavia
 */
object TileHotPlate
{
  final val maxSmeltTime: Int = 200
}

class TileHotPlate extends SpatialTile(Material.iron) with TInventory with TPacketSender with TPacketReceiver
{
  /** Amount of smelt time left */
  final val smeltTime = Array[Int](0, 0, 0, 0)
  final val stackSizeCache = Array[Int](0, 0, 0, 0)

  //Constructor
  bounds = new Cuboid(0, 0, 0, 1, 0.2f, 1)
  forceItemToRenderAsBlock = true
  isOpaqueCube = false
  tickRandomly = true

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
            smeltTime(i) = TileHotPlate.maxSmeltTime * stackSizeCache(i)
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
          }
          else if (smeltTime(i) > 0)
          {
            smeltTime(i) -= 1

            if (smeltTime(i) == 0)
            {
              if (!worldObj.isRemote)
              {
                val outputStack = FurnaceRecipes.smelting.getSmeltingResult(getStackInSlot(i)).copy
                outputStack.stackSize = stackSizeCache(i)
                setInventorySlotContents(i, outputStack)
                markUpdate()
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

  override def randomDisplayTick()
  {
    val height = 0.2
    val deviation = 0.22

    (0 to 3).foreach(
      i =>
      {
        if (smeltTime(i) > 0)
        {
          for (t <- 0 until (TileHotPlate.maxSmeltTime * stackSizeCache(i) - smeltTime(i)))
          {
            i match
            {
              case 0 => world.spawnParticle("smoke", x + 0.5 - deviation + world.rand.deviate(0.1), y + height, z + 0.5 - deviation + world.rand.deviate(0.1), 0.0D, 0.0D, 0.0D)
              case 1 => world.spawnParticle("smoke", x + 0.5 - deviation + world.rand.deviate(0.1), y + height, z + 0.5 + deviation + world.rand.deviate(0.1), 0.0D, 0.0D, 0.0D)
              case 2 => world.spawnParticle("smoke", x + 0.5 + deviation + world.rand.deviate(0.1), y + height, z + 0.5 - deviation + world.rand.deviate(0.1), 0.0D, 0.0D, 0.0D)
              case 3 => world.spawnParticle("smoke", x + 0.5 + deviation + world.rand.deviate(0.1), y + height, z + 0.5 + deviation + world.rand.deviate(0.1), 0.0D, 0.0D, 0.0D)
            }
          }
        }
      }
    )
  }

  def isSmelting: Boolean = (0 until getSizeInventory).exists(getSmeltTime(_) > 0)

  def getSmeltTime(i: Int): Int =
  {
    return smeltTime(i)
  }

  override def isItemValidForSlot(i: Int, itemStack: ItemStack): Boolean =
  {
    return i < getSizeInventory && canSmelt(itemStack)
  }

  def canSmelt(stack: ItemStack): Boolean = stack != null && FurnaceRecipes.smelting.getSmeltingResult(stack) != null

  override def write(buf: ByteBuf, id: Int)
  {
    super.write(buf, id)
    val nbt = new NBTTagCompound
    writeToNBT(nbt)
    buf <<< nbt
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)

    (0 until stackSizeCache.size).foreach(i => nbt.setInteger("stackSizeCache" + i, stackSizeCache(i)))
    (0 until getSizeInventory).foreach(i => nbt.setInteger("smeltTime" + i, smeltTime(i)))
  }

  override def getSizeInventory: Int = 4

  override def read(buf: ByteBuf, id: Int, packetType: PacketType)
  {
    super.read(buf, id, packetType)

    readFromNBT(buf.readTag())
    markRender()
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)

    (0 until stackSizeCache.size).foreach(i => stackSizeCache(i) = nbt.getInteger("stackSizeCache" + i))
    (0 until getSizeInventory).foreach(i => smeltTime(i) = nbt.getInteger("smeltTime" + i))

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
            smeltTime(i) += (getStackInSlot(i).stackSize - stackSizeCache(i)) * TileHotPlate.maxSmeltTime
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

}