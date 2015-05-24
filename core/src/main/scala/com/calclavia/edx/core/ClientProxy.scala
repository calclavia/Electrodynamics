package com.calclavia.edx.core

import java.awt.Color

import codechicken.multipart.TileMultipart
import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.client.registry.{ClientRegistry, RenderingRegistry}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.basic.process.smelting.firebox.{RenderHotPlate, TileHotPlate}
import edx.basic.process.smelting.{RenderCastingMold, TileCastingMold}
import edx.core.fx.FXElectricBolt
import edx.electrical.ElectricalContent
import edx.electrical.circuit.component.laser.fx.{EntityBlockParticleFX, EntityLaserFX, EntityScorchFX}
import edx.electrical.circuit.component.tesla.{RenderTesla, TileTesla}
import edx.electrical.circuit.transformer.RenderTransformer
import edx.electrical.multimeter.{GuiMultimeter, PartMultimeter, RenderMultimeter}
import edx.mechanical.MechanicalContent
import edx.mechanical.fluid.pipe.RenderPipe
import edx.mechanical.mech.gear.RenderGear
import edx.mechanical.mech.gearshaft.RenderGearShaft
import edx.mechanical.mech.process.crusher.{RenderMechanicalPiston, TileMechanicalPiston}
import edx.mechanical.mech.turbine._
import edx.quantum.gate.RenderQuantumGlyph
import edx.quantum.machine.accelerator.{EntityParticle, RenderParticle}
import edx.quantum.machine.boiler.{GuiNuclearBoiler, RenderNuclearBoiler, TileNuclearBoiler}
import edx.quantum.machine.centrifuge.{GuiCentrifuge, RenderCentrifuge, TileCentrifuge}
import edx.quantum.machine.extractor.{GuiChemicalExtractor, RenderChemicalExtractor, TileChemicalExtractor}
import edx.quantum.machine.quantum.{RenderQuantumAssembler, TileQuantumAssembler}
import edx.quantum.machine.thermometer.{RenderThermometer, TileThermometer}
import edx.quantum.reactor.{GuiReactorCell, TileReactorCell}
import net.minecraft.block.Block
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.particle.{EntityDiggingFX, EntityFX}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.world.World
import resonantengine.lib.render.wrapper.ItemRenderHandler
import resonantengine.lib.transform.vector.Vector3

/** @author Calclavia */
@SideOnly(Side.CLIENT) class ClientProxy extends CommonProxy
{
  override def getArmorIndex(armor: String): Int =
  {
    return RenderingRegistry.addNewArmourRendererPrefix(armor)
  }

  override def init()
  {
    //Basic

    //Mechanical
    ItemRenderHandler.register(MechanicalContent.itemGear, RenderGear)
    ItemRenderHandler.register(MechanicalContent.itemGearShaft, RenderGearShaft)
    ItemRenderHandler.register(MechanicalContent.itemPipe, RenderPipe)
    ItemRenderHandler.register(Item.getItemFromBlock(MechanicalContent.blockWaterTurbine), new RenderWaterTurbine)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileMechanicalPiston], new RenderMechanicalPiston)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileWaterTurbine], new RenderWaterTurbine)

    //Electrical
    ItemRenderHandler.register(ElectricalContent.itemTransformer, RenderTransformer)
    ItemRenderHandler.register(ElectricalContent.itemMultimeter, RenderMultimeter)
    ItemRenderHandler.register(ElectricalContent.itemQuantumGlyph, RenderQuantumGlyph)

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileTesla], new RenderTesla)

    //Quantum
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileCentrifuge], new RenderCentrifuge)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileNuclearBoiler], new RenderNuclearBoiler)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileThermometer], new RenderThermometer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileChemicalExtractor], new RenderChemicalExtractor)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileQuantumAssembler], new RenderQuantumAssembler)
    RenderingRegistry.registerEntityRenderingHandler(classOf[EntityParticle], new RenderParticle)

    //Archaic
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileCastingMold], new RenderCastingMold)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileHotPlate], new RenderHotPlate)
  }

  override def getClientGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef =
  {
    world.getTileEntity(x, y, z) match
    {
      case tile: TileMultipart =>
        val part = tile.partMap(id)
        if (part.isInstanceOf[PartMultimeter])
          new GuiMultimeter(player.inventory, part.asInstanceOf[PartMultimeter])
        null
      case tile: TileCentrifuge =>
        new GuiCentrifuge(player.inventory, tile)
      case tile: TileChemicalExtractor =>
        new GuiChemicalExtractor(player.inventory, tile)
      case tile: TileNuclearBoiler =>
        new GuiNuclearBoiler(player, tile)
      case tile: TileReactorCell =>
        new GuiReactorCell(player.inventory, tile)
      case _ => null
    }
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
      FMLClientHandler.instance.getClient.effectRenderer.addEffect(new FXElectricBolt(world, start, target, split).setColor(r, g, b))
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