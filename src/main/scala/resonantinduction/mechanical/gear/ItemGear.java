package resonantinduction.mechanical.gear;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.prefab.part.IHighlight;
import resonantinduction.mechanical.gearshaft.PartGearShaft;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.FacePlacementGrid$;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class ItemGear extends JItemMultiPart implements IHighlight
{
	public ItemGear(int id)
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
		PartGear part = (PartGear) MultiPartRegistry.createPart("resonant_induction_gear", false);
		side = FacePlacementGrid$.MODULE$.getHitSlot(hit, side);

		TileEntity tile = world.getBlockTileEntity(pos.x, pos.y, pos.z);

		if (tile instanceof TileMultipart)
		{
			TMultiPart occupyingPart = ((TileMultipart) tile).partMap(side);
			TMultiPart centerPart = ((TileMultipart) tile).partMap(PartMap.CENTER.ordinal());
			boolean clickedCenter = hit.mag() < 0.4;

			if ((clickedCenter && centerPart instanceof PartGearShaft))
			{
				side ^= 1;
			}
		}

		part.preparePlacement(side, itemStack.getItemDamage());
		return part;
	}

	@Override
	public void getSubItems(int itemID, CreativeTabs tab, List listToAddTo)
	{
		for (int i = 0; i < 3; i++)
		{
			listToAddTo.add(new ItemStack(itemID, 1, i));
		}
		
		listToAddTo.add(new ItemStack(itemID, 1, 10));
	}

	@Override
	public int getHighlightType()
	{
		return 0;
	}
}
