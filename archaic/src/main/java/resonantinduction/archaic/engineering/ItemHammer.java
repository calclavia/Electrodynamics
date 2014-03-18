package resonantinduction.archaic.engineering;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.ResonantInduction.RecipeType;
import universalelectricity.api.vector.Vector3;
import calclavia.api.recipe.MachineRecipes;
import calclavia.api.recipe.RecipeResource;
import calclavia.lib.utility.inventory.InventoryUtility;

public class ItemHammer extends Item
{
	public ItemHammer(int id)
	{
		super(id);
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

			// We don't want to bash the output slots
			for (int i = 0; i < TileEngineeringTable.CRAFTING_OUTPUT_END; i++)
			{
				ItemStack inputStack = tile.getStackInSlot(i);

				if (inputStack != null)
				{
					String oreName = OreDictionary.getOreName(OreDictionary.getOreID(inputStack));

					if (oreName != null && !oreName.equals("Unknown"))
					{
						RecipeResource[] outputs = MachineRecipes.INSTANCE.getOutput(RecipeType.CRUSHER.name(), oreName);

						if (outputs.length > 0)
						{
							if (!world.isRemote && world.rand.nextFloat() < 0.04)
							{
								for (RecipeResource resource : outputs)
								{
									ItemStack outputStack = resource.getItemStack().copy();

									if (outputStack != null)
									{
										InventoryUtility.dropItemStack(world, new Vector3(player), outputStack, 0);
										tile.setInventorySlotContents(i, --inputStack.stackSize <= 0 ? null : inputStack);
									}
								}
							}
							
							ResonantInduction.proxy.renderBlockParticle(world, new Vector3(x + 0.5, y + 0.5, z + 0.5), new Vector3((Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3), inputStack.itemID, 1);
							world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, Reference.PREFIX + "hammer", 0.5f, 0.8f + (0.2f * world.rand.nextFloat()));
							player.addExhaustion(0.3f);
							stack.damageItem(1, player);
							return true;
						}
					}
				}
			}
		}

		return false;
	}
}
