package resonantinduction.archaic.engineering;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import resonantinduction.core.prefab.block.BlockRI;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Advanced tiering of a crafting table adding advanced features such as visual crafting, and auto
 * crafting. Original idea by Infinite
 * 
 * @author DarkGuardsman
 */
public class BlockEngineeringTable extends BlockRI
{
	@SideOnly(Side.CLIENT)
	private Icon workbenchIconTop;
	@SideOnly(Side.CLIENT)
	private Icon workbenchIconFront;

	public BlockEngineeringTable()
	{
		super("engineeringTable");
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int par1, int par2)
	{
		return par1 == 1 ? this.workbenchIconTop : (par1 == 0 ? Block.planks.getBlockTextureFromSide(par1) : (par1 != 2 && par1 != 4 ? this.blockIcon : this.workbenchIconFront));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon(this.getTextureName() + "_side");
		this.workbenchIconTop = par1IconRegister.registerIcon(this.getTextureName() + "_top");
		this.workbenchIconFront = par1IconRegister.registerIcon(this.getTextureName() + "_front");
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEngineeringTable();
	}
}
