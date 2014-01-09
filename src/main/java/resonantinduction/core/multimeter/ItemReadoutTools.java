package resonantinduction.core.multimeter;

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
import resonantinduction.Reference;
import resonantinduction.core.Settings;
import resonantinduction.lib.interfaces.IReadOut;
import resonantinduction.lib.interfaces.IReadOut.EnumTools;
import resonantinduction.lib.prefab.TileEntityEnergyMachine;
import resonantinduction.mechanics.generator.TileGenerator;
import universalelectricity.api.electricity.IVoltageInput;
import universalelectricity.api.electricity.IVoltageOutput;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyContainer;
import universalelectricity.api.energy.IEnergyNetwork;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import buildcraft.api.power.IPowerReceptor;
import calclavia.lib.prefab.item.ItemBase;
import calclavia.lib.utility.FluidHelper;
import cofh.api.energy.IEnergyStorage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemReadoutTools extends ItemBase
{
    Icon pipeGuage, multiMeter;

    public ItemReadoutTools()
    {
        super(Settings.getNextItemID(), "DMTools", Settings.CONFIGURATION,Reference.PREFIX, CreativeTabs.tabTools);
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);

    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IconRegister iconRegister)
    {
        this.pipeGuage = iconRegister.registerIcon(Reference.PREFIX + "readout.PipeGauge");
        this.multiMeter = iconRegister.registerIcon(Reference.PREFIX + "readout.multi-Meter");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Icon getIconFromDamage(int meta)
    {
        if (meta == 0)
        {
            return pipeGuage;
        }
        if (meta == 1)
        {
            return multiMeter;
        }
        return null;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack)
    {
        int meta = itemStack.getItemDamage();
        if (meta == 0)
        {
            return "item." + "PipeGauge";
        }
        else if (meta == 1)
        {
            return "item." + "MultiMeter";
        }
        return "item." + this.getUnlocalizedName() + "." + meta;
    }

    @Override
    public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        par3List.add(new ItemStack(this, 1, 0));
        par3List.add(new ItemStack(this, 1, 1));
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
                // TODO add shift click support to bring up a easier to read GUI or link to the
                // block and add an on screen gui so the player can toy with a design and be updated
                // TODO add toggle support to only get one energy system(UE,TE,BC,IC2) readings
                // rather than all or first type read
                if (tool == EnumTools.MULTI_METER)
                {
                    if (tileEntity instanceof IVoltageInput && ((IVoltageInput) tileEntity).getVoltageInput(hitSide) > 0)
                    {
                        player.sendChatToPlayer(ChatMessageComponent.createFromText("V~In:" + ((IVoltageInput) tileEntity).getVoltageInput(hitSide)));
                    }
                    if (tileEntity instanceof IVoltageOutput && ((IVoltageOutput) tileEntity).getVoltageOutput(hitSide) > 0)
                    {
                        player.sendChatToPlayer(ChatMessageComponent.createFromText("V~Out:" + ((IVoltageOutput) tileEntity).getVoltageOutput(hitSide)));
                    }
                    if (tileEntity instanceof IConductor)
                    {
                        IEnergyNetwork network = ((IConductor) tileEntity).getNetwork();
                        if (network != null)
                        {
                            player.sendChatToPlayer(ChatMessageComponent.createFromText("Network:" + network.toString()));
                            player.sendChatToPlayer(ChatMessageComponent.createFromText("E~In:" + UnitDisplay.getDisplay(network.getRequest(), Unit.JOULES, 2, false)));
                            player.sendChatToPlayer(ChatMessageComponent.createFromText("E~Out:" + UnitDisplay.getDisplay(network.getLastBuffer(), Unit.JOULES, 2, false)));
                            player.sendChatToPlayer(ChatMessageComponent.createFromText("R~Ohm:" + UnitDisplay.getDisplay(network.getResistance(), Unit.RESISTANCE, 2, false)));
                        }
                    }
                    else if (tileEntity instanceof IEnergyContainer)
                    {
                        if (((IEnergyContainer) tileEntity).getEnergyCapacity(hitSide) > 0)
                        {
                            player.sendChatToPlayer(ChatMessageComponent.createFromText("E~:" + UnitDisplay.getDisplay(((IEnergyContainer) tileEntity).getEnergy(hitSide), Unit.JOULES, 2, false) + "/" + UnitDisplay.getDisplay(((IEnergyContainer) tileEntity).getEnergyCapacity(hitSide), Unit.JOULES, 2, false)));
                        }
                        if (tileEntity instanceof TileEntityEnergyMachine)
                        {
                            if (tileEntity instanceof TileGenerator)
                            {
                                player.sendChatToPlayer(ChatMessageComponent.createFromText("E~Out:" + UnitDisplay.getDisplay(((TileEntityEnergyMachine) tileEntity).getJoulesPerTick(), Unit.JOULES, 2, true) + "/tick"));
                            }
                            else
                            {
                                player.sendChatToPlayer(ChatMessageComponent.createFromText("E~In:" + UnitDisplay.getDisplay(((TileEntityEnergyMachine) tileEntity).getJoulesPerTick(), Unit.JOULES, 2, true) + "/tick"));

                            }
                        }
                    }
                    else if (tileEntity instanceof IEnergyStorage && ((IEnergyStorage) tileEntity).getMaxEnergyStored() > 0)
                    {
                        player.sendChatToPlayer(ChatMessageComponent.createFromText("RF~:" + UnitDisplay.getDisplay(((IEnergyStorage) tileEntity).getEnergyStored(), Unit.REDFLUX, 2, true) + "/" + UnitDisplay.getDisplay(((IEnergyStorage) tileEntity).getMaxEnergyStored(), Unit.REDFLUX, 2, true)));
                    }
                    else if (tileEntity instanceof IPowerReceptor && ((IPowerReceptor) tileEntity).getPowerReceiver(hitSide) != null && ((IPowerReceptor) tileEntity).getPowerReceiver(hitSide).getMaxEnergyStored() > 0)
                    {
                        // TODO recode for input and output
                        if (((IPowerReceptor) tileEntity).getPowerReceiver(hitSide) != null)
                        {
                            player.sendChatToPlayer(ChatMessageComponent.createFromText("Mj~:" + UnitDisplay.getDisplay(((IPowerReceptor) tileEntity).getPowerReceiver(hitSide).getEnergyStored(), Unit.MINECRAFT_JOULES, 2, true) + "/" + UnitDisplay.getDisplay(((IPowerReceptor) tileEntity).getPowerReceiver(hitSide).getMaxEnergyStored(), Unit.MINECRAFT_JOULES, 2, true)));
                        }
                    }
                    // TODO add IC2 support
                }
            }

        }

        return false;
    }
}
