package resonantinduction.lib.helpers;

import java.awt.Color;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public enum ColorCode
{
	BLACK("Black", Color.black), RED("Red", Color.red), GREEN("Green", Color.green),
	BROWN("Brown", new Color(139, 69, 19)), BLUE("Blue", Color.BLUE),
	PURPLE("Purple", new Color(75, 0, 130)), CYAN("Cyan", Color.cyan),
	SILVER("Silver", new Color(192, 192, 192)), GREY("Grey", Color.gray), PINK("Pink", Color.pink),
	LIME("Lime", new Color(0, 255, 0)), YELLOW("Yellow", Color.yellow),
	LIGHTBLUE("LightBlue", new Color(135, 206, 250)), MAGENTA("Magenta", Color.magenta),
	ORANGE("Orange", Color.orange), WHITE("White", Color.white), UNKOWN("", Color.BLACK);

	public String name;
	public Color color;

	private ColorCode(String name, Color color)
	{
		this.name = name;
		this.color = color;
	}

	public String getName()
	{
		return this.name;
	}

	/**
	 * gets a ColorCode from any of the following
	 * 
	 * @param obj - Integer,String,LiquidData,ColorCode
	 * @return Color NONE if it can't find it
	 */
	public static ColorCode get(Object obj)
	{
		if (obj instanceof Integer && ((Integer) obj) < ColorCode.values().length)
		{
			return ColorCode.values()[((Integer) obj)];
		}
		else if (obj instanceof ColorCode)
		{
			return (ColorCode) obj;
		}
		else if (obj instanceof String)
		{
			for (int i = 0; i < ColorCode.values().length; i++)
			{
				if (((String) obj).equalsIgnoreCase(ColorCode.get(i).getName()))
				{
					return ColorCode.get(i);
				}
			}
		}
		return UNKOWN;
	}

	/** Used on anything that is coded for a set color for varies reasons */
	public static interface IColorCoded
	{
		/** Returns the ColorCode of the object */
		public ColorCode getColor();

		/** Sets the ColorCode of the Object */
		public boolean setColor(Object obj);
	}

	public static interface IColoredItem
	{
		/** Returns the ColorCode of the object */
		public ColorCode getColor(ItemStack stack);

		/** Sets the ColorCode of the Object */
		public boolean setColor(ItemStack stack, Object obj);
	}

	public static interface IColoredId
	{
		/** Returns the ColorCode of the object */
		public ColorCode getColor(int i);
	}

	public static interface IColoredBlock
	{
		/** Returns the ColorCode of the object */
		public ColorCode getColor(World world, int x, int y, int z);

		/** Sets the ColorCode of the Object */
		public void setColor(World world, int x, int y, int z, Object obj);
	}

}
