package resonantinduction.mechanical.fluid.pipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import resonantinduction.core.Settings;
import resonantinduction.core.prefab.part.ItemMultipartBase;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;

public class ItemPipe extends ItemMultipartBase
{
	public ItemPipe()
	{
		super("pipe", Settings.getNextItemID());
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	public TMultiPart newPart(ItemStack itemStack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 hit)
	{
		PartPipe part = (PartPipe) MultiPartRegistry.createPart("resonant_induction_pipe", false);
		part.preparePlacement(itemStack.getItemDamage());
		return part;
	}

	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}
	/*
	@Override
	public void getSubItems(int itemID, CreativeTabs tab, List listToAddTo)
	{
		for (EnumPipeMaterial material : EnumPipeMaterial.values())
		{
			listToAddTo.add(new ItemStack(itemID, 1, material.ordinal()));
		}
	}*/
}
