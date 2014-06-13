package resonantinduction.atomic;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import resonant.lib.render.block.BlockRenderingHandler;
import resonantinduction.atomic.machine.accelerator.EntityParticle;
import resonantinduction.atomic.machine.accelerator.GuiAccelerator;
import resonantinduction.atomic.machine.accelerator.RenderParticle;
import resonantinduction.atomic.machine.accelerator.TileAccelerator;
import resonantinduction.atomic.machine.boiler.GuiChemicalExtractor;
import resonantinduction.atomic.machine.boiler.GuiNuclearBoiler;
import resonantinduction.atomic.machine.boiler.RenderNuclearBoiler;
import resonantinduction.atomic.machine.boiler.TileNuclearBoiler;
import resonantinduction.atomic.machine.centrifuge.GuiCentrifuge;
import resonantinduction.atomic.machine.centrifuge.RenderCentrifuge;
import resonantinduction.atomic.machine.centrifuge.TileCentrifuge;
import resonantinduction.atomic.machine.extractor.RenderChemicalExtractor;
import resonantinduction.atomic.machine.extractor.TileChemicalExtractor;
import resonantinduction.atomic.machine.extractor.turbine.RenderElectricTurbine;
import resonantinduction.atomic.machine.extractor.turbine.TileElectricTurbine;
import resonantinduction.atomic.machine.plasma.RenderPlasmaHeater;
import resonantinduction.atomic.machine.plasma.TilePlasmaHeater;
import resonantinduction.atomic.machine.quantum.GuiQuantumAssembler;
import resonantinduction.atomic.machine.quantum.TileQuantumAssembler;
import resonantinduction.atomic.machine.reactor.GuiReactorCell;
import resonantinduction.atomic.machine.reactor.RenderReactorCell;
import resonantinduction.atomic.machine.reactor.TileReactorCell;
import resonantinduction.atomic.machine.thermometer.RenderThermometer;
import resonantinduction.atomic.machine.thermometer.TileThermometer;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

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
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
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
