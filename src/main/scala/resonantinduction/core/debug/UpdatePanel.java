package resonantinduction.core.debug;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Panel;

import javax.swing.JPanel;

/** @author Darkguardsman */
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
