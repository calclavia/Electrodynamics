/**
 * 
 */
package resonantinduction.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.ResonantInduction;
import resonantinduction.multimeter.ContainerMultimeter;
import resonantinduction.multimeter.TileEntityMultimeter;
import calclavia.lib.gui.GuiContainerBase;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Multimeter GUI
 * 
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class GuiMultimeter extends GuiContainerBase
{
	TileEntityMultimeter tileEntity;

	private int containerWidth;
	private int containerHeight;
	private GuiTextField textFieldLimit;

	public GuiMultimeter(InventoryPlayer inventoryPlayer, TileEntityMultimeter tileEntity)
	{
		super(new ContainerMultimeter(inventoryPlayer, tileEntity));
		this.tileEntity = tileEntity;
		this.ySize = 217;
		this.baseTexture = new ResourceLocation(ResonantInduction.DOMAIN, ResonantInduction.GUI_DIRECTORY + "gui_multimeter.png");
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.add(new GuiButton(0, this.width / 2 + 20, this.height / 2 - 30, 50, 20, "Toggle"));
		this.textFieldLimit = new GuiTextField(fontRenderer, 35, 82, 65, 12);
		this.textFieldLimit.setMaxStringLength(8);
		this.textFieldLimit.setText("" + this.tileEntity.getLimit());
	}

	@Override
	protected void keyTyped(char par1, int par2)
	{
		super.keyTyped(par1, par2);
		this.textFieldLimit.textboxKeyTyped(par1, par2);

		try
		{
			PacketDispatcher.sendPacketToServer(ResonantInduction.PACKET_TILE.getPacket(this.tileEntity, (byte) 3, Float.parseFloat(this.textFieldLimit.getText())));
		}
		catch (Exception e)
		{
		}
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3)
	{
		super.mouseClicked(par1, par2, par3);
		this.textFieldLimit.mouseClicked(par1 - this.containerWidth, par2 - this.containerHeight, par3);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		String s = this.tileEntity.getBlockType().getLocalizedName();
		this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 15, 4210752);
		this.fontRenderer.drawString("Average Energy: " + Math.round(this.tileEntity.getAverageDetectedEnergy()) + " J", 35, 25, 4210752);
		this.fontRenderer.drawString("Energy: " + Math.round(this.tileEntity.getDetectedEnergy()) + " J", 35, 35, 4210752);
		this.fontRenderer.drawString("Output Redstone If... ", 35, 54, 4210752);
		this.fontRenderer.drawString(this.tileEntity.getMode().display, 35, 65, 4210752);
		this.fontRenderer.drawString("KiloJoules", 35, 100, 4210752);

		this.textFieldLimit.drawTextBox();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
	{
		super.drawGuiContainerBackgroundLayer(f, x, y);
		int length = Math.min((int) (this.tileEntity.getDetectedEnergy() / this.tileEntity.getPeak()) * 115, 115);
		this.drawTexturedModalRect(this.containerWidth + 14, this.containerHeight + 126 - length, 176, 115 - length, 6, length);
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		PacketDispatcher.sendPacketToServer(ResonantInduction.PACKET_TILE.getPacket(this.tileEntity, (byte) 2));
	}

}
