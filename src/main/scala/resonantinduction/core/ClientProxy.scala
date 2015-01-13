package resonantinduction.core

import java.awt.Color

import codechicken.multipart.{TMultiPart, TileMultipart}
import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.client.registry.{ClientRegistry, RenderingRegistry}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.Block
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.particle.{EntityDiggingFX, EntityFX}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import resonant.lib.render.fx.FXElectricBolt2
import resonant.lib.render.wrapper.ItemRenderHandler
import resonant.lib.transform.vector.Vector3
import resonantinduction.archaic.ArchaicContent
import resonantinduction.archaic.firebox.{RenderHotPlate, TileHotPlate}
import resonantinduction.archaic.process.mixing.TileGlassJar
import resonantinduction.archaic.process.{RenderCastingMold, RenderMillstone, TileCastingMold, TileMillstone}
import resonantinduction.atomic.gate.RenderQuantumGlyph
import resonantinduction.atomic.machine.accelerator.{EntityParticle, GuiAccelerator, RenderParticle, TileAccelerator}
import resonantinduction.atomic.machine.boiler.{GuiNuclearBoiler, RenderNuclearBoiler, TileNuclearBoiler}
import resonantinduction.atomic.machine.centrifuge.{GuiCentrifuge, RenderCentrifuge, TileCentrifuge}
import resonantinduction.atomic.machine.extractor.{GuiChemicalExtractor, RenderChemicalExtractor, TileChemicalExtractor}
import resonantinduction.atomic.machine.plasma.{RenderPlasmaHeater, TilePlasmaHeater}
import resonantinduction.atomic.machine.quantum.{GuiQuantumAssembler, RenderQuantumAssembler, TileQuantumAssembler}
import resonantinduction.atomic.machine.reactor.{GuiReactorCell, RenderReactorCell, TileReactorCell}
import resonantinduction.atomic.machine.thermometer.{RenderThermometer, TileThermometer}
import resonantinduction.electrical.ElectricalContent
import resonantinduction.electrical.laser.fx.{EntityBlockParticleFX, EntityLaserFX, EntityScorchFX}
import resonantinduction.electrical.multimeter.{GuiMultimeter, PartMultimeter, RenderMultimeter}
import resonantinduction.electrical.tesla.{RenderTesla, TileTesla}
import resonantinduction.electrical.transformer.RenderTransformer
import resonantinduction.mechanical.MechanicalContent
import resonantinduction.mechanical.fluid.pipe.RenderPipe
import resonantinduction.mechanical.mech.gear.RenderGear
import resonantinduction.mechanical.mech.gearshaft.RenderGearShaft
import resonantinduction.mechanical.mech.process.crusher.{RenderMechanicalPiston, TileMechanicalPiston}
import resonantinduction.mechanical.mech.turbine._

/** @author Calclavia */
@SideOnly(Side.CLIENT) class ClientProxy extends CommonProxy
{
  override def getArmorIndex(armor: String): Int =
  {
    return RenderingRegistry.addNewArmourRendererPrefix(armor)
  }

  override def init()
  {
    //Mech content TODO no-load if mech content is not loaded
    ItemRenderHandler.register(Item.getItemFromBlock(ArchaicContent.blockJar), new TileGlassJar)
    ItemRenderHandler.register(MechanicalContent.itemGear, RenderGear)
    ItemRenderHandler.register(MechanicalContent.itemGearShaft, RenderGearShaft)
    ItemRenderHandler.register(MechanicalContent.itemPipe, RenderPipe)
    ItemRenderHandler.register(Item.getItemFromBlock(MechanicalContent.blockWaterTurbine), new RenderWaterTurbine)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileMechanicalPiston], new RenderMechanicalPiston)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileWaterTurbine], new RenderWaterTurbine)

    //Electrical content
    ItemRenderHandler.register(ElectricalContent.itemTransformer, RenderTransformer)
    ItemRenderHandler.register(ElectricalContent.itemMultimeter, RenderMultimeter)
    ItemRenderHandler.register(ElectricalContent.itemQuantumGlyph, RenderQuantumGlyph)

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileTesla], new RenderTesla)

    //Atomic content
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileCentrifuge], new RenderCentrifuge)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TilePlasmaHeater], new RenderPlasmaHeater)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileNuclearBoiler], new RenderNuclearBoiler)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileThermometer], new RenderThermometer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileChemicalExtractor], new RenderChemicalExtractor)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileReactorCell], new RenderReactorCell)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileQuantumAssembler], new RenderQuantumAssembler)
    RenderingRegistry.registerEntityRenderingHandler(classOf[EntityParticle], new RenderParticle)

    //Archaic
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileCastingMold], new RenderCastingMold)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileMillstone], new RenderMillstone)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileHotPlate], new RenderHotPlate)
  }

  override def getClientGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef =
  {
    val tileEntity: TileEntity = world.getTileEntity(x, y, z)
    if (tileEntity.isInstanceOf[TileMultipart])
    {
      val part: TMultiPart = (tileEntity.asInstanceOf[TileMultipart]).partMap(id)
      if (part.isInstanceOf[PartMultimeter])
      {
        return new GuiMultimeter(player.inventory, part.asInstanceOf[PartMultimeter])
      }
    }
    else if (tileEntity.isInstanceOf[TileCentrifuge])
    {
      return new GuiCentrifuge(player.inventory, (tileEntity.asInstanceOf[TileCentrifuge]))
    }
    else if (tileEntity.isInstanceOf[TileChemicalExtractor])
    {
      return new GuiChemicalExtractor(player.inventory, (tileEntity.asInstanceOf[TileChemicalExtractor]))
    }
    else if (tileEntity.isInstanceOf[TileAccelerator])
    {
      return new GuiAccelerator(player, (tileEntity.asInstanceOf[TileAccelerator]))
    }
    else if (tileEntity.isInstanceOf[TileQuantumAssembler])
    {
      return new GuiQuantumAssembler(player.inventory, (tileEntity.asInstanceOf[TileQuantumAssembler]))
    }
    else if (tileEntity.isInstanceOf[TileNuclearBoiler])
    {
      return new GuiNuclearBoiler(player, (tileEntity.asInstanceOf[TileNuclearBoiler]))
    }
    else if (tileEntity.isInstanceOf[TileReactorCell])
    {
      return new GuiReactorCell(player.inventory, tileEntity.asInstanceOf[TileReactorCell])
    }
    return null
  }

  override def isPaused: Boolean =
  {
    if (FMLClientHandler.instance.getClient.isSingleplayer && !FMLClientHandler.instance.getClient.getIntegratedServer.getPublic)
    {
      val screen: GuiScreen = FMLClientHandler.instance.getClient.currentScreen
      if (screen != null)
      {
        if (screen.doesGuiPauseGame)
        {
          return true
        }
      }
    }
    return false
  }

  override def isGraphicsFancy: Boolean =
  {
    return FMLClientHandler.instance.getClient.gameSettings.fancyGraphics
  }

  override def renderBlockParticle(world: World, position: Vector3, velocity: Vector3, blockID: Int, scale: Float)
  {
    this.renderBlockParticle(world, position.x, position.y, position.z, velocity, blockID, scale)
  }

  def renderBlockParticle(world: World, x: Double, y: Double, z: Double, velocity: Vector3, block: Block, scale: Float)
  {
    val fx: EntityFX = new EntityDiggingFX(world, x, y, z, velocity.x, velocity.y, velocity.z, block, 0, 0)
    fx.multipleParticleScaleBy(scale)
    fx.noClip = true
    FMLClientHandler.instance.getClient.effectRenderer.addEffect(fx)
  }

  override def renderBeam(world: World, position: Vector3, hit: Vector3, color: Color, age: Int)
  {
    renderBeam(world, position, hit, color.getRed, color.getGreen, color.getBlue, age)
  }

  override def renderBeam(world: World, position: Vector3, target: Vector3, red: Float, green: Float, blue: Float, age: Int)
  {
  }

  override def renderElectricShock(world: World, start: Vector3, target: Vector3, r: Float, g: Float, b: Float, split: Boolean)
  {
    if (world.isRemote)
    {
      FMLClientHandler.instance.getClient.effectRenderer.addEffect(new FXElectricBolt2(world, start, target, split).setColor(r, g, b))
    }
  }

  override def renderScorch(world: World, position: Vector3, side: Int)
  {
    if (FMLClientHandler.instance.getClient.gameSettings.particleSetting != 2)
      FMLClientHandler.instance().getClient.effectRenderer.addEffect(new EntityScorchFX(world, position, side))
  }

  override def renderBlockParticle(world: World, position: Vector3, block: Block, side: Int)
  {
    if (FMLClientHandler.instance.getClient.gameSettings.particleSetting != 2)
      FMLClientHandler.instance.getClient.effectRenderer.addEffect(new EntityBlockParticleFX(world, position.x, position.y, position.z, 0, 0, 0, block, side))
  }

  override def renderLaser(world: World, start: Vector3, end: Vector3, color: Vector3, enegy: Double)
  {
    FMLClientHandler.instance().getClient.effectRenderer.addEffect(new EntityLaserFX(world, start, end, color, enegy))
  }
}