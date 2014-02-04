package resonantinduction.core.part;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import resonantinduction.core.Reference;
import calclavia.lib.prefab.block.BlockTile;

/**
 * A block used to build machines or decoration.
 * 
 * @author Calclavia
 * 
 */
public class BlockMachineMaterial extends BlockTile
{
	String[] iconNames = new String[] { "material_stone_brick", "material_stone_brick2", "material_stone_chiseled", "material_stone_cobble", "material_stone_cracked", "material_stone", "material_stone_slab", "material_stone_mossy", "material_steel_dark", "material_steel_tint", "material_steel" };
	Icon[] icons = new Icon[iconNames.length];

	public BlockMachineMaterial(int id)
	{
		super(id, Material.iron);
	}

	@Override
	public int damageDropped(int par1)
	{
		return par1;
	}

	@Override
	public Icon getIcon(int side, int metadata)
	{
		return icons[metadata];
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon(this.getTextureName());

		for (int i = 0; i < iconNames.length; i++)
			icons[i] = par1IconRegister.registerIcon(Reference.PREFIX + iconNames[i]);
	}

	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for (int i = 0; i < iconNames.length; i++)
			par3List.add(new ItemStack(par1, 1, i));
	}
}
