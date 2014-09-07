package resonantinduction.core.debug;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.core.transform.vector.IVectorWorld;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Darkguardsman
 */
@SuppressWarnings("serial")
public class FrameDebug extends Frame implements IVectorWorld
{
	protected long tick = 0;
	/**
	 * Linked tile
	 */
	TileEntity tile = null;
	boolean debugNode = false;

	public FrameDebug(TileEntity tile)
	{
		this();
		this.tile = tile;
	}

	protected FrameDebug()
	{
		buildGUI();
	}

	/**
	 * Called to build the base of the GUI
	 */
	protected void buildGUI()
	{
		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		UpdatePanel topPanel = new UpdatePanel();
		UpdatePanel botPanel = new UpdatePanel();
		UpdatePanel rightPanel = new UpdatePanel();

		topPanel.setBorder(loweredetched);
		botPanel.setBorder(loweredetched);
		rightPanel.setBorder(loweredetched);

		buildTop(topPanel);
		buildBottom(botPanel);
		buildCenter(rightPanel);

		this.add(topPanel, BorderLayout.NORTH);
		this.add(botPanel, BorderLayout.SOUTH);
		this.add(rightPanel, BorderLayout.CENTER);

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

	/**
	 * Top are of the Frame
	 */
	public void buildTop(UpdatePanel panel)
	{
		panel.setLayout(new GridLayout(1, 2, 0, 0));
		UpdatedLabel tickLabel = new UpdatedLabel("Tile: ")
		{
			@Override
			public String buildLabel()
			{
				return super.buildLabel() + tile;
			}
		};
		panel.add(tickLabel);
	}

	/**
	 * Bottom are of the Frame
	 */
	public void buildBottom(UpdatePanel panel)
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
				return super.buildLabel() + x();
			}
		};
		panel.add(xLabel);

		UpdatedLabel yLabel = new UpdatedLabel("Y: ")
		{
			@Override
			public String buildLabel()
			{
				return super.buildLabel() + y();
			}
		};
		panel.add(yLabel);

		UpdatedLabel zLabel = new UpdatedLabel("Z: ")
		{
			@Override
			public String buildLabel()
			{
				return super.buildLabel() + z();
			}
		};
		panel.add(zLabel);
	}

	/**
	 * Left are of the Frame
	 */
	public void buildCenter(UpdatePanel panel)
	{
		panel.setLayout(new GridLayout(1, 4, 0, 0));
		UpdatedLabel tickLabel = new UpdatedLabel("Valid: ")
		{
			@Override
			public String buildLabel()
			{
				return super.buildLabel() + (tile != null ? tile.isInvalid() : "null");
			}
		};
		panel.add(tickLabel);
		UpdatedLabel block_label = new UpdatedLabel("BLOCK: ")
		{
			@Override
			public String buildLabel()
			{
				return super.buildLabel() + (tile != null ? tile.getBlockType() : "null");
			}
		};
		panel.add(block_label);

		UpdatedLabel meta_label = new UpdatedLabel("META: ")
		{
			@Override
			public String buildLabel()
			{
				return super.buildLabel() + (tile != null && tile.getBlockType() != null ? tile.getBlockType() : "-");
			}
		};
		panel.add(meta_label);

		UpdatedLabel id_label = new UpdatedLabel("ID: ")
		{
			@Override
			public String buildLabel()
			{
				return super.buildLabel() + (tile != null && tile.getBlockType() != null ? tile.getBlockType() : "-");
			}
		};
		panel.add(id_label);

	}

	/**
	 * Called each tick by the host of this GUI
	 */
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

	/**
	 * Shows the frame
	 */
	public void showDebugFrame()
	{
		setTitle("Resonant Engine Debug Window");
		setBounds(200, 200, 450, 600);
		setVisible(true);
	}

	/**
	 * Hides the frame and tells it to die off
	 */
	public void closeDebugFrame()
	{
		dispose();
	}

	@Override
	public double z()
	{
		return tile != null ? tile.zCoord : 0;
	}

	@Override
	public double x()
	{
		return tile != null ? tile.xCoord : 0;
	}

	@Override
	public double y()
	{
		return tile != null ? tile.yCoord : 0;
	}

	@Override
	public World world()
	{
		return tile != null ? tile.getWorldObj() : null;
	}
}
