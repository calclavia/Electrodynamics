package mffs.base;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.api.ICamouflageMaterial;
import mffs.MFFSCreativeTab;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.card.ICardLink;
import mffs.render.RenderBlockHandler;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.UniversalElectricity;
import universalelectricity.prefab.block.BlockRotatable;
import universalelectricity.prefab.implement.IRedstoneReceptor;

public abstract class BlockMachine extends BlockRotatable implements ICamouflageMaterial
{
	public BlockMachine(int id, String name)
	{
		super(Settings.CONFIGURATION.getBlock(name, id).getInt(id), UniversalElectricity.machine);
		this.setUnlocalizedName(ModularForceFieldSystem.PREFIX + name);
		this.setBlockUnbreakable();
		this.setResistance(100.0F);
		this.setStepSound(soundMetalFootstep);
		this.setCreativeTab(MFFSCreativeTab.INSTANCE);
	}

	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			if (entityPlayer.getCurrentEquippedItem() != null)
			{
				if (entityPlayer.getCurrentEquippedItem().getItem() instanceof ICardLink)
				{
					return false;
				}
			}

			if (!world.isRemote)
			{
				entityPlayer.openGui(ModularForceFieldSystem.instance, 0, world, x, y, z);
			}
		}

		return true;
	}

	@Override
	public boolean onSneakMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		return this.onUseWrench(world, x, y, z, entityPlayer, side, hitX, hitY, hitZ);
	}

	@Override
	public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		this.setDirection(world, x, y, z, ForgeDirection.getOrientation(ForgeDirection.ROTATION_MATRIX[2][this.getDirection(world, x, y, z).ordinal()]));
		return true;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
	{
		if (!world.isRemote)
		{
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

			if (tileEntity instanceof IRedstoneReceptor)
			{
				if (world.isBlockIndirectlyGettingPowered(x, y, z))
				{
					((IRedstoneReceptor) tileEntity).onPowerOn();
				}
				else
				{
					((IRedstoneReceptor) tileEntity).onPowerOff();
				}
			}
		}
	}

	@Override
	public float getExplosionResistance(Entity entity, World world, int i, int j, int k, double d, double d1, double d2)
	{
		return 100.0F;
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon(ModularForceFieldSystem.PREFIX + "machine");
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

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return RenderBlockHandler.ID;
	}

}