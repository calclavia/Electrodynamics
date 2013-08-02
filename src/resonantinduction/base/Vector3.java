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


	public Vector3 normalize()
	{
		double d = getMagnitude();

		if (d != 0)
		{
			scale(1 / d);
		}
		return this;
	}
	
	/**
	 * Rotate by a this vector around an axis.
	 * 
	 * @return The new Vector3 rotation.
	 */
	public Vector3 rotate(float angle, Vector3 axis)
	{
		return translateMatrix(getRotationMatrix(angle, axis), this.clone());
	}

	public double[] getRotationMatrix(float angle)
	{
		double[] matrix = new double[16];
		Vector3 axis = this.clone().normalize();
		double x = axis.x;
		double y = axis.y;
		double z = axis.z;
		angle *= 0.0174532925D;
		float cos = (float) Math.cos(angle);
		float ocos = 1.0F - cos;
		float sin = (float) Math.sin(angle);
		matrix[0] = (x * x * ocos + cos);
		matrix[1] = (y * x * ocos + z * sin);
		matrix[2] = (x * z * ocos - y * sin);
		matrix[4] = (x * y * ocos - z * sin);
		matrix[5] = (y * y * ocos + cos);
		matrix[6] = (y * z * ocos + x * sin);
		matrix[8] = (x * z * ocos + y * sin);
		matrix[9] = (y * z * ocos - x * sin);
		matrix[10] = (z * z * ocos + cos);
		matrix[15] = 1.0F;
		return matrix;
	}

	public static Vector3 translateMatrix(double[] matrix, Vector3 translation)
	{
		double x = translation.x * matrix[0] + translation.y * matrix[1] + translation.z * matrix[2] + matrix[3];
		double y = translation.x * matrix[4] + translation.y * matrix[5] + translation.z * matrix[6] + matrix[7];
		double z = translation.x * matrix[8] + translation.y * matrix[9] + translation.z * matrix[10] + matrix[11];
		translation.x = x;
		translation.y = y;
		translation.z = z;
		return translation;
	}

	public static double[] getRotationMatrix(float angle, Vector3 axis)
	{
		return axis.getRotationMatrix(angle);
	}

	@Override
	public Vector3 clone()
	{
		return new Vector3(this.x, this.y, this.z);
	}
}
