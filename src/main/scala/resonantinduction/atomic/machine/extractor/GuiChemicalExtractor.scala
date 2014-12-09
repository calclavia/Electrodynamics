package resonantinduction.atomic.machine.extractor

import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.util.StatCollector
import resonant.lib.prefab.gui.GuiContainerBase
import resonant.lib.prefab.gui.GuiContainerBase.SlotType
import resonant.lib.utility.science.UnitDisplay

class GuiChemicalExtractor(par1InventoryPlayer: InventoryPlayer, tileEntity: TileChemicalExtractor) extends GuiContainerBase(new ContainerChemicalExtractor(par1InventoryPlayer, tileEntity))
{
    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    override def drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int)
    {
        this.fontRendererObj.drawString("Chemical Extractor", 45, 6, 4210752)
        this.renderUniversalDisplay(8, 112, TileChemicalExtractor.ENERGY * 20, mouseX, mouseY, UnitDisplay.Unit.WATT)
        this.renderUniversalDisplay(100, 112, this.tileEntity.getVoltage, mouseX, mouseY, UnitDisplay.Unit.VOLTAGE)
        this.fontRendererObj.drawString("The extractor can extract", 8, 75, 4210752)
        this.fontRendererObj.drawString("uranium, deuterium and tritium.", 8, 85, 4210752)
        this.fontRendererObj.drawString("Place them in the input slot.", 8, 95, 4210752)
        this.fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, 4210752)
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    protected override def drawGuiContainerBackgroundLayer(par1: Float, x: Int, y: Int)
    {
        super.drawGuiContainerBackgroundLayer(par1, x, y)
        drawSlot(79, 49, SlotType.BATTERY)
        drawSlot(52, 24)
        drawSlot(106, 24)
        drawBar(75, 24, tileEntity.time.asInstanceOf[Float] / TileChemicalExtractor.TICK_TIME.asInstanceOf[Float])
        drawMeter(8, 18, tileEntity.inputTank.getFluidAmount.asInstanceOf[Float] / tileEntity.inputTank.getCapacity.asInstanceOf[Float], tileEntity.inputTank.getFluid)
        drawSlot(24, 18, SlotType.LIQUID)
        drawSlot(24, 49, SlotType.LIQUID)
        drawMeter(154, 18, tileEntity.outputTank.getFluidAmount.asInstanceOf[Float] / tileEntity.outputTank.getCapacity.asInstanceOf[Float], tileEntity.outputTank.getFluid)
        drawSlot(134, 18, SlotType.LIQUID)
        drawSlot(134, 49, SlotType.LIQUID)
    }
}