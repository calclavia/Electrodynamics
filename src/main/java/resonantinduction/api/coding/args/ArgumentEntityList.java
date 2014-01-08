package resonantinduction.api.coding.args;

import net.minecraft.entity.Entity;

public class ArgumentEntityList extends ArgumentListData<Class<? extends Entity>>
{

    public ArgumentEntityList(String name, Object defaultvalue, Class<? extends Entity>... object)
    {
        super(name, defaultvalue, object);
    }

}
