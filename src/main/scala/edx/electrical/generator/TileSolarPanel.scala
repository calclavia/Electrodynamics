package edx.electrical.generator

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.util.IIcon
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.content.prefab.TIO
import resonant.lib.grid.core.TBlockNodeProvider
import resonant.lib.grid.energy.electric.NodeElectricComponent
import resonant.lib.prefab.tile.spatial.{SpatialBlock, SpatialTile}
import resonant.lib.render.block.RenderConnectedTexture
import resonant.lib.transform.region.Cuboid

import scala.collection.convert.wrapAll._

class TileSolarPanel extends SpatialTile(Material.iron) with TBlockNodeProvider with TIO with RenderConnectedTexture
{
  private val electricNode = new NodeElectricComponent(this)

  ioMap = 728
  textureName = "solarPanel_top"
  bounds = new Cuboid(0, 0, 0, 1, 0.3f, 1)

  isOpaqueCube = false
  normalRender = false
  renderStaticBlock = true

  edgeTexture = Reference.prefix + "tankEdge"
  electricNode.dynamicTerminals = true
  electricNode.setPositives(Set(ForgeDirection.NORTH, ForgeDirection.EAST))
  electricNode.setNegatives(Set(ForgeDirection.SOUTH, ForgeDirection.WEST))
  nodes.add(electricNode)

  @SideOnly(Side.CLIENT)
  override def registerIcons(iconReg: IIconRegister)
  {
    SpatialBlock.icon.put("solarPanel_side", iconReg.registerIcon(Reference.prefix + "solarPanel_side"))
    SpatialBlock.icon.put("solarPanel_bottom", iconReg.registerIcon(Reference.prefix + "solarPanel_bottom"))
    super.registerIcons(iconReg)
  }

  @SideOnly(Side.CLIENT)
  override def getIcon(side: Int, meta: Int): IIcon =
  {
    if (side == 0)
      return SpatialBlock.icon.get("solarPanel_bottom")
    else if (side == 1)
      return getIcon

    return SpatialBlock.icon.get("solarPanel_side")
  }

  override def update()
  {
    super.update()

    if (!world.isRemote)
    {
      if (world.canBlockSeeTheSky(xCoord, yCoord + 1, zCoord) && !this.worldObj.provider.hasNoSky)
      {
        if (world.isDaytime)
        {
          if (!(world.isThundering || world.isRaining))
          {
            electricNode.generateVoltage(15)
          }
        }
      }
    }
  }
}