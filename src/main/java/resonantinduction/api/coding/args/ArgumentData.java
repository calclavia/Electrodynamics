package resonantinduction.api.coding.args;

import net.minecraft.nbt.NBTTagCompound;
import calclavia.lib.utility.ISaveObj;

/**
 * Used to store arguments in a way that can be easier to read, limit, and understand
 * 
 * @author DarkGuardsman
 */
public class ArgumentData implements ISaveObj
{
	protected String name;
	protected Object currentValue;
	protected final Object defaultValue;

	public ArgumentData(String name, Object defaultValue)
	{
		this.name = name;
		this.defaultValue = defaultValue;
		this.currentValue = defaultValue;
	}

	/**
	 * Sets the value
	 * 
	 * @return true if the value was accepted
	 */
	public boolean setData(Object object)
	{
		if (this.isValid(object))
		{
			this.currentValue = object;
			return true;
		}
		return false;
	}

	/** Gets the value of the stored data */
	public Object getData()
	{
		return this.currentValue;
	}

	public String getName()
	{
		return this.name;
	}

	public boolean isValid(Object object)
	{
		return object != null;
	}

	/** Is this argument valid. */
	public boolean isValid()
	{
		return true;
	}

	/** Used by things like a gui to give a warning such as limits of data this can accept */
	public String warning()
	{
		return "";
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		NBTFileHelper.saveObject(nbt, "ObjectData", this.currentValue);
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		this.currentValue = NBTFileHelper.loadObject(nbt, "ObjectData");

	}
}
