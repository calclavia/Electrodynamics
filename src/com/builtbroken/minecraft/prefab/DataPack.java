package com.builtbroken.minecraft.prefab;

import net.minecraft.nbt.NBTTagCompound;

import com.builtbroken.minecraft.save.ISaveObj;
import com.builtbroken.minecraft.save.NBTFileHelper;

/** Wrapper for data to be sent threw a network to a device
 * 
 * @author DarkGuardsman */
public class DataPack implements ISaveObj, Cloneable
{
    private Object[] data;

    public DataPack(Object... data)
    {
        this.data = data;
    }

    public Object[] getData()
    {
        return this.data;
    }

    @Override
    public void save(NBTTagCompound nbt)
    {
        if (data != null)
        {
            nbt.setInteger("dataCnt", data.length);
            for (int i = 0; i < data.length; i++)
            {
                if (data[i] != null)
                {
                    NBTFileHelper.saveObject(nbt, "data" + i, data[i]);
                }
            }
        }
    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        if (nbt.hasKey("dataCnt"))
        {
            int dataLength = nbt.getInteger("dataCnt");
            data = new Object[dataLength];
            for (int i = 0; i < dataLength; i++)
            {
                if (nbt.hasKey("data" + i))
                {
                    data[i] = NBTFileHelper.loadObject(nbt, "data" + i);
                }
            }
        }

    }

    @Override
    public DataPack clone()
    {
        return new DataPack(this.data);
    }

    public boolean isEqual(DataPack pack)
    {
        return this.data != null && pack.data != null && this.data.equals(pack.data);
    }

    @Override
    public String toString()
    {
        return "DataPack [Obj:" + (this.data != null ? data.length : "none") + "]";
    }
}
