/**
 * 
 */
package resonantinduction.multimeter;

import net.minecraft.block.material.Material;
import resonantinduction.base.BlockBase;

/**
 * A block that detects power.
 * 
 * @author Calclavia
 * 
 */
public class BlockMultimeter extends BlockBase
{
	public BlockMultimeter(int id)
	{
		super("multimeter", id, Material.iron);
	}

}
