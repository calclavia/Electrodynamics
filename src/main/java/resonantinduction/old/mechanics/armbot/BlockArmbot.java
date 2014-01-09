package resonantinduction.old.mechanics.armbot;

import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import resonantinduction.core.Settings;
import resonantinduction.old.client.render.BlockRenderingHandler;
import resonantinduction.old.client.render.RenderArmbot;
import universalelectricity.api.UniversalElectricity;
import calclavia.lib.multiblock.link.IMultiBlock;
import calclavia.lib.prefab.block.BlockMachine;

import com.builtbroken.common.Pair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockArmbot extends BlockMachine
{
	public BlockArmbot()
	{
		super(Settings.CONFIGURATION,"armbot", UniversalElectricity.machine);
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z)
	{
		return world.getBlockMaterial(x, y - 1, z).isSolid();
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof IMultiBlock)
		{
			DarkCore.multiBlock.createMultiBlockStructure((IMultiBlock) tileEntity);
		}
	}

	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof IBlockActivated)
		{
			return ((IBlockActivated) tileEntity).onActivated(player);
		}

		return false;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileEntityArmbot)
		{
			((TileEntityArmbot) tileEntity).dropHeldObject();
			DarkCore.multiBlock.destroyMultiBlockStructure((TileEntityArmbot) tileEntity);
		}
		this.dropBlockAsItem_do(world, x, y, z, new ItemStack(this));
		super.breakBlock(world, x, y, z, par5, par6);
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		return new ItemStack(this);
	}

	@Override
	public int quantityDropped(Random par1Random)
	{
		return 0;
	}

	@Override
	public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
	{
		list.add(new Pair("ALArmbot", TileEntityArmbot.class));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getClientTileEntityRenderers(List<Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>> list)
	{
		list.add(new Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>(TileEntityArmbot.class, new RenderArmbot()));
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileEntityArmbot();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return BlockRenderingHandler.BLOCK_RENDER_ID;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
}
