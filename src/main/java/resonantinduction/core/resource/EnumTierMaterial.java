package resonantinduction.core.resource;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Class for storing materials, there icon names, sub items to be made from them or there sub ores
 * 
 * 
 * @author DarkGuardsman
 */
public enum EnumTierMaterial
{
	WOOD("Wood", EnumToolMaterial.WOOD, EnumRecipePart.INGOTS, EnumRecipePart.PLATES, EnumRecipePart.RUBBLE, EnumRecipePart.ROD, EnumRecipePart.MOLTEN),
	STONE("Stone", EnumToolMaterial.STONE, EnumRecipePart.INGOTS, EnumRecipePart.SCRAPS, EnumRecipePart.MOLTEN),
	IRON("Iron", EnumToolMaterial.IRON, EnumRecipePart.INGOTS),
	OBBY("Obby", true, 7.0f, 500, 4, EnumRecipePart.INGOTS, EnumRecipePart.RUBBLE, EnumRecipePart.SCRAPS, EnumRecipePart.PLATES, EnumRecipePart.MOLTEN),
	GOLD("Gold", EnumToolMaterial.GOLD, EnumRecipePart.GEARS, EnumRecipePart.INGOTS),
	COAL("Coal", EnumToolMaterial.WOOD, EnumRecipePart.GEARS, EnumRecipePart.TUBE, EnumRecipePart.PLATES, EnumRecipePart.RUBBLE, EnumRecipePart.SCRAPS, EnumRecipePart.MOLTEN),

	COPPER("Copper", true, 3.5f, 79, 1),
	TIN("Tin", true, 2.0f, 50, 1, EnumRecipePart.GEARS, EnumRecipePart.TUBE),
	LEAD("Lead", false, 0, 0, 1, EnumRecipePart.GEARS, EnumRecipePart.TUBE),
	ALUMINIUM("Aluminum", true, 5.0f, 100, 2, EnumRecipePart.GEARS, EnumRecipePart.TUBE),
	SILVER("Silver", true, 11.0f, 30, 0, EnumRecipePart.GEARS),
	STEEL("Steel", true, 7.0f, 4, 1000, EnumRecipePart.RUBBLE),
	BRONZE("Bronze", true, 6.5f, 3, 560, EnumRecipePart.RUBBLE);

	/** Name of the material */
	public String simpleName;
	/** List of ore parts that to not be created for the material */
	public List<EnumRecipePart> unneedItems;

	public boolean hasTools = false;

	/** Limit by which each material is restricted by for creating orePart sub items */
	public static final int itemCountPerMaterial = 50;

	/** Client side only var used by ore items to store icon per material set */
	@SideOnly(Side.CLIENT)
	public Icon[] itemIcons;

	public float materialEffectiveness = 2.0f;
	public int maxUses = 100;
	public float damageBoost = 0;

	private EnumTierMaterial(String name, EnumToolMaterial material, EnumRecipePart... enumOreParts)
	{
		this(name, false, material.getEfficiencyOnProperMaterial(), material.getMaxUses(), material.getDamageVsEntity(), enumOreParts);
	}

	private EnumTierMaterial(String name, boolean tool, float effectiveness, int toolUses, float damage, EnumRecipePart... enumOreParts)
	{
		this.simpleName = name;
		this.hasTools = tool;
		this.materialEffectiveness = effectiveness;
		this.maxUses = toolUses;
		this.damageBoost = damage;
		unneedItems = new ArrayList<EnumRecipePart>();
		for (int i = 0; enumOreParts != null && i < enumOreParts.length; i++)
		{
			unneedItems.add(enumOreParts[i]);
		}
	}

	/**
	 * Creates a new item stack using material and part given. Uses a preset length of 50 for parts
	 * enum so to prevent any unwanted changes in loading of itemStacks metadata.
	 * 
	 * @param mat - material
	 * @param part - part
	 * @return new ItemStack created from the two enums as long as everything goes right
	 */
	public static ItemStack getStack(Item item, EnumTierMaterial mat, EnumRecipePart part, int ammount)
	{
		ItemStack reStack = null;
		if (mat != null && part != null)
		{
			if (part == EnumRecipePart.INGOTS)
			{
				if (mat == EnumTierMaterial.IRON)
				{
					return new ItemStack(Item.ingotIron, 1);
				}
				else if (mat == EnumTierMaterial.GOLD)
				{
					return new ItemStack(Item.ingotGold, 1);
				}
			}
			int meta = mat.ordinal() * itemCountPerMaterial;
			meta += part.ordinal();
			return new ItemStack(item, ammount, meta);
		}
		return reStack;
	}

	public ItemStack getStack(Item item, EnumRecipePart part)
	{
		return this.getStack(item, part, 1);
	}

	public ItemStack getStack(Item item, EnumRecipePart part, int ammount)
	{
		return getStack(item, this, part, ammount);
	}

	public static Icon getIcon(int metadata)
	{
		int mat = metadata / EnumTierMaterial.itemCountPerMaterial;
		if (mat < EnumTierMaterial.values().length)
		{
			return EnumTierMaterial.values()[metadata / EnumTierMaterial.itemCountPerMaterial].itemIcons[metadata % EnumTierMaterial.itemCountPerMaterial];
		}
		return null;
	}

	public static String getOreName(EnumTierMaterial mat, EnumRecipePart part)
	{
		return mat.getOreName(part);
	}

	public String getOreName(EnumRecipePart part)
	{
		return part.simpleName.toLowerCase() + this.simpleName;
	}

	public boolean shouldCreateItem(EnumRecipePart part)
	{
		return this.unneedItems == null || !this.unneedItems.contains(part);
	}

	public boolean shouldCreateTool()
	{
		return this.hasTools;
	}
}
