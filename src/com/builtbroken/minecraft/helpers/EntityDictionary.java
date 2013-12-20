package com.builtbroken.minecraft.helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;

/** Dictionary to track entities by several names to be used for anything. Current use is armbot task
 * so the user has an easy way to ID creatures.
 * 
 * @author DarkGuardsman */
public class EntityDictionary
{
    public static HashMap<String, Class<? extends Entity>> entityMap = new HashMap();
    public static HashMap<Class<? extends Entity>, Boolean> grabMap = new HashMap();

    private static boolean init = false;

    /** Call this very last in a mod so that all mods have a chance to load there entities */
    public static void init()
    {
        if (!init)
        {
            init = true;
            for (Object object : EntityList.classToStringMapping.entrySet())
            {
                if (object instanceof Entry)
                {
                    Object key = ((Entry) object).getKey();
                    Object value = ((Entry) object).getKey();
                    if (key instanceof Class && value instanceof String)
                    {
                        entityMap.put((String) value, (Class) key);
                    }
                }
            }
        }
    }

    static
    {
        addName("chicken", EntityChicken.class);
        addName("cow", EntityCow.class);
        addName("sheep", EntitySheep.class);
        addName("pig", EntityPig.class);
        addName("player", EntityPlayer.class);
        addName("zombie", EntityZombie.class);
        addName("zomb", EntityZombie.class);
        addName("skeleton", EntitySkeleton.class);
        addName("skel", EntitySkeleton.class);
        addName("animal", EntityAnimal.class);
        addName("monster", EntityMob.class);
        addName("mob", EntityMob.class);
        addName("creeper", EntityCreeper.class);
        addName("spider", EntitySpider.class);
        addName("slime", EntitySlime.class);
        addName("items", EntityItem.class);
        addName("item", EntityItem.class);
        addName("all", Entity.class);
        addName("everything", Entity.class);
        addName("boat", EntityBoat.class);
        addName("cart", EntityMinecart.class);
        setCanNotBeGrabbed(EntityDragon.class);
        setCanNotBeGrabbed(EntityWither.class);
    }

    public static Class<? extends Entity> get(String name)
    {
        return entityMap.get(name);
    }

    /** Can the entity be grabbed by something such as a robot, or another entity. By default most
     * entities can be grabbed by another object. */
    public static boolean canGrab(String name)
    {
        if (entityMap.containsKey(name))
        {
            return canGrab(entityMap.get(name));
        }
        return true;
    }

    /** Can the entity be grabbed by something such as a robot, or another entity. By default most
     * entities can be grabbed by another object. */
    public static boolean canGrab(Entity entity)
    {
        if (entity != null)
        {
            if (canGrab(entity.getClass()))
            {
                return true;
            }
            else
            {
                for (Entry<Class<? extends Entity>, Boolean> entry : grabMap.entrySet())
                {
                    if (entry.getKey().isInstance(entity))
                    {
                        return entry.getValue();
                    }
                }
                return true;
            }
        }
        return false;
    }

    /** Can the entity be grabbed by something such as a robot, or another entity. By default most
     * entities can be grabbed by another object. */
    public static boolean canGrab(Class<? extends Entity> clazz)
    {
        if (grabMap.containsKey(clazz))
        {
            return grabMap.get(clazz);
        }
        else
        {
            for (Entry<Class<? extends Entity>, Boolean> entry : grabMap.entrySet())
            {
                if (entry.getKey().isAssignableFrom(clazz))
                {
                    return entry.getValue();
                }
            }
            return true;
        }
    }

    public static void setCanNotBeGrabbed(Class<? extends Entity> clazz)
    {
        grabMap.put(clazz, false);
    }

    public static void setCanBeGrabbed(Class<? extends Entity> clazz)
    {
        grabMap.put(clazz, true);
    }

    public static void addName(Class<? extends Entity> clazz, String name)
    {
        entityMap.put(name, clazz);
    }

    public static void addName(String name, Class<? extends Entity> clazz)
    {
        addName(clazz, name);
    }

    public static List<Class<? extends Entity>> getList()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public static String get(Class<? extends Entity> entityToInclude)
    {
        for (Entry<String, Class<? extends Entity>> entry : entityMap.entrySet())
        {
            if (entry.getClass() != null && entry.getClass().equals(entityToInclude))
            {
                return entry.getKey();
            }
        }
        return null;
    }
}
