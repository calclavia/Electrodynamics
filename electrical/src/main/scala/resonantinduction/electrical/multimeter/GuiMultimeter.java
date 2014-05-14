/**
 * 
 */
package resonantinduction.electrical.multimeter;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import resonant.lib.gui.GuiContainerBase;
import resonant.lib.render.EnumColor;
import resonant.lib.utility.LanguageUtility;
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
	PartMultimeter multimeter;
	private GuiTextField textFieldLimit;

	public GuiMultimeter(InventoryPlayer inventoryPlayer, PartMultimeter tileEntity)
	{
		super(new ContainerMultimeter(inventoryPlayer, tileEntity));
		this.multimeter = tileEntity;
		this.ySize = 217;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.add(new GuiButton(0, this.width / 2 + 20, this.height / 2 - 23, 50, 20, LanguageUtility.getLocal("gui.resonantinduction.multimeter.toggle")));
		this.buttonList.add(new GuiButton(1, this.width / 2 - 80, this.height / 2 - 75, 100, 20, LanguageUtility.getLocal("gui.resonantinduction.multimeter.toggleDetection")));
		this.buttonList.add(new GuiButton(2, this.width / 2 - 80, this.height / 2 + 0, 80, 20, LanguageUtility.getLocal("gui.resonantinduction.multimeter.toggleGraph")));
		this.textFieldLimit = new GuiTextField(fontRenderer, 9, 90, 90, 12);
		this.textFieldLimit.setMaxStringLength(8);
		this.textFieldLimit.setText("" + this.multimeter.redstoneTriggerLimit);
	}

	@Override
	protected void keyTyped(char par1, int par2)
	{
		super.keyTyped(par1, par2);
		this.textFieldLimit.textboxKeyTyped(par1, par2);

		try
		{
			multimeter.redstoneTriggerLimit = Double.parseDouble(textFieldLimit.getText());
			multimeter.updateServer();
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
	protected void actionPerformed(GuiButton button)
	{
		switch (button.id)
		{
			case 0:
				multimeter.toggleMode();
				break;
			case 1:
				multimeter.toggleDetectionValue();
				break;
			case 2:
				multimeter.toggleGraphType();
				break;
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		String graphName = multimeter.getNetwork().getLocalized(multimeter.getNetwork().graphs.get(multimeter.graphType));
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		String s = LanguageUtility.getLocal("item.resonantinduction:multimeter.name");
		this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 4210752);
		this.fontRenderer.drawString(EnumColor.INDIGO + "Detection Type", 9, 20, 4210752);
		this.fontRenderer.drawString(multimeter.getNetwork().getDisplay(multimeter.detectType), 9, 60, 4210752);
		this.fontRenderer.drawString(LanguageUtility.getLocal("gui.resonantinduction.multimeter.logic")+" " + EnumColor.RED + LanguageUtility.getLocal("gui.resonantinduction.multimeter." + this.multimeter.getMode().display), 9, 75, 4210752);
		this.fontRenderer.drawString(graphName, 95, 115, 4210752);
		this.textFieldLimit.drawTextBox();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
	{
		super.drawGuiContainerBackgroundLayer(f, x, y);
	}

}
