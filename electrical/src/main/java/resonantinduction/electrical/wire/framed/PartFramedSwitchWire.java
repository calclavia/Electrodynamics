package resonantinduction.electrical.wire.framed;

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

public class PartFramedSwitchWire extends PartFramedWire
{
	@Override
	public boolean isBlockedOnSide(ForgeDirection side)
	{
		if (this.checkRedstone(6))
		{
			return super.isBlockedOnSide(side);
		}
		return true;
	}

	@Override
	public String getType()
	{
		return "resonant_induction_switch_wire";
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
	{
		TileMultipart tile = tile();
		World w = world();

		if (item != null && item.getItem().itemID == Block.lever.blockID)
		{
			if (!w.isRemote)
			{
				PartFramedWire wire = (PartFramedWire) MultiPartRegistry.createPart("resonant_induction_wire", false);
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
	public Iterable<ItemStack> getDrops()
	{
		List<ItemStack> drops = (List<ItemStack>) super.getDrops();
		drops.add(new ItemStack(Block.lever, 1));

		return drops;
	}

	@Override
	public byte getPossibleAcceptorConnections()
	{
		if (this.checkRedstone(6))
		{
			return super.getPossibleAcceptorConnections();
		}
		return 0x00;
	}

	@Override
	public byte getPossibleWireConnections()
	{
		if (this.checkRedstone(6))
		{
			return super.getPossibleWireConnections();
		}
		return 0x00;
	}
}
