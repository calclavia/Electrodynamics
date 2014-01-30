/**
 * 
 */
package resonantinduction.core.prefab.block;

import net.minecraft.block.material.Material;
import net.minecraftforge.common.Configuration;
import resonantinduction.core.Reference;
import resonantinduction.core.TabRI;
import resonantinduction.core.Settings;
import universalelectricity.api.UniversalElectricity;
import calclavia.lib.prefab.block.BlockSidedIO;

/** Blocks that have specific sided input and output should extend this.
 * 
 * @author Calclavia */
public class BlockIOBase extends BlockSidedIO
{
    public BlockIOBase(String name)
    {
        this(name, Settings.getNextBlockID());
    }

    public BlockIOBase(String name, int id)
    {
        this(name, id, UniversalElectricity.machine);
    }

    public BlockIOBase(String name, int id, Material material)
    {
        super(Settings.CONFIGURATION.get(Configuration.CATEGORY_BLOCK, name, id).getInt(id), material);
        this.setCreativeTab(TabRI.CORE);
        this.setUnlocalizedName(Reference.PREFIX + name);
        this.setTextureName(Reference.PREFIX + name);
        this.setHardness(1f);
    }
}
