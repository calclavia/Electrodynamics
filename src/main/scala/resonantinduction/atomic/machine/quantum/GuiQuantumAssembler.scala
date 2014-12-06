package resonantinduction.atomic.machine.quantum

import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import resonant.lib.prefab.gui.GuiContainerBase
import resonant.lib.science.UnitDisplay
import resonantinduction.core.Reference

object GuiQuantumAssembler
{
    final val TEXTURE: ResourceLocation = new ResourceLocation(Reference.domain, Reference.guiDirectory + "gui_atomic_assembler.png")
}

class GuiQuantumAssembler(par1InventoryPlayer: InventoryPlayer, tileEntity: TileQuantumAssembler) extends GuiContainerBase(new ContainerQuantumAssembler(par1InventoryPlayer, tileEntity))
{
    //Constructor
    this.ySize = 230

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    override def drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int)
    {
        this.fontRendererObj.drawString("Assembler", 65 - "Assembler".length, 6, 4210752)
        var displayText: String = ""
        if (this.tileEntity.time > 0)
        {
            displayText = "Process: " + (100 - (this.tileEntity.time.asInstanceOf[Float] / this.tileEntity.MAX_TIME.asInstanceOf[Float]) * 100).asInstanceOf[Int] + "%"
        }
        else if (this.tileEntity.canProcess)
        {
            displayText = "Ready"
        }
        else
        {
            displayText = "Idle"
        }
        this.fontRendererObj.drawString(displayText, 9, this.ySize - 106, 4210752)
        this.renderUniversalDisplay(100, this.ySize - 94, this.tileEntity.getVoltage, mouseX, mouseY, UnitDisplay.Unit.VOLTAGE)
        this.renderUniversalDisplay(8, this.ySize - 95, tileEntity.MAX_TIME, mouseX, mouseY, UnitDisplay.Unit.WATT)
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    protected override def drawGuiContainerBackgroundLayer(par1: Float, par2: Int, par3: Int)
    {
        this.mc.renderEngine.bindTexture(GuiQuantumAssembler.TEXTURE)
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
        this.containerWidth = (this.width - this.xSize) / 2
        this.containerHeight = (this.height - this.ySize) / 2
        this.drawTexturedModalRect(this.containerWidth, this.containerHeight, 0, 0, this.xSize, this.ySize)
    }

}