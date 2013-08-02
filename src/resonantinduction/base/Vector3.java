/**
 * 
 */
package resonantinduction.base;

import net.minecraft.entity.Entity;
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

	public Vector3(double amount)
	{
		this(amount, amount, amount);
	}

	public Vector3(Vector3 clone)
	{
		this(clone.x, clone.y, clone.z);
	}

	public Vector3(TileEntity tileEntity)
	{
		this(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
	}

	public Vector3(Entity entity)
	{
		this(entity.posX, entity.posY, entity.posZ);
	}

	public Vector3 scale(double amount)
	{
		return this.scale(new Vector3(amount));
	}

	public Vector3 scale(Vector3 amount)
	{
		return new Vector3(this.x * amount.x, this.y * amount.y, this.z * amount.z);
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
		return difference.getMagnitude();
	}

	/**
	 * Cross product functions
	 * 
	 * @return The cross product between this vector and another.
	 */
	public Vector3 crossProduct(Vector3 vec2)
	{
		return new Vector3(this.y * vec2.z - this.z * vec2.y, this.z * vec2.x - this.x * vec2.z, this.x * vec2.y - this.y * vec2.x);
	}

	public Vector3 xCrossProduct()
	{
		return new Vector3(0.0D, this.z, -this.y);
	}

	public Vector3 zCrossProduct()
	{
		return new Vector3(-this.y, this.x, 0.0D);
	}

	public double dotProduct(Vector3 vec2)
	{
		return this.x * vec2.x + this.y * vec2.y + this.z * vec2.z;
	}

	/**
	 * @return The perpendicular vector.
	 */
	public Vector3 getPerpendicular()
	{
		if (this.z == 0.0F)
		{
			return this.zCrossProduct();
		}

		return this.xCrossProduct();
	}

	/**
	 * @return True if this Vector3 is zero.
	 */
	public boolean isZero()
	{
		return (this.x == 0.0F) && (this.y == 0.0F) && (this.z == 0.0F);
	}

	public Vector3 translate(Vector3 offset)
	{
		return new Vector3(this.x + offset.x, this.y + offset.y, this.z + offset.z);
	}
}
