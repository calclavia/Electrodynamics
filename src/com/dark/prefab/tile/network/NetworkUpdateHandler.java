package com.dark.prefab.tile.network;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.dark.tile.network.ITileNetwork;


import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

/** Manages all the tile networks making sure they get world save events, and updates every so often
 * 
 * @author DarkGuardsman */
public class NetworkUpdateHandler implements ITickHandler
{
    private static HashMap<String, Class<?>> nameToClassMap = new HashMap<String, Class<?>>();
    private static HashMap<Class<?>, String> classToNameMap = new HashMap<Class<?>, String>();

    private int count = 0;
    private static int refreshTicks = 6000;

    private Set<ITileNetwork> activeNetworks = new HashSet();
    private Set<ITileNetwork> allNetworks = new HashSet();

    private static NetworkUpdateHandler instance;

    static
    {
        registerNetworkClass("base", NetworkTileEntities.class);
    }

    public static NetworkUpdateHandler instance()
    {
        if (instance == null)
        {
            instance = new NetworkUpdateHandler();
        }
        return instance;
    }

    public void registerNetwork(ITileNetwork network)
    {
        if (network != null && !activeNetworks.contains(network))
        {
            this.allNetworks.add(network);
            if (network.getUpdateRate() > 0)
            {
                this.activeNetworks.add(network);
            }
        }
    }

    public static void registerNetworkClass(String id, Class<?> clazz)
    {
        if (!nameToClassMap.containsKey(id) && !classToNameMap.containsKey(clazz))
        {
            nameToClassMap.put(id, clazz);
            classToNameMap.put(clazz, id);
        }
    }

    public static String getID(Class<?> clazz)
    {
        return classToNameMap.get(clazz);
    }

    public static Class<?> getClazz(String id)
    {
        return nameToClassMap.get(id);
    }

    public static ITileNetwork createNewNetwork(String id)
    {
        Class<?> clazz = getClazz(id);
        if (clazz != null)
        {
            try
            {
                Object object = clazz.newInstance();
                if (object instanceof ITileNetwork)
                {
                    return (ITileNetwork) object;
                }
            }
            catch (Exception e)
            {
                System.out.println("[CoreMachine]TileNetworkHandler: Failed to create a new network object");
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("[CoreMachine]TileNetworkHandler: Unkown id: " + id);
        }

        return null;
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {
        if (count + 1 >= NetworkUpdateHandler.refreshTicks)
        {
            count = 0;
            for (ITileNetwork network : allNetworks)
            {
                if (!network.isInvalid())
                {
                    network.refreshTick();
                }
            }
        }
        else
        {
            count++;
            for (ITileNetwork network : activeNetworks)
            {
                if (!network.isInvalid())
                {
                    network.updateTick();
                }
            }
        }

    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        Iterator<ITileNetwork> it = activeNetworks.iterator();
        while (it.hasNext())
        {
            ITileNetwork network = it.next();
            if (network.isInvalid())
            {
                network.invalidate();
                it.remove();
                allNetworks.remove(network);
            }
        }

    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.SERVER);
    }

    @Override
    public String getLabel()
    {
        return "[CoreMachine]TileNetworkHandler";
    }
}
