package com.builtbroken.assemblyline.api;

import java.util.List;

import com.builtbroken.assemblyline.api.coding.args.ArgumentData;

/** The IUseable inteface is used by the ArmBot so that it may interact with Tile Entities. onUse
 * will be called on the block an ArmBot is touching whenever the USE command is run on it.
 * 
 * @author Briman0094 */
public interface IArmbotUseable
{

    /** Called when the ArmBot command "USE" is run. This is called on any IUseable the ArmBot is
     * touching.
     * 
     * @param armbot - The Armbot instance.
     * 
     * @return true if the use was completed correctly */
    public boolean onUse(IArmbot armbot, List<ArgumentData> list);

}
