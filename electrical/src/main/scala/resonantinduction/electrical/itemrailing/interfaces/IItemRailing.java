package resonantinduction.electrical.itemrailing.interfaces;

import java.util.Map;

import net.minecraft.inventory.IInventory;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.grid.INode;
import resonant.lib.render.EnumColor;
import universalelectricity.api.vector.VectorWorld;

/**
 * implement on Part Railings.
 *
 * @since 16/03/14
 * @author tgame14
 */
public interface IItemRailing extends INode
{
    public boolean canItemEnter (IItemRailingTransfer item);

    public boolean canConnectToRailing (IItemRailing railing, ForgeDirection to);

    public EnumColor getRailingColor ();

    public IItemRailing setRailingColor (EnumColor color);

	public VectorWorld getWorldPos();

	public Map<Object, ForgeDirection> getConnectionMap();

	public IInventory[] getInventoriesNearby();

	public boolean isLeaf();



}
