package resonantinduction.electrical.multimeter;

import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import resonantinduction.core.prefab.part.IHighlight;
import resonantinduction.electrical.wire.EnumWireMaterial;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import calclavia.lib.render.EnumColor;
import calclavia.lib.utility.LanguageUtility;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.FacePlacementGrid$;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;

public class ItemMultimeter extends JItemMultiPart implements IHighlight
{
	private Icon[] icons = new Icon[EnumWireMaterial.values().length];

	public ItemMultimeter(int id)
	{
		super(id);
	}

	@Override
	public TMultiPart newPart(ItemStack itemStack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 hit)
	{
		side = FacePlacementGrid$.MODULE$.getHitSlot(hit, side);

		PartMultimeter part = (PartMultimeter) MultiPartRegistry.createPart("resonant_induction_multimeter", false);

		if (part != null)
		{
			int l = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
			int facing = l == 0 ? 2 : (l == 1 ? 5 : (l == 2 ? 3 : (l == 3 ? 4 : 0)));
			part.preparePlacement(side, facing);
		}

		return part;
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer par2EntityPlayer, List list, boolean par4)
	{
		if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
		{
			list.add(LanguageUtility.getLocal("tooltip.noShift").replace("%0", EnumColor.AQUA.toString()).replace("%1", EnumColor.GREY.toString()));
		}
		else
		{
			list.addAll(LanguageUtility.splitStringPerWord(LanguageUtility.getLocal("item.resonantinduction:multimeter.tooltip"), 5));
		}
	}

	public float getDetection(ItemStack itemStack)
	{
		if (itemStack.stackTagCompound == null || !itemStack.getTagCompound().hasKey("detection"))
		{
			return -1;
		}

		return itemStack.stackTagCompound.getFloat("detection");
	}

	public void setDetection(ItemStack itemStack, float detection)
	{
		if (itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setFloat("detection", detection);
	}

	@Override
	public int getHighlightType()
	{
		return 0;
	}
}
