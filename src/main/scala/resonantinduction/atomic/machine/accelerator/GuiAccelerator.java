package resonantinduction.atomic.machine.accelerator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.lib.gui.GuiContainerBase;
import universalelectricity.api.UnitDisplay;
import universalelectricity.core.transform.vector.Vector3;

public class GuiAccelerator extends GuiContainerBase
{
    private TileAccelerator tileEntity;

    private int containerWidth;
    private int containerHeight;

    public GuiAccelerator(EntityPlayer player, TileAccelerator tileEntity)
    {
        super(new ContainerAccelerator(player, tileEntity));
        this.tileEntity = tileEntity;
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of the items) */
    @Override
    public void drawGuiContainerForegroundLayer(int x, int y)
    {
        this.fontRendererObj.drawString("Accelerator", 40, 10, 4210752);

        String status = "";
        Vector3 position = new Vector3(this.tileEntity);
        position.add(this.tileEntity.getDirection().getOpposite());

        if (!EntityParticle.canSpawnParticle(this.tileEntity.world(), position))
        {
            status = "\u00a74Fail to emit; try rotating.";
        }
        else if (this.tileEntity.entityParticle != null && this.tileEntity.velocity > 0)
        {
            status = "\u00a76Accelerating";
        }
        else
        {
            status = "\u00a72Idle";
        }

        this.fontRendererObj.drawString("Velocity: " + Math.round((this.tileEntity.velocity / TileAccelerator.clientParticleVelocity) * 100) + "%", 8, 27, 4210752);
        this.fontRendererObj.drawString("Energy Used:", 8, 38, 4210752);
        this.fontRendererObj.drawString(new UnitDisplay(UnitDisplay.Unit.JOULES, this.tileEntity.totalEnergyConsumed).toString(), 8, 49, 4210752);
        this.fontRendererObj.drawString(new UnitDisplay(UnitDisplay.Unit.WATT, TileAccelerator.energyPerTick * 20).toString(), 8, 60, 4210752);
        this.fontRendererObj.drawString("N?A", 8, 70, 4210752);
        this.fontRendererObj.drawString("Antimatter: " + this.tileEntity.antimatter + " mg", 8, 80, 4210752);
        this.fontRendererObj.drawString("Status:", 8, 90, 4210752);
        this.fontRendererObj.drawString(status, 8, 100, 4210752);
        this.fontRendererObj.drawString("Buffer: " + this.tileEntity.electricNode().getEnergy(ForgeDirection.UNKNOWN) + "/" + new UnitDisplay(UnitDisplay.Unit.JOULES, this.tileEntity.electricNode().getEnergyCapacity(ForgeDirection.UNKNOWN),true ), 8, 110,
                4210752);
        this.fontRendererObj.drawString("Facing: " + this.tileEntity.getDirection().getOpposite(), 100, 123, 4210752);
    }

    /** Draw the background layer for the GuiContainer (everything behind the items) */
    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int x, int y)
    {
        super.drawGuiContainerBackgroundLayer(par1, x, y);

        this.drawSlot(131, 25);
        this.drawSlot(131, 50);
        this.drawSlot(131, 74);
        this.drawSlot(105, 74);
    }
}