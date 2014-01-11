package resonantinduction.electrical.encoder.coding;

/**
 * IDs that an item can load/save a program
 * 
 * @author Darkguardsman
 */
public interface IProgramItem
{
	/** Sets the program into the item */
	public void setProgram(IProgram program);

	/** Gets the program from the item */
	public IProgram getProgram();
}
