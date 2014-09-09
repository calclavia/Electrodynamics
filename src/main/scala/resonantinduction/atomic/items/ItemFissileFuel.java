package resonantinduction.atomic.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import resonant.api.IReactor;
import resonant.api.IReactorComponent;
import resonantinduction.atomic.AtomicContent;
import resonantinduction.atomic.machine.reactor.TileReactorCell;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantTab;
import resonantinduction.core.Settings;
import universalelectricity.core.transform.vector.Vector3;

import java.util.List;

/**
 * Fissile fuel rod
 */
public class ItemFissileFuel extends ItemRadioactive implements IReactorComponent
{
	public static final int DECAY = 2500;

	/**
	 * Temperature at which the fuel rod will begin to re-enrich itself.
	 */
	public static final int BREEDING_TEMP = 1100;

	/**
	 * The energy in one KG of uranium is: 72PJ, 100TJ in one cell of uranium.
	 */
	public static final long ENERGY = 100000000000L;

	/**
	 * Approximately 20,000,000J per tick. 400 MW.
	 */
	public static final long ENERGY_PER_TICK = ENERGY / 50000;

	public ItemFissileFuel()
	{
		super();
		this.setMaxStackSize(1);
		this.setMaxDamage(DECAY);
		this.setNoRepair();
        this.setUnlocalizedName(Reference.prefix() + "rodBreederFuel");
        this.setTextureName(Reference.prefix() + "rodBreederFuel");
        setCreativeTab(ResonantTab.tab());
	}

	@Override
	public void onReact(ItemStack itemStack, IReactor reactor)
	{
		TileEntity tileEntity = (TileEntity) reactor;
		World worldObj = tileEntity.getWorldObj();
		int reactors = 0;

		for (int i = 0; i < 6; i++)
		{
			Vector3 checkPos = new Vector3(tileEntity).add(ForgeDirection.getOrientation(i));
			TileEntity tile = checkPos.getTileEntity(worldObj);

			// Check that the other reactors not only exist but also are running.
			if (tile instanceof TileReactorCell && ((TileReactorCell) tile).getTemperature() > BREEDING_TEMP)
			{
				reactors++;
			}
		}

		// Only two reactor cells are required to begin the uranium breeding process.
		if (reactors >= 2)
		{
			// Begin the process of re-enriching the uranium rod but not consistently.
			// Note: The center reactor cell only needs to be half of breeding temperature for this to work.
			if (worldObj.rand.nextInt(1000) <= 100 && reactor.getTemperature() > (BREEDING_TEMP / 2))
			{
				// Cells can regain a random amount of health per tick.
				int healAmt = worldObj.rand.nextInt(5);

				// Determine if this is a completely dead cell (creative menu fission rod is like this).
				//System.out.println("[Atomic Science] [Reactor Cell] Breeding " + String.valueOf(healAmt) + " back into fissle rod. " + String.valueOf(itemStack.getItemDamage()) + " / " + String.valueOf(itemStack.getMaxDamage()));
				itemStack.setItemDamage(Math.max(itemStack.getItemDamage() - healAmt, 0));
			}
		}
		else
		{
			reactor.heat(ENERGY_PER_TICK);

			if (reactor.world().getWorldTime() % 20 == 0)
			{
				itemStack.setItemDamage(Math.min(itemStack.getItemDamage() + 1, itemStack.getMaxDamage()));
			}

			// Create toxic waste.
			if (Settings.allowToxicWaste() && worldObj.rand.nextFloat() > 0.5)
			{
				FluidStack fluid = AtomicContent.FLUIDSTACK_TOXIC_WASTE().copy();
				fluid.amount = 1;
				reactor.fill(ForgeDirection.UNKNOWN, fluid, true);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(new ItemStack(item, 1, 0));
		par3List.add(new ItemStack(item, 1, getMaxDamage() - 1));
	}

}
