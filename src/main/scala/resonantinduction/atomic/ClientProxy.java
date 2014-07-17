package resonantinduction.atomic;

import resonantinduction.atomic.machine.accelerator.EntityParticle;
import resonantinduction.atomic.machine.accelerator.RenderParticle;
import atomic.machine.boiler.GuiNuclearBoiler;
import resonantinduction.atomic.machine.boiler.RenderNuclearBoiler;
import atomic.machine.boiler.TileNuclearBoiler;
import resonantinduction.atomic.machine.centrifuge.GuiCentrifuge;
import atomic.machine.centrifuge.RenderCentrifuge;
import resonantinduction.atomic.machine.extractor.RenderChemicalExtractor;
import atomic.machine.extractor.TileChemicalExtractor;
import resonantinduction.atomic.machine.extractor.turbine.TileElectricTurbine;
import atomic.machine.plasma.RenderPlasmaHeater;
import resonantinduction.atomic.machine.quantum.TileQuantumAssembler;
import atomic.machine.reactor.GuiReactorCell;
import resonantinduction.atomic.machine.reactor.RenderReactorCell;
import resonantinduction.atomic.machine.thermometer.RenderThermometer;
import resonantinduction.atomic.machine.thermometer.TileThermometer;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import resonant.lib.render.block.BlockRenderingHandler;
import atomic.machine.accelerator.GuiAccelerator;
import atomic.machine.accelerator.TileAccelerator;
import atomic.machine.boiler.GuiChemicalExtractor;
import atomic.machine.centrifuge.TileCentrifuge;
import atomic.machine.extractor.turbine.RenderElectricTurbine;
import atomic.machine.plasma.TilePlasmaHeater;
import atomic.machine.quantum.GuiQuantumAssembler;
import atomic.machine.reactor.TileReactorCell;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import resonantinduction.atomic.machine.accelerator.EntityParticle;
import resonantinduction.atomic.machine.accelerator.RenderParticle;
import resonantinduction.atomic.machine.boiler.RenderNuclearBoiler;
import resonantinduction.atomic.machine.centrifuge.GuiCentrifuge;
import resonantinduction.atomic.machine.extractor.RenderChemicalExtractor;
import resonantinduction.atomic.machine.extractor.turbine.TileElectricTurbine;
import resonantinduction.atomic.machine.quantum.TileQuantumAssembler;
import resonantinduction.atomic.machine.reactor.RenderReactorCell;
import resonantinduction.atomic.machine.thermometer.RenderThermometer;
import resonantinduction.atomic.machine.thermometer.TileThermometer;

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        MinecraftForge.EVENT_BUS.register(SoundHandler.INSTANCE);
        RenderingRegistry.registerBlockHandler(new BlockRenderingHandler());
    }

    @Override
    public int getArmorIndex(String armor)
    {
        return RenderingRegistry.addNewArmourRendererPrefix(armor);
    }

    @Override
    public void init()
    {
        super.init();
        ClientRegistry.bindTileEntitySpecialRenderer(TileCentrifuge.class, new RenderCentrifuge());
        ClientRegistry.bindTileEntitySpecialRenderer(TilePlasmaHeater.class, new RenderPlasmaHeater());
        ClientRegistry.bindTileEntitySpecialRenderer(TileNuclearBoiler.class, new RenderNuclearBoiler());
        ClientRegistry.bindTileEntitySpecialRenderer(TileElectricTurbine.class, new RenderElectricTurbine());
        ClientRegistry.bindTileEntitySpecialRenderer(TileThermometer.class, new RenderThermometer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileChemicalExtractor.class, new RenderChemicalExtractor());
        ClientRegistry.bindTileEntitySpecialRenderer(TileReactorCell.class, new RenderReactorCell());

        RenderingRegistry.registerEntityRenderingHandler(EntityParticle.class, new RenderParticle());
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        Block block = Block.blocksList[world.getBlockId(x, y, z)];

        if (tileEntity instanceof TileCentrifuge)
        {
            return new GuiCentrifuge(player.inventory, ((TileCentrifuge) tileEntity));
        }
        else if (tileEntity instanceof TileChemicalExtractor)
        {
            return new GuiChemicalExtractor(player.inventory, ((TileChemicalExtractor) tileEntity));
        }
        else if (tileEntity instanceof TileAccelerator)
        {
            return new GuiAccelerator(player.inventory, ((TileAccelerator) tileEntity));
        }
        else if (tileEntity instanceof TileQuantumAssembler)
        {
            return new GuiQuantumAssembler(player.inventory, ((TileQuantumAssembler) tileEntity));
        }
        else if (tileEntity instanceof TileNuclearBoiler)
        {
            return new GuiNuclearBoiler(player.inventory, ((TileNuclearBoiler) tileEntity));
        }
        else if (tileEntity instanceof TileReactorCell)
        {
            return new GuiReactorCell(player.inventory, (TileReactorCell) tileEntity);
        }

        return null;
    }

    @Override
    public boolean isFancyGraphics()
    {
        return Minecraft.getMinecraft().gameSettings.fancyGraphics;
    }

}
