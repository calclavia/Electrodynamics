package resonantinduction.electrical.wire.flat;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TileMultipart;

public class PartFlatSwitchWire extends PartFlatWire
{
	@Override
	public boolean canConnectTo(Object obj)
	{
		if (this.checkRedstone(this.side ^ 0x1))
		{
			return super.canConnectTo(obj);
		}

		return false;
	}

	@Override
	public boolean canConnectTo(Object obj, ForgeDirection dir)
	{
		if (this.checkRedstone(this.side ^ 0x1))
		{
			return super.canConnectTo(obj, dir);
		}

		return false;
	}

	@Override
	public String getType()
	{
		return "resonant_induction_flat_switch_wire";
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
	{
		TileMultipart tile = tile();
		World w = world();

		if (item.getItem().itemID == Block.lever.blockID)
		{
			if (!w.isRemote)
			{
				PartFlatWire wire = (PartFlatWire) MultiPartRegistry.createPart("resonant_induction_flat_wire", false);
				wire.copyFrom(this);

				if (tile.canReplacePart(this, wire))
				{
					tile.remPart(this);
					TileMultipart.addPart(w, new BlockCoord(tile), wire);

					if (!player.capabilities.isCreativeMode)
					{
						tile.dropItems(Collections.singletonList(new ItemStack(Block.lever, 1)));
					}
				}
			}
			return true;
		}
		else
		{
			return super.activate(player, part, item);
		}
	}

	@Override
	public void drop()
	{
		tile().dropItems(Collections.singletonList(new ItemStack(Block.lever, 1)));
		super.drop();
	}

	@Override
	public Iterable<ItemStack> getDrops()
	{
		List<ItemStack> drops = (List<ItemStack>) super.getDrops();
		drops.add(new ItemStack(Block.lever, 1));

		return drops;
	}
}
