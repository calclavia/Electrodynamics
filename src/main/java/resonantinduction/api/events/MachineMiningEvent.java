package resonantinduction.api.events;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Cancelable;
import universalelectricity.api.vector.Vector3;

/**
 * Event called at all stages of mining a block using a machine
 * 
 * @author Darkguardsman
 */
public class MachineMiningEvent extends MachineEvent
{
	public final Vector3 targetBlock;

	public MachineMiningEvent(World world, Vector3 spot, Vector3 target)
	{
		super(world, spot);
		this.targetBlock = target;
	}

	@Cancelable
	public static class PreMine extends MachineMiningEvent
	{
		public PreMine(World world, Vector3 spot, Vector3 target)
		{
			super(world, spot, target);
		}
	}

	public static class MiningDrop extends MachineMiningEvent
	{
		List<ItemStack> items;

		public MiningDrop(World world, Vector3 spot, Vector3 target, List<ItemStack> items)
		{
			super(world, spot, target);
			this.items = items;
		}
	}

	public static class PostMine extends MachineMiningEvent
	{
		public PostMine(World world, Vector3 spot, Vector3 target)
		{
			super(world, spot, target);
		}
	}

	public static boolean doMachineMiningCheck(World world, Vector3 target, TileEntity machine)
	{
		Block block = Block.blocksList[target.getBlockID(world)];

		return block != null && target.getTileEntity(world) == null && !block.isAirBlock(world, target.intX(), target.intY(), target.intZ()) && block.getBlockHardness(world, target.intX(), target.intY(), target.intZ()) >= 0;

	}

	public static List<ItemStack> getItemsMined(World world, Vector3 spot, Vector3 target)
	{
		Block block = Block.blocksList[target.getBlockID(world)];
		if (block != null)
		{
			List<ItemStack> items = block.getBlockDropped(world, target.intX(), target.intY(), target.intZ(), target.getBlockMetadata(world), 1);
			if (items != null)
			{
				MiningDrop event = new MiningDrop(world, spot, target, items);
				MinecraftForge.EVENT_BUS.post(event);
				items = event.items;
				return items;
			}
		}
		return null;
	}

	public static List<ItemStack> getItemsMined(TileEntity entity, Vector3 target)
	{
		return getItemsMined(entity.worldObj, new Vector3(entity), target);
	}
}