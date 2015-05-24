package com.calclavia.edx.mechanical.mech.turbine

import java.util.{HashSet, Set}

import com.calclavia.edx.mechanical.mech.TileMechanical
import cpw.mods.fml.relauncher.{Side, SideOnly}
import io.netty.buffer.ByteBuf
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.AxisAlignedBB
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import resonantengine.core.network.discriminator.PacketType
import resonantengine.lib.transform.vector.Vector3
import resonantengine.lib.wrapper.ByteBufWrapper._
import resonantengine.prefab.block.multiblock.IMultiBlockStructure

import scala.collection.JavaConversions._

/** Reduced version of the main turbine class */
class TileTurbine extends TileMechanical(Material.wood) with IMultiBlockStructure[TileTurbine]
{
  /** MutliBlock methods. */
  private val multiBlock = new TurbineMBlockHandler(this)
  /** Tier of the turbine */
  var tier = 0
  /** Radius of large turbine? */
  var multiBlockRadius = 1

  //Constructor
  mechanicalNode = new NodeTurbine(this)
  normalRender = false
  isOpaqueCube = false
  textureName = "material_wood_surface"
  rotationMask = 63

  override def onRemove(block: Block, par1: Int)
  {
    getMultiBlock.deconstruct()
    super.onRemove(block, par1)
  }

  override def update()
  {
    super.update()

    getMultiBlock.update()

    if (getMultiBlock.isPrimary)
    {
      if (mechanicalNode.angularVelocity != 0)
      {
        playSound()
      }
    }
  }

  /** Called to play sound effects */
  def playSound()
  {
  }

  def getArea: Int = (((multiBlockRadius + 0.5) * 2) * ((multiBlockRadius + 0.5) * 2)).toInt

  @SideOnly(Side.CLIENT)
  override def getRenderBoundingBox: AxisAlignedBB =
  {
    return AxisAlignedBB.getBoundingBox(this.xCoord - multiBlockRadius, this.yCoord - multiBlockRadius, this.zCoord - multiBlockRadius, this.xCoord + 1 + multiBlockRadius, this.yCoord + 1 + multiBlockRadius, this.zCoord + 1 + multiBlockRadius)
  }

  def getMultiBlockVectors: java.lang.Iterable[Vector3] =
  {
    val vectors: Set[Vector3] = new HashSet[Vector3]
    val dir: ForgeDirection = getDirection
    val xMulti: Int = if (dir.offsetX != 0) 0 else 1
    val yMulti: Int = if (dir.offsetY != 0) 0 else 1
    val zMulti: Int = if (dir.offsetZ != 0) 0 else 1

    for (x: Int <- -multiBlockRadius to multiBlockRadius)
    {
      for (y: Int <- -multiBlockRadius to multiBlockRadius)
      {

        for (z: Int <- -multiBlockRadius to multiBlockRadius)
        {
          vectors.add(new Vector3(x * xMulti, y * yMulti, z * zMulti))
        }
      }
    }

    return vectors
  }

  override def getPosition: Vector3 = position

  def onMultiBlockChanged()
  {
    worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, if (getBlockType != null) getBlockType else null)
    worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
  }

  /** Reads a tile entity from NBT. */
  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    multiBlockRadius = nbt.getInteger("multiBlockRadius")
    tier = nbt.getInteger("tier")
    getMultiBlock.load(nbt)
  }

  /** Writes a tile entity to NBT. */
  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setInteger("multiBlockRadius", multiBlockRadius)
    nbt.setInteger("tier", tier)
    getMultiBlock.save(nbt)
  }

  override def read(buf: ByteBuf, id: Int, packetType: PacketType)
  {
    super.read(buf, id, packetType)

    if (id == 0)
    {
      multiBlockRadius = buf.readInt()
      tier = buf.readInt()
      buf >>>> getMultiBlock.load
    }
  }

  def getMultiBlock: TurbineMBlockHandler =
  {
    return multiBlock
  }

  override def write(buf: ByteBuf, id: Int)
  {
    super.write(buf, id)

    if (id == 0)
    {
      buf <<< multiBlockRadius
      buf <<< tier
      buf <<<< getMultiBlock.save
    }
  }

  def getWorld: World =
  {
    return worldObj
  }

  override def configure(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (!player.isSneaking)
    {
      if (getMultiBlock.isConstructed)
      {
        getMultiBlock.deconstruct()
        multiBlockRadius += 1
        if (!getMultiBlock.construct())
        {
          multiBlockRadius = 1
        }
        return true
      }
      else
      {
        if (!getMultiBlock.construct())
        {
          multiBlockRadius = 1
          getMultiBlock.construct()
        }
      }
    }
    else
    {
      val toFlip: Set[TileTurbine] = new HashSet[TileTurbine]
      if (!getMultiBlock.isConstructed)
      {
        toFlip.add(this)
      }
      else
      {
        val str: Set[TileTurbine] = getMultiBlock.getPrimary.getMultiBlock.getStructure
        if (str != null) toFlip.addAll(str)
      }
      for (turbine <- toFlip)
      {
        if (side == turbine.getDirection.ordinal) world.setBlockMetadataWithNotify(turbine.xCoord, turbine.yCoord, turbine.zCoord, side ^ 1, 3)
        else world.setBlockMetadataWithNotify(turbine.xCoord, turbine.yCoord, turbine.zCoord, side, 3)
      }
    }
    return true
  }
}