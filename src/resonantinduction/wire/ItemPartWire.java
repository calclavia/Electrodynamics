package resonantinduction.wire;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import resonantinduction.ResonantInduction;
import resonantinduction.TabRI;
import resonantinduction.wire.part.PartFlatWire;
import resonantinduction.wire.part.PartWire;
import resonantinduction.wire.render.RenderPartWire;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.TMultiPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPartWire extends JItemMultiPart
{
	private Icon[] icons = new Icon[EnumWireMaterial.values().length];

	public ItemPartWire(int id)
	{
		super(ResonantInduction.CONFIGURATION.get(Configuration.CATEGORY_ITEM, "wire", id).getInt(id));
		this.setUnlocalizedName(ResonantInduction.PREFIX + "wire");
		this.setCreativeTab(TabRI.INSTANCE);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	public TMultiPart newPart(ItemStack arg0, EntityPlayer player, World arg2, BlockCoord arg3, int arg4, Vector3 arg5)
	{
		/*if (player.isSneaking())
		{
			return new PartWire(getDamage(arg0));
		}*/

		return new PartFlatWire(getDamage(arg0));
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
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean par4)
	{
		list.add("Resistance: " + UnitDisplay.getDisplay(EnumWireMaterial.values()[itemstack.getItemDamage()].resistance, Unit.RESISTANCE));
		list.add("Max Amperage: " + UnitDisplay.getDisplay(EnumWireMaterial.values()[itemstack.getItemDamage()].maxAmps, Unit.AMPERE));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		for (EnumWireMaterial material : EnumWireMaterial.values())
		{
			icons[material.ordinal()] = register.registerIcon(ResonantInduction.PREFIX + "wire." + EnumWireMaterial.values()[material.ordinal()].getName().toLowerCase());
		}

		RenderPartWire.registerIcons(register);
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
