package com.calclavia.edx.quantum.machine.centrifuge

import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.util.StatCollector
import resonantengine.lib.utility.science.UnitDisplay
import resonantengine.prefab.gui.GuiContainerBase
import resonantengine.prefab.gui.GuiContainerBase.SlotType

class GuiCentrifuge(par1InventoryPlayer: InventoryPlayer, tileEntity: TileCentrifuge) extends GuiContainerBase(new ContainerCentrifuge(par1InventoryPlayer, tileEntity))
{
  /**
   * Draw the foreground layer for the GuiContainer (everything in front of the items)
   */
  override def drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int)
  {
    this.fontRendererObj.drawString("Centrifuge", 60, 6, 4210752)
    var displayText: String = ""
    if (this.tileEntity.timer > 0)
    {
      displayText = "Processing"
    }
    else if (this.tileEntity.nengYong)
    {
      displayText = "Ready"
    }
    else
    {
      displayText = "Idle"
    }
    this.fontRendererObj.drawString("Status: " + displayText, 70, 50, 4210752)
    this.renderUniversalDisplay(8, 112, TileCentrifuge.DIAN * 20, mouseX, mouseY, UnitDisplay.Unit.WATT)
    this.fontRendererObj.drawString("The centrifuge spins", 8, 75, 4210752)
    this.fontRendererObj.drawString("uranium hexafluoride gas into", 8, 85, 4210752)
    this.fontRendererObj.drawString("enriched uranium for fission.", 8, 95, 4210752)
    this.fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, 4210752)
  }

  /**
   * Draw the background layer for the GuiContainer (everything behind the items)
   */
  protected override def drawGuiContainerBackgroundLayer(par1: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(par1, x, y)
    this.drawSlot(80, 25)
    this.drawSlot(100, 25)
    this.drawSlot(130, 25, SlotType.BATTERY)
    this.drawBar(40, 26, this.tileEntity.timer.asInstanceOf[Float] / TileCentrifuge.SHI_JIAN.asInstanceOf[Float])
    this.drawMeter(8, 18, this.tileEntity.gasTank.getFluidAmount.asInstanceOf[Float] / this.tileEntity.gasTank.getCapacity.asInstanceOf[Float], this.tileEntity.gasTank.getFluid)
    this.drawSlot(24, 49, SlotType.GAS)
  }
}