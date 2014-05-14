package resonantinduction.mechanical.fluid.pipe;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import resonant.lib.render.EnumColor;
import resonant.lib.utility.LanguageUtility;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;

public class ItemPipe extends JItemMultiPart
{
	public ItemPipe(int id)
	{
		super(id);
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

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		return super.getUnlocalizedName(itemStack) + "." + LanguageUtility.underscoreToCamel(EnumPipeMaterial.values()[itemStack.getItemDamage()].name());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean par4)
	{
		if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
		{
			list.add(LanguageUtility.getLocal("tooltip.noShift").replace("%0", EnumColor.AQUA.toString()).replace("%1", EnumColor.GREY.toString()));
		}
		else
		{
			list.add(EnumColor.AQUA + LanguageUtility.getLocal("tooltip.pipe.rate").replace("%v", "" + EnumColor.ORANGE + UnitDisplay.getDisplay(EnumPipeMaterial.values()[itemstack.getItemDamage()].maxFlowRate * 20, Unit.LITER) + "/s"));
			list.add(EnumColor.AQUA + LanguageUtility.getLocal("tooltip.pipe.pressure").replace("%v", "" + EnumColor.ORANGE + EnumPipeMaterial.values()[itemstack.getItemDamage()].maxPressure + " Pa"));
		}
	}

	@Override
	public void getSubItems(int itemID, CreativeTabs tab, List listToAddTo)
	{
		for (EnumPipeMaterial material : EnumPipeMaterial.values())
		{
			listToAddTo.add(new ItemStack(itemID, 1, material.ordinal()));
		}
	}
}
