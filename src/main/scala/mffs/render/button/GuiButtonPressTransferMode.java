package mffs.render.button;

import mffs.base.GuiMFFS;
import mffs.production.TileFortronCapacitor;
import net.minecraft.client.Minecraft;
import universalelectricity.api.vector.Vector2;

public class GuiButtonPressTransferMode extends GuiButtonPress
{
	private TileFortronCapacitor tileEntity;

	public GuiButtonPressTransferMode(int id, int x, int y, GuiMFFS mainGui, TileFortronCapacitor tileEntity)
	{
		super(id, x, y, new Vector2(), mainGui);
		this.tileEntity = tileEntity;
	}

	@Override
	public void drawButton(Minecraft minecraft, int x, int y)
	{
		String transferName = this.tileEntity.getTransferMode().name().toLowerCase();
		char first = Character.toUpperCase(transferName.charAt(0));
		transferName = first + transferName.substring(1);
		this.displayString = "transferMode" + transferName;
		this.offset.y = 18 * this.tileEntity.getTransferMode().ordinal();

		super.drawButton(minecraft, x, y);
	}
}
