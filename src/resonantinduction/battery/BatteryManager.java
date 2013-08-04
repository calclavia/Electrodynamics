package resonantinduction.battery;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.base.Vector3;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class BatteryManager implements ITickHandler
{
	public static final int CELLS_PER_BATTERY = 16;
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		
	}

	@Override
	public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public String getLabel()
	{
		return "BatteryMultiblockManager";
	}
}
