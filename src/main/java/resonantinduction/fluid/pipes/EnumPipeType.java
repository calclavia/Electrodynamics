package resonantinduction.fluid.pipes;

import com.builtbroken.minecraft.helpers.ColorCode;
import com.builtbroken.minecraft.helpers.ColorCode.IColoredId;

public enum EnumPipeType implements IColoredId
{
	Base(0, 0, true), COLOR(new IPipeType()
	{
		@Override
		public ColorCode getColor(int meta)
		{
			return ColorCode.get(meta);
		}

		@Override
		public String getName(int pipeID)
		{
			if (pipeID < 16 && pipeID > 0)
			{
				return ColorCode.get(pipeID - 1).name;
			}
			return "";
		}
	}, 1, 16, true);

	private IPipeType type;
	public int metaStart = 1;
	public int metaEnd = 16;
	public boolean canColor = false;

	private EnumPipeType()
	{
		this.metaStart = this.ordinal() * 16;
		this.metaEnd = this.metaStart + 15;
	}

	private EnumPipeType(int metaStart, int metaEnd, boolean canColor)
	{

	}

	private EnumPipeType(IPipeType type, int metaStart, int metaEnd, boolean canColor)
	{
		this.type = type;
		this.metaStart = metaStart;
		this.metaEnd = metaEnd;
		this.canColor = canColor;
	}

	public static EnumPipeType get(int meta)
	{
		for (EnumPipeType type : EnumPipeType.values())
		{
			if (meta >= type.metaStart && meta <= type.metaEnd)
			{
				return type;
			}
		}
		return null;
	}

	public static boolean canColor(int meta)
	{
		EnumPipeType type = get(meta);
		if (type != null)
		{
			return type.canColor;
		}
		return false;
	}

	public static int getUpdatedID(int pipeID, ColorCode newColor)
	{
		if (pipeID == 0)
		{
			return 1 + newColor.ordinal();
		}
		return pipeID;
	}

	public static ColorCode getColorCode(int meta)
	{
		EnumPipeType type = get(meta);
		if (type != null)
		{
			return type.getColor(meta);
		}
		return ColorCode.UNKOWN;
	}

	@Override
	public ColorCode getColor(int meta)
	{
		if (type != null)
		{
			return type.getColor(meta);
		}
		return ColorCode.UNKOWN;
	}

	public String getName(int pipeID)
	{
		if (type != null)
		{
			return type.getName(pipeID);
		}
		return "";
	}
}
