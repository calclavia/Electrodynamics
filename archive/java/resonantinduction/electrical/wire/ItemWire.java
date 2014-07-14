package resonantinduction.electrical.wire;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.input.Keyboard;

import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInductionTabs;
import resonantinduction.core.Settings;
import resonantinduction.core.Utility;
import resonantinduction.electrical.wire.flat.PartFlatWire;
import resonantinduction.electrical.wire.flat.RenderFlatWire;
import resonantinduction.electrical.wire.framed.PartFramedWire;
import resonantinduction.electrical.wire.framed.RenderPartWire;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import calclavia.lib.render.EnumColor;
import calclavia.lib.utility.LanguageUtility;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.ControlKeyModifer;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
//TODO: AUTO COLOR WIRES!
public class ItemWire extends JItemMultiPart
{
	private Icon[] icons = new Icon[EnumWireMaterial.values().length];

	public ItemWire(int id)
	{
		super(Settings.config.get(Configuration.CATEGORY_ITEM, "wire", id).getInt(id));
		this.setUnlocalizedName(Reference.PREFIX + "wire");
		this.setCreativeTab(ResonantInductionTabs.CORE);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	public TMultiPart newPart(ItemStack itemStack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 hit)
	{
		BlockCoord onPos = pos.copy().offset(side ^ 1);

		if (ControlKeyModifer.isControlDown(player))
		{
			PartFramedWire wire = (PartFramedWire) MultiPartRegistry.createPart("resonant_induction_wire", false);

			if (wire != null)
			{
				wire.preparePlacement(itemStack.getItemDamage());
			}

			return wire;
		}
		else
		{
			if (!Utility.canPlaceWireOnSide(world, onPos.x, onPos.y, onPos.z, ForgeDirection.getOrientation(side), false))
			{
				return null;
			}

			PartFlatWire wire = (PartFlatWire) MultiPartRegistry.createPart("resonant_induction_flat_wire", false);

			if (wire != null)
			{
				wire.preparePlacement(side, itemStack.getItemDamage());
			}

			return wire;
		}
	}

	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		return super.getUnlocalizedName(itemStack) + "." + EnumWireMaterial.values()[itemStack.getItemDamage()].getName().toLowerCase();
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
			list.add(EnumColor.AQUA + LanguageUtility.getLocal("tooltip.wire.resistance").replace("%v", "" + EnumColor.ORANGE + UnitDisplay.getDisplay(EnumWireMaterial.values()[itemstack.getItemDamage()].resistance, Unit.RESISTANCE)));
			list.add(EnumColor.AQUA + LanguageUtility.getLocal("tooltip.wire.current").replace("%v", "" + EnumColor.ORANGE + UnitDisplay.getDisplay(EnumWireMaterial.values()[itemstack.getItemDamage()].maxAmps, Unit.AMPERE)));
			list.add(EnumColor.AQUA + LanguageUtility.getLocal("tooltip.wire.damage").replace("%v", "" + EnumColor.ORANGE + EnumWireMaterial.values()[itemstack.getItemDamage()].damage));
			list.addAll(LanguageUtility.splitStringPerWord(LanguageUtility.getLocal("tooltip.wire.helpText"), 5));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber()
	{
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		for (EnumWireMaterial material : EnumWireMaterial.values())
		{
			icons[material.ordinal()] = register.registerIcon(Reference.PREFIX + "wire." + EnumWireMaterial.values()[material.ordinal()].getName().toLowerCase());
		}

		RenderFlatWire.flatWireTexture = register.registerIcon(Reference.PREFIX + "models/flatWire");
		RenderPartWire.wireIcon = register.registerIcon(Reference.PREFIX + "models/wire");
		RenderPartWire.insulationIcon = register.registerIcon(Reference.PREFIX + "models/insulation" + (Settings.LO_FI_INSULATION ? "tiny" : ""));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int meta)
	{
		return icons[meta];
	}

	@Override
	public void getSubItems(int itemID, CreativeTabs tab, List listToAddTo)
	{
		for (EnumWireMaterial mat : EnumWireMaterial.values())
		{
			listToAddTo.add(new ItemStack(itemID, 1, mat.ordinal()));
		}
	}
}
