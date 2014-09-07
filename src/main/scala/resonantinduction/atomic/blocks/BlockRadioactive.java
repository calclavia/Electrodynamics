package resonantinduction.atomic.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntitySmokeFX;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import resonant.lib.prefab.poison.PoisonRadiation;
import universalelectricity.core.transform.vector.Vector3;

import java.util.List;
import java.util.Random;

public class BlockRadioactive extends Block
{
	public boolean canSpread = true;
	public float radius = 5;
	public int amplifier = 2;
	public boolean canWalkPoison = true;
	public boolean isRandomlyRadioactive = true;
	public boolean spawnParticle = true;

	private IIcon iconTop;
	private IIcon iconBottom;

	public BlockRadioactive(Material material)
	{
		super(material);
		this.setTickRandomly(true);
		this.setHardness(0.2F);
	}

	@Override
	public IIcon getIcon(int side, int metadata)
	{
		return side == 1 ? this.iconTop : (side == 0 ? this.iconBottom : this.blockIcon);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		super.registerBlockIcons(iconRegister);
		this.iconTop = iconRegister.registerIcon(this.getUnlocalizedName().replace("tile.", "") + "_top");
		this.iconBottom = iconRegister.registerIcon(this.getUnlocalizedName().replace("tile.", "") + "_bottom");
	}

	/**
	 * Ticks the block if it's been scheduled
	 */
	@Override
	public void updateTick(World world, int x, int y, int z, Random rand)
	{
		if (!world.isRemote)
		{
			if (this.isRandomlyRadioactive)
			{
				AxisAlignedBB bounds = AxisAlignedBB.getBoundingBox(x - this.radius, y - this.radius, z - this.radius, x + this.radius, y + this.radius, z + this.radius);
				List<EntityLivingBase> entitiesNearby = world.getEntitiesWithinAABB(EntityLivingBase.class, bounds);

				for (EntityLivingBase entity : entitiesNearby)
				{
					PoisonRadiation.INSTANCE.poisonEntity(new Vector3(x, y, z), entity, amplifier);
				}
			}

			if (this.canSpread)
			{
				for (int i = 0; i < 4; ++i)
				{
					int newX = x + rand.nextInt(3) - 1;
					int newY = y + rand.nextInt(5) - 3;
					int newZ = z + rand.nextInt(3) - 1;
					Block block = world.getBlock(newX, newY, newZ);

					if (rand.nextFloat() > 0.4 && (block == Blocks.farmland || block == Blocks.grass))
					{
						world.setBlock(newX, newY, newZ, this);
					}
				}

				if (rand.nextFloat() > 0.85)
				{
					world.setBlock(x, y, z, Blocks.dirt);
				}
			}
		}
	}

	/**
	 * Called whenever an entity is walking on top of this block. Args: world, x, y, z, entity
	 */
	@Override
	public void onEntityWalking(World par1World, int x, int y, int z, Entity par5Entity)
	{
		if (par5Entity instanceof EntityLiving && this.canWalkPoison)
		{
			PoisonRadiation.INSTANCE.poisonEntity(new Vector3(x, y, z), (EntityLiving) par5Entity);
		}
	}

	@Override
	public int quantityDropped(Random par1Random)
	{
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random par5Random)
	{
		if (this.spawnParticle)
		{
			if (Minecraft.getMinecraft().gameSettings.particleSetting == 0)
			{
				int radius = 3;

				for (int i = 0; i < 2; i++)
				{
					Vector3 pos = new Vector3(x, y, z);
					pos.add(Math.random() * radius - radius / 2, Math.random() * radius - radius / 2, Math.random() * radius - radius / 2);
					EntitySmokeFX fx = new EntitySmokeFX(world, pos.x(), pos.y(), pos.z(), (Math.random() - 0.5) / 2, (Math.random() - 0.5) / 2, (Math.random() - 0.5) / 2);
					fx.setRBGColorF(0.2f, 0.8f, 0);
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);
				}
			}
		}
	}
}