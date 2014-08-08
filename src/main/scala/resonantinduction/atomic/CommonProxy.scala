package resonantinduction.atomic

import cpw.mods.fml.common.network.IGuiHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import resonantinduction.atomic.machine.accelerator.ContainerAccelerator
import resonantinduction.atomic.machine.accelerator.TileAccelerator
import resonantinduction.atomic.machine.boiler.TileNuclearBoiler
import resonantinduction.atomic.machine.centrifuge.ContainerCentrifuge
import resonantinduction.atomic.machine.centrifuge.TileCentrifuge
import resonantinduction.atomic.machine.extractor.ContainerChemicalExtractor
import resonantinduction.atomic.machine.boiler.ContainerNuclearBoiler
import resonantinduction.atomic.machine.extractor.TileChemicalExtractor
import resonantinduction.atomic.machine.quantum.ContainerQuantumAssembler
import resonantinduction.atomic.machine.quantum.TileQuantumAssembler
import resonantinduction.atomic.machine.reactor.ContainerReactorCell
import resonantinduction.atomic.machine.reactor.TileReactorCell

class CommonProxy extends IGuiHandler {
  def preInit {
  }

  def init {
  }

  def postInit {
  }

  def getArmorIndex(armor: String): Int = {
    return 0
  }

  def getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = {
    return null
  }

  def getServerGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = {
    val tileEntity: TileEntity = world.getTileEntity(x, y, z)
    if (tileEntity.isInstanceOf[TileCentrifuge]) {
      return new ContainerCentrifuge(player.inventory, (tileEntity.asInstanceOf[TileCentrifuge]))
    }
    else if (tileEntity.isInstanceOf[TileChemicalExtractor]) {
      return new ContainerChemicalExtractor(player.inventory, (tileEntity.asInstanceOf[TileChemicalExtractor]))
    }
    else if (tileEntity.isInstanceOf[TileAccelerator]) {
      return new ContainerAccelerator(player, (tileEntity.asInstanceOf[TileAccelerator]))
    }
    else if (tileEntity.isInstanceOf[TileQuantumAssembler]) {
      return new ContainerQuantumAssembler(player.inventory, (tileEntity.asInstanceOf[TileQuantumAssembler]))
    }
    else if (tileEntity.isInstanceOf[TileNuclearBoiler]) {
      return new ContainerNuclearBoiler(player, (tileEntity.asInstanceOf[TileNuclearBoiler]))
    }
    else if (tileEntity.isInstanceOf[TileReactorCell]) {
      return new ContainerReactorCell(player, (tileEntity.asInstanceOf[TileReactorCell]))
    }
    return null
  }

  def isFancyGraphics: Boolean = {
    return false
  }
}