package edx.electrical.circuit.source

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.init.Blocks
import net.minecraft.util.IIcon
import net.minecraftforge.common.util.ForgeDirection
import resonantengine.lib.content.prefab.TIO
import resonantengine.lib.grid.energy.electric.NodeElectricComponent
import resonantengine.lib.modcontent.block.{ResonantBlock, ResonantTile}
import resonantengine.prefab.block.impl.TBlockNodeProvider

import scala.collection.convert.wrapAll._

class TileThermopile extends ResonantTile(Material.rock) with TBlockNodeProvider with TIO
{
  /**
   * The amount of ticks the thermopile will use the temperature differences before turning all
   * adjacent sides to thermal equilibrium.
   */
  private val maxTicks = 120 * 20
  private val electricNode = new NodeElectricComponent(this)
  private var ticksUsed = 0

  ioMap = 728
  nodes.add(electricNode)

  electricNode.dynamicTerminals = true
  electricNode.setPositives(Set(ForgeDirection.NORTH, ForgeDirection.EAST))
  electricNode.setNegatives(Set(ForgeDirection.SOUTH, ForgeDirection.WEST))

  override def update()
  {
    super.update()

    if (!world.isRemote)
    {
      var heatSources = 0
      var coolingSources = 0

      for (dir <- ForgeDirection.VALID_DIRECTIONS)
      {
        val checkPos = position + dir
        val block = checkPos.getBlock

        if (block == Blocks.water || block == Blocks.flowing_water)
        {
          coolingSources += 1
        }
        else if (block == Blocks.snow)
        {
          coolingSources += 2
        }
        else if (block == Blocks.ice)
        {
          coolingSources += 2
        }
        else if (block == Blocks.fire)
        {
          heatSources += 1
        }
        else if (block == Blocks.lava || block == Blocks.flowing_lava)
        {
          heatSources += 2
        }
      }
      val multiplier = 3 - Math.abs(heatSources - coolingSources)

      if (multiplier > 0 && coolingSources > 0 && heatSources > 0)
      {
        electricNode.generateVoltage(0.1 * multiplier)
        ticksUsed += 1

        if (ticksUsed >= maxTicks)
        {
          for (dir <- ForgeDirection.VALID_DIRECTIONS)
          {
            val checkPos = position.add(dir)
            val block = checkPos.getBlock(worldObj)

            if (block == Blocks.water || block == Blocks.flowing_water)
            {
              checkPos.setBlockToAir(worldObj)
            }
            else if (block == Blocks.ice)
            {
              checkPos.setBlock(worldObj, Blocks.water)
            }
            else if (block == Blocks.fire)
            {
              checkPos.setBlockToAir(worldObj)
            }
            else if (block == Blocks.lava || block == Blocks.flowing_lava)
            {
              checkPos.setBlock(worldObj, Blocks.stone)
            }
          }
          ticksUsed = 0
        }
      }
    }
  }

  @SideOnly(Side.CLIENT)
  override def registerIcons(iconReg: IIconRegister)
  {
    ResonantBlock.icon.put("thermopile_top", iconReg.registerIcon(Reference.prefix + "thermopile_top"))
    super.registerIcons(iconReg)
  }

  @SideOnly(Side.CLIENT)
  override def getIcon(side: Int, meta: Int): IIcon =
  {
    if (side == 1)
    {
      return ResonantBlock.icon.get("thermopile_top")
    }

    return super.getIcon(side, meta)
  }
}