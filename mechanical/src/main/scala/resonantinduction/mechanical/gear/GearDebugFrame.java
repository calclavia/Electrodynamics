package resonantinduction.mechanical.gear;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.energy.grid.MechanicalNode;
import resonantinduction.mechanical.energy.grid.PartMechanical;

/** Java GUI used to help debug gear information
 * 
 * @author Darkguardsman */
@SuppressWarnings("serial")
public class GearDebugFrame extends Frame implements ActionListener
{
    List<DataLabel> dataLabels = new ArrayList<DataLabel>();
    Label[] connections = new Label[20];

    long tick = 0;
    PartMechanical part = null;

    public GearDebugFrame(PartMechanical part)
    {
        this.part = part;
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        //Top bar
        Panel topPanel = new Panel(new GridLayout(1, 4, 0, 0));

        DataLabel tickLabel = new DataLabel()
        {
            @Override
            public void update()
            {
                setText("Tick: " + tick);
            }
        };
        topPanel.add(tickLabel);

        DataLabel xLabel = new DataLabel()
        {
            @Override
            public void update()
            {
                setText("X: " + GearDebugFrame.this.part.x());
            }
        };
        topPanel.add(xLabel);
        DataLabel yLabel = new DataLabel()
        {
            @Override
            public void update()
            {
                setText("Y: " + GearDebugFrame.this.part.y());
            }
        };
        topPanel.add(yLabel);
        DataLabel zLabel = new DataLabel()
        {
            @Override
            public void update()
            {
                setText("Z: " + GearDebugFrame.this.part.z());
            }
        };
        topPanel.add(zLabel);
        add(topPanel, BorderLayout.NORTH);

        //Middle bar
        Panel middlePanel = new Panel(new GridLayout(8, 1, 0, 0));

        DataLabel velLabel = new DataLabel()
        {
            @Override
            public void update()
            {
                setText("Vel: " + GearDebugFrame.this.part.node.angularVelocity);
            }
        };
        middlePanel.add(velLabel);

        DataLabel angleLabel = new DataLabel()
        {
            @Override
            public void update()
            {
                setText("Angle: " + GearDebugFrame.this.part.node.renderAngle);
            }
        };
        middlePanel.add(angleLabel);

        DataLabel torqueLabel = new DataLabel()
        {
            @Override
            public void update()
            {
                setText("Torque: " + GearDebugFrame.this.part.node.torque);
            }
        };
        middlePanel.add(torqueLabel);

        add(middlePanel, BorderLayout.EAST);

        Panel connectionPanel = new Panel(new GridLayout(this.connections.length / 4, 4, 0, 0));
        for (int i = 0; i < connections.length; i++)
        {
            this.connections[i] = new Label("Connection" + i + ":  null");
            connectionPanel.add(connections[i]);
        }
        add(connectionPanel, BorderLayout.WEST);

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

    /** Called each cpu cycle */
    public void update()
    {
        tick++;
        if (this.part != null)
        {
            for (DataLabel label : dataLabels)
            {
                label.update();
            }
            int c = 0;
            for (Entry<MechanicalNode, ForgeDirection> entry : part.node.getConnections().entrySet())
            {
                this.connections[c].setText("Connection" + c + ": " + entry.getKey());
                c++;
            }
            for (int i = c; i < connections.length; i++)
            {
                this.connections[c].setText("Connection" + c + ": NONE");
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

    @Override
    public void actionPerformed(ActionEvent arg0)
    {
        // TODO Auto-generated method stub

    }
}