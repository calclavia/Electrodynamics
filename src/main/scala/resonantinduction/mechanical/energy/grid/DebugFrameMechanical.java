package resonantinduction.mechanical.energy.grid;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import net.minecraftforge.common.util.ForgeDirection;
import resonantinduction.core.debug.FrameNodeDebug;
import resonantinduction.core.debug.UpdatePanel;
import resonantinduction.core.debug.UpdatedLabel;
import universalelectricity.api.core.grid.INode;
import universalelectricity.api.core.grid.INodeProvider;

/** Java GUI used to help debug gear information
 * 
 * @author Darkguardsman */
@SuppressWarnings("serial")
public class DebugFrameMechanical extends FrameNodeDebug
{
    JList<String> connectionList_component = null;
    DefaultListModel<String> connectionList_model = new DefaultListModel<String>();;

    public DebugFrameMechanical(INodeProvider node)
    {
        super(node, MechanicalNode.class);
    }

    @Override
    public void buildTop(UpdatePanel panel)
    {
        panel.setLayout(new GridLayout(1, 2, 0, 0));
        UpdatedLabel tickLabel = new UpdatedLabel("Node: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + DebugFrameMechanical.this.getNode();
            }
        };
        panel.add(tickLabel);

        UpdatedLabel xLabel = new UpdatedLabel("Parent: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + (DebugFrameMechanical.this.getNode() != null ? DebugFrameMechanical.this.getNode().getParent() : "null");
            }
        };
        panel.add(xLabel);
    }

    @Override
    public void buildCenter(UpdatePanel panel)
    {        
        panel.setLayout(new BorderLayout());
        TableModel dataModel = new AbstractTableModel()
        {
            @Override
            public int getColumnCount()
            {
                return 4;
            }

            @Override
            public String getColumnName(int column)
            {
                switch (column)
                {
                    case 0:
                        return "Direction";
                    case 1:
                        return "Tile";
                    case 2:
                        return "Force";
                    case 3:
                        return "Speed";
                }
                return "---";
            }

            @Override
            public int getRowCount()
            {
                if (getNode() != null && getNode().getConnections(MechanicalNode.class) != null)
                {
                    return getNode().getConnections(MechanicalNode.class).size();
                }
                return 10;
            }

            @Override
            public Object getValueAt(int row, int col)
            {
                if (getNode() != null && getNode().getConnections(MechanicalNode.class) != null)
                {
                    ForgeDirection dir = (ForgeDirection) getNode().getConnections(MechanicalNode.class).values().toArray()[row];
                    MechanicalNode node = (MechanicalNode) getNode().getConnections(MechanicalNode.class).keySet().toArray()[row];
                    switch(col)
                    {
                        case 0: return dir;
                        case 1: return node;
                        case 2: return node.getForce(dir);
                        case 3: return node.getAngularSpeed(dir);
                    }
                }
                return "00000";
            }
        };
        JTable table = new JTable(dataModel);
        table.setAutoCreateRowSorter(true);
        JScrollPane tableScroll = new JScrollPane(table);
        Dimension tablePreferred = tableScroll.getPreferredSize();
        tableScroll.setPreferredSize(new Dimension(tablePreferred.width, tablePreferred.height / 3));

        panel.add(tableScroll, BorderLayout.SOUTH);
        
        UpdatePanel topPanel = new UpdatePanel();
        topPanel.setLayout(new GridLayout(1, 3, 0, 0));
        
        UpdatedLabel velLabel = new UpdatedLabel("Vel: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + DebugFrameMechanical.this.getNode().angularVelocity;
            }
        };
        topPanel.add(velLabel);

        UpdatedLabel angleLabel = new UpdatedLabel("Angle: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + DebugFrameMechanical.this.getNode().renderAngle;
            }
        };
        topPanel.add(angleLabel);

        UpdatedLabel torqueLabel = new UpdatedLabel("Torque: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + DebugFrameMechanical.this.getNode().torque;
            }
        };
        topPanel.add(torqueLabel);
        panel.add(topPanel, BorderLayout.NORTH);
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