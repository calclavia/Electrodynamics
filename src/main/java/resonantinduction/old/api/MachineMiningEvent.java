package resonantinduction.old.api;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.Event;
import universalelectricity.api.vector.Vector3;

public class MachineMiningEvent extends Event
{
	public final World world;
	public final Vector3 spot;
	public final TileEntity machine;

	public MachineMiningEvent(World world, Vector3 spot, TileEntity machine)
	{
		this.world = world;
		this.spot = spot;
		this.machine = machine;
	}

	@Cancelable
	public static class PreMine extends MachineMiningEvent
	{
		public PreMine(World world, Vector3 spot, TileEntity machine)
		{
			super(world, spot, machine);
		}
	}

	public static class MiningDrop extends MachineMiningEvent
	{
		List<ItemStack> items;

		public MiningDrop(World world, Vector3 spot, TileEntity machine, List<ItemStack> items)
		{
			super(world, spot, machine);
			this.items = items;
		}
	}

	public static class PostMine extends MachineMiningEvent
	{
		public PostMine(World world, Vector3 spot, TileEntity machine)
		{
			super(world, spot, machine);
		}
	}

	public static boolean doMachineMiningCheck(World world, Vector3 target, TileEntity machine)
	{
		Block block = Block.blocksList[target.getBlockID(world)];

		return block != null && target.getTileEntity(world) == null && !block.isAirBlock(world, target.intX(), target.intY(), target.intZ()) && block.getBlockHardness(world, target.intX(), target.intY(), target.intZ()) >= 0;

	}

	public static List<ItemStack> getItemsMined(World world, Vector3 target, TileEntity machine)
	{
		Block block = Block.blocksList[target.getBlockID(world)];
		if (block != null)
		{
			List<ItemStack> items = block.getBlockDropped(world, target.intX(), target.intY(), target.intZ(), target.getBlockMetadata(world), 1);
			if (items != null)
			{
				MiningDrop event = new MiningDrop(world, target, machine, items);
				MinecraftForge.EVENT_BUS.post(event);
				items = event.items;
				return items;
			}
		}
		return null;
	}
}