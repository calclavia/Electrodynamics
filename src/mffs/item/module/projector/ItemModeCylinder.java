package mffs.item.module.projector;

import java.util.HashSet;
import java.util.Set;

import mffs.api.IProjector;
import mffs.item.mode.ItemMode;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.vector.Region3;
import calclavia.lib.CalculationHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A cylinder mode.
 * 
 * @author Calclavia, Thutmose
 * 
 */
public class ItemModeCylinder extends ItemMode
{
	private static final int RADIUS_TRANSLATION = 2;

	public ItemModeCylinder(int i)
	{
		super(i, "modeCylinder");
	}

	@Override
	public Set<Vector3> getExteriorPoints(IProjector projector)
	{
		final Set<Vector3> fieldBlocks = new HashSet<Vector3>();

		final Vector3 posScale = projector.getPositiveScale();
		final Vector3 negScale = projector.getNegativeScale();

		int radius = (posScale.intX() + negScale.intX() + posScale.intZ() + negScale.intZ()) / 2;
		int height = posScale.intY() + negScale.intY();

		for (int x = -radius; x <= radius; x++)
		{
			for (int z = -radius; z <= radius; z++)
			{
				for (int y = 0; y < height; y++)
				{
					if ((y == 0 || y == height - 1) && (x * x + z * z + RADIUS_TRANSLATION) <= (radius * radius))
					{
						fieldBlocks.add(new Vector3(x, y, z));
					}
					if ((x * x + z * z + RADIUS_TRANSLATION) <= (radius * radius) && (x * x + z * z + RADIUS_TRANSLATION) >= ((radius - 1) * (radius - 1)))
					{
						fieldBlocks.add(new Vector3(x, y, z));
					}
				}
			}
		}

		return fieldBlocks;
	}

	@Override
	public Set<Vector3> getInteriorPoints(IProjector projector)
	{
		final Set<Vector3> fieldBlocks = new HashSet<Vector3>();

		final Vector3 translation = projector.getTranslation();

		final Vector3 posScale = projector.getPositiveScale();
		final Vector3 negScale = projector.getNegativeScale();

		int radius = (posScale.intX() + negScale.intX() + posScale.intZ() + negScale.intZ()) / 2;
		int height = posScale.intY() + negScale.intY();

		for (int x = -radius; x <= radius; x++)
		{
			for (int z = -radius; z <= radius; z++)
			{
				for (int y = 0; y < height; y++)
				{
					Vector3 position = new Vector3(x, y, z);

					if (this.isInField(projector, Vector3.add(position, new Vector3((TileEntity) projector)).add(translation)))
					{
						fieldBlocks.add(position);
					}
				}
			}
		}

		return fieldBlocks;
	}

	@Override
	public boolean isInField(IProjector projector, Vector3 position)
	{
		Vector3 posScale = projector.getPositiveScale().clone();
		Vector3 negScale = projector.getNegativeScale().clone();

		int radius = (posScale.intX() + negScale.intX() + posScale.intZ() + negScale.intZ()) / 2;

		Vector3 projectorPos = new Vector3((TileEntity) projector);
		projectorPos.add(projector.getTranslation());

		Vector3 relativePosition = position.clone().subtract(projectorPos);
		CalculationHelper.rotateByAngle(relativePosition, -projector.getRotationYaw(), -projector.getRotationPitch());

		Region3 region = new Region3(negScale.multiply(-1), posScale);

		if (region.isIn(relativePosition) && relativePosition.y > 0)
		{
			if (relativePosition.x * relativePosition.x + relativePosition.z * relativePosition.z + RADIUS_TRANSLATION <= radius * radius)
			{
				return true;
			}
			else
			{
				System.out.println(relativePosition.x * relativePosition.x + relativePosition.z * relativePosition.z + " vs " + radius * radius);
			}
		}

		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void render(IProjector projector, double x, double y, double z, float f, long ticks)
	{
		Tessellator tessellator = Tessellator.instance;

		GL11.glPushMatrix();
		GL11.glRotatef(180, 0, 0, 1);

		float height = 0.5f;
		float width = 0.3f;
		int uvMaxX = 2;
		int uvMaxY = 2;
		Vector3 translation = new Vector3(0, -0.4, 0);
		tessellator.startDrawing(6);
		tessellator.setColorRGBA(72, 198, 255, 255);
		tessellator.addVertexWithUV(0 + translation.x, 0 + translation.y, 0 + translation.z, 0, 0);
		tessellator.addVertexWithUV(-width + translation.x, height + translation.y, -width + translation.z, -uvMaxX, -uvMaxY);
		tessellator.addVertexWithUV(-width + translation.x, height + translation.y, width + translation.z, -uvMaxX, uvMaxY);
		tessellator.addVertexWithUV(width + translation.x, height + translation.y, width + translation.z, uvMaxX, uvMaxY);
		tessellator.addVertexWithUV(width + translation.x, height + translation.y, -width + translation.z, uvMaxX, -uvMaxY);
		tessellator.addVertexWithUV(-width + translation.x, height + translation.y, -width + translation.z, -uvMaxX, -uvMaxY);
		tessellator.draw();
		GL11.glPopMatrix();
	}
}
