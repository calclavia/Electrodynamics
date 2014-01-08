package resonantinduction.core.misc;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.transport.ResonantInductionTransport;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.minecraft.DarkCore;
import com.builtbroken.minecraft.IExtraInfo.IExtraItemInfo;
import com.builtbroken.minecraft.helpers.HelperMethods;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Item for storing all kinds of food based items including meats, fruits, pies, cakes, breads, etc
 * we have 1000s of meta to work with :)
 * 
 * @author DarkGuardsman
 */
public class ItemFarmFood extends Item implements IExtraItemInfo
{

	public static boolean loadIllegalDrugs = true;

	public ItemFarmFood(int par1)
	{
		super(ResonantInductionTransport.CONFIGURATION.getItem("Food", DarkCore.getNextID()).getInt());
		this.setHasSubtypes(true);
		this.setCreativeTab(CreativeTabs.tabFood);
		this.setUnlocalizedName("FarmFood");
	}

	@Override
	public ItemStack onEaten(ItemStack itemStack, World world, EntityPlayer player)
	{
		FarmFood food = FarmFood.get(itemStack);
		if (player != null && food != null)
		{
			--itemStack.stackSize;
			// TODO add stats and achievements for some of the foods
			world.playSoundAtEntity(player, "random.burp", 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);

			if (!world.isRemote && food.type == FoodType.MEAT && world.rand.nextFloat() < 0.3f)
			{
				player.addPotionEffect(new PotionEffect(Potion.hunger.id, 300, 0));
			}
			ItemStack re = null;
			if (food.type == FoodType.CUP)
			{
				re = new ItemStack(this, 1, FarmFood.Cup.ordinal());
			}
			else if (food.type == FoodType.JAR)
			{
				re = new ItemStack(this, 1, FarmFood.JAR.ordinal());
			}
			else if (food.type == FoodType.BOTTLE)
			{
				re = new ItemStack(this, 1, FarmFood.BOTTLE.ordinal());
			}
			else if (food.type == FoodType.PROCESSED)
			{
				re = new ItemStack(this, 1, FarmFood.TRASH.ordinal());
			}
			if (re != null)
			{
				if (!player.inventory.addItemStackToInventory(re))
				{
					HelperMethods.dropItemStack(player.worldObj, new Vector3(player), re, true);
				}
			}
		}
		return itemStack;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack)
	{
		return 32;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack)
	{
		return EnumAction.eat;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
	{
		FarmFood food = FarmFood.get(itemStack);
		if (food != null && food.type.canEat && player.canEat(food.type.canEat))
		{
			player.setItemInUse(itemStack, this.getMaxItemUseDuration(itemStack));
		}
		return itemStack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		for (FarmFood food : FarmFood.values())
		{
			food.icon = par1IconRegister.registerIcon(ResonantInductionTransport.PREFIX + "food_" + food.name);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Icon getIconFromDamage(int par1)
	{
		FarmFood food = FarmFood.get(par1);
		if (food != null)
		{
			return food.icon;
		}
		return this.itemIcon;
	}

	@Override
	public void getSubItems(int itemID, CreativeTabs tab, List list)
	{
		for (FarmFood food : FarmFood.values())
		{
			if (food.type != FoodType.ILLEGAL_DRUGS || ItemFarmFood.loadIllegalDrugs)
			{
				list.add(new ItemStack(this.itemID, 1, food.ordinal()));
			}
		}
	}

	@Override
	public boolean hasExtraConfigs()
	{
		return true;
	}

	@Override
	public void loadExtraConfigs(Configuration config)
	{
		for (FarmFood food : FarmFood.values())
		{
			if (food.foodValue != 0 && food.saturation != 0)
			{
				food.foodValue = (float) config.get("FoodValue", food.name, food.foodValue).getDouble(food.foodValue);
				food.saturation = (float) config.get("FoodSaturation", food.name, food.saturation).getDouble(food.saturation);
			}
		}
	}

	@Override
	public void loadOreNames()
	{
		for (FarmFood food : FarmFood.values())
		{
			OreDictionary.registerOre(food.name, new ItemStack(this, 1, food.ordinal()));
		}
	}

	/**
	 * enum that stores data for each meta value that represents a food object for the item
	 * 
	 * @Source http://urbanext.illinois.edu/herbs/list.cfm
	 * @author DarkGuardsman
	 */
	public static enum FarmFood
	{
		Cup("Cup", FoodType.ITEM),
		JAR("Jar", FoodType.ITEM),
		TRASH("Trash", FoodType.ITEM),
		BOTTLE("Cup", FoodType.ITEM),
		// Turkey
		TurkeyRaw("TurkeyRaw", FoodType.MEAT, 2, 0.3F),
		TurkeyCooked("TurkeyCooked", FoodType.COOKED_MEAT, 6, 0.6F),
		TGTurkeyCooked("TGTurkeyCooked", FoodType.COOKED_MEAT, 9, 0.8F),
		// cabage
		HeadOFCabage("RawCabage", FoodType.VEG, 2, 0.2F),
		CabageSeeds("CabageSeeds", FoodType.SEEDS),
		HeadOfCookedCabage("CabageCooked", FoodType.VEG, 4, 0.2F),

		// Corn
		CornCob("RawCornCob", FoodType.VEG, 2, 0.2F),
		CookedCornCob("CornCob", FoodType.VEG, 4, 0.2F),
		Corn("Corn", FoodType.VEG, 4, 0.3f),
		CornSeeds("Corn", FoodType.SEEDS),
		CookedCorn("CookedCorn", FoodType.VEG, 4, 0.3f),
		// drinks
		Coffee("Coffee", FoodType.ITEM), TeaLeaves("TeaLeaves", FoodType.ITEM),
		CupCoffee("CoffeeCup", FoodType.CUP),
		CupTea("TeaCup", FoodType.CUP),
		// Soups
		VegSoup("VegSoup", FoodType.SOUP), MeatSoup("MeatSoup", FoodType.SOUP),
		HamCabageSoup("HawCabageSoup", FoodType.SOUP, 2, 0.2F),
		BeafCabageSoup("BeafCabageSoup", FoodType.SOUP, 2, 0.2F);

		/** Metadata id that this item converts to for each recipe creation */
		public int returnID = -1;
		public final FoodType type;
		/** Name used for translation and icons */
		final String name;

		float foodValue;
		float saturation;

		Icon icon;

		private FarmFood(String name, FoodType type)
		{
			this.name = name;
			this.type = type;
		}

		private FarmFood(String name, FoodType type, float foodValue, float saturation)
		{
			this(name, type, foodValue, saturation, -1);
		}

		private FarmFood(String name, FoodType type, float foodValue, float saturation, int returnID)
		{
			this(name, type);
			this.foodValue = foodValue;
			this.saturation = saturation;
			this.returnID = returnID;
		}

		public static FarmFood get(ItemStack stack)
		{
			return get(stack != null ? stack.getItemDamage() : -1);
		}

		public static FarmFood get(int meta)
		{
			if (meta >= 0 && meta < FarmFood.values().length)
			{
				return FarmFood.values()[meta];
			}
			return null;
		}
	}

	/**
	 * IDs what the item is and determines how its used
	 * 
	 * @author DarkGuardsman
	 */
	public static enum FoodType
	{
		MEAT(true), COOKED_MEAT(true), VEG(true), SEEDS(false), FRUIT(true), CUP(true), SOUP(true),
		/** food containers */
		ITEM(false),
		/** Herbs used to add to foods */
		HERB(false),
		/** Medical Drugs, mainly ones to heal animals */
		MEDICAL_DRUGS(false),
		/** Drugs that are considered illegal but are still valid drug items */
		ILLEGAL_DRUGS(false),
		/** Food that has been canned */
		CAN(true),
		/** Foods that have been sealed in a glass jar. Same as a can */
		JAR(true),
		/** Liquids like milk that were bottled */
		BOTTLE(true),
		/** Food that comes in a plastic wrapper */
		PROCESSED(true);

		boolean canEat = true;

		private FoodType(boolean canEat)
		{
			this.canEat = canEat;
		}

	}

	@Override
	public int getItemStackLimit(ItemStack stack)
	{
		FarmFood food = FarmFood.get(stack);
		if (food != null)
		{

		}
		return 64;
	}

}
