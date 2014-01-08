package resonantinduction.api.coding;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import resonantinduction.api.coding.args.ArgumentData;
import universalelectricity.api.vector.Vector2;

import com.builtbroken.minecraft.save.ISaveObj;

/** @author DarkGuardsman */
public interface ITask extends Cloneable, ISaveObj
{
	/** Called each time the program is loaded or changed */
	public void refresh();

	/** Called when this task is asked to return to default */
	public void reset();

	/** Sets the task's program reference letting it know what it is part of */
	public void setProgram(IProgram program);

	/** Location in the program */
	public int getRow();

	public int getCol();

	/** Sets the tasks position in the program */
	public void setPosition(int col, int row);

	/**
	 * Method name or rather command name this will be called. Uses both to ID this command, and do
	 * basic command structuring.
	 */
	public String getMethodName();

	/** Type of task used mainly for GUI displays */
	public TaskType getType();

	/** Get an argument by a given name */
	public Object getArg(String name);

	/** Get all given arguments */
	public List<ArgumentData> getArgs();

	public void setArg(String arg, Object data);

	/** Reads the progress of the command if it was saved mid process */
	public ITask loadProgress(NBTTagCompound nbt);

	/** Saves the current progress of the current command */
	public NBTTagCompound saveProgress(NBTTagCompound nbt);

	/**
	 * Can this task function for this machine. Only do basic checks here as its only used to make
	 * sure the machine, or task will not crash while running.
	 */
	public boolean canUseTask(IProgrammableMachine device);

	/**
	 * Used to create a new task from this task. Make sure to return a fresh copy without anything.
	 * This includes no arguments, progress, variables, etc. As this is used to make new tasks from
	 * the TaskRegistry
	 */
	public ITask clone();

	/**
	 * Texture used by encoder's to render the icon for the task. Make sure not to create a new
	 * instance of the resource location each call. Doing so will cause the client to experience
	 * increase RAM usage
	 */
	public ResourceLocation getTextureSheet();

	/** Location of the texture in the sheet */
	public Vector2 getTextureUV();

	/** Passes in a list so that the task can add to the tool tip render */
	public void getToolTips(List<String> list);

	/** Used mainly for display purposes in the encoder */
	public static enum TaskType
	{
		DATA("Data", 120, 40), DEFINEDPROCESS("Defined Process", 40, 40),
		PROCESS("Process", 60, 40), DECISION("Decision", 80, 40), START("Start", 20, 40),
		END("End", 100, 40);
		public final String name;
		public final Vector2 UV;

		/**
		 * This is only loaded when assembly line is installed, and only used as a backup if tasks
		 * don't return textures
		 */
		public static final ResourceLocation TEXTURE = new ResourceLocation("al", "textures/gui/gui_coder_icons.png");

		private TaskType(String name, int uu, int vv)
		{
			this.name = name;
			this.UV = new Vector2(uu, vv);
		}
	}
}
