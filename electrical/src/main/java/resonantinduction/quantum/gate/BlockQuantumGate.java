package resonantinduction.quantum.gate;

import java.util.Random;

import resonantinduction.core.ResonantInduction;
import resonantinduction.electrical.Electrical;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import calclavia.lib.prefab.block.BlockTile;
import calclavia.lib.utility.LanguageUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockQuantumGate extends BlockTile
{
	public Icon iconTop, iconSide, iconBot;

	public BlockQuantumGate(int id)
	{
		super(id, Material.iron);
		this.setHardness(32F);
		this.setResistance(1000F);
	}

	/** A randomly called display update to be able to add particles or other items for display */
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random par5Random)
	{
		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (tile instanceof TileQuantumGate)
		{
			int frequency = ((TileQuantumGate) tile).getFrequency();

			if (frequency != -1)
			{
				/**
				 * Spawn particles all around the pillar
				 */

				for (int height = 0; height < 4; height++)
				{
					for (int i = 2; i < 6; i++)
					{
						ForgeDirection dir = ForgeDirection.getOrientation(i);
						double spawnX = x + 0.5f + dir.offsetX;
						double spawnY = y + 0.255f + par5Random.nextFloat() * 0.25f - height;
						double spawnZ = z + 0.5f + dir.offsetZ;
						double xRand = par5Random.nextFloat() * 0.6F - 0.3F;
						double zRand = par5Random.nextFloat() * 0.6F - 0.3F;

						world.spawnParticle("enchantmenttable", spawnX + xRand, spawnY, spawnZ + zRand, Math.random() * 0.5, 0.1, Math.random() * 0.5);
						world.spawnParticle("enchantmenttable", spawnX - xRand, spawnY, spawnZ + zRand, Math.random() * 0.5, 0.1, Math.random() * 0.5);
						world.spawnParticle("enchantmenttable", spawnX + xRand, spawnY, spawnZ - zRand, Math.random() * 0.5, 0.1, Math.random() * 0.5);
						world.spawnParticle("enchantmenttable", spawnX - xRand, spawnY, spawnZ - zRand, Math.random() * 0.5, 0.1, Math.random() * 0.5);

						if (((TileQuantumGate) tile).canFunction())
						{
							world.spawnParticle("portal", spawnX + xRand, spawnY, spawnZ + zRand, Math.random() * 0.5, 0.1, Math.random() * 0.5);
							world.spawnParticle("portal", spawnX - xRand, spawnY, spawnZ + zRand, Math.random() * 0.5, 0.1, Math.random() * 0.5);
							world.spawnParticle("portal", spawnX + xRand, spawnY, spawnZ - zRand, Math.random() * 0.5, 0.1, Math.random() * 0.5);
							world.spawnParticle("portal", spawnX - xRand, spawnY, spawnZ - zRand, Math.random() * 0.5, 0.1, Math.random() * 0.5);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int i, float f1, float f2, float f3)
	{
		if (player != null && player.getHeldItem() == null || player.getHeldItem().itemID != (Electrical.blockGlyph.blockID))
		{
			TileEntity tile = world.getBlockTileEntity(x, y, z);

			if (tile instanceof TileQuantumGate)
			{
				int frequency = ((TileQuantumGate) tile).getFrequency();

				if (frequency == -1)
				{
					if (!world.isRemote)
						player.addChatMessage("Quantum Gate not set up.");
				}
				else
				{
					if (!world.isRemote)
					{
						player.addChatMessage("Quantum Gate frequency: " + " " + frequency);
					}
				}
			}
			return true;
		}

		return false;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity par5Entity)
	{

	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileQuantumGate();
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

}
