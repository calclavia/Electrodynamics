package resonantinduction.electrical.armbot;

import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.prefab.block.BlockRI;
import resonantinduction.core.render.RIBlockRenderingHandler;
import calclavia.lib.content.IExtraInfo.IExtraBlockInfo;
import calclavia.lib.multiblock.link.IBlockActivate;
import calclavia.lib.multiblock.link.IMultiBlock;

import com.builtbroken.common.Pair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockArmbot extends BlockRI implements IExtraBlockInfo
{
	public BlockArmbot()
	{
		super("armbot");
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
			ResonantInduction.blockMulti.createMultiBlockStructure((IMultiBlock) tileEntity);
		}
	}

	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof IBlockActivate)
		{
			return ((IBlockActivate) tileEntity).onActivated(player);
		}

		return false;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileArmbot)
		{
			((TileArmbot) tileEntity).dropHeldObject();
			ResonantInduction.blockMulti.destroyMultiBlockStructure((TileArmbot) tileEntity);
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
		list.add(new Pair("ALArmbot", TileArmbot.class));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getClientTileEntityRenderers(List<Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>> list)
	{
		list.add(new Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>(TileArmbot.class, new RenderArmbot()));
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileArmbot();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return RIBlockRenderingHandler.ID;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean hasExtraConfigs()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void loadExtraConfigs(Configuration config)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void loadOreNames()
	{
		// TODO Auto-generated method stub

	}
}
