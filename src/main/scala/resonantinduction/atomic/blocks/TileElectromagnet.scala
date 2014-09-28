package resonantinduction.atomic.blocks

import java.util.List

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.IIcon
import net.minecraft.world.IBlockAccess
import resonant.api.IElectromagnet
import resonant.content.prefab.itemblock.ItemBlockMetadata
import resonant.content.spatial.block.SpatialBlock
import resonant.lib.wrapper.WrapList._

/**
 * Electromagnet block
 */
object TileElectromagnet
{
    private var iconTop: IIcon = null
    private var iconGlass: IIcon = null
}

class TileElectromagnet extends SpatialBlock(Material.iron) with IElectromagnet
{
    //Constructor
    blockResistance(20)
    forceStandardRender(true)
    normalRender(false)
    isOpaqueCube(false)
    this.itemBlock(classOf[ItemBlockMetadata])

    override def getIcon(side: Int, metadata: Int): IIcon =
    {
        if (metadata == 1)
        {
            return TileElectromagnet.iconGlass
        }
        if (side == 0 || side == 1)
        {
            return TileElectromagnet.iconTop
        }
        return super.getIcon(side, metadata)
    }

    @SideOnly(Side.CLIENT) override def registerIcons(iconRegister: IIconRegister)
    {
        super.registerIcons(iconRegister)
        TileElectromagnet.iconTop = iconRegister.registerIcon(domain + textureName + "_top")
        TileElectromagnet.iconGlass = iconRegister.registerIcon(domain + "electromagnetGlass")
    }

    override def metadataDropped(meta: Int, fortune: Int): Int =
    {
        return meta
    }

    override def canUpdate: Boolean =
    {
        return false
    }

    override def shouldSideBeRendered(access: IBlockAccess, x: Int, y: Int, z: Int, side: Int): Boolean =
    {
        return true
    }

    override def getRenderBlockPass: Int =
    {
        return 0
    }

    override def getSubBlocks(item: Item, par2CreativeTabs: CreativeTabs, par3List: List[_])
    {
        super.getSubBlocks(item, par2CreativeTabs, par3List)
        par3List.add(new ItemStack(item, 1, 1))
    }

    def isRunning: Boolean =
    {
        return true
    }
}