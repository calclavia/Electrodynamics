package resonantinduction.electrical.encoder;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import resonant.lib.prefab.block.BlockTile;
import resonantinduction.core.Reference;
import resonantinduction.electrical.Electrical;
import universalelectricity.api.UniversalElectricity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockEncoder extends BlockTile
{
	IIcon encoder_side;
	IIcon encoder_top;
	IIcon encoder_bottom;

	public BlockEncoder(int id)
	{
		super(id, UniversalElectricity.machine);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconReg)
	{
		this.encoder_side = iconReg.registerIcon(Reference.PREFIX + "encoder_side");
		this.encoder_top = iconReg.registerIcon(Reference.PREFIX + "encoder_top");
		this.encoder_bottom = iconReg.registerIcon(Reference.PREFIX + "encoder_bottom");
	}

	/** Returns the block texture based on the side being looked at. Args: side */
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
	{
		return getIcon(side, 0);
	}

	/** Returns the block texture based on the side being looked at. Args: side */
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		if (side == 1)
		{
			return this.encoder_top;

		}
		else if (side == 0)
		{
			return this.encoder_bottom;

		}

		return this.encoder_side;
	}

	/** Called upon block activation (right click on the block.) */
	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
	{
		if (!world.isRemote)
		{
			entityPlayer.openGui(Electrical.INSTANCE, 0, world, x, y, z);
		}

		return true;

	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEncoder();
	}
}
