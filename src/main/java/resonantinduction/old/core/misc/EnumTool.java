package resonantinduction.old.core.misc;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import resonantinduction.old.core.recipe.RecipeLoader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dark.lib.EnumMaterial;

/**
 * Enum to store tools that can be created from the material sheet.
 * 
 * @author DarkGuardsman
 */
public enum EnumTool
{
	PICKAX("pickaxe", Material.rock, Material.iron, Material.ice, Material.anvil, Material.glass, Material.tnt, Material.piston),
	AX("axe", Material.wood, Material.pumpkin, Material.plants, Material.vine),
	SPADE("shovel", Material.sand, Material.snow, Material.clay, Material.craftedSnow, Material.grass, Material.ground),
	HOE("hoe", Material.plants, Material.pumpkin),
	SHEAR("shear", Material.cloth, Material.circuits, Material.web),
	SWORD("sword", false, Material.web, Material.vine, Material.coral, Material.pumpkin, Material.leaves, Material.plants),
	NA6(), NA7(), NA8(), NA9();

	public final List<Material> effecticVsMaterials = new ArrayList<Material>();
	public String name = "tool";
	public boolean enabled = false;
	public static final int toolCountPerMaterial = 10;

	@SideOnly(Side.CLIENT)
	public Icon[] toolIcons;

	private EnumTool()
	{

	}

	private EnumTool(String name, boolean enabled, Material... mats)
	{
		this.name = name;
		this.enabled = true;
		this.setEffectiveList(mats);
	}

	private EnumTool(String name, Material... mats)
	{
		this(name, true, mats);
	}

	public void setEffectiveList(Material... blocks)
	{
		for (Material block : blocks)
		{
			this.addEffectiveBlock(block);
		}
	}

	public void addEffectiveBlock(Material block)
	{
		if (block != null)
		{
			this.effecticVsMaterials.add(block);
		}
	}

	public static String getFullName(int meta)
	{
		EnumMaterial mat = getToolMatFromMeta(meta);
		EnumTool tool = getToolFromMeta(meta);
		if (mat != null && tool != null)
		{
			return mat.simpleName + tool.name;
		}
		return "CommonTool[" + meta + "]";
	}

	public static ItemStack getTool(EnumTool tool, EnumMaterial mat)
	{
		return tool.getTool(mat);
	}

	public ItemStack getTool(EnumMaterial mat)
	{
		ItemStack stack = null;
		if (RecipeLoader.itemDiggingTool instanceof ItemCommonTool)
		{
			stack = new ItemStack(RecipeLoader.itemDiggingTool.itemID, 1, (mat.ordinal() * toolCountPerMaterial) + this.ordinal());
		}
		return stack;
	}

	public static EnumTool getToolFromMeta(int meta)
	{
		return EnumTool.values()[meta % toolCountPerMaterial];
	}

	public static EnumMaterial getToolMatFromMeta(int meta)
	{
		return EnumMaterial.values()[meta / toolCountPerMaterial];
	}

	public static Icon getToolIcon(int metadata)
	{
		if (metadata < EnumMaterial.values().length)
		{
			int mat = getToolMatFromMeta(metadata).ordinal();
			int tool = getToolFromMeta(metadata).ordinal();
			if (EnumTool.values()[tool].toolIcons == null)
			{
				EnumTool.values()[tool].toolIcons = new Icon[EnumMaterial.values().length];
			}
			if (tool < EnumTool.values()[tool].toolIcons.length)
			{
				return EnumTool.values()[tool].toolIcons[mat];
			}
		}
		return null;
	}

}
