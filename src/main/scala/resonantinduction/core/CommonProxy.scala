package resonantinduction.core

import java.awt._

import codechicken.multipart.{TMultiPart, TileMultipart}
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import resonant.lib.prefab.AbstractProxy
import resonantinduction.atomic.machine.accelerator.{ContainerAccelerator, TileAccelerator}
import resonantinduction.atomic.machine.boiler.{ContainerNuclearBoiler, TileNuclearBoiler}
import resonantinduction.atomic.machine.centrifuge.{ContainerCentrifuge, TileCentrifuge}
import resonantinduction.atomic.machine.extractor.{ContainerChemicalExtractor, TileChemicalExtractor}
import resonantinduction.atomic.machine.quantum.{ContainerQuantumAssembler, TileQuantumAssembler}
import resonantinduction.atomic.machine.reactor.{ContainerReactorCell, TileReactorCell}
import resonantinduction.electrical.multimeter.{ContainerMultimeter, PartMultimeter}
import universalelectricity.core.transform.vector.Vector3

/**
 * @author Calclavia
 */
class CommonProxy extends AbstractProxy
{
    def getArmorIndex(armor: String): Int =
    {
        return 0
    }

    override def getServerGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef =
    {
        val tileEntity: TileEntity = world.getTileEntity(x, y, z)
        if (tileEntity.isInstanceOf[TileMultipart])
        {
            val part: TMultiPart = (tileEntity.asInstanceOf[TileMultipart]).partMap(id)
            if (part.isInstanceOf[PartMultimeter])
            {
                return new ContainerMultimeter(player.inventory, (part.asInstanceOf[PartMultimeter]))
            }
        }
        else if (tileEntity.isInstanceOf[TileCentrifuge])
        {
            return new ContainerCentrifuge(player.inventory, (tileEntity.asInstanceOf[TileCentrifuge]))
        }
        else if (tileEntity.isInstanceOf[TileChemicalExtractor])
        {
            return new ContainerChemicalExtractor(player.inventory, (tileEntity.asInstanceOf[TileChemicalExtractor]))
        }
        else if (tileEntity.isInstanceOf[TileAccelerator])
        {
            return new ContainerAccelerator(player, (tileEntity.asInstanceOf[TileAccelerator]))
        }
        else if (tileEntity.isInstanceOf[TileQuantumAssembler])
        {
            return new ContainerQuantumAssembler(player.inventory, (tileEntity.asInstanceOf[TileQuantumAssembler]))
        }
        else if (tileEntity.isInstanceOf[TileNuclearBoiler])
        {
            return new ContainerNuclearBoiler(player, (tileEntity.asInstanceOf[TileNuclearBoiler]))
        }
        else if (tileEntity.isInstanceOf[TileReactorCell])
        {
            return new ContainerReactorCell(player, (tileEntity.asInstanceOf[TileReactorCell]))
        }
        return null
    }

    def isPaused: Boolean =
    {
        return false
    }

    def isGraphicsFancy: Boolean =
    {
        return false
    }

    def renderBlockParticle(world: World, x: Double, y: Double, z: Double, velocity: Vector3, blockID: Int, scale: Float)
    {
    }

    def renderBlockParticle(world: World, position: Vector3, velocity: Vector3, blockID: Int, scale: Float)
    {
    }

    def renderBeam(world: World, position: Vector3, hit: Vector3, color: Color, age: Int)
    {
    }

    def renderBeam(world: World, position: Vector3, target: Vector3, red: Float, green: Float, blue: Float, age: Int)
    {
    }


    def renderElectricShock(world: World, start: Vector3, target: Vector3, r: Float, g: Float, b: Float, split: Boolean)
    {
    }

    def renderElectricShock(world: World, start: Vector3, target: Vector3, r: Float, g: Float, b: Float)
    {
        this.renderElectricShock(world, start, target, r, g, b, true)
    }

    def renderElectricShock(world: World, start: Vector3, target: Vector3, color: Color)
    {
        this.renderElectricShock(world, start, target, color.getRed / 255f, color.getGreen / 255f, color.getBlue / 255f)
    }

    def renderElectricShock(world: World, start: Vector3, target: Vector3, color: Color, split: Boolean)
    {
        this.renderElectricShock(world, start, target, color.getRed / 255f, color.getGreen / 255f, color.getBlue / 255f, split)
    }

    def renderElectricShock(world: World, start: Vector3, target: Vector3)
    {
        this.renderElectricShock(world, start, target, true)
    }

    def renderElectricShock(world: World, start: Vector3, target: Vector3, b: Boolean)
    {
        this.renderElectricShock(world, start, target, 0.55f, 0.7f, 1f, b)
    }

    def renderBlockParticle(world: World, position: Vector3, block: Block, side: Int)
    {

    }

    def renderLaser(world: World, start: Vector3, end: Vector3, color: Vector3, energy: Double)
    {

    }

    def renderScorch(world: World, position: Vector3, side: Int)
    {

    }
}