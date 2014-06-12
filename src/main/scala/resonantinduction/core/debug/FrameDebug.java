package resonantinduction.core.debug;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import net.minecraft.tileentity.TileEntity;

/** @author Darkguardsman */
@SuppressWarnings("serial")
public class FrameDebug extends Frame
{
    /** Linked tile */
    TileEntity tile = null;
   
    boolean debugNode = false;
    protected long tick = 0;

    public FrameDebug(TileEntity tile)
    {
        this();
        this.tile = tile;
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

        setLayout(new BorderLayout());

        buildTop(topPanel);
        buildBottom(botPanel);
        buildLeft(leftPanel);
        buildRight(rightPanel);

        this.add(topPanel, BorderLayout.NORTH);
        this.add(botPanel, BorderLayout.SOUTH);
        this.add(rightPanel, BorderLayout.EAST);
        this.add(leftPanel, BorderLayout.WEST);

        //exit icon handler
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                Frame f = (Frame) e.getSource();
                f.setVisible(false);
                f.dispose();
            }
        });
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
        tick++;
        if (tick >= Long.MAX_VALUE)
        {
            tick = 0;
        }

        for (Component component : getComponents())
        {
            if (component instanceof IUpdate)
            {
                ((IUpdate) component).update();
            }
        }
    }  

    /** Shows the frame */
    public void showDebugFrame()
    {
        setTitle("Resonant Engine Debug Window");
        setBounds(200, 200, 450, 600);
        setVisible(true);
    }

    /** Hides the frame and tells it to die off */
    public void closeDebugFrame()
    {
        dispose();
    }
}
