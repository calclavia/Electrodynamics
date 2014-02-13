package resonantinduction.mechanical.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.IInformation;
import calclavia.lib.utility.FluidUtility;

public class ItemPipeGauge extends Item
{
	public ItemPipeGauge(int id)
	{
		super(id);
		this.setHasSubtypes(true);
		this.setMaxStackSize(1);

	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
			ForgeDirection hitSide = ForgeDirection.getOrientation(side);

			if (tileEntity instanceof IInformation)
			{
				List<String> list = new ArrayList<String>();
				((IInformation) tileEntity).getInformation(list);

				if (list.size() > 0)
				{
					for (String output : list)
						player.addChatMessage(output);
					return true;
				}
			}
			if (tileEntity instanceof IFluidHandler)
			{
				FluidTankInfo[] tanks = ((IFluidHandler) tileEntity).getTankInfo(ForgeDirection.getOrientation(side));
				if (tanks != null)
				{
					player.sendChatToPlayer(ChatMessageComponent.createFromText("FluidHandler> Side:" + hitSide.toString() + " Tanks:" + tanks.length));

					for (FluidStack stack : FluidUtility.getFluidList(tanks))
						player.addChatMessage("Fluid: " + stack.amount + "mb of " + stack.getFluid().getName());

					return true;
				}
			}

		}

		return false;
	}
}
