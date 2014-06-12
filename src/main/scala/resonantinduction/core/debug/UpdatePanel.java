package resonantinduction.core.debug;

import java.awt.Component;
import java.awt.Panel;

@SuppressWarnings("serial")
public class UpdatePanel extends Panel implements IUpdate
{
    @Override
    public void update()
    {
        for(Component component : getComponents())
        {
            if(component instanceof IUpdate)
            {
                ((IUpdate)component).update();
            }
        }
    }
}
