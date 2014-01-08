package resonantinduction.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.World;
import resonantinduction.transport.ResonantInductionTransport;

import com.builtbroken.minecraft.DarkCore;

//TODO fix instant falling sand
public class BlockColorSand extends BlockColored
{
	public BlockColorSand()
	{
		super("colorSand", ResonantInductionTransport.CONFIGURATION.getBlock("colorSand", DarkCore.getNextID()).getInt(), Material.sand);
		this.setCreativeTab(CreativeTabs.tabDecorations);
		this.setHardness(1f);
		this.setResistance(.5f);
	}

	public static boolean fallInstantly = true;

	@Override
	public void onBlockAdded(World par1World, int par2, int par3, int par4)
	{
		par1World.scheduleBlockUpdate(par2, par3, par4, this.blockID, this.tickRate());
	}

	@Override
	public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5)
	{
		par1World.scheduleBlockUpdate(par2, par3, par4, this.blockID, this.tickRate());
	}

	@Override
	public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random)
	{
		if (!par1World.isRemote)
		{
			this.tryToFall(par1World, par2, par3, par4);
		}
	}

	private void tryToFall(World par1World, int par2, int par3, int par4)
	{
		int meta = par1World.getBlockMetadata(par2, par3, par4);
		if (canFallBelow(par1World, par2, par3 - 1, par4) && par3 >= 0)
		{
			byte var8 = 32;

			if (!fallInstantly && par1World.checkChunksExist(par2 - var8, par3 - var8, par4 - var8, par2 + var8, par3 + var8, par4 + var8))
			{
				if (!par1World.isRemote)
				{
					return;
				}
			}
			else
			{
				par1World.setBlock(par2, par3, par4, 0);

				while (canFallBelow(par1World, par2, par3 - 1, par4) && par3 > 0)
				{
					--par3;
				}

				if (par3 > 0)
				{
					par1World.setBlock(par2, par3, par4, this.blockID, meta, 3);
				}
			}
		}
	}

	public int tickRate()
	{
		return 3;
	}

	public static boolean canFallBelow(World par0World, int par1, int par2, int par3)
	{
		int var4 = par0World.getBlockId(par1, par2, par3);

		if (var4 == 0)
		{
			return true;
		}
		else if (var4 == Block.fire.blockID)
		{
			return true;
		}
		else
		{
			Material var5 = Block.blocksList[var4].blockMaterial;
			return var5 == Material.water ? true : var5 == Material.lava;
		}
	}

}
