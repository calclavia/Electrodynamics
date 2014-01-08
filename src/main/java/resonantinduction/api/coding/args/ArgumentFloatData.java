package resonantinduction.api.coding.args;

import net.minecraft.util.MathHelper;
import universalelectricity.api.energy.UnitDisplay;

/**
 * Used to create argument data for the encoder. Should only be used if the value needs to be
 * clearly limited inside the encoder display.
 * 
 * @author DarkGuardsman
 */
public class ArgumentFloatData extends ArgumentData
{
	protected float max, min;

	public ArgumentFloatData(String name, float value, float max, float min)
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
			float value = (float) MathHelper.parseDoubleWithDefault("" + object, min - 100);
			return value != min - 100 && value >= min && value <= max;
		}
		return false;
	}

	@Override
	public String warning()
	{
		return "" + UnitDisplay.roundDecimals(min, 2) + " - " + UnitDisplay.roundDecimals(max, 2);
	}
}
