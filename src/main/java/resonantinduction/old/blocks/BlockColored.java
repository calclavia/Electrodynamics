package resonantinduction.old.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import resonantinduction.core.Reference;
import resonantinduction.old.lib.helpers.ColorCode;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Prefab class to make any block have 16 separate color instances similar to wool block
 * 
 * @author DarkGuardsman
 */
public class BlockColored extends Block
{
	@SideOnly(Side.CLIENT)
	private Icon[] icons;

	/** Use a single icon to create all 16 colors */
	boolean colorized = true;

	public BlockColored(String name, int id, Material par2Material)
	{
		super(id, par2Material);
		this.setUnlocalizedName(name);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Icon getIcon(int side, int meta)
	{
		if (colorized)
		{
			return this.blockIcon;
		}
		return this.icons[~meta & 15];
	}

	@Override
	public int damageDropped(int meta)
	{
		return meta;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void getSubBlocks(int par1, CreativeTabs tab, List contentList)
	{
		for (int j = 0; j < 16; ++j)
		{
			contentList.add(new ItemStack(par1, 1, j));
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconReg)
	{
		if (colorized)
		{
			this.blockIcon = iconReg.registerIcon(Reference.PREFIX + this.getUnlocalizedName().replace("tile.", ""));
		}
		else
		{
			this.icons = new Icon[16];

			for (int i = 0; i < this.icons.length; ++i)
			{
				this.icons[i] = iconReg.registerIcon(Reference.PREFIX + ColorCode.get(~i & 15).name + this.getUnlocalizedName().replace("tile.", ""));
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z)
	{
		return this.getRenderColor(world.getBlockMetadata(x, y, z));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderColor(int meta)
	{
		if (this.colorized)
		{
			return ColorCode.get(~meta & 15).color.getRGB();
		}
		return super.getRenderColor(meta);
	}

}
