package resonantinduction.client.model;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex3d;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelHelper
{
	private static int gTexWidth = 64;
	private static int gTexHeight = 32;
	private static int sTexWidth = 64;
	private static int sTexHeight = 32;
	private static int texOffsetX = 0;
	private static int texOffsetY = 0;
	private static float texScale = 16f; // 16 pixels per world unit
	private static boolean clip = false; // clip textures instead of scaling them

	/**
	 * @param v1 Top Left
	 * @param v2 Top Right
	 * @param v3 Bottom Right
	 * @param v4 Bottom Left
	 */
	private static void drawQuadRaw(Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, ForgeDirection side)
	{
		glBegin(GL_QUADS);

		float quadWidth = 1;
		float quadHeight = 1;

		float subWidth = ((float) sTexWidth / (float) gTexWidth);
		float subHeight = ((float) sTexHeight / (float) gTexHeight);
		float xMin = ((float) texOffsetX / sTexWidth) * subWidth;
		float yMin = ((float) texOffsetY / sTexHeight) * subHeight;
		float subSqX = 0;
		float subSqY = 0;
		float subSqWidth = 0.25f * ((float) sTexWidth / (float) gTexWidth); // constant for now
		float subSqHeight = 0.5f * ((float) sTexHeight / (float) gTexHeight);

		switch (side)
		{
			case UP: // top
			{
				subSqX = 2f * subSqWidth;
				subSqY = 0;
				quadWidth = (float) Math.abs(v2.xCoord - v1.xCoord);
				quadHeight = (float) Math.abs(v4.zCoord - v1.zCoord);
				break;
			}
			case DOWN: // bottom
			{
				subSqX = 1f * subSqWidth;
				subSqY = 0;
				quadWidth = (float) Math.abs(v2.xCoord - v1.xCoord);
				quadHeight = (float) Math.abs(v4.zCoord - v1.zCoord);
				break;
			}
			case EAST: // right
			{
				subSqX = 0;
				subSqY = subSqHeight;
				quadWidth = (float) Math.abs(v2.zCoord - v1.zCoord);
				quadHeight = (float) Math.abs(v4.yCoord - v1.yCoord);
				break;
			}
			case WEST: // left
			{
				subSqX = 2f * subSqWidth;
				subSqY = subSqHeight;
				quadWidth = (float) Math.abs(v2.zCoord - v1.zCoord);
				quadHeight = (float) Math.abs(v4.yCoord - v1.yCoord);
				break;
			}
			case SOUTH: // back
			{
				subSqX = subSqWidth;
				subSqY = subSqHeight;
				quadWidth = (float) Math.abs(v2.xCoord - v1.xCoord);
				quadHeight = (float) Math.abs(v4.yCoord - v1.yCoord);
				break;
			}
			case NORTH: // front
			{
				subSqX = 3f * subSqWidth;
				subSqY = subSqHeight;
				quadWidth = (float) Math.abs(v2.xCoord - v1.xCoord);
				quadHeight = (float) Math.abs(v4.yCoord - v1.yCoord);
				break;
			}
			default:
				break;
		}

		float xMax, yMax;

		xMin += subSqX;
		yMin += subSqY;

		if (clip)
		{
			xMin += (1f - quadWidth) * subSqWidth;
			yMin += (1f - quadHeight) * subSqHeight;
			xMax = xMin + (subSqWidth * quadWidth);
			yMax = yMin + (subSqHeight * quadHeight);
		}
		else
		{
			xMax = xMin + (subSqWidth);
			yMax = yMin + (subSqHeight);
		}

		// System.out.println("xMin: " + xMin + "; xMax: " + xMax);

		glTexCoord2f(xMin, yMin);
		glVertex3d(v1.xCoord, v1.yCoord, v1.zCoord);
		glTexCoord2f(xMax, yMin);
		glVertex3d(v2.xCoord, v2.yCoord, v2.zCoord);
		glTexCoord2f(xMax, yMax);
		glVertex3d(v3.xCoord, v3.yCoord, v3.zCoord);
		glTexCoord2f(xMin, yMax);
		glVertex3d(v4.xCoord, v4.yCoord, v4.zCoord);

		glEnd();
	}

	/**
	 * @param v1 Top Left Back
	 * @param v2 Top Right Back
	 * @param v3 Top Right Front
	 * @param v4 Top Left Front
	 * @param v5 Bottom Left Front
	 * @param v6 Bottom Right Front
	 * @param v7 Bottom Right Back
	 * @param v8 Bottom Left Back
	 */
	private static void drawCuboidRaw(Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, Vec3 v5, Vec3 v6, Vec3 v7, Vec3 v8)
	{
		drawQuadRaw(v1, v2, v3, v4, ForgeDirection.UP); // top
		drawQuadRaw(v7, v6, v3, v2, ForgeDirection.EAST); // right
		drawQuadRaw(v5, v6, v7, v8, ForgeDirection.DOWN); // bottom
		drawQuadRaw(v5, v8, v1, v4, ForgeDirection.WEST); // left
		drawQuadRaw(v6, v5, v4, v3, ForgeDirection.NORTH); // front
		drawQuadRaw(v8, v7, v2, v1, ForgeDirection.SOUTH); // back
	}

	public static void drawCuboid(float xOffset, float yOffset, float zOffset, float xSize, float ySize, float zSize)
	{
		Vec3 v1, v2, v3, v4, v5, v6, v7, v8;
		float x, y, z;
		float x2, y2, z2;
		x = xOffset;
		y = yOffset;
		z = zOffset;
		x2 = x + xSize;
		y2 = y + ySize;
		z2 = z + zSize;
		v1 = Vec3.createVectorHelper(x, y2, z2);
		v2 = Vec3.createVectorHelper(x2, y2, z2);
		v3 = Vec3.createVectorHelper(x2, y2, z);
		v4 = Vec3.createVectorHelper(x, y2, z);
		v5 = Vec3.createVectorHelper(x, y, z);
		v6 = Vec3.createVectorHelper(x2, y, z);
		v7 = Vec3.createVectorHelper(x2, y, z2);
		v8 = Vec3.createVectorHelper(x, y, z2);
		drawCuboidRaw(v1, v2, v3, v4, v5, v6, v7, v8);
	}

	public static void setTextureOffset(int xOffset, int yOffset)
	{
		texOffsetX = xOffset;
		texOffsetY = yOffset;
	}

	public static void setGlobalTextureResolution(int width, int height)
	{
		gTexWidth = width;
		gTexHeight = height;
	}

	public static void setTextureSubResolution(int width, int height)
	{
		sTexWidth = width;
		sTexHeight = height;
	}

	/**
	 * Sets whether or not to clip the texture.
	 * 
	 * @param clip If true, textures on blocks less than 1x1x1 will be clipped. If false, they will
	 * be scaled.
	 */
	public static void setTextureClip(boolean clip)
	{
		ModelHelper.clip = clip;
	}
}
