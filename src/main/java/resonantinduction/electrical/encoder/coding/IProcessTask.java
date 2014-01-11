package resonantinduction.electrical.encoder.coding;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;

/**
 * Use to construct a basic task that can be used in any device that supports this interface.
 * 
 * @Note - there are several methods that look a like. GetArgs is used to get the programs arguments
 * that were set by the encoder. GetEncoderParms should be a constant set of arguments that the
 * device can support. GetMemory is a list of variables that the program needs to store outside of
 * the task. That way it can save values after the task has been refreshed or even deleted.
 * 
 * @author DarkGuardsman
 */
public interface IProcessTask extends ITask
{
	/**
	 * Passed in from the device to the program manager then here after a Computer craft machine
	 * calls a this commands method name. {@IPeripheral #callMethod()}
	 */
	public Object[] onCCMethodCalled(IComputerAccess computer, ILuaContext context) throws Exception;

	/**
	 * Called when the task is being run by the devices program manager. Used mainly to setup the
	 * task before actually doing the task.
	 * 
	 * @param world - current world
	 * @param location - current location
	 * @param armbot - armbot instance
	 * @param arguments - arguments for command
	 * @return false to stop the task here.
	 */
	public ProcessReturn onMethodCalled();

	/** Update the current segment of the task */
	public ProcessReturn onUpdate();

	/** Called when the task is finish and then cleared */
	public void terminated();

	public static enum ProcessReturn
	{
		CONTINUE("Continue", "Running"), DONE("Done", "Done"),
		GENERAL_ERROR("General Error", "Error program failure"),
		SYNTAX_ERROR("Syntax Error", "Error incorrect syntax"),
		ARGUMENT_ERROR("Arument error", "Error incorrect arguments");
		public String name, userOutput;

		private ProcessReturn(String name, String userOutput)
		{
			this.name = name;
			this.userOutput = userOutput;
		}
	}

}
