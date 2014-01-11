package resonantinduction.mechanical.fluid.pipes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.mechanical.fluid.prefab.TileFluidNetworkTile;
import resonantinduction.old.core.recipe.RecipeLoader;
import dark.lib.helpers.ColorCode;

/**
 * Enum to hold info about each pipe material. Values are by default and some can change with pipe
 * upgrades.
 * 
 * @Note unsupportedFluids should only be used by filters. All pipes should allow all fluid types.
 * However, pipes that can't support the fluid should have an effect. Eg no gas support should cause
 * the pipe to leak. No molten support should cause the pipe to take damage.
 * 
 * @author DarkGuardsman
 */
public enum FluidPartsMaterial
{
	/** Simple water only pipe. Should render open toped when it can */
	WOOD("wood", false, true, false, -1, 200),
	/** Gas only pipe */
	GLASS("glass", true, false, false, 100, 300),
	/** Another version of the wooden pipe */
	STONE("stone", false, true, false, -1, 1000),
	/** Cheap fluid pipe */
	TIN("tin", false, true, false, 300, 1000),
	/** Cheap fluid pipe */
	COPPER("copper", false, true, false, 400, 1000),
	/** First duel gas and fluid pipe */
	IRON("iron", true, true, false, 500, 1000),
	/** Fluid movement pipe that doesn't work well with pressure */
	GOLD("gold", true, true, false, 200, 2000),
	/** Cheap molten metal pipe */
	OBBY("obby", false, true, true, 1000, 1000),
	/**
	 * Very strong fluid and gas support pipe. Should also support molten metal as long as they
	 * don't stay in the pipe too long.
	 */
	STEEL("steel", true, true, false, 10000, 3000),
	/** Weaker equal to steel pipes. Should also support steam very well */
	BRONZE("bronze", true, true, false, 6000, 2000),
	/**
	 * Hell fluids only. Meaning lava, and molten metals. Water should turn to steam, fuel and oil
	 * should cause an explosion around the pipe
	 */
	HELL("hell", true, true, true, 10000, 5000, "water", "fuel", "oil");
	public String matName = "material";
	List<String> unsupportedFluids = new ArrayList<String>();
	public boolean canSupportGas = false;
	public boolean canSupportFluids = false;
	public boolean canSupportMoltenFluids = false;
	public int maxPressure = 1000;
	public int maxVolume = 2000;
	/**
	 * Materials are stored as meta were there sub types are stored by NBT. Item versions of the
	 * pipes are still meta so there is a set spacing to allow for a large but defined range of sub
	 * pipes
	 */
	public static int spacing = 1000;

	private FluidPartsMaterial()
	{
		this.canSupportGas = true;
		this.canSupportFluids = true;
		canSupportMoltenFluids = true;
	}

	private FluidPartsMaterial(String name, boolean gas, boolean fluid, boolean molten, String... strings)
	{
		this.matName = name;
		this.canSupportGas = gas;
		this.canSupportFluids = fluid;
		this.canSupportMoltenFluids = molten;
	}

	private FluidPartsMaterial(String name, boolean gas, boolean fluid, boolean molten, int pressure, int volume, String... strings)
	{
		this(name, gas, fluid, molten, strings);
		this.maxPressure = pressure;
		this.maxVolume = volume;
	}

	public static FluidPartsMaterial get(World world, int x, int y, int z)
	{
		return get(world.getBlockMetadata(x, y, z));
	}

	public static FluidPartsMaterial get(int i)
	{
		if (i < FluidPartsMaterial.values().length)
		{
			return FluidPartsMaterial.values()[i];
		}
		return null;
	}

	public static FluidPartsMaterial get(ItemStack stack)
	{
		if (stack != null)
		{
			return getFromItemMeta(stack.getItemDamage());
		}
		return null;
	}

	public static FluidPartsMaterial getFromItemMeta(int meta)
	{
		meta = meta / spacing;
		if (meta < FluidPartsMaterial.values().length)
		{
			return FluidPartsMaterial.values()[meta];
		}
		return FluidPartsMaterial.WOOD;
	}

	public ItemStack getStack()
	{
		return getStack(1);
	}

	public ItemStack getStack(ColorCode color)
	{
		return getStack(1, color);
	}

	public ItemStack getStack(int s)
	{
		return new ItemStack(RecipeLoader.blockPipe, s, (this.ordinal() * spacing));
	}

	public ItemStack getStack(int s, ColorCode color)
	{
		return new ItemStack(RecipeLoader.blockPipe, s, (this.ordinal() * spacing) + color.ordinal() + 1);
	}

	public int getMeta(int typeID)
	{
		return (this.ordinal() * spacing) + typeID;
	}

	public int getMeta()
	{
		return this.getMeta(0);
	}

	public static int getType(int meta)
	{
		return meta / spacing;
	}

	public static int getDropItemMeta(World world, int x, int y, int z)
	{
		int meta = world.getBlockMetadata(x, y, z);
		TileEntity ent = world.getBlockTileEntity(x, y, z);
		meta *= spacing;
		if (ent instanceof TileFluidNetworkTile)
		{
			meta += ((TileFluidNetworkTile) ent).getSubID();
		}
		return meta;
	}

	public boolean canSupport(FluidStack fluid)
	{
		if (fluid != null && fluid.getFluid() != null)
		{
			if (fluid.getFluid().isGaseous(fluid) && this.canSupportGas)
			{
				return true;
			}
			else if (!fluid.getFluid().isGaseous(fluid) && this.canSupportFluids)
			{
				return true;
			}
		}
		return false;
	}
}
