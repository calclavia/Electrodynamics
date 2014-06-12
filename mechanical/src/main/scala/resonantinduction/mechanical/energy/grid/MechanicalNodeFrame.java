package resonantinduction.mechanical.energy.grid;

import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.util.Map.Entry;

import net.minecraftforge.common.ForgeDirection;
import resonant.api.grid.INode;
import resonant.api.grid.INodeProvider;
import resonantinduction.core.debug.FrameNodeDebug;
import resonantinduction.core.debug.UpdatedLabel;

/** Java GUI used to help debug gear information
 * 
 * @author Darkguardsman */
@SuppressWarnings("serial")
public class MechanicalNodeFrame extends FrameNodeDebug
{
    Label[] connections;

    public MechanicalNodeFrame(INodeProvider node)
    {
        super(node, MechanicalNode.class);
    }

    @Override
    public void buildTop(Panel panel)
    {
        panel.setLayout(new GridLayout(1, 2, 0, 0));
        UpdatedLabel tickLabel = new UpdatedLabel("Node: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + MechanicalNodeFrame.this.getNode();
            }
        };
        panel.add(tickLabel);

        UpdatedLabel xLabel = new UpdatedLabel("Parent: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + (MechanicalNodeFrame.this.getNode() != null ? MechanicalNodeFrame.this.getNode().getParent() : "null");
            }
        };
        panel.add(xLabel);
    }

    @Override
    public void buildBottom(Panel panel)
    {
        panel.setLayout(new GridLayout(1, 4, 0, 0));
        UpdatedLabel tickLabel = new UpdatedLabel("Tick: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + tick;
            }
        };
        panel.add(tickLabel);

        UpdatedLabel xLabel = new UpdatedLabel("X: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + (MechanicalNodeFrame.this.getNode() != null ? MechanicalNodeFrame.this.getNode().x() : 0);
            }
        };
        panel.add(xLabel);

        UpdatedLabel yLabel = new UpdatedLabel("Y: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + (MechanicalNodeFrame.this.getNode() != null ? MechanicalNodeFrame.this.getNode().y() : 0);
            }
        };
        panel.add(yLabel);

        UpdatedLabel zLabel = new UpdatedLabel("Z: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + (MechanicalNodeFrame.this.getNode() != null ? MechanicalNodeFrame.this.getNode().z() : 0);
            }
        };
        panel.add(zLabel);
    }

    @Override
    public void buildRight(Panel panel)
    {
        connections = new Label[10];
        panel.setLayout(new GridLayout(5, 2, 0, 0));
        for (int i = 0; i < connections.length; i++)
        {
            this.connections[i] = new Label("Connection" + i + ":  ----");
            panel.add(connections[i]);
        }
    }

    @Override
    public void buildLeft(Panel panel)
    {
        panel.setLayout(new GridLayout(3, 1, 0, 0));
        UpdatedLabel velLabel = new UpdatedLabel("Vel: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + MechanicalNodeFrame.this.getNode().angularVelocity;
            }
        };
        panel.add(velLabel);

        UpdatedLabel angleLabel = new UpdatedLabel("Angle: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + MechanicalNodeFrame.this.getNode().renderAngle;
            }
        };
        panel.add(angleLabel);

        UpdatedLabel torqueLabel = new UpdatedLabel("Torque: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + MechanicalNodeFrame.this.getNode().torque;
            }
        };
        panel.add(torqueLabel);
    }

    @Override
    public void update()
    {
        super.update();
        if (this.getNode() != null && connections != null)
        {
            int c = 0;
            for (Entry<MechanicalNode, ForgeDirection> entry : getNode().getConnections().entrySet())
            {
                if (entry.getKey() != null && this.connections[c] != null)
                {
                    this.connections[c].setText("Connection" + c + ": " + entry.getKey());
                    c++;
                }
            }
            for (int i = c; i < connections.length; i++)
            {
                if (this.connections[c] != null)
                    this.connections[i].setText("Connection" + i + ": NONE");
            }
        }
    }

    @Override
    public MechanicalNode getNode()
    {
        INode node = super.getNode();
        if (node instanceof MechanicalNode)
        {
            return (MechanicalNode) node;
        }
        return null;
    }
}