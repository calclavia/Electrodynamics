package resonantinduction.assemblyline.api.coding;

import net.minecraft.nbt.NBTTagCompound;

/** Used to ID that an item can support saving data to it NBT
 * 
 * @author DarkGuardsman */
public interface IDataItem
{
    /** Saves the data to the item */
    public NBTTagCompound saveData(NBTTagCompound nbt);

    /** Gets the data from the item */
    public NBTTagCompound getData();
}
