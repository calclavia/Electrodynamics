package resonantinduction.old.transport.logistic;

import java.util.List;
import java.util.Set;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;
import resonantinduction.core.prefab.block.BlockImprintable;
import resonantinduction.mechanical.render.MechanicalBlockRenderingHandler;
import resonantinduction.old.client.render.RenderManipulator;
import universalelectricity.api.UniversalElectricity;

import com.builtbroken.common.Pair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A block that manipulates item movement between inventories.
 * 
 * @author Calclavia, DarkGuardsman
 */
public class BlockManipulator extends BlockImprintable
{
	public BlockManipulator()
	{
		super("manipulator", UniversalElectricity.machine);
		this.setBlockBounds(0, 0, 0, 1, 0.29f, 1);
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
	{
		return AxisAlignedBB.getAABBPool().getAABB(par2, par3, par4, (double) par2 + 1, (double) par3 + 1, (double) par4 + 1);
	}

	@Override
	public boolean onSneakMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileEntityManipulator)
		{
			((TileEntityManipulator) tileEntity).setSelfPulse(!((TileEntityManipulator) tileEntity).isSelfPulse());
			entityPlayer.sendChatToPlayer(ChatMessageComponent.createFromText("Manip. set to " + (((TileEntityManipulator) tileEntity).isSelfPulse() ? "auto pulse" : "not pulse")));
		}

		return true;
	}

	@Override
	public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileEntityManipulator)
		{
			TileEntityManipulator manip = (TileEntityManipulator) tileEntity;
			boolean manipMode = manip.isOutput();
			boolean inverted = manip.isInverted();
			if (manipMode && !inverted)
			{
				manip.toggleInversion();
			}
			else if (manipMode && inverted)
			{
				manip.toggleOutput();
				manip.toggleInversion();
			}
			else if (!manipMode && !inverted)
			{
				manip.toggleInversion();
			}
			else
			{
				manip.toggleOutput();
				manip.toggleInversion();
			}
			entityPlayer.sendChatToPlayer(ChatMessageComponent.createFromText("Manip. outputing =  " + manip.isOutput()));

		}

		return true;
	}

	@Override
	public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
	{
		list.add(new Pair("ALManipulator", TileEntityManipulator.class));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getClientTileEntityRenderers(List<Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>> list)
	{
		list.add(new Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>(TileEntityManipulator.class, new RenderManipulator()));
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileEntityManipulator();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return MechanicalBlockRenderingHandler.ID;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public int damageDropped(int par1)
	{
		return 0;
	}
}
