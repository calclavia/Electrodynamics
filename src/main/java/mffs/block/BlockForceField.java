package mffs.block;

import calclavia.api.mffs.IForceFieldBlock;
import calclavia.api.mffs.IProjector;
import calclavia.api.mffs.fortron.IFortronStorage;
import calclavia.api.mffs.modules.IModule;
import calclavia.api.mffs.security.IBiometricIdentifier;
import calclavia.api.mffs.security.Permission;
import calclavia.lib.prefab.CustomDamageSource;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.ModularForceFieldSystem;
import mffs.base.BlockBase;
import mffs.render.RenderForceField;
import mffs.tile.TileForceField;
import micdoodle8.mods.galacticraft.api.block.IPartialSealableBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;

import java.util.List;
import java.util.Random;

public class BlockForceField extends BlockBase implements IForceFieldBlock, IPartialSealableBlock
{
	public BlockForceField(int id)
	{
		super(id, "forceField", Material.glass);
		this.setBlockUnbreakable();
		this.setResistance(Integer.MAX_VALUE);
		this.setCreativeTab(null);
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	protected boolean canSilkHarvest()
	{
		return false;
	}

	@Override
	public int quantityDropped(Random random)
	{
		return 0;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderBlockPass()
	{
		return 1;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return RenderForceField.ID;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int par5)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileForceField)
		{
			if (((TileForceField) tileEntity).camoStack != null)
			{
				try
				{
					Block block = Block.blocksList[((ItemBlock) ((TileForceField) tileEntity).camoStack.getItem()).getBlockID()];
					return block.shouldSideBeRendered(world, x, y, z, par5);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				return true;
			}
		}

		int i1 = world.getBlockId(x, y, z);
		return i1 == this.blockID ? false : super.shouldSideBeRendered(world, x, y, z, par5);

	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer entityPlayer)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileForceField)
		{
			if (((TileForceField) tileEntity).getProjector() != null)
			{
				for (ItemStack moduleStack : ((TileForceField) tileEntity).getProjector().getModuleStacks(((TileForceField) tileEntity).getProjector().getModuleSlots()))
				{
					if (((IModule) moduleStack.getItem()).onCollideWithForceField(world, x, y, z, entityPlayer, moduleStack))
					{
						return;
					}
				}
			}
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		if (this.getProjector(world, x, y, z) != null)
		{
			IBiometricIdentifier biometricIdentifier = this.getProjector(world, x, y, z).getBiometricIdentifier();

			List<EntityPlayer> entities = world.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 0.9, z + 1));

			for (EntityPlayer entityPlayer : entities)
			{
				if (entityPlayer != null)
				{
					if (entityPlayer.isSneaking())
					{
						if (entityPlayer.capabilities.isCreativeMode)
						{
							return null;
						}
						else if (biometricIdentifier != null)
						{
							if (biometricIdentifier.isAccessGranted(entityPlayer.username, Permission.FORCE_FIELD_WARP))
							{
								return null;
							}
						}
					}
				}
			}
		}

		float f = 0.0625F;
		return AxisAlignedBB.getBoundingBox(x + f, y + f, z + f, x + 1 - f, y + 1 - f, z + 1 - f);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileForceField)
		{
			if (this.getProjector(world, x, y, z) != null)
			{
				for (ItemStack moduleStack : ((TileForceField) tileEntity).getProjector().getModuleStacks(((TileForceField) tileEntity).getProjector().getModuleSlots()))
				{
					if (((IModule) moduleStack.getItem()).onCollideWithForceField(world, x, y, z, entity, moduleStack))
					{
						return;
					}
				}

				IBiometricIdentifier biometricIdentifier = this.getProjector(world, x, y, z).getBiometricIdentifier();

				if (new Vector3(entity).distance(new Vector3(x, y, z).add(0.4)) < 0.5)
				{
					if (entity instanceof EntityLiving && !world.isRemote)
					{
						((EntityLiving) entity).addPotionEffect(new PotionEffect(Potion.confusion.id, 4 * 20, 3));
						((EntityLiving) entity).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 20, 1));

						boolean hasPermission = false;

						List<EntityPlayer> entities = world.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 0.9, z + 1));

						for (EntityPlayer entityPlayer : entities)
						{
							if (entityPlayer != null)
							{
								if (entityPlayer.isSneaking())
								{
									if (entityPlayer.capabilities.isCreativeMode)
									{
										hasPermission = true;
										break;

									}
									else if (biometricIdentifier != null)
									{
										if (biometricIdentifier.isAccessGranted(entityPlayer.username, Permission.FORCE_FIELD_WARP))
										{
											hasPermission = true;
										}
									}
								}
							}
						}

						if (!hasPermission)
						{
							entity.attackEntityFrom(CustomDamageSource.electrocution, 100);
						}
					}
				}
			}
		}

	}

	@Override
	public Icon getBlockTexture(IBlockAccess iBlockAccess, int x, int y, int z, int side)
	{
		TileEntity tileEntity = iBlockAccess.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileForceField)
		{
			ItemStack checkStack = ((TileForceField) tileEntity).camoStack;

			if (checkStack != null)
			{
				try
				{
					Block block = Block.blocksList[((ItemBlock) checkStack.getItem()).getBlockID()];

					Icon icon = block.getIcon(side, checkStack.getItemDamage());

					if (icon != null)
					{
						return icon;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		return this.getIcon(side, iBlockAccess.getBlockMetadata(x, y, z));
	}

	/**
	 * Returns a integer with hex for 0xrrggbb with this color multiplied against the blocks color.
	 * Note only called when first determining what to render.
	 */
	@Override
	public int colorMultiplier(IBlockAccess iBlockAccess, int x, int y, int z)
	{
		try
		{
			TileEntity tileEntity = iBlockAccess.getBlockTileEntity(x, y, z);

			if (tileEntity instanceof TileForceField)
			{
				ItemStack checkStack = ((TileForceField) tileEntity).camoStack;

				if (checkStack != null)
				{
					try
					{
						return Block.blocksList[((ItemBlock) checkStack.getItem()).getBlockID()].colorMultiplier(iBlockAccess, x, y, x);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return super.colorMultiplier(iBlockAccess, x, y, z);
	}

	@Override
	public int getLightValue(IBlockAccess iBlockAccess, int x, int y, int z)
	{
		try
		{
			TileEntity tileEntity = iBlockAccess.getBlockTileEntity(x, y, z);

			if (tileEntity instanceof TileForceField)
			{
				IProjector zhuYao = ((TileForceField) tileEntity).getProjectorSafe();

				if (zhuYao instanceof IProjector)
				{
					return (int) (((float) Math.min(zhuYao.getModuleCount(ModularForceFieldSystem.itemModuleGlow), 64) / 64) * 15f);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return 0;
	}

	@Override
	public float getExplosionResistance(Entity entity, World world, int x, int y, int z, double d, double d1, double d2)
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileForceField();
	}

	@Override
	public void weakenForceField(World world, int x, int y, int z, int joules)
	{
		IProjector projector = this.getProjector(world, x, y, z);

		if (projector != null)
		{
			((IFortronStorage) projector).provideFortron(joules, true);
		}

		if (!world.isRemote)
		{
			world.setBlockToAir(x, y, z);
		}
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		return null;
	}

	@Override
	public IProjector getProjector(IBlockAccess iBlockAccess, int x, int y, int z)
	{
		TileEntity tileEntity = iBlockAccess.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileForceField)
		{
			return ((TileForceField) tileEntity).getProjector();
		}

		return null;
	}

	@Override
	public boolean isSealed(World world, int x, int y, int z, ForgeDirection direction)
	{
		return true;
	}
}