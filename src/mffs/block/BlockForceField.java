package mffs.block;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import mffs.ModularForceFieldSystem;
import mffs.api.IForceFieldBlock;
import mffs.api.IProjector;
import mffs.api.fortron.IFortronStorage;
import mffs.api.modules.IModule;
import mffs.api.security.IBiometricIdentifier;
import mffs.api.security.Permission;
import mffs.base.BlockBase;
import mffs.render.RenderForceField;
import mffs.tileentity.TileEntityForceField;
import micdoodle8.mods.galacticraft.API.IPartialSealedBlock;
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
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.CustomDamageSource;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockForceField extends BlockBase implements IForceFieldBlock, IPartialSealedBlock
{
	public BlockForceField(int id)
	{
		super(id, "forceField", Material.glass);
		this.setBlockUnbreakable();
		this.setResistance(999.0F);
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
	public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
	{
		int i1 = par1IBlockAccess.getBlockId(par2, par3, par4);
		return i1 == this.blockID ? false : super.shouldSideBeRendered(par1IBlockAccess, par2, par3, par4, par5);
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer entityPlayer)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileEntityForceField)
		{
			if (((TileEntityForceField) tileEntity).getProjector() != null)
			{
				for (ItemStack moduleStack : ((TileEntityForceField) tileEntity).getProjector().getModuleStacks(((TileEntityForceField) tileEntity).getProjector().getModuleSlots()))
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
			IBiometricIdentifier BiometricIdentifier = this.getProjector(world, x, y, z).getBiometricIdentifier();

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
						else if (BiometricIdentifier != null)
						{
							if (BiometricIdentifier.isAccessGranted(entityPlayer.username, Permission.FORCE_FIELD_WARP))
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

		if (tileEntity instanceof TileEntityForceField)
		{
			if (this.getProjector(world, x, y, z) != null)
			{
				for (ItemStack moduleStack : ((TileEntityForceField) tileEntity).getProjector().getModuleStacks(((TileEntityForceField) tileEntity).getProjector().getModuleSlots()))
				{
					if (((IModule) moduleStack.getItem()).onCollideWithForceField(world, x, y, z, entity, moduleStack))
					{
						return;
					}
				}

				IBiometricIdentifier biometricIdentifier = this.getProjector(world, x, y, z).getBiometricIdentifier();

				if (new Vector3(entity).distanceTo(new Vector3(x, y, z).add(0.4)) < 0.5)
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
							((EntityLiving) entity).setEntityHealth(1);
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

		if (tileEntity instanceof TileEntityForceField)
		{
			ItemStack checkStack = ((TileEntityForceField) tileEntity).camoStack;

			if (checkStack != null)
			{
				try
				{
					Block block = Block.blocksList[((ItemBlock) checkStack.getItem()).getBlockID()];

					final Integer[] allowedRenderTypes = { 0, 1, 4, 31, 20, 39, 5, 13, 23, 6, 8, 7, 12, 29, 30, 14, 16, 17 };

					if (Arrays.asList(allowedRenderTypes).contains(block.getRenderType()))
					{
						Icon icon = block.getIcon(side, checkStack.getItemDamage());

						if (icon != null)
						{
							return icon;
						}
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

			if (tileEntity instanceof TileEntityForceField)
			{
				ItemStack checkStack = ((TileEntityForceField) tileEntity).camoStack;

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

			if (tileEntity instanceof TileEntityForceField)
			{
				IProjector zhuYao = ((TileEntityForceField) tileEntity).getProjectorSafe();

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
		return new TileEntityForceField();
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

		if (tileEntity instanceof TileEntityForceField)
		{
			return ((TileEntityForceField) tileEntity).getProjector();
		}

		return null;
	}

	@Override
	public boolean isSealed(World world, int x, int y, int z)
	{
		return true;
	}
}