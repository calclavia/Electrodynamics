package resonantinduction.assemblyline.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.assemblyline.AssemblyLine;
import resonantinduction.assemblyline.imprinter.ContainerImprinter;
import resonantinduction.assemblyline.imprinter.TileEntityImprinter;

import com.builtbroken.minecraft.TranslationHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiImprinter extends GuiContainer
{
    private int containerWidth;
    private int containerHeight;
    private TileEntityImprinter tileEntity;

    public GuiImprinter(InventoryPlayer par1InventoryPlayer, TileEntityImprinter tileEntity)
    {
        super(new ContainerImprinter(par1InventoryPlayer, tileEntity));
        this.tileEntity = tileEntity;
        this.ySize = 201;
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        this.fontRenderer.drawString("Use Inventories:", 70, 58, 4210752);
        this.fontRenderer.drawString("" + this.tileEntity.searchInventories, 105, 67, 4210752);
        this.fontRenderer.drawString(TranslationHelper.getLocal("tile.imprinter.name"), 68, 6, 4210752);
    }

    /** Draw the background layer for the GuiContainer (everything behind the items) */
    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        this.mc.renderEngine.bindTexture(new ResourceLocation(AssemblyLine.DOMAIN, AssemblyLine.GUI_DIRECTORY + "gui_imprinter.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.containerWidth = (this.width - this.xSize) / 2;
        this.containerHeight = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(containerWidth, containerHeight, 0, 0, this.xSize, this.ySize);
    }
}
