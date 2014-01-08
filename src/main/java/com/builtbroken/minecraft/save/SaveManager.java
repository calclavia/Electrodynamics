package com.builtbroken.minecraft.save;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.FMLLog;

public class SaveManager
{
    private static HashMap<String, Class<?>> idToClassMap = new HashMap<String, Class<?>>();
    private static HashMap<Class<?>, String> classToIDMap = new HashMap<Class<?>, String>();
    private static List<Object> saveList = new ArrayList<Object>();
    private static List<Object> objects = new ArrayList<Object>();
    private static SaveManager instance;

    public static SaveManager instance()
    {
        if (instance == null)
        {
            instance = new SaveManager();
        }
        return instance;
    }

    /** Called when the object wants to be save only on the next save call. Will be removed from the
     * save manager after */
    public static void markNeedsSaved(Object object)
    {
        if (object instanceof IVirtualObject && !saveList.contains(object))
        {
            saveList.add(object);
        }
    }

    /** Registers the object to be saved on each world save event */
    public static void register(Object object)
    {
        if (object instanceof IVirtualObject && !objects.contains(object))
        {
            objects.add(object);
        }
    }

    public static void registerClass(String id, Class clazz)
    {
        if (id != null && clazz != null)
        {
            if (idToClassMap.containsKey(id) && idToClassMap.get(id) != null)
            {
                System.out.println("[CoreMachine]SaveManager: Something attempted to register a class with the id of another class");
                System.out.println("[CoreMachine]SaveManager: Id:" + id + "  Class:" + clazz.getName());
                System.out.println("[CoreMachine]SaveManager: OtherClass:" + idToClassMap.get(id).getName());
            }
            else
            {
                idToClassMap.put(id, clazz);
                classToIDMap.put(clazz, id);
            }
        }
    }

    public static Object createAndLoad(File file)
    {
        if (file.exists())
        {
            Object obj = createAndLoad(NBTFileHelper.loadNBTFile(file, false));
            if (obj instanceof IVirtualObject)
            {
                ((IVirtualObject) obj).setSaveFile(file);
            }
            return obj;
        }
        return null;
    }

    /** Creates an object from the save using its id */
    public static Object createAndLoad(NBTTagCompound par0NBTTagCompound)
    {
        Object obj = null;
        if (par0NBTTagCompound != null && par0NBTTagCompound.hasKey("id"))
        {
            try
            {
                Class clazz = getClass(par0NBTTagCompound.getString("id"));

                if (clazz != null)
                {
                    obj = clazz.newInstance();
                }
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }

            if (obj instanceof IVirtualObject)
            {
                try
                {
                    ((IVirtualObject) obj).load(par0NBTTagCompound);
                }
                catch (Exception e)
                {
                    FMLLog.log(Level.SEVERE, e, "An object %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author", par0NBTTagCompound.getString("id"), obj.getClass().getName());
                    obj = null;
                }
            }
            else
            {
                MinecraftServer.getServer().getLogAgent().logWarning("Skipping object with id " + par0NBTTagCompound.getString("id"));
            }

            return obj;
        }
        return null;
    }

    @ForgeSubscribe
    public void worldSave(WorldEvent evt)
    {
        SaveManager.saveList.addAll(SaveManager.objects);
        for (Object object : SaveManager.saveList)
        {
            if (object instanceof IVirtualObject)
            {
                saveObject(object);
            }
        }
        saveList.clear();
    }

    /** Saves an object along with its ID */
    public static void saveObject(Object object)
    {
        if (object instanceof IVirtualObject && getID(object.getClass()) != null && ((IVirtualObject) object).getSaveFile() != null)
        {
            File file = ((IVirtualObject) object).getSaveFile();
            file.mkdirs();
            NBTTagCompound tag = new NBTTagCompound();
            ((IVirtualObject) object).save(tag);
            tag.setString("id", getID(object.getClass()));
            NBTFileHelper.saveNBTFile(file, tag);
        }
    }

    /** Gets the ID that the class will be saved using */
    public static String getID(Class clazz)
    {
        return classToIDMap.get(clazz);
    }

    /** Gets the class that was registered with the ID */
    public static Class getClass(String id)
    {
        return idToClassMap.get(id);
    }
}
