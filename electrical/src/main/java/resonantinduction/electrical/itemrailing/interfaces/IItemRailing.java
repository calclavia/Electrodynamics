package resonantinduction.electrical.itemrailing.interfaces;

import calclavia.lib.render.EnumColor;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

/**
 * implement on Part Railings.
 *
 * @since 16/03/14
 * @author tgame14
 */
public interface IItemRailing
{
    public boolean canItemEnter (IItemRailingTransfer item);

    public boolean canConnectToRailing (IItemRailing railing, ForgeDirection to);

    public EnumColor getRailingColor ();

    public IItemRailing setRailingColor (EnumColor color);

    /** an easy implementation for tiles / parts that already have this method in them */
    public World getWorldObj ();

    public int x ();

    public int y ();

    public int z ();



}
