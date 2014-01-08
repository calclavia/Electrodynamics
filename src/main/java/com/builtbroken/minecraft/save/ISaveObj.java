package com.builtbroken.minecraft.save;

import net.minecraft.nbt.NBTTagCompound;

public interface ISaveObj
{
	/** Saves the object to NBT */
	public void save(NBTTagCompound nbt);

	/** Load the object from NBT */
	public void load(NBTTagCompound nbt);
}
