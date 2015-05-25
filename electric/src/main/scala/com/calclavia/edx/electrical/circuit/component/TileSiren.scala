package com.calclavia.edx.electrical.circuit.component

import edx.core.Reference
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import nova.core.util.Direction
import resonantengine.lib.grid.energy.electric.NodeElectricComponent
import resonantengine.lib.modcontent.block.ResonantTile
import resonantengine.lib.transform.vector.Vector3
import resonantengine.prefab.block.impl.TBlockNodeProvider

import scala.collection.convert.wrapAll._

/**
 * Siren block
 */
class TileSiren extends ResonantTile(Material.wood) with TBlockNodeProvider
{
  val electricNode = new NodeElectricComponent(this)
  nodes.add(electricNode)
  electricNode.dynamicTerminals = true
  electricNode.setPositives(Set(Direction.NORTH, Direction.UP, Direction.EAST))
  electricNode.setNegatives(Set(Direction.SOUTH, Direction.DOWN, Direction.WEST))

  override def update()
  {
    super.update()

    if (ticks % 30 == 0)
    {
      if (world != null)
      {
        val metadata: Int = world.getBlockMetadata(x, y, z)
        if (world.getBlockPowerInput(x, y, z) > 0)
        {
          var volume: Float = 0.5f
          for (i <- 0 to 6)
          {
            val check: Vector3 = position.add(Direction.getOrientation(i))
            if (check.getBlock(world) == getBlockType)
            {
              volume *= 1.5f
            }
          }
          world.playSoundEffect(x, y, z, Reference.prefix + "siren", volume, 1f - 0.18f * (metadata / 15f))
        }
      }

      if (!world.isRemote)
      {
        val volume = electricNode.power.toFloat / 1000f
        world.playSoundEffect(x, y, z, Reference.prefix + "siren", volume, 1f - 0.18f * (metadata / 15f))
      }
    }
  }

  override def configure(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    var metadata: Int = world.getBlockMetadata(x, y, z)
    if (player.isSneaking)
    {
      metadata -= 1
    }
    else
    {
      metadata += 1
    }
    metadata = Math.max(metadata % 16, 0)
    world.setBlockMetadataWithNotify(x, y, z, metadata, 2)
    return true
  }
}