package resonantinduction.assemblyline.fluid;

import net.minecraftforge.fluids.Fluid;

/** These is an extension of the Fluid system forcing it to be a gas on creation
 * 
 * @author Archadia, DarkGuardsman */
public class Gas extends Fluid
{

    public Gas(String name)
    {
        super(name);
        this.isGaseous = true;
        this.density = -1000;
    }
}
