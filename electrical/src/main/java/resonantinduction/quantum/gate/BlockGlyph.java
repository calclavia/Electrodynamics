package resonantinduction.quantum.gate;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import resonantinduction.core.Reference;
import calclavia.lib.prefab.block.BlockAdvanced;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockGlyph extends BlockAdvanced
{
	public static final int MAX_GLYPH = 4;
	public static final Icon[] icons = new Icon[MAX_GLYPH];

	public BlockGlyph(int id)
	{
		super(id, Material.iron);
		setHardness(32F);
		setResistance(1000F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		return icons[meta];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		for (int i = 0; i < icons.length; i++)
		{
			icons[i] = register.registerIcon(Reference.PREFIX + "glyph_" + i);
		}

		this.blockIcon = icons[0];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for (int i = 0; i < icons.length; i++)
		{
			par3List.add(new ItemStack(par1, 1, i));
		}
	}
}
