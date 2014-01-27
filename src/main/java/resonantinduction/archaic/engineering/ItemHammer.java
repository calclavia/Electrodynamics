package resonantinduction.archaic.engineering;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.api.recipe.MachineRecipes;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;
import resonantinduction.api.recipe.RecipeUtils.Resource;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.item.ItemRI;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.inventory.InventoryUtility;

public class ItemHammer extends ItemRI
{
	public ItemHammer()
	{
		super("hammer");
		setMaxStackSize(1);
		setMaxDamage(400);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileEngineeringTable)
		{
			TileEngineeringTable tile = (TileEngineeringTable) tileEntity;
			ItemStack inputStack = tile.getStackInSlot(TileEngineeringTable.CENTER_SLOT);

			if (inputStack != null)
			{
				String oreName = OreDictionary.getOreName(OreDictionary.getOreID(inputStack));

				if (oreName != null && !oreName.equals("Unknown"))
				{
					if (!world.isRemote && world.rand.nextFloat() < 0.04)
					{
						Resource[] outputs = MachineRecipes.INSTANCE.getOutput(RecipeType.CRUSHER, oreName);

						// TODO: Fix multiple outputs.
						for (Resource resource : outputs)
						{
							ItemStack outputStack = resource.getItemStack().copy();

							if (outputStack != null)
							{
								InventoryUtility.dropItemStack(world, new Vector3(player), outputStack, 0);
								tile.setInventorySlotContents(TileEngineeringTable.CENTER_SLOT, --inputStack.stackSize <= 0 ? null : inputStack);
							}
						}
					}

					world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, Reference.PREFIX + "hammer", 0.5f, 0.8f + (0.2f * world.rand.nextFloat()));
					player.addExhaustion(1);
					stack.damageItem(1, player);
				}
			}

			return true;
		}
		return false;
	}
}
