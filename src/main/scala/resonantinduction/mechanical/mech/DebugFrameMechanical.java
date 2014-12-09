package resonantinduction.mechanical.mech;

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
import resonant.lib.debug.FrameNodeDebug;
import resonant.lib.debug.UpdatePanel;
import resonant.lib.debug.UpdatedLabel;
import resonant.api.tile.node.INode;
import resonant.api.tile.INodeProvider;
import resonantinduction.mechanical.mech.grid.NodeMechanical;

/** Java GUI used to help debug gear information
 *
 * @author Darkguardsman */
@SuppressWarnings("serial")
//Don't convert to scala as this will find its way into RE later - From Darkguardsman
public class DebugFrameMechanical extends FrameNodeDebug
{
    JList<String> connectionList_component = null;
    DefaultListModel<String> connectionList_model = new DefaultListModel<String>();;

    public DebugFrameMechanical(INodeProvider node)
    {
        super(node, NodeMechanical.class);
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
                if (getNode() != null && getNode().connections() != null)
                {
                    return getNode().connections().size();
                }
                return 10;
            }

            @Override
            public Object getValueAt(int row, int col)
            {
                if (getNode() != null && getNode().connections() != null)
                {
                    ForgeDirection dir = (ForgeDirection) getNode().directionMap().values().toArray()[row];
                    NodeMechanical node = (NodeMechanical) getNode().directionMap().keySet().toArray()[row];
                    switch(col)
                    {
                        case 0: return dir;
                        case 1: return node;
                        case 2: return node.torque();
                        case 3: return node.angularVelocity();
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
                return super.buildLabel() + DebugFrameMechanical.this.getNode().angularVelocity();
            }
        };
        topPanel.add(velLabel);

        UpdatedLabel angleLabel = new UpdatedLabel("Angle: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + DebugFrameMechanical.this.getNode().angle();
            }
        };
        topPanel.add(angleLabel);

        UpdatedLabel torqueLabel = new UpdatedLabel("Torque: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + DebugFrameMechanical.this.getNode().torque();
            }
        };
        topPanel.add(torqueLabel);
        panel.add(topPanel, BorderLayout.NORTH);
    }

    @Override
    public NodeMechanical getNode()
    {
        INode node = super.getNode();
        if (node instanceof NodeMechanical)
        {
            return (NodeMechanical) node;
        }
        return null;
    }
}