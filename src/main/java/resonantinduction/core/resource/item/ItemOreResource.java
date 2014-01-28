package resonantinduction.core.resource.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.api.recipe.MachineRecipes;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;
import resonantinduction.api.recipe.RecipeUtils.Resource;
import resonantinduction.core.prefab.item.ItemRI;
import resonantinduction.core.resource.ResourceGenerator;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.LanguageUtility;
import calclavia.lib.utility.inventory.InventoryUtility;
import calclavia.lib.utility.nbt.NBTUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * An item used for auto-generated dusts based on registered ingots in the OreDict.
 * 
 * @author Calclavia
 * 
 */
public class ItemOreResource extends ItemRI
{
	public ItemOreResource(int id, String name)
	{
		super(name, id);
		setHasSubtypes(true);
		setMaxDamage(0);
	}

	@Override
	public String getItemDisplayName(ItemStack is)
	{
		String dustName = getMaterialFromStack(is);

		if (dustName != null)
		{
			List<ItemStack> list = OreDictionary.getOres("ingot" + dustName.substring(0, 1).toUpperCase() + dustName.substring(1));

			if (list.size() > 0)
			{
				ItemStack type = list.get(0);

				String name = type.getDisplayName().replace(LanguageUtility.getLocal("misc.resonantinduction.ingot"), "").replaceAll("^ ", "").replaceAll(" $", "");
				return (LanguageUtility.getLocal(this.getUnlocalizedName() + ".name")).replace("%v", name).replace("  ", " ");
			}
		}

		return "";
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		/**
		 * Manually wash dust into refined dust.
		 */
		Resource[] outputs = MachineRecipes.INSTANCE.getOutput(RecipeType.MIXER, stack);

		if (outputs.length > 0)
		{
			int blockId = world.getBlockId(x, y, z);
			int metadata = world.getBlockMetadata(x, y, z);
			Block block = Block.blocksList[blockId];

			if (block == Block.cauldron)
			{
				if (metadata > 0)
				{
					if (world.rand.nextFloat() > 0.9)
					{
						for (Resource res : outputs)
						{
							InventoryUtility.dropItemStack(world, new Vector3(player), res.getItemStack().copy(), 0);
						}

						stack.splitStack(1);

						if (stack.stackSize <= 0)
							player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
					}

					world.setBlockMetadataWithNotify(x, y, z, metadata - 1, 3);
					world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "liquid.water", 0.5f, 1);
				}

				return true;
			}
		}
		return false;
	}

	public ItemStack getStackFromMaterial(String name)
	{
		ItemStack itemStack = new ItemStack(this);
		NBTTagCompound nbt = NBTUtility.getNBTTagCompound(itemStack);
		nbt.setString("name", name);
		itemStack.setItemDamage(ResourceGenerator.materialNames.indexOf(name));
		return itemStack;
	}

	public static String getMaterialFromStack(ItemStack itemStack)
	{
		NBTTagCompound nbt = NBTUtility.getNBTTagCompound(itemStack);

		if (nbt.hasKey("name"))
		{
			return nbt.getString("name");
		}

		return null;
	}

	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for (String materialName : ResourceGenerator.materialNames)
		{
			par3List.add(getStackFromMaterial(materialName));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack itemStack, int par2)
	{
		/**
		 * Auto-color based on the texture of the ingot.
		 */
		String name = ItemOreResource.getMaterialFromStack(itemStack);

		if (ResourceGenerator.materialColors.containsKey(name))
		{
			return ResourceGenerator.materialColors.get(name);
		}

		return 16777215;
	}
}
