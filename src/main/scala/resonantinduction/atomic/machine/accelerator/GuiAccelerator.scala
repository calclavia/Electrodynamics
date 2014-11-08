package resonantinduction.atomic.machine.accelerator

import net.minecraft.entity.player.EntityPlayer
import resonant.lib.gui.GuiContainerBase
import resonant.lib.science.UnitDisplay
import resonantinduction.core.Settings
import resonant.lib.transform.vector.Vector3

class GuiAccelerator(player: EntityPlayer, tileEntity: TileAccelerator) extends GuiContainerBase(new ContainerAccelerator(player, tileEntity))
{

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    override def drawGuiContainerForegroundLayer(x: Int, y: Int)
    {
        this.fontRendererObj.drawString("Accelerator", 40, 10, 4210752)
        var status: String = ""
        val position: Vector3 = tileEntity.toVector3 + tileEntity.getDirection.getOpposite
        if (!EntityParticle.canSpawnParticle(this.tileEntity.world, position))
        {
            status = "\u00a74Fail to emit; try rotating."
        }
        else if (this.tileEntity.entityParticle != null && this.tileEntity.velocity > 0)
        {
            status = "\u00a76Accelerating"
        }
        else
        {
            status = "\u00a72Idle"
        }
        this.fontRendererObj.drawString("Velocity: " + Math.round((this.tileEntity.velocity / EntityParticle.ANITMATTER_CREATION_SPEED) * 100) + "%", 8, 27, 4210752)
        this.fontRendererObj.drawString("Energy Used:", 8, 38, 4210752)
        this.fontRendererObj.drawString(new UnitDisplay(UnitDisplay.Unit.JOULES, this.tileEntity.totalEnergyConsumed).toString, 8, 49, 4210752)
        this.fontRendererObj.drawString(new UnitDisplay(UnitDisplay.Unit.WATT, Settings.ACCELERATOR_ENERGY_COST_PER_TICK * 20).toString, 8, 60, 4210752)
        this.fontRendererObj.drawString("Voltage: N?A", 8, 70, 4210752)
        this.fontRendererObj.drawString("Antimatter: " + this.tileEntity.antimatter + " mg", 8, 80, 4210752)
        this.fontRendererObj.drawString("Status:", 8, 90, 4210752)
        this.fontRendererObj.drawString(status, 8, 100, 4210752)
       // this.fontRendererObj.drawString("Buffer: " + this.tileEntity.energy.getEnergy + "/" + new UnitDisplay(UnitDisplay.Unit.JOULES, this.tileEntity.energy.getEnergyCapacity, true), 8, 110, 4210752)
        this.fontRendererObj.drawString("Facing: " + this.tileEntity.getDirection.getOpposite, 100, 123, 4210752)
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    protected override def drawGuiContainerBackgroundLayer(par1: Float, x: Int, y: Int)
    {
        super.drawGuiContainerBackgroundLayer(par1, x, y)
        this.drawSlot(131, 25)
        this.drawSlot(131, 50)
        this.drawSlot(131, 74)
        this.drawSlot(105, 74)
    }
}