package resonantinduction.electrical.wire;

import java.awt.Color;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.input.Keyboard;

import resonant.lib.render.EnumColor;
import resonant.lib.utility.LanguageUtility;
import resonantinduction.core.prefab.part.MultipartUtility;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantTab;
import resonantinduction.electrical.wire.flat.PartFlatWire;
import resonantinduction.electrical.wire.flat.RenderFlatWire;
import resonantinduction.electrical.wire.framed.PartFramedWire;
import resonantinduction.electrical.wire.framed.RenderFramedWire;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.ControlKeyModifer;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import universalelectricity.api.UnitDisplay;

public class ItemWire extends JItemMultiPart
{
	public ItemWire()
	{
		super();
		this.setUnlocalizedName(Reference.prefix() + "wire");
		this.setTextureName(Reference.prefix() + "wire");
		this.setCreativeTab(ResonantTab.tab());
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	public TMultiPart newPart(ItemStack itemStack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 hit)
	{
		BlockCoord onPos = pos.copy().offset(side ^ 1);

		if (player.isSneaking() && !ControlKeyModifer.isControlDown(player))
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
			if (!MultipartUtility.canPlaceWireOnSide(world, onPos.x, onPos.y, onPos.z, ForgeDirection.getOrientation(side), false))
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
			list.add(EnumColor.AQUA + LanguageUtility.getLocal("tooltip.wire.resistance").replace("%v", "" + EnumColor.ORANGE + new UnitDisplay(UnitDisplay.Unit.RESISTANCE, EnumWireMaterial.values()[itemstack.getItemDamage()].resistance)));
			list.add(EnumColor.AQUA + LanguageUtility.getLocal("tooltip.wire.current").replace("%v", "" + EnumColor.ORANGE + new UnitDisplay(UnitDisplay.Unit.AMPERE, EnumWireMaterial.values()[itemstack.getItemDamage()].maxAmps)));
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
	public void registerIcons(IIconRegister register)
	{
		RenderFlatWire.flatWireTexture = register.registerIcon(Reference.prefix() + "models/flatWire");
		RenderFramedWire.wireIcon = register.registerIcon(Reference.prefix() + "models/wire");
		RenderFramedWire.insulationIcon = register.registerIcon(Reference.prefix() + "models/insulation");
		super.registerIcons(register);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack itemStack, int par2)
	{
		return new Color(EnumWireMaterial.values()[itemStack.getItemDamage()].color.rgb()).darker().getRGB();
	}

	@Override
	public void getSubItems(Item itemID, CreativeTabs tab, List listToAddTo)
	{
		for (EnumWireMaterial mat : EnumWireMaterial.values())
		{
			listToAddTo.add(new ItemStack(itemID, 1, mat.ordinal()));
		}
	}
}
