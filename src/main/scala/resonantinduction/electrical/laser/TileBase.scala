package resonantinduction.electrical.laser

import net.minecraft.block.material.Material
import net.minecraftforge.common.util.ForgeDirection
import resonant.content.prefab.java.TileAdvanced

/**
 * @author Calclavia
 */
class TileBase extends TileAdvanced(Material.iron)
{
  def isPowered(): Boolean = getWorldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)

  def direction: ForgeDirection = ForgeDirection.getOrientation(getBlockMetadata)
}
