package resonantinduction.api.coding.args;

import net.minecraft.util.MathHelper;

/** Used to create argument data for the encoder. Should only be used if the value needs to be
 * clearly limited inside the encoder display.
 * 
 * @author DarkGuardsman */
public class ArgumentIntData extends ArgumentData
{
    protected int max, min;

    public ArgumentIntData(String name, int value, int max, int min)
    {
        super(name, value);
        this.max = max;
        this.min = min;
    }

    @Override
    public boolean isValid(Object object)
    {
        if (super.isValid())
        {
            int value = MathHelper.parseIntWithDefault("" + object, min - 100);
            return value != min - 100 && value >= min && value <= max;
        }
        return false;
    }

    @Override
    public String warning()
    {
        return "" + min + " - " + max;
    }
}
