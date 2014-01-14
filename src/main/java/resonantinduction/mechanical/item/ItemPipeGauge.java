package resonantinduction.mechanical.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.IReadOut;
import resonantinduction.api.IReadOut.EnumTools;
import resonantinduction.core.Reference;
import resonantinduction.core.Settings;
import resonantinduction.core.prefab.item.ItemBase;
import calclavia.lib.utility.FluidHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPipeGauge extends ItemBase
{
    Icon pipeGuage;

    public ItemPipeGauge()
    {
        super("PipeGuage");
        this.setHasSubtypes(true);
        this.setCreativeTab(CreativeTabs.tabTools);
        this.setMaxStackSize(1);
        this.setTextureName(Reference.PREFIX + "readout.PipeGauge");

    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote)
        {
            TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
            EnumTools tool = EnumTools.get(itemStack.getItemDamage());

            if (tool != null)
            {
                ForgeDirection hitSide = ForgeDirection.getOrientation(side);
                if (tileEntity instanceof IReadOut)
                {
                    String output = ((IReadOut) tileEntity).getMeterReading(player, hitSide, tool);
                    if (output != null && !output.isEmpty())
                    {
                        if (output.length() > 100)
                        {
                            output = output.substring(0, 100);
                        }
                        output.trim();
                        player.sendChatToPlayer(ChatMessageComponent.createFromText("ReadOut> " + output));
                        return true;
                    }
                }
                if (tool == EnumTools.PIPE_GUAGE)
                {
                    if (tileEntity instanceof IFluidHandler)
                    {
                        FluidTankInfo[] tanks = ((IFluidHandler) tileEntity).getTankInfo(ForgeDirection.getOrientation(side));
                        if (tanks != null)
                        {
                            player.sendChatToPlayer(ChatMessageComponent.createFromText("FluidHandler> Side:" + hitSide.toString() + " Tanks:" + tanks.length));
                            for (FluidStack stack : FluidHelper.getFluidList(tanks))
                            {
                                player.sendChatToPlayer(ChatMessageComponent.createFromText("Fluid>" + stack.amount + "mb of " + stack.getFluid().getName()));
                            }
                            return true;
                        }
                    }
                }
            }

        }

        return false;
    }
}
