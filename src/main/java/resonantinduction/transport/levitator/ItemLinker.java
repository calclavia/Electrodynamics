/**
 * 
 */
package resonantinduction.transport.levitator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import resonantinduction.ResonantInduction;
import resonantinduction.TabRI;
import universalelectricity.api.vector.VectorWorld;
import calclavia.lib.prefab.item.ItemCoordLink;

/**
 * @author Calclavia
 * 
 */
public class ItemLinker extends ItemCoordLink
{
	public ItemLinker(int id)
	{
		super(id, "linker", ResonantInduction.CONFIGURATION, ResonantInduction.PREFIX, TabRI.INSTANCE);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
	{
		if (!world.isRemote)
		{
			player.addChatMessage("Set link to block [" + x + ", " + y + ", " + z + "], Dimension: '" + world.getWorldInfo().getWorldName() + "'");
			this.setLink(stack, new VectorWorld(world, x, y, z));
		}

		return true;
	}
}
