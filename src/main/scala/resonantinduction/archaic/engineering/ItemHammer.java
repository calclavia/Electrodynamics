package resonantinduction.archaic.engineering;

import net.minecraft.item.Item;

/** Item used to interact with engineering table to crush ore */
public class ItemHammer extends Item
{
    public ItemHammer(int id)
    {
        super(id);
        setMaxStackSize(1);
        setMaxDamage(400);
    }
}
