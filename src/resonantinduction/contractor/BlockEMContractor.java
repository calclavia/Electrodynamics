package resonantinduction.contractor;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
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
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemstack)
    {
    	TileEntityEMContractor tileEntity = (TileEntityEMContractor)world.getBlockTileEntity(x, y, z);
        int side = MathHelper.floor_double((double)(entityliving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int height = Math.round(entityliving.rotationPitch);
        
        int change = 1;
        
        if(height >= 65)
        {
        	change = 1;
        }
        else if(height <= -65)
        {
        	change = 0;
        }
        else {
	        switch(side)
	        {
	        	case 0: change = 2; break;
	        	case 1: change = 5; break;
	        	case 2: change = 3; break;
	        	case 3: change = 4; break;
	        }
        }
        
        tileEntity.setFacing(ForgeDirection.getOrientation(change));
    }
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		TileEntityEMContractor tileContractor = (TileEntityEMContractor)world.getBlockTileEntity(x, y, z);
		tileContractor.updateBounds();
		
		if(!tileContractor.isLatched())
		{
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tileEntity = world.getBlockTileEntity(x+side.offsetX, y+side.offsetY, z+side.offsetZ);
				
				if(tileEntity instanceof IInventory)
				{
					tileContractor.setFacing(side.getOpposite());
					return;
				}
			}
		}
	}
	
	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
    {
		TileEntityEMContractor contractor = (TileEntityEMContractor)par1World.getBlockTileEntity(par2, par3, par4);
		
		if(!par5EntityPlayer.isSneaking())
		{
			contractor.incrementFacing();
		}
		else {
			contractor.suck = !contractor.suck;
		}
		
		return true;
    }
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
	{
		TileEntityEMContractor tileContractor = (TileEntityEMContractor)world.getBlockTileEntity(x, y, z);
		
		if(!tileContractor.isLatched())
		{
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tileEntity = world.getBlockTileEntity(x+side.offsetX, y+side.offsetY, z+side.offsetZ);
				
				if(tileEntity instanceof IInventory)
				{
					tileContractor.setFacing(side.getOpposite());
					return;
				}
			}
		}
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
