package resonantinduction.mechanical.fluid.pipe;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import net.minecraftforge.common.ForgeDirection;
import resonant.api.grid.INode;
import resonant.api.grid.INodeProvider;
import resonantinduction.core.debug.FrameNodeDebug;
import resonantinduction.core.debug.UpdatePanel;
import resonantinduction.core.debug.UpdatedLabel;

/** Java GUI used to help debug pipe information
 * 
 * @author Darkguardsman */
@SuppressWarnings("serial")
public class PipeNodeFrame extends FrameNodeDebug
{
    public PipeNodeFrame(INodeProvider node)
    {
        super(node, PipePressureNode.class);
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
                return super.buildLabel() + PipeNodeFrame.this.getNode();
            }
        };
        panel.add(tickLabel);

        UpdatedLabel xLabel = new UpdatedLabel("Parent: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + (PipeNodeFrame.this.getNode() != null ? PipeNodeFrame.this.getNode().pipe() : "null");
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
                return 3;
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
                        return "Pressure";
                }
                return "---";
            }

            @Override
            public int getRowCount()
            {
                if (getNode() != null && getNode().getConnections() != null)
                {
                    return getNode().getConnections().size();
                }
                return 10;
            }

            @Override
            public Object getValueAt(int row, int col)
            {
                if (getNode() != null && getNode().getConnections() != null)
                {
                    ForgeDirection dir = (ForgeDirection) getNode().getConnections().values().toArray()[row];
                    switch (col)
                    {
                        case 0:
                            return dir;
                        case 1:
                            return getNode().getConnections().keySet().toArray()[row];
                        case 2:
                            return getNode().getPressure(dir);
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
        topPanel.setLayout(new GridLayout(1, 2, 0, 0));
        UpdatedLabel velLabel = new UpdatedLabel("Fluid: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + PipeNodeFrame.this.getNode().pipe().tank.getFluid();
            }
        };
        topPanel.add(velLabel);

        UpdatedLabel angleLabel = new UpdatedLabel("Volume: ")
        {
            @Override
            public String buildLabel()
            {
                return super.buildLabel() + PipeNodeFrame.this.getNode().pipe().tank.getFluidAmount() + "mb";
            }
        };
        topPanel.add(angleLabel);
        panel.add(topPanel, BorderLayout.NORTH);
    }

    @Override
    public PipePressureNode getNode()
    {
        INode node = super.getNode();
        if (node instanceof PipePressureNode)
        {
            return (PipePressureNode) node;
        }
        return null;
    }
}