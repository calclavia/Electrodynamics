package resonantinduction.core.debug;

import javax.swing.*;
import java.awt.*;

/**
 * @author Darkguardsman
 */
@SuppressWarnings("serial")
public class UpdatePanel extends JPanel implements IUpdate
{
	public UpdatePanel()
	{
	}

	public UpdatePanel(BorderLayout borderLayout)
	{
		super(borderLayout);
	}

	@Override
	public void update()
	{
		for (Component component : getComponents())
		{
			if (component instanceof IUpdate)
			{
				((IUpdate) component).update();
			}
		}
	}
}
