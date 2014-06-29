package mffs.item.mode;

import resonant.api.mffs.IFieldInteraction;
import resonant.api.mffs.IProjector;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import resonant.lib.prefab.vector.Cuboid;
import universalelectricity.core.transform.vector.Vector3;

import java.util.HashSet;
import java.util.Set;

public class ItemModePyramid extends ItemMode
{
	public ItemModePyramid(int i)
	{
		super(i, "modePyramid");
	}

	@Override
	public Set<Vector3> getExteriorPoints(IFieldInteraction projector)
	{
		final Set<Vector3> fieldBlocks = new HashSet<Vector3>();

		final Vector3 posScale = projector.getPositiveScale();
		final Vector3 negScale = projector.getNegativeScale();

		final int xStretch = posScale.xi() + negScale.xi();
		final int yStretch = posScale.yi() + negScale.yi();
		final int zStretch = posScale.zi() + negScale.zi();
		final Vector3 translation = new Vector3(0, -negScale.yi(), 0);

		final int inverseThickness = (int) Math.max((yStretch + zStretch) / 4f, 1);
		System.out.println(inverseThickness);

		for (float y = 0; y <= yStretch; y += 1f)
		{
			for (float x = -xStretch; x <= xStretch; x += 1f)
			{
				for (float z = -zStretch; z <= zStretch; z += 1f)
				{
					double yTest = (y / yStretch) * inverseThickness;
					double xzPositivePlane = (1 - (x / xStretch) - (z / zStretch)) * inverseThickness;
					double xzNegativePlane = (1 + (x / xStretch) - (z / zStretch)) * inverseThickness;

					// Positive Positive Plane
					if (x >= 0 && z >= 0 && Math.round(xzPositivePlane) == Math.round(yTest))
					{
						fieldBlocks.add(new Vector3(x, y, z).add(translation));
						fieldBlocks.add(new Vector3(x, y, -z).add(translation));
					}

					// Negative Positive Plane
					if (x <= 0 && z >= 0 && Math.round(xzNegativePlane) == Math.round(yTest))
					{
						fieldBlocks.add(new Vector3(x, y, -z).add(translation));
						fieldBlocks.add(new Vector3(x, y, z).add(translation));
					}

					// Ground Level Plane
					if (y == 0 && (Math.abs(x) + Math.abs(z)) < (xStretch + yStretch) / 2)
					{
						fieldBlocks.add(new Vector3(x, y, z).add(translation));
					}
				}
			}
		}

		return fieldBlocks;
	}

	@Override
	public Set<Vector3> getInteriorPoints(IFieldInteraction projector)
	{
		Set<Vector3> fieldBlocks = new HashSet<Vector3>();

		Vector3 posScale = projector.getPositiveScale();
		Vector3 negScale = projector.getNegativeScale();

		int xStretch = posScale.xi() + negScale.xi();
		int yStretch = posScale.yi() + negScale.yi();
		int zStretch = posScale.zi() + negScale.zi();
		Vector3 translation = new Vector3(0, -0.4, 0);

		for (float x = -xStretch; x <= xStretch; x++)
		{
			for (float z = -zStretch; z <= zStretch; z++)
			{
				for (float y = 0; y <= yStretch; y++)
				{
					Vector3 position = new Vector3(x, y, z).add(translation);

					if (this.isInField(projector, Vector3.translate(position, new Vector3((TileEntity) projector))))
					{
						fieldBlocks.add(position);
					}
				}
			}
		}

		return fieldBlocks;
	}

	@Override
	public boolean isInField(IFieldInteraction projector, Vector3 position)
	{
		Vector3 posScale = projector.getPositiveScale().clone();
		Vector3 negScale = projector.getNegativeScale().clone();

		int xStretch = posScale.xi() + negScale.xi();
		int yStretch = posScale.yi() + negScale.yi();
		int zStretch = posScale.zi() + negScale.zi();

		Vector3 projectorPos = new Vector3((TileEntity) projector);
		projectorPos.add(projector.getTranslation());
		projectorPos.add(new Vector3(0, -negScale.yi() + 1, 0));

		Vector3 relativePosition = position.clone().subtract(projectorPos);
		relativePosition.rotate(-projector.getRotationYaw(), -projector.getRotationPitch());

		Cuboid region = new Cuboid(negScale.scale(-1), posScale);

		if (region.isIn(relativePosition) && relativePosition.y > 0)
		{
			if ((1 - (Math.abs(relativePosition.x) / xStretch) - (Math.abs(relativePosition.z) / zStretch) > relativePosition.y / yStretch))
			{
				return true;
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
