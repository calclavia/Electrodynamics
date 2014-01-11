package resonantinduction.api.events;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.Event;
import universalelectricity.api.vector.Vector3;

/** Prefab for all machine based events
 * 
 * @author DarkGuardsman */
public class MachineEvent extends Event
{
    public final World world;
    public final Vector3 spot;
    public TileEntity machine;

    public MachineEvent(World world, Vector3 spot)
    {
        this.world = world;
        this.spot = spot;
        machine = spot.getTileEntity(world);
    }

    public MachineEvent(TileEntity machine)
    {
        this.world = machine.worldObj;
        this.spot = new Vector3(machine);
        this.machine = machine;
    }
}
