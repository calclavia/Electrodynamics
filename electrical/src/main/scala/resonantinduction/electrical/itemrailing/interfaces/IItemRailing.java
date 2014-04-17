package resonantinduction.electrical.itemrailing.interfaces;

import calclavia.lib.grid.INode;
import calclavia.lib.render.EnumColor;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.VectorWorld;

import java.util.Map;

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

	public Map<IItemRailing, ForgeDirection> getConnectionMap();

	public IInventory[] getInventoriesNearby();

	public boolean isLeaf();



}
