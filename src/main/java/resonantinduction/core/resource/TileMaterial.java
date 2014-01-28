package resonantinduction.core.resource;

import net.minecraft.nbt.NBTTagCompound;
import calclavia.lib.prefab.tile.TileAdvanced;

/**
 * A tile that stores the material name.
 * 
 * @author Calclavia
 * 
 */
public class TileMaterial extends TileAdvanced
{
	public String name;

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		name = nbt.getString("name");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setString("name", name);
	}
}
