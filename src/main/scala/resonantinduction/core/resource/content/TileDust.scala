package resonantinduction.core.resource.content

import net.minecraft.block.material.Material
import net.minecraft.nbt.NBTTagCompound
import resonant.lib.prefab.tile.spatial.SpatialTile
import resonant.lib.transform.region.Cuboid
import resonantinduction.core.Reference

/**
 * A block for physical dust blocks
 *
 * Metadata is used as a value from 0 to 7, indicating the amount of dust that is within this block.
 *
 * @author Calclavia
 */
class TileDust extends SpatialTile(Material.sand)
{
  var resMaterial = ""
  textureName = Reference.prefix + ""
  isOpaqueCube = false
  creativeTab = null

  override def bounds: Cuboid = new Cuboid(0, 0, 0, 1, (metadata + 1) / 8f, 1)

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    resMaterial = nbt.getString("material")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setString("material", resMaterial)
  }
}
