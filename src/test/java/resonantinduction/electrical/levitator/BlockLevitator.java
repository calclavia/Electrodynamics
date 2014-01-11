package resonantinduction.electrical.levitator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.block.BlockMachine;
import resonantinduction.electrical.render.ElectricalBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockLevitator extends BlockMachine
{
	public BlockLevitator()
	{
		super("levitator");
		this.setTextureName(Reference.PREFIX + "machine");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return ElectricalBlockRenderingHandler.INSTANCE.getRenderId();
	}

	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
	{
		TileEMLevitator levitator = (TileEMLevitator) world.getBlockTileEntity(x, y, z);

		if (entityPlayer.getCurrentEquippedItem() != null)
		{
			if (entityPlayer.getCurrentEquippedItem().itemID == Item.dyePowder.itemID)
			{
				levitator.setDye(entityPlayer.getCurrentEquippedItem().getItemDamage());

				if (!entityPlayer.capabilities.isCreativeMode)
				{
					entityPlayer.inventory.decrStackSize(entityPlayer.inventory.currentItem, 1);
				}

				return true;
			}
		}

		levitator.suck = !levitator.suck;
		levitator.updatePath();

		return false;
	}

	@Override
	public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		if (!entityPlayer.isSneaking())
		{
			TileEMLevitator levitator = (TileEMLevitator) world.getBlockTileEntity(x, y, z);
			levitator.incrementFacing();
			return true;
		}

		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
	{
		TileEMLevitator tileContractor = (TileEMLevitator) world.getBlockTileEntity(x, y, z);

		if (!world.isRemote && !tileContractor.isLatched())
		{
			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tileEntity = world.getBlockTileEntity(x + side.offsetX, y + side.offsetY, z + side.offsetZ);

				if (tileEntity instanceof IInventory)
				{
					tileContractor.setDirection(side.getOpposite());
					return;
				}
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEMLevitator();
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
