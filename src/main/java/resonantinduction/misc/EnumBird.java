package resonantinduction.misc;

import java.awt.Color;

import resonantinduction.AssemblyLine;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import cpw.mods.fml.common.registry.EntityRegistry;

/** Enum of different birds that can be spawned by an egg or in the world threw normal spawning
 * 
 * @author DarkGuardsman */
public enum EnumBird
{

    VANILLA_CHICKEN("", new SpawnEntity()
    {
        @Override
        public void register()
        {
            //Vanilla chicken is already registered
        }

        @Override
        public Entity getNew(World world)
        {
            return new EntityChicken(world);
        }

    }),
    TURKEY("Turkey", new SpawnEntity()
    {
        @Override
        public void register()
        {
            EntityRegistry.registerGlobalEntityID(EntityTurkey.class, "FTTurkey", EntityRegistry.findGlobalUniqueEntityId(), 5651507, Color.red.getRGB());
            EntityRegistry.registerModEntity(EntityTurkey.class, "FTTurkey", AssemblyLine.entitiesIds++, AssemblyLine.instance, 64, 1, true);
            EntityRegistry.addSpawn(EntityTurkey.class, 3, 1, 10, EnumCreatureType.creature, BiomeGenBase.forest, BiomeGenBase.river);
        }

        @Override
        public Entity getNew(World world)
        {
            return new EntityTurkey(world);
        }
    });

    private SpawnEntity entity;
    private String name;

    private EnumBird(String name, SpawnEntity entity)
    {
        this.entity = entity;
        this.name = name;
    }

    public Entity getNewEntity(World world)
    {
        return this.entity.getNew(world);
    }

    public void register()
    {
        this.entity.register();
    }

    public static Entity getNewEntity(World world, int id)
    {
        if (get(id) != null)
        {
            return get(id).getNewEntity(world);
        }
        return null;
    }

    public static EnumBird get(int id)
    {
        if (id >= 0 && id < EnumBird.values().length)
        {
            return EnumBird.values()[id];
        }
        return null;
    }

    public String getName()
    {
        return this.name;
    }

    /** Quick interface to allow each enum to define how the entity is created without using
     * reflection */
    public static interface SpawnEntity
    {
        /** Anything that needs registered including spawning and event calls */
        public void register();

        /** Return a new entity for this enum instance */
        public Entity getNew(World world);
    }
}
