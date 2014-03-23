package resonantinduction.core.render;

import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.VariableTransformation;

public class InvertX extends VariableTransformation
{
	public InvertX()
	{
		super(new Matrix4(1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1));
	}

	@Override
	public Transformation inverse()
	{
		return this;
	}

	@Override
	public void apply(codechicken.lib.vec.Vector3 vec)
	{
		vec.x = -vec.x;
	}
}