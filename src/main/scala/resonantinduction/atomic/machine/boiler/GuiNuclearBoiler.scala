package resonantinduction.atomic.machine.boiler

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.StatCollector
import resonant.lib.prefab.gui.GuiContainerBase
import resonant.lib.prefab.gui.GuiContainerBase.SlotType
import resonant.lib.utility.science.UnitDisplay

class GuiNuclearBoiler(player: EntityPlayer, tileEntity: TileNuclearBoiler) extends GuiContainerBase(new ContainerNuclearBoiler(player, tileEntity))
{

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    override def drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int)
    {
        this.fontRendererObj.drawString("Boiler", 52, 6, 4210752)
        this.renderUniversalDisplay(8, 112, TileNuclearBoiler.DIAN * 20, mouseX, mouseY, UnitDisplay.Unit.WATT)
        //this.renderUniversalDisplay(110, 112, tileEntity.getVoltage, mouseX, mouseY, UnitDisplay.Unit.VOLTAGE)
        this.fontRendererObj.drawString("The nuclear boiler can boil", 8, 75, 4210752)
        this.fontRendererObj.drawString("yellow cake into uranium", 8, 85, 4210752)
        this.fontRendererObj.drawString("hexafluoride gas to be refined.", 8, 95, 4210752)
        this.fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, 4210752)

        //if (this.isPointInRegion(8, 18, this.meterWidth, this.meterHeight, mouseX, mouseY) && this.tileEntity.waterTank.getFluid() != null)
        //{
        //    this.drawTooltip(mouseX - this.guiLeft, mouseY - this.guiTop + 10, this.tileEntity.waterTank.getFluid().getFluid().getLocalizedName(), this.tileEntity.waterTank.getFluid().amount + " L");
        //}
        //else if (this.isPointInRegion(155, 18, this.meterWidth, this.meterHeight, mouseX, mouseY) && this.tileEntity.gasTank.getFluid() != null)
        //{
        //    this.drawTooltip(mouseX - this.guiLeft, mouseY - this.guiTop + 10, this.tileEntity.gasTank.getFluid().getFluid().getLocalizedName(), this.tileEntity.gasTank.getFluid().amount + " L");
        //}
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    protected override def drawGuiContainerBackgroundLayer(par1: Float, x: Int, y: Int)
    {
        super.drawGuiContainerBackgroundLayer(par1, x, y)
        this.drawSlot(55, 25, SlotType.BATTERY)
        this.drawSlot(80, 25)
        this.drawBar(110, 26, this.tileEntity.timer.asInstanceOf[Float] / this.tileEntity.SHI_JIAN.asInstanceOf[Float])
        this.drawMeter(8, 18, this.tileEntity.waterTank.getFluidAmount.asInstanceOf[Float] / this.tileEntity.waterTank.getCapacity.asInstanceOf[Float], this.tileEntity.waterTank.getFluid)
        this.drawSlot(24, 49, SlotType.LIQUID)
        this.drawMeter(155, 18, this.tileEntity.gasTank.getFluidAmount.asInstanceOf[Float] / this.tileEntity.gasTank.getCapacity.asInstanceOf[Float], this.tileEntity.gasTank.getFluid)
        this.drawSlot(135, 49, SlotType.GAS)
    }
}