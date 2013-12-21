package com.builtbroken.minecraft.save;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import universalelectricity.api.vector.Vector2;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.common.science.units.UnitHelper;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;

/** Helper class used to work with minecraft's NBT file system.
 * 
 * @author DarkGuardsman */
public class NBTFileHelper
{
    /** @param saveDirectory - file
     * @param filename
     * @param data
     * @return */
    public static boolean saveNBTFile(File saveDirectory, String filename, NBTTagCompound data)
    {
        return saveNBTFile(new File(saveDirectory, filename), data);
    }

    /** Saves an NBT file
     * 
     * @param file - exact File
     * @param data - nbt data
     * @return */
    public static boolean saveNBTFile(File file, NBTTagCompound data)
    {
        if (file != null && data != null)
        {
            try
            {
                File tempFile = new File(file.getParent(), file.getName() + ".tmp");

                CompressedStreamTools.writeCompressed(data, new FileOutputStream(tempFile));

                if (file.exists())
                {
                    file.delete();
                }

                tempFile.renameTo(file);

                FMLLog.fine("Saved " + file.getName() + " NBT data file successfully.");
                return true;
            }
            catch (Exception e)
            {
                System.out.println("Failed to save " + file.getName());
                e.printStackTrace();
            }
        }
        return false;
    }

    /** Uses the default world directory to save the data to file by the given name
     * 
     * @param filename - file name
     * @param data - nbt data
     * @return true if everything goes well */
    public static boolean saveNBTFile(String filename, NBTTagCompound data)
    {
        return saveNBTFile(getWorldSaveDirectory(MinecraftServer.getServer().getFolderName()), filename + ".dat", data);
    }

    /** Reads NBT data from the world folder.
     * 
     * @return The NBT data */
    public static NBTTagCompound loadNBTFile(File saveDirectory, String filename, boolean create)
    {
        if (saveDirectory != null && filename != null)
        {
            if (create && !saveDirectory.exists())
            {
                saveDirectory.mkdirs();
            }
            return loadNBTFile(new File(saveDirectory, filename + ".dat"), create);
        }
        return null;
    }

    public static NBTTagCompound loadNBTFile(File file, boolean create)
    {
        if (file != null)
        {
            try
            {

                if (file.exists())
                {
                    FMLLog.fine("Loaded " + file.getName() + " data.");
                    return CompressedStreamTools.readCompressed(new FileInputStream(file));
                }
                else if (create)
                {
                    FMLLog.fine("Created new " + file.getName() + " data.");
                    return new NBTTagCompound();
                }
            }
            catch (Exception e)
            {
                System.out.println("Failed to load " + file.getName() + ".dat!");
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /** Loads an NBT file from the current world file
     * 
     * @param filename - name of the file
     * @return NBTTagCompound that was stored in the file */
    public static NBTTagCompound loadNBTFile(String filename)
    {
        return loadNBTFile(getWorldSaveDirectory(MinecraftServer.getServer().getFolderName()), filename, true);
    }

    public static File getWorldSaveDirectory(String worldName)
    {
        File parent = getBaseDirectory();

        if (FMLCommonHandler.instance().getSide().isClient())
        {
            parent = new File(getBaseDirectory(), "saves" + File.separator);
        }

        return new File(parent, worldName + File.separator);
    }

    public static File getBaseDirectory()
    {
        if (FMLCommonHandler.instance().getSide().isClient())
        {
            FMLClientHandler.instance().getClient();
            return Minecraft.getMinecraft().mcDataDir;
        }
        else
        {
            return new File(".");
        }
    }

    /** Used to save an object without knowing what the object is exactly. Supports most
     * NBTTagCompound save methods including some special cases. Which includes boolean being saves
     * as a string so it can be loaded as a boolean from an object save.
     * 
     * @param tag - NBTTagCompound to save the tag too
     * @param key - name to save the object as
     * @param value - the actual object
     * @return the tag when done saving too i */
    public static NBTTagCompound saveObject(NBTTagCompound tag, String key, Object value)
    {
        if (value instanceof Float)
        {
            tag.setFloat(key, (Float) value);
        }
        else if (value instanceof Double)
        {
            tag.setDouble(key, (Double) value);
        }
        else if (value instanceof Integer)
        {
            tag.setInteger(key, (Integer) value);
        }
        else if (value instanceof String)
        {
            tag.setString(key, (String) value);
        }
        else if (value instanceof Short)
        {
            tag.setShort(key, (Short) value);
        }
        else if (value instanceof Byte)
        {
            tag.setByte(key, (Byte) value);
        }
        else if (value instanceof Long)
        {
            tag.setLong(key, (Long) value);
        }
        else if (value instanceof Boolean)
        {
            tag.setString(key, "NBT:SAVE:BOOLEAN:" + value);
        }
        else if (value instanceof NBTBase)
        {
            tag.setTag(key, (NBTBase) value);
        }
        else if (value instanceof String)
        {
            tag.setString(key, (String) value);
        }
        else if (value instanceof byte[])
        {
            tag.setByteArray(key, (byte[]) value);
        }
        else if (value instanceof int[])
        {
            tag.setIntArray(key, (int[]) value);
        }
        else if (value instanceof NBTTagCompound)
        {
            tag.setCompoundTag(key, (NBTTagCompound) value);
        }
        else if (value instanceof Vector2)
        {
            tag.setString(key, "NBT:SAVE:VECTOR:2:" + ((Vector2) value).x + ":" + ((Vector2) value).y);
        }
        else if (value instanceof Vector3)
        {
            tag.setString(key, "NBT:SAVE:VECTOR:3:" + ((Vector3) value).x + ":" + ((Vector3) value).y + ":" + ((Vector3) value).z);
        }
        return tag;

    }

    /** @param key
     * @param value
     * @return NBTTagCompound that then can be added to save file */
    public static NBTTagCompound saveObject(String key, Object value)
    {
        return NBTFileHelper.saveObject(new NBTTagCompound(), key, value);
    }

    /** Reads an unknown object with a known name from NBT
     * 
     * @param tag - tag to read the value from
     * @param key - name of the value
     * @param suggestionValue - value to return in case nothing is found
     * @return object or suggestionValue if nothing is found */
    public static Object loadObject(NBTTagCompound tag, String key)
    {
        if (tag != null && key != null)
        {
            NBTBase saveTag = tag.getTag(key);
            if (saveTag instanceof NBTTagFloat)
            {
                return tag.getFloat(key);
            }
            else if (saveTag instanceof NBTTagDouble)
            {
                return tag.getDouble(key);
            }
            else if (saveTag instanceof NBTTagInt)
            {
                return tag.getInteger(key);
            }
            else if (saveTag instanceof NBTTagString)
            {
                String str = tag.getString(key);
                if (str.startsWith("NBT:SAVE:"))
                {
                    str.replaceAll("NBT:SAVE:", "");
                    if (str.startsWith("BOOLEAN:"))
                    {
                        str.replaceAll("BOOLEAN:", "");
                        if (str.equalsIgnoreCase("true"))
                        {
                            return true;
                        }
                        if (str.equalsIgnoreCase("false"))
                        {
                            return false;
                        }
                    }
                    if (str.startsWith("VECTOR:"))
                    {
                        str.replaceAll("VECTOR:", "");
                        String[] nums = str.split(":");
                        if (UnitHelper.tryToParseDouble(nums[0]) == 2)
                        {
                            return new Vector2(UnitHelper.tryToParseDouble(nums[1]), UnitHelper.tryToParseDouble(nums[2]));
                        }
                        if (UnitHelper.tryToParseDouble(nums[0]) == 3)
                        {
                            return new Vector3(UnitHelper.tryToParseDouble(nums[1]), UnitHelper.tryToParseDouble(nums[2]), UnitHelper.tryToParseDouble(nums[3]));
                        }
                    }
                    return null;
                }
                return str;
            }
            else if (saveTag instanceof NBTTagShort)
            {
                return tag.getShort(key);
            }
            else if (saveTag instanceof NBTTagByte)
            {
                return tag.getByte(key);
            }
            else if (saveTag instanceof NBTTagLong)
            {
                return tag.getLong(key);
            }
            else if (saveTag instanceof NBTBase)
            {
                return tag.getTag(key);
            }
            else if (saveTag instanceof NBTTagByteArray)
            {
                return tag.getByteArray(key);
            }
            else if (saveTag instanceof NBTTagIntArray)
            {
                return tag.getIntArray(key);
            }
            else if (saveTag instanceof NBTTagCompound)
            {
                return tag.getCompoundTag(key);
            }
        }
        return null;
    }

}
