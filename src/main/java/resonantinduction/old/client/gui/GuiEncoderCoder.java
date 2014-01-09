package resonantinduction.old.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import resonantinduction.old.lib.prefab.invgui.GuiButtonImage;
import resonantinduction.old.lib.prefab.invgui.GuiButtonImage.ButtonIcon;
import resonantinduction.old.transport.encoder.TileEntityEncoder;

public class GuiEncoderCoder extends GuiEncoderBase
{
	private GuiTaskList taskListGui;
	GuiButtonImage left, right, up, down;
	GuiButton newTask, newTask2;
	String helpMessage = "";
	boolean insertingTask = false;
	boolean bellow = true;

	public GuiEncoderCoder(InventoryPlayer player, TileEntityEncoder tileEntity)
	{
		super(player, tileEntity);
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.getTaskListElement(true);
		left = new GuiButtonImage(3, containerWidth + 13, containerHeight + 120, ButtonIcon.ARROW_LEFT);
		this.buttonList.add(left);
		right = new GuiButtonImage(4, containerWidth + 147, containerHeight + 120, ButtonIcon.ARROW_RIGHT);
		this.buttonList.add(right);
		up = new GuiButtonImage(5, containerWidth + 147, containerHeight + 30, ButtonIcon.ARROW_UP);
		this.buttonList.add(up);
		down = new GuiButtonImage(6, containerWidth + 147, containerHeight + 100, ButtonIcon.ARROW_DOWN);
		this.buttonList.add(down);
		newTask = new GuiButton(7, containerWidth + 15, containerHeight + 135, 70, 10, "Insert bellow");
		this.buttonList.add(newTask);
		newTask2 = new GuiButton(8, containerWidth + 90, containerHeight + 135, 70, 10, "Insert Above");
		newTask2.enabled = false; // TODO fix
		this.buttonList.add(newTask2);

	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		if (((TileEntityEncoder) tileEntity).getProgram() != null)
		{
			switch (button.id)
			{
				case 3:
					getTaskListElement().scrollSide(-1);
					break;
				case 4:
					getTaskListElement().scrollSide(1);
					break;
				case 5:
					getTaskListElement().scroll(-1);
					break;
				case 6:
					getTaskListElement().scroll(1);
					break;
				case 7:
					this.insertingTask = true;
					this.bellow = true;
					this.helpMessage = "Click a task to create a new task";
					break;
				case 8:
					this.insertingTask = true;
					this.bellow = false;
					this.helpMessage = "Click a task to create a new task";
					break;
			}
		}
	}

	protected GuiTaskList getTaskListElement(boolean renew)
	{
		if (taskListGui == null || renew)
		{
			if (taskListGui != null)
			{
				taskListGui.xPos = this.containerWidth + 25;
				taskListGui.yPos = this.containerHeight + 15;
			}
			else
			{
				taskListGui = new GuiTaskList(this.tileEntity, this, this.containerWidth + 25, this.containerHeight + 15);
			}
		}
		return this.taskListGui;
	}

	@Override
	public void handleMouseInput()
	{
		super.handleMouseInput();
		int wheel = Mouse.getEventDWheel();
		if (((TileEntityEncoder) tileEntity).getProgram() != null)
		{
			if (wheel > 0)
			{
				this.getTaskListElement().scroll(-2);
			}
			else if (wheel < 0)
			{
				this.getTaskListElement().scroll(2);
			}
		}
	}

	@Override
	protected void keyTyped(char character, int keycode)
	{
		if (keycode == Keyboard.KEY_ESCAPE)
		{
			this.mc.thePlayer.closeScreen();
		}
		else if (((TileEntityEncoder) tileEntity).getProgram() != null)
		{

			if (keycode == Keyboard.KEY_UP) // PAGE UP (no constant)
			{
				this.getTaskListElement().scroll(-1);
			}
			else if (keycode == Keyboard.KEY_DOWN) // PAGE DOWN (no constant)
			{
				this.getTaskListElement().scroll(1);
			}
			else if (keycode == Keyboard.KEY_LEFT) // PAGE LEFT (no constant)
			{
				if (this.getTaskListElement().scrollX > -5)
					this.getTaskListElement().scrollSide(-1);
			}
			else if (keycode == Keyboard.KEY_RIGHT) // PAGE RIGHT (no constant)
			{
				if (this.getTaskListElement().scrollX < ((TileEntityEncoder) tileEntity).getProgram().getSize().intX())
					this.getTaskListElement().scrollSide(1);
			}
		}
	}

	protected GuiTaskList getTaskListElement()
	{
		return this.getTaskListElement(false);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
	{
		super.drawGuiContainerBackgroundLayer(f, i, j);
		getTaskListElement().drawConsole(this.mc);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y)
	{
		super.drawGuiContainerForegroundLayer(x, y);
		taskListGui.drawGuiContainerForegroundLayer(this.mc, x, y);
		if (!this.insertingTask)
		{
			this.helpMessage = "";
		}
		this.fontRenderer.drawString(this.helpMessage, (this.xSize / 2 - 82), 150, 4210752);

	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3)
	{
		super.mouseClicked(par1, par2, par3);
		if (par3 == 0)
		{
			this.taskListGui.mousePressed(par1, par2);
		}
	}

	@Override
	public TileEntityEncoder getTile()
	{
		return (TileEntityEncoder) this.tileEntity;
	}

}
