package resonantinduction.electrical

import calclavia.lib.content.prefab.{TraitElectrical, TraitInventory}
import net.minecraft.block.material.Material
import calclavia.lib.content.module.TileBase

/**
 * @since 22/03/14
 * @author tgame14
 */
class TileDistributor extends TileBase(Material.rock) with TraitInventory with TraitElectrical
{

}
