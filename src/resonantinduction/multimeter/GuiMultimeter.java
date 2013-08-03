/**
 * 
 */
package resonantinduction.multimeter;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.ResonantInduction;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Multimeter GUI
 * 
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class GuiMultimeter extends GuiContainer
{
	private static final ResourceLocation TEXTURE = new ResourceLocation(ResonantInduction.DOMAIN, ResonantInduction.GUI_DIRECTORY + "gui_base.png");
	TileEntityMultimeter tileEntity;

	private int containerWidth;
	private int containerHeight;

	public GuiMultimeter(InventoryPlayer inventoryPlayer, TileEntityMultimeter tileEntity)
	{
		super(new ContainerMultimeter(inventoryPlayer, tileEntity));
		this.tileEntity = tileEntity;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		String s = this.tileEntity.getBlockType().getLocalizedName();
		this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
	{
		this.containerWidth = (this.width - this.xSize) / 2;
		this.containerHeight = (this.height - this.ySize) / 2;

		this.mc.renderEngine.func_110577_a(TEXTURE);
		GL11.glColor4f(1, 1, 1, 1);
		this.drawTexturedModalRect(this.containerWidth, this.containerHeight, 0, 0, this.xSize, this.ySize);
	}

}
