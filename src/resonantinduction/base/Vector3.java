/**
 * 
 */
package resonantinduction.base;

import net.minecraft.tileentity.TileEntity;

/**
 * @author Calclavia
 * 
 */
public class Vector3
{
	public double x, y, z;

	public Vector3(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3()
	{
		this(0, 0, 0);
	}

	public Vector3(TileEntity tileEntity)
	{
		this(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
	}

	public Vector3 difference(Vector3 compare)
	{
		return new Vector3(compare.x - this.x, compare.y - this.y, compare.z - this.z);
	}

	public double getMagnitudeSquared()
	{
		return this.x * this.x + this.y * this.y + this.z * this.z;
	}

	public double getMagnitude()
	{
		return Math.sqrt(this.getMagnitudeSquared());
	}

	public double distance(Vector3 compare)
	{
		Vector3 difference = this.difference(compare);
		return this.getMagnitude();
	}
}
