package resonantinduction.core.resource;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.core.Reference;
import resonantinduction.core.Settings;
import resonantinduction.core.prefab.item.ItemBase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dark.lib.IExtraInfo.IExtraItemInfo;

/**
 * A meta data item containing parts of various crafting recipes. These parts do not do anything but
 * allow new crafting recipes to be created.
 * 
 * @author DarkGuardsman
 */
public class ItemParts extends ItemBase implements IExtraItemInfo
{
	public ItemParts()
	{
		super("DMParts", Settings.getNextItemID());
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		this.setMaxStackSize(64);
		this.setCreativeTab(CreativeTabs.tabMaterials);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		if (itemStack != null && itemStack.getItemDamage() < Parts.values().length)
		{
			return "item." + Parts.values()[itemStack.getItemDamage()].name;
		}
		return super.getUnlocalizedName();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int meta)
	{
		if (meta < Parts.values().length)
		{
			return Parts.values()[meta].icon;
		}
		return this.itemIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister)
	{
		super.registerIcons(iconRegister);
		for (Parts part : Parts.values())
		{
			part.icon = iconRegister.registerIcon(Reference.PREFIX + "part." + part.name);
		}
	}

	@Override
	public int getMetadata(int meta)
	{
		return meta;
	}

	@Override
	public void getSubItems(int blockID, CreativeTabs tab, List itemStackList)
	{
		for (Parts part : Parts.values())
		{
			if (part.show)
			{
				itemStackList.add(new ItemStack(this, 1, part.ordinal()));
			}
		}
	}

	public static enum Parts
	{
		Seal("leatherSeal"), GasSeal("gasSeal"), Tank("unfinishedTank"), Valve("valvePart"),
		MiningIcon("miningIcon", false), CircuitBasic("circuitBasic"),
		CircuitAdvanced("circuitAdvanced"), CircuitElite("circuitElite"), Motor("motor"),
		IC("ic_chip"), COIL("coilCopper"), LASER("diodeLaser");

		public String name;
		public Icon icon;
		boolean show = true;

		private Parts(String name)
		{
			this.name = name;
		}

		private Parts(String name, boolean show)
		{
			this(name);
			this.show = show;
		}
	}

	@Override
	public boolean hasExtraConfigs()
	{
		return false;
	}

	@Override
	public void loadExtraConfigs(Configuration config)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void loadOreNames()
	{
		for (Parts part : Parts.values())
		{
			OreDictionary.registerOre(part.name, new ItemStack(this, 1, part.ordinal()));
		}

	}
}
