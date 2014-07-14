package resonantinduction.electrical.encoder.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import resonant.lib.gui.ContainerDummy;
import resonant.lib.gui.GuiContainerBase;
import resonantinduction.core.ArgumentData;
import resonantinduction.core.Reference;
import resonantinduction.electrical.encoder.coding.ITask;
import universalelectricity.api.vector.Vector2;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;

public class GuiEncoderEditTask extends GuiContainerBase
{
	public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.GUI_DIRECTORY + "gui_task_edit.png");

	protected GuiEncoderCoder gui;
	protected ITask task, editTask;
	int ySpacing = 20;
	int xStart = 13, yStart = 50;
	protected GuiTextField[] argTextBoxes;
	int getFocus = -1;
	boolean newTask = false;

	public GuiEncoderEditTask(GuiEncoderCoder gui, ITask task, boolean newTask)
	{
		super(new ContainerDummy());
		this.newTask = newTask;
		this.ySize = 380 / 2;
		this.gui = gui;
		this.task = task;
		this.editTask = task.clone();
		NBTTagCompound nbt = new NBTTagCompound();
		task.save(nbt);
		this.editTask.load(nbt);
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.drawButtons();
		Keyboard.enableRepeatEvents(true);
	}

	@SuppressWarnings("unchecked")
	public void drawButtons()
	{
		this.buttonList.clear();

		this.buttonList.add(new GuiButton(0, (this.width - xSize) / 2 + 13, (this.height - ySize) / 2 + 135, 50, 20, "Save"));

		this.buttonList.add(new GuiButton(1, (this.width - xSize) / 2 + 68, (this.height - ySize) / 2 + 135, 50, 20, "Cancel"));
		if (!this.newTask)
			this.buttonList.add(new GuiButton(2, (this.width - xSize) / 2 + 125, (this.height - ySize) / 2 + 135, 40, 20, "Del"));

		if (task.getArgs() != null)
		{
			this.argTextBoxes = new GuiTextField[task.getArgs().size()];
			int i = 0;
			for (ArgumentData arg : task.getArgs())
			{
				this.argTextBoxes[i] = new GuiTextField(this.fontRenderer, (this.width - xSize) / 2 + 60, (this.height - ySize) / 2 + 64 + (i * this.ySpacing), 30, 10);
				this.argTextBoxes[i].setMaxStringLength(30);
				this.argTextBoxes[i].setVisible(true);
				this.argTextBoxes[i].setText("" + arg.getData());
				i++;
			}
		}

	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void keyTyped(char character, int keycode)
	{
		if (keycode == Keyboard.KEY_ESCAPE)
		{
			this.mc.thePlayer.closeScreen();
		}
		else if (keycode == Keyboard.KEY_TAB)
		{
			if (this.argTextBoxes != null)
			{
				this.getFocus += 1;
				if (this.getFocus >= this.argTextBoxes.length)
				{
					this.getFocus = 0;
				}
			}
			else
			{
				this.getFocus = -1;
			}
		}
		else
		{
			if (this.argTextBoxes != null && this.getFocus > -1 && this.getFocus < this.argTextBoxes.length)
			{
				if (this.argTextBoxes[this.getFocus] != null)
					this.argTextBoxes[this.getFocus].textboxKeyTyped(character, keycode);
			}
		}
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3)
	{
		super.mouseClicked(par1, par2, par3);
		this.getFocus = -1;
		if (this.argTextBoxes != null)
		{
			for (int i = 0; i < this.argTextBoxes.length; i++)
			{
				GuiTextField box = this.argTextBoxes[i];
				if (box != null && box.getVisible())
				{
					box.mouseClicked(par1, par2, par3);
					if (box.isFocused())
					{
						this.getFocus = i;
					}
				}
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		switch (button.id)
		{
			case 0:
			case 1:

				if (button.id == 0)
				{
					if (this.argTextBoxes != null)
					{
						int i = 0;
						for (ArgumentData arg : task.getArgs())
						{
							if (this.argTextBoxes[i] != null)
							{
								if (arg.isValid(this.argTextBoxes[i].getText()))
								{
									editTask.setArg(arg.getName(), this.argTextBoxes[i].getText());
								}
								else
								{
									this.argTextBoxes[i].setText("");
								}
							}
							i++;
						}
					}
					if (!this.newTask)
					{
						this.gui.getTile().updateTask(this.editTask);
						FMLCommonHandler.instance().showGuiScreen(this.gui);
					}
					else
					{
						// new GuiMessageBox(this, 1, "Create new Task", "Are you sure?").show();
					}
				}
				else
				{
					FMLCommonHandler.instance().showGuiScreen(this.gui);
				}

				break;
			case 2:
				// new GuiMessageBox(this, 0, "Remove Task", "Are you sure?").show();
				break;
		}
	}

	/** Draw the background layer for the GuiContainer (everything behind the items) */
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY)
	{
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		int containerWidth = (this.width - xSize) / 2;
		int containerHeight = (this.height - ySize) / 2;
		this.drawTexturedModalRect(containerWidth, containerHeight, 0, 0, xSize, ySize);
		if (this.argTextBoxes != null)
		{
			for (int i = 0; i < this.argTextBoxes.length; i++)
			{
				GuiTextField box = this.argTextBoxes[i];
				if (box != null)
				{
					box.drawTextBox();
				}
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		this.fontRenderer.drawString("Edit Task", (int) (xSize / 2 - 7 * 2.5), 5, 4210752);
		this.fontRenderer.drawString("Task: " + "\u00a77" + this.task.getMethodName(), ((xSize / 2) - 70), 20, 4210752);
		this.fontRenderer.drawString("----Task Arguments---- ", ((xSize / 2) - 70), 50, 4210752);

		int i = 0;
		if (task.getArgs() != null)
		{
			for (ArgumentData arg : task.getArgs())
			{
				i++;
				this.fontRenderer.drawString(arg.getName() + ":", ((xSize / 2) - 70), 45 + (i * this.ySpacing), 4210752);
				this.fontRenderer.drawString(arg.warning(), ((xSize / 2) + 11), 45 + (i * this.ySpacing), 4210752);

			}
		}
		else
		{
			this.fontRenderer.drawString("\u00a77" + "     No editable args ", ((xSize / 2) - 70), 70, 4210752);

		}

	}

	public void onMessageBoxClosed(int id, boolean yes)
	{
		if (id == 0 && yes)
		{
			this.gui.getTile().removeTask(new Vector2(this.editTask.getCol(), this.editTask.getRow()));
			FMLCommonHandler.instance().showGuiScreen(this.gui);
		}
		if (id == 1)
		{
			if (yes)
			{
				this.gui.getTile().insertTask(this.editTask);
				FMLCommonHandler.instance().showGuiScreen(this.gui);
			}
			this.gui.insertingTask = false;
		}
	}
}
