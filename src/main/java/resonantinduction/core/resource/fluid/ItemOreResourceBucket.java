package resonantinduction.core.resource.fluid;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.core.Reference;
import resonantinduction.core.TabRI;
import resonantinduction.core.resource.ItemOreResource;
import resonantinduction.core.resource.ResourceGenerator;
import calclavia.lib.utility.LanguageUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Modified version of the MC bucket to meet the needs of a dynamic fluid registry system
 * 
 * @author Darkguardsman
 */
public class ItemOreResourceBucket extends Item
{
	final boolean isMolten;

	public ItemOreResourceBucket(int id, String name, boolean isMolten)
	{
		super(id);
		this.isMolten = isMolten;
		setMaxStackSize(1);
		setUnlocalizedName(Reference.PREFIX + name);
		setTextureName(Reference.PREFIX + name);
		setCreativeTab(TabRI.CORE);
		setHasSubtypes(true);
		setMaxDamage(0);
	}

	@Override
	public String getItemDisplayName(ItemStack is)
	{
		String material = getMaterialFromStack(is);
		if (material != null)
		{
			String fluidID = isMolten ? ResourceGenerator.materialNameToMolten(material) : ResourceGenerator.materialNameToMixture(material);

			if (fluidID != null && FluidRegistry.getFluid(fluidID) != null)
			{
				String fluidName = FluidRegistry.getFluid(fluidID).getLocalizedName();
				return (LanguageUtility.getLocal(this.getUnlocalizedName() + ".name")).replace("%v", fluidName).replace("  ", " ");
			}

			return material;
		}

		return null;
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack,
	 * world, entityPlayer
	 */
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer)
	{
		String materialName = ResourceGenerator.getName(itemStack.getItemDamage());
		int fluidID = isMolten ? ResourceGenerator.getMolten(materialName).blockID : ResourceGenerator.getMixture(materialName).blockID;

		MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, entityPlayer, false);

		if (movingobjectposition == null)
		{
			return itemStack;
		}
		else
		{
			FillBucketEvent event = new FillBucketEvent(entityPlayer, itemStack, world, movingobjectposition);
			if (MinecraftForge.EVENT_BUS.post(event))
			{
				return itemStack;
			}

			if (event.getResult() == Event.Result.ALLOW)
			{
				if (entityPlayer.capabilities.isCreativeMode)
				{
					return itemStack;
				}

				if (--itemStack.stackSize <= 0)
				{
					return event.result;
				}

				if (!entityPlayer.inventory.addItemStackToInventory(event.result))
				{
					entityPlayer.dropPlayerItem(event.result);
				}

				return itemStack;
			}

			if (movingobjectposition.typeOfHit == EnumMovingObjectType.TILE)
			{
				int i = movingobjectposition.blockX;
				int j = movingobjectposition.blockY;
				int k = movingobjectposition.blockZ;

				if (!world.canMineBlock(entityPlayer, i, j, k))
				{
					return itemStack;
				}

				if (fluidID == 0)
				{
					if (!entityPlayer.canPlayerEdit(i, j, k, movingobjectposition.sideHit, itemStack))
					{
						return itemStack;
					}

					if (world.getBlockMaterial(i, j, k) == Material.water && world.getBlockMetadata(i, j, k) == 0)
					{
						world.setBlockToAir(i, j, k);

						if (entityPlayer.capabilities.isCreativeMode)
						{
							return itemStack;
						}

						if (--itemStack.stackSize <= 0)
						{
							return new ItemStack(Item.bucketWater);
						}

						if (!entityPlayer.inventory.addItemStackToInventory(new ItemStack(Item.bucketWater)))
						{
							entityPlayer.dropPlayerItem(new ItemStack(Item.bucketWater.itemID, 1, 0));
						}

						return itemStack;
					}

					if (world.getBlockMaterial(i, j, k) == Material.lava && world.getBlockMetadata(i, j, k) == 0)
					{
						world.setBlockToAir(i, j, k);

						if (entityPlayer.capabilities.isCreativeMode)
						{
							return itemStack;
						}

						if (--itemStack.stackSize <= 0)
						{
							return new ItemStack(Item.bucketLava);
						}

						if (!entityPlayer.inventory.addItemStackToInventory(new ItemStack(Item.bucketLava)))
						{
							entityPlayer.dropPlayerItem(new ItemStack(Item.bucketLava.itemID, 1, 0));
						}

						return itemStack;
					}
				}
				else
				{
					if (fluidID < 0)
					{
						return new ItemStack(Item.bucketEmpty);
					}

					if (movingobjectposition.sideHit == 0)
					{
						--j;
					}

					if (movingobjectposition.sideHit == 1)
					{
						++j;
					}

					if (movingobjectposition.sideHit == 2)
					{
						--k;
					}

					if (movingobjectposition.sideHit == 3)
					{
						++k;
					}

					if (movingobjectposition.sideHit == 4)
					{
						--i;
					}

					if (movingobjectposition.sideHit == 5)
					{
						++i;
					}

					if (!entityPlayer.canPlayerEdit(i, j, k, movingobjectposition.sideHit, itemStack))
					{
						return itemStack;
					}

					if (this.tryPlaceContainedLiquid(world, i, j, k, fluidID) && !entityPlayer.capabilities.isCreativeMode)
					{
						return new ItemStack(Item.bucketEmpty);
					}
				}
			}

			return itemStack;
		}
	}

	/**
	 * Attempts to place the liquid contained inside the bucket.
	 */
	public boolean tryPlaceContainedLiquid(World world, int x, int y, int z, int fluidID)
	{
		if (fluidID <= 0)
		{
			return false;
		}
		else
		{
			Material material = world.getBlockMaterial(x, y, z);
			boolean flag = !material.isSolid();

			if (!world.isAirBlock(x, y, z) && !flag)
			{
				return false;
			}
			else
			{

				if (!world.isRemote && flag && !material.isLiquid())
				{
					world.destroyBlock(x, y, z, true);
				}

				world.setBlock(x, y, z, fluidID, 8, 3);

				return true;
			}
		}
	}

	public ItemStack getStackFromMaterial(String name)
	{
		ItemStack itemStack = new ItemStack(this);
		itemStack.setItemDamage(ResourceGenerator.getID(name));
		return itemStack;
	}

	public static String getMaterialFromStack(ItemStack itemStack)
	{
		return ResourceGenerator.getName(itemStack.getItemDamage());
	}

	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for (String materialName : ResourceGenerator.getMaterials())
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
		return ResourceGenerator.getColor(name);
	}
}
