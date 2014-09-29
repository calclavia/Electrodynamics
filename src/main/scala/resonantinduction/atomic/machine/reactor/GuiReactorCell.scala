package resonantinduction.atomic.machine.reactor

import net.minecraft.entity.player.InventoryPlayer
import org.lwjgl.opengl.GL11
import resonant.lib.gui.GuiContainerBase
import resonant.lib.utility.LanguageUtility

class GuiReactorCell(inventory: InventoryPlayer, tileEntity: TileReactorCell) extends GuiContainerBase(new ContainerReactorCell(inventory.player, tileEntity))
{
    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    override def drawGuiContainerForegroundLayer(x: Int, y: Int)
    {
        fontRendererObj.drawString(tileEntity.getInvName, xSize / 2 - fontRendererObj.getStringWidth(tileEntity.getInvName) / 2, 6, 4210752)
        if (tileEntity.getStackInSlot(0) != null)
        {
            fontRendererObj.drawString(LanguageUtility.getLocal("tooltip.temperature"), 9, 45, 4210752)
            fontRendererObj.drawString(String.valueOf(tileEntity.getTemperature.asInstanceOf[Int]) + "/" + String.valueOf(TileReactorCell.MELTING_POINT) + " K", 9, 58, 4210752)
            val secondsLeft: Int = (tileEntity.getStackInSlot(0).getMaxDamage - tileEntity.getStackInSlot(0).getItemDamage)
            fontRendererObj.drawString(LanguageUtility.getLocal("tooltip.remainingTime"), 100, 45, 4210752)
            fontRendererObj.drawString(secondsLeft + " seconds", 100, 58, 4210752)
        }
        fontRendererObj.drawString(LanguageUtility.getLocal("tooltip.remainingTime"), 100, 45, 4210752)
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    protected override def drawGuiContainerBackgroundLayer(par1: Float, x: Int, y: Int)
    {
        super.drawGuiContainerBackgroundLayer(par1, x, y)
        drawSlot(78, 16)
        drawMeter(80, 36, tileEntity.tank.getFluidAmount.asInstanceOf[Float] / tileEntity.tank.getCapacity.asInstanceOf[Float], tileEntity.tank.getFluid)
        if (tileEntity.getStackInSlot(0) != null)
        {
            GL11.glPushMatrix
            GL11.glTranslatef(32 * 2, 0, 0)
            GL11.glScalef(0.5f, 1, 1)
            drawForce(20, 70, (tileEntity.getTemperature) / (TileReactorCell.MELTING_POINT))
            GL11.glPopMatrix
            GL11.glPushMatrix
            GL11.glTranslatef(68 * 2, 0, 0)
            GL11.glScalef(0.5f, 1, 1)
            val ticksLeft: Float = (tileEntity.getStackInSlot(0).getMaxDamage - tileEntity.getStackInSlot(0).getItemDamage)
            drawElectricity(70, 70, ticksLeft / tileEntity.getStackInSlot(0).getMaxDamage)
            GL11.glPopMatrix
        }
    }
}