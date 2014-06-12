package resonantinduction.core.debug;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Panel;

import net.minecraft.tileentity.TileEntity;
import resonant.api.grid.INode;

@SuppressWarnings("serial")
public class FrameDebug extends Frame
{
    /** Linked tile */
    TileEntity tile = null;
    /** Linked node */
    INode node = null;
    /** Are we debugging a node */
    boolean debugNode = false;

    public FrameDebug(TileEntity tile)
    {
        this();
        this.tile = tile;
    }

    public FrameDebug(INode node)
    {
        this();
        this.node = node;
    }

    protected FrameDebug()
    {
        buildGUI();
    }

    /** Called to build the base of the GUI */
    protected void buildGUI()
    {
        UpdatePanel topPanel = new UpdatePanel();
        UpdatePanel botPanel = new UpdatePanel();
        UpdatePanel leftPanel = new UpdatePanel();
        UpdatePanel rightPanel = new UpdatePanel();

        buildTop(topPanel);
        buildBottom(botPanel);
        buildLeft(leftPanel);
        buildRight(rightPanel);

        this.add(topPanel, BorderLayout.NORTH);
        this.add(botPanel, BorderLayout.SOUTH);
        this.add(rightPanel, BorderLayout.EAST);
        this.add(leftPanel, BorderLayout.WEST);
    }

    /** Top are of the Frame */
    public void buildTop(Panel panel)
    {

    }

    /** Bottom are of the Frame */
    public void buildBottom(Panel panel)
    {

    }

    /** Left are of the Frame */
    public void buildLeft(Panel panel)
    {

    }

    /** Right are of the Frame */
    public void buildRight(Panel panel)
    {

    }

    /** Called each tick by the host of this GUI */
    public void update()
    {
        for(Component component : getComponents())
        {
            if(component instanceof IUpdate)
            {
                ((IUpdate)component).update();
            }
        }
    }
}
