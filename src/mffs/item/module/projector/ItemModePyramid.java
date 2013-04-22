package mffs.item.module.projector;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import calclavia.lib.CalculationHelper;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;

import mffs.api.IProjector;
import mffs.item.mode.ItemMode;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.vector.Region3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemModePyramid extends ItemMode
{
	public ItemModePyramid(int i)
	{
		super(i, "modePyramid");
	}

	@Override
	public Set<Vector3> getExteriorPoints(IProjector projector)
	{
		Set<Vector3> fieldBlocks = new HashSet<Vector3>();

		Vector3 posScale = projector.getPositiveScale();
		Vector3 negScale = projector.getNegativeScale();

		int xStretch = posScale.intX() + negScale.intX();
		int yStretch = posScale.intY() + negScale.intY();
		int zStretch = posScale.intZ() + negScale.intZ();
		final Vector3 translation = new Vector3(0, -negScale.intY(), 0);

		int inverseThickness = 1000;

		for (float x = -xStretch; x <= xStretch; x++)
		{
			for (float z = -zStretch; z <= zStretch; z++)
			{
				for (float y = 0; y <= yStretch; y++)
				{
					double q = (1 - (x / xStretch) - (z / zStretch)) * inverseThickness;
					double p = (y / yStretch) * inverseThickness;

					if (x >= 0 && z >= 0 && Math.round(q) == (Math.round(p)))
					{
						fieldBlocks.add(new Vector3(x, y, z).add(translation));
						fieldBlocks.add(new Vector3(x, y, -z).add(translation));
					}

					q = (1 + (x / xStretch) - (z / zStretch)) * inverseThickness;

					if (x <= 0 && z >= 0 && Math.round(q) == (Math.round(p)))
					{
						fieldBlocks.add(new Vector3(x, y, -z).add(translation));
						fieldBlocks.add(new Vector3(x, y, z).add(translation));
					}
					
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
	public Set<Vector3> getInteriorPoints(IProjector projector)
	{
		Set<Vector3> fieldBlocks = new HashSet<Vector3>();

		Vector3 posScale = projector.getPositiveScale();
		Vector3 negScale = projector.getNegativeScale();

		int xStretch = posScale.intX() + negScale.intX();
		int yStretch = posScale.intY() + negScale.intY();
		int zStretch = posScale.intZ() + negScale.intZ();
		final Vector3 translation = new Vector3(0, -negScale.intY(), 0);

		for (float x = -xStretch; x <= xStretch; x++)
		{
			for (float z = -zStretch; z <= zStretch; z++)
			{
				for (float y = 0; y <= yStretch; y++)
				{
					Vector3 position = new Vector3(x, y, z).add(translation);

					if (this.isInField(projector, Vector3.add(position, new Vector3((TileEntity)projector))))
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

		int xStretch = posScale.intX() + negScale.intX();
		int yStretch = posScale.intY() + negScale.intY();
		int zStretch = posScale.intZ() + negScale.intZ();

		Vector3 projectorPos = new Vector3((TileEntity) projector);

		Vector3 relativePosition = position.clone().subtract(projectorPos);
		CalculationHelper.rotateXZByAngle(relativePosition, -projector.getRotationYaw());
		CalculationHelper.rotateYByAngle(relativePosition, -projector.getRotationPitch());

		Region3 region = new Region3(negScale.multiply(-1), posScale);

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
		int uvMaxX = 112;
		int uvMaxY = 70;
		Vector3 translation = new Vector3(0, -0.4, 0);

		tessellator.startDrawing(6);
		tessellator.setColorRGBA(72, 198, 255, 255);
		tessellator.addVertexWithUV(0 + translation.x, 0 + translation.y, 0 + translation.z, 0, 0);
		tessellator.addVertexWithUV(-width + translation.x, height + translation.y, -width + translation.z, uvMaxX, uvMaxY);
		tessellator.addVertexWithUV(-width + translation.x, height + translation.y, width + translation.z, uvMaxX, uvMaxY);
		tessellator.addVertexWithUV(width + translation.x, height + translation.y, width + translation.z, uvMaxX, uvMaxY);
		tessellator.addVertexWithUV(width + translation.x, height + translation.y, -width + translation.z, uvMaxX, uvMaxY);
		tessellator.addVertexWithUV(-width + translation.x, height + translation.y, -width + translation.z, uvMaxX, uvMaxY);
		tessellator.draw();
		GL11.glPopMatrix();
	}
}
