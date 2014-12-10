package resonantinduction.electrical.generator

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.util.IIcon
import resonant.lib.prefab.tile.TileElectric
import resonant.lib.prefab.tile.spatial.SpatialBlock
import resonant.lib.render.block.RenderConnectedTexture
import resonant.lib.transform.region.Cuboid
import resonantinduction.core.{Reference, Settings}

class TileSolarPanel extends TileElectric(Material.iron) with RenderConnectedTexture
{
  ioMap = 728
  textureName = "solarPanel_top"
  bounds = new Cuboid(0, 0, 0, 1, 0.3f, 1)
  isOpaqueCube = false

  edgeTexture = Reference.prefix + "tankEdge"

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

  override def update
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
            println(dcNode)
            dcNode.buffer(Settings.solarPower / 20)
          }
        }
      }
    }
  }
}