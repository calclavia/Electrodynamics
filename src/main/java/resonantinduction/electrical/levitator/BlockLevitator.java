package resonantinduction.electrical.levitator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.block.BlockRIRotatable;
import resonantinduction.core.render.RIBlockRenderingHandler;
import universalelectricity.api.vector.VectorWorld;
import calclavia.components.tool.ToolModeLink;
import calclavia.lib.prefab.block.ILinkable;
import calclavia.lib.utility.WrenchUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockLevitator extends BlockRIRotatable
{
	public BlockLevitator()
	{
		super("levitator");
		this.setTextureName(Reference.PREFIX + "machine");
	}

	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
	{
		TileLevitator levitator = (TileLevitator) world.getBlockTileEntity(x, y, z);

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
		ItemStack itemStack = entityPlayer.getCurrentEquippedItem();

		if (WrenchUtility.isWrench(itemStack))
		{
			TileEntity tile = world.getBlockTileEntity(x, y, z);

			if (tile instanceof ILinkable)
			{
				ILinkable linkable = (ILinkable) tile;

				if (linkable.onLink(entityPlayer, ToolModeLink.getLink(itemStack)))
				{
					ToolModeLink.clearLink(itemStack);
				}
				else
				{
					ToolModeLink.setLink(itemStack, new VectorWorld(world, x, y, z));
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
	{
		TileLevitator tileContractor = (TileLevitator) world.getBlockTileEntity(x, y, z);

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

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return RIBlockRenderingHandler.INSTANCE.getRenderId();
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileLevitator();
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
