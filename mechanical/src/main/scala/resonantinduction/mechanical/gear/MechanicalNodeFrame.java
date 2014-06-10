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
import resonantinduction.mechanical.energy.grid.TileMechanical;

/** Java GUI used to help debug gear information
 * 
 * @author Darkguardsman */
@SuppressWarnings("serial")
public class MechanicalNodeFrame extends Frame implements ActionListener
{
    List<UpdatedLabel> dataLabels = new ArrayList<UpdatedLabel>();
    Label[] connections = new Label[10];

    long tick = 0;
    PartMechanical partMechanical = null;
    TileMechanical tileMechanical = null;

    public MechanicalNodeFrame(TileMechanical tile)
    {        
        this();
        this.tileMechanical = tile;
        
    }
    
    public MechanicalNodeFrame(PartMechanical part)
    {
        this();
        this.partMechanical = part;
    }
    
    protected MechanicalNodeFrame()
    {
        setLayout(new BorderLayout());
        setBackground(Color.LIGHT_GRAY);
        this.createBottomBar();
        this.createTopBar();

        //Middle bar
        Panel middlePanel = new Panel(new GridLayout(3, 1, 0, 0));

        UpdatedLabel velLabel = new UpdatedLabel("Vel: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + MechanicalNodeFrame.this.getNode().angularVelocity;
            }
        };
        dataLabels.add(velLabel);
        middlePanel.add(velLabel);

        UpdatedLabel angleLabel = new UpdatedLabel("Angle: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + MechanicalNodeFrame.this.getNode().renderAngle;
            }
        };
        dataLabels.add(angleLabel);
        middlePanel.add(angleLabel);

        UpdatedLabel torqueLabel = new UpdatedLabel("Torque: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + MechanicalNodeFrame.this.getNode().torque;
            }
        };
        dataLabels.add(torqueLabel);
        middlePanel.add(torqueLabel);

        add(middlePanel, BorderLayout.WEST);

        Panel connectionPanel = new Panel(new GridLayout(this.connections.length / 2, 2, 0, 0));
        for (int i = 0; i < connections.length; i++)
        {
            this.connections[i] = new Label("Connection" + i + ":  ----");
            connectionPanel.add(connections[i]);
        }
        add(connectionPanel, BorderLayout.EAST);

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

    public MechanicalNode getNode()
    {
        if(partMechanical != null)
        {
            return partMechanical.node;
        }
        return tileMechanical.mechanicalNode;
    }

    public void createTopBar()
    {
        Panel panel = new Panel(new GridLayout(1, 2, 0, 0));

        UpdatedLabel tickLabel = new UpdatedLabel("Node: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + MechanicalNodeFrame.this.getNode();
            }
        };
        dataLabels.add(tickLabel);
        panel.add(tickLabel);

        UpdatedLabel xLabel = new UpdatedLabel("Parent: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + (MechanicalNodeFrame.this.getNode() != null ? MechanicalNodeFrame.this.getNode().getParent() : "null");
            }
        };
        dataLabels.add(xLabel);
        panel.add(xLabel);

        add(panel, BorderLayout.NORTH);
    }

    public void createBottomBar()
    {
        Panel bottomPanel = new Panel(new GridLayout(1, 4, 0, 0));

        UpdatedLabel tickLabel = new UpdatedLabel("Tick: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + tick;
            }
        };
        dataLabels.add(tickLabel);
        bottomPanel.add(tickLabel);

        UpdatedLabel xLabel = new UpdatedLabel("X: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + MechanicalNodeFrame.this.getNode().x();
            }
        };
        dataLabels.add(xLabel);
        bottomPanel.add(xLabel);

        UpdatedLabel yLabel = new UpdatedLabel("Y: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + MechanicalNodeFrame.this.getNode().y();
            }
        };
        bottomPanel.add(yLabel);
        dataLabels.add(yLabel);

        UpdatedLabel zLabel = new UpdatedLabel("Z: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + MechanicalNodeFrame.this.getNode().z();
            }
        };
        bottomPanel.add(zLabel);
        dataLabels.add(zLabel);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /** Called each cpu cycle */
    public void update()
    {
        tick++;
        if (this.getNode() != null)
        {
            for (UpdatedLabel label : dataLabels)
            {
                label.update();
            }
            int c = 0;
            for (Entry<MechanicalNode, ForgeDirection> entry : getNode().getConnections().entrySet())
            {
                if (entry.getKey() != null)
                {
                    this.connections[c].setText("Connection" + c + ": " + entry.getKey());
                    c++;
                }
            }
            for (int i = c; i < connections.length; i++)
            {
                this.connections[i].setText("Connection" + i + ": NONE");
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