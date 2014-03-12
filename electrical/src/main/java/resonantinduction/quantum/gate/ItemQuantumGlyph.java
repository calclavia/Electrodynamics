package resonantinduction.quantum.gate;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.prefab.part.IHighlight;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.CornerPlacementGrid$;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class ItemQuantumGlyph extends JItemMultiPart implements IHighlight
{
	public ItemQuantumGlyph(int id)
	{
		super(id);
		setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		return super.getUnlocalizedName(itemStack) + "." + itemStack.getItemDamage();
	}

	@Override
	public TMultiPart newPart(ItemStack itemStack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 hit)
	{
		PartQuantumGlyph part = (PartQuantumGlyph) MultiPartRegistry.createPart("resonant_induction_quantum_glyph", false);
		int slot = CornerPlacementGrid$.MODULE$.getHitSlot(hit, side);

		TileEntity tile = world.getBlockTileEntity(pos.x, pos.y, pos.z);

		if (tile instanceof TileMultipart)
		{
			TMultiPart checkPart = ((TileMultipart) tile).partMap(slot);

			if (checkPart != null)
			{
				switch (side)
				{
					case 0:
						slot -= 1;
						break;
					case 1:
						slot += 1;
						break;
					case 2:
						slot -= 2;
						break;
					case 3:
						slot += 2;
						break;
					case 4:
						slot -= 4;
						break;
					case 5:
						slot += 4;
						break;
				}
			}
			else
			{
				// pos.offset(side);
			}
		}

		part.preparePlacement(slot, itemStack.getItemDamage());
		return part;
	}

	@Override
	public void getSubItems(int itemID, CreativeTabs tab, List listToAddTo)
	{
		for (int i = 0; i < PartQuantumGlyph.MAX_GLYPH; i++)
		{
			listToAddTo.add(new ItemStack(itemID, 1, i));
		}
	}

	@Override
	public int getHighlightType()
	{
		return 1;
	}
}
