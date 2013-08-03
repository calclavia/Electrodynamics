package resonantinduction.contractor;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.ResonantInduction;
import resonantinduction.base.BlockBase;
import resonantinduction.render.BlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockEMContractor extends BlockBase implements ITileEntityProvider
{
	public BlockEMContractor(int id) 
	{
		super("contractor", id, Material.iron);
		this.func_111022_d(ResonantInduction.PREFIX + "machine");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return BlockRenderingHandler.INSTANCE.getRenderId();
	}
	
	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
    {
		if(par1World.isRemote)
		{
			TileEntityEMContractor contractor = (TileEntityEMContractor)par1World.getBlockTileEntity(par2, par3, par4);
			contractor.incrementFacing();
			
	        return true;
		}
		
		return true;
    }

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityEMContractor();
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
}
