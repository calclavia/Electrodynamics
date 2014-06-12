package resonantinduction.mechanical.energy.grid;

import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.util.Map.Entry;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;

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
    JList<String> connectionList_component = null;
    DefaultListModel<String> connectionList_model = new DefaultListModel<String>();;

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
    public void buildRight(Panel panel)
    {
        panel.setLayout(new GridLayout(2, 1, 0, 0));

        Label label = new Label("Connections");
        panel.add(label);

        AbstractListModel model = new AbstractListModel()
        {
            @Override
            public int getSize()
            {
                if (getNode() != null)
                {
                    return getNode().getConnections().size();
                }
                return 0;
            }

            @Override
            public Object getElementAt(int index)
            {
                if (getNode() != null)
                {
                    return "[" + getNode().getConnections().keySet().toArray()[index] + "@" + getNode().getConnections().values().toArray()[index] + "]";
                }
                return null;
            }
        };
        connectionList_component = new JList(model);

        panel.add(connectionList_component);
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