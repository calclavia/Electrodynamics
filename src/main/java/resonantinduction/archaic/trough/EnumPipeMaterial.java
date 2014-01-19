package resonantinduction.archaic.trough;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.mechanical.fluid.prefab.TileFluidNetwork;

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
public enum EnumPipeMaterial
{
	/** Simple water only pipe. Should render open toped when it can */
	WOOD("wood", false, true, false, -1, 200),
	/** Another version of the wooden pipe */
	STONE("stone", false, true, false, -1, 1000);

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

	private EnumPipeMaterial()
	{
		this.canSupportGas = true;
		this.canSupportFluids = true;
		canSupportMoltenFluids = true;
	}

	private EnumPipeMaterial(String name, boolean gas, boolean fluid, boolean molten, String... strings)
	{
		this.matName = name;
		this.canSupportGas = gas;
		this.canSupportFluids = fluid;
		this.canSupportMoltenFluids = molten;
	}

	private EnumPipeMaterial(String name, boolean gas, boolean fluid, boolean molten, int pressure, int volume, String... strings)
	{
		this(name, gas, fluid, molten, strings);
		this.maxPressure = pressure;
		this.maxVolume = volume;
	}

	public static EnumPipeMaterial get(World world, int x, int y, int z)
	{
		return get(world.getBlockMetadata(x, y, z));
	}

	public static EnumPipeMaterial get(int i)
	{
		if (i < EnumPipeMaterial.values().length)
		{
			return EnumPipeMaterial.values()[i];
		}
		return null;
	}

	public static EnumPipeMaterial get(ItemStack stack)
	{
		if (stack != null)
		{
			return getFromItemMeta(stack.getItemDamage());
		}
		return null;
	}

	public static EnumPipeMaterial getFromItemMeta(int meta)
	{
		meta = meta / spacing;
		if (meta < EnumPipeMaterial.values().length)
		{
			return EnumPipeMaterial.values()[meta];
		}
		return EnumPipeMaterial.WOOD;
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
		if (ent instanceof TileFluidNetwork)
		{
			meta += ((TileFluidNetwork) ent).getSubID();
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
