package resonantinduction.atomic

import resonantinduction.atomic.machine.accelerator.EntityParticle
import resonantinduction.atomic.machine.accelerator.GuiAccelerator
import resonantinduction.atomic.machine.accelerator.RenderParticle
import resonantinduction.atomic.machine.accelerator.TileAccelerator
import resonantinduction.atomic.machine.boiler.GuiNuclearBoiler
import resonantinduction.atomic.machine.boiler.RenderNuclearBoiler
import resonantinduction.atomic.machine.boiler.TileNuclearBoiler
import resonantinduction.atomic.machine.centrifuge.GuiCentrifuge
import resonantinduction.atomic.machine.centrifuge.RenderCentrifuge
import resonantinduction.atomic.machine.centrifuge.TileCentrifuge
import resonantinduction.atomic.machine.extractor.GuiChemicalExtractor
import resonantinduction.atomic.machine.extractor.RenderChemicalExtractor
import resonantinduction.atomic.machine.extractor.TileChemicalExtractor
import resonantinduction.atomic.machine.plasma.RenderPlasmaHeater
import resonantinduction.atomic.machine.plasma.TilePlasmaHeater
import resonantinduction.atomic.machine.quantum.{RenderQuantumAssembler, GuiQuantumAssembler, TileQuantumAssembler}
import resonantinduction.atomic.machine.reactor.GuiReactorCell
import resonantinduction.atomic.machine.reactor.RenderReactorCell
import resonantinduction.atomic.machine.reactor.TileReactorCell
import resonantinduction.atomic.machine.thermometer.RenderThermometer
import resonantinduction.atomic.machine.thermometer.TileThermometer
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import cpw.mods.fml.client.registry.ClientRegistry
import cpw.mods.fml.client.registry.RenderingRegistry
import resonantinduction.mechanical.turbine.{TileElectricTurbine, RenderElectricTurbine}

class ClientProxy extends CommonProxy {
  override def preInit {
    //RenderingRegistry.registerBlockHandler(BlockRenderingHandler)
  }

  override def getArmorIndex(armor: String): Int = {
    return RenderingRegistry.addNewArmourRendererPrefix(armor)
  }

  override def init {
    super.init
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileCentrifuge], new RenderCentrifuge)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TilePlasmaHeater], new RenderPlasmaHeater)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileNuclearBoiler], new RenderNuclearBoiler)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileElectricTurbine], new RenderElectricTurbine)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileThermometer], new RenderThermometer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileChemicalExtractor], new RenderChemicalExtractor)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileReactorCell], new RenderReactorCell)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileQuantumAssembler], new RenderQuantumAssembler)
    RenderingRegistry.registerEntityRenderingHandler(classOf[EntityParticle], new RenderParticle)
  }

  override def getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = {
    val tileEntity: TileEntity = world.getTileEntity(x, y, z)
    val block: Block = world.getBlock(x, y, z)
    if (tileEntity.isInstanceOf[TileCentrifuge]) {
      return new GuiCentrifuge(player.inventory, (tileEntity.asInstanceOf[TileCentrifuge]))
    }
    else if (tileEntity.isInstanceOf[TileChemicalExtractor]) {
      return new GuiChemicalExtractor(player.inventory, (tileEntity.asInstanceOf[TileChemicalExtractor]))
    }
    else if (tileEntity.isInstanceOf[TileAccelerator]) {
      return new GuiAccelerator(player, (tileEntity.asInstanceOf[TileAccelerator]))
    }
    else if (tileEntity.isInstanceOf[TileQuantumAssembler]) {
      return new GuiQuantumAssembler(player.inventory, (tileEntity.asInstanceOf[TileQuantumAssembler]))
    }
    else if (tileEntity.isInstanceOf[TileNuclearBoiler]) {
      return new GuiNuclearBoiler(player, (tileEntity.asInstanceOf[TileNuclearBoiler]))
    }
    else if (tileEntity.isInstanceOf[TileReactorCell]) {
      return new GuiReactorCell(player.inventory, tileEntity.asInstanceOf[TileReactorCell])
    }
    return null
  }

  override def isFancyGraphics: Boolean = {
    return Minecraft.getMinecraft.gameSettings.fancyGraphics
  }
}