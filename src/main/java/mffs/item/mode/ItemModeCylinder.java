package mffs.item.mode;

import java.util.HashSet;
import java.util.Set;

import mffs.api.IFieldInteraction;
import mffs.api.IProjector;
import mffs.render.model.ModelCube;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import universalelectricity.api.vector.Vector3;
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
	private static final int RADIUS_Expansion = 0;

	public ItemModeCylinder(int i)
	{
		super(i, "modeCylinder");
	}

	@Override
	public Set<Vector3> getExteriorPoints(IFieldInteraction projector)
	{
		final Set<Vector3> fieldBlocks = new HashSet<Vector3>();

		final Vector3 posScale = projector.getPositiveScale();
		final Vector3 negScale = projector.getNegativeScale();

		int radius = (posScale.intX() + negScale.intX() + posScale.intZ() + negScale.intZ()) / 2;
		int height = posScale.intY() + negScale.intY();

		for (float x = -radius; x <= radius; x += 1)
		{
			for (float z = -radius; z <= radius; z += 1)
			{
				for (float y = 0; y < height; y += 1)
				{
					if ((y == 0 || y == height - 1) && (x * x + z * z + RADIUS_Expansion) <= (radius * radius))
					{
						fieldBlocks.add(new Vector3(x, y, z));
					}
					if ((x * x + z * z + RADIUS_Expansion) <= (radius * radius) && (x * x + z * z + RADIUS_Expansion) >= ((radius - 1) * (radius - 1)))
					{
						fieldBlocks.add(new Vector3(x, y, z));
					}
				}
			}
		}

		return fieldBlocks;
	}

	@Override
	public Set<Vector3> getInteriorPoints(IFieldInteraction projector)
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

					if (this.isInField(projector, Vector3.translate(position, new Vector3((TileEntity) projector)).add(translation)))
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
		Vector3 posScale = projector.getPositiveScale();
		Vector3 negScale = projector.getNegativeScale();

		int radius = (posScale.intX() + negScale.intX() + posScale.intZ() + negScale.intZ()) / 2;

		Vector3 projectorPos = new Vector3((TileEntity) projector);
		projectorPos.add(projector.getTranslation());

		Vector3 relativePosition = position.clone().subtract(projectorPos);
		relativePosition.rotate(-projector.getRotationYaw(), -projector.getRotationPitch());

		if (relativePosition.x * relativePosition.x + relativePosition.z * relativePosition.z + RADIUS_Expansion <= radius * radius)
		{
			return true;
		}

		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void render(IProjector projector, double x, double y, double z, float f, long ticks)
	{
		float scale = 0.15f;
		float detail = 0.5f;

		GL11.glScalef(scale, scale, scale);

		float radius = 1.5f;

		int i = 0;

		for (float renderX = -radius; renderX <= radius; renderX += detail)
		{
			for (float renderZ = -radius; renderZ <= radius; renderZ += detail)
			{
				for (float renderY = -radius; renderY <= radius; renderY += detail)
				{
					if (((renderX * renderX + renderZ * renderZ + RADIUS_Expansion) <= (radius * radius) && (renderX * renderX + renderZ * renderZ + RADIUS_Expansion) >= ((radius - 1) * (radius - 1))) || ((renderY == 0 || renderY == radius - 1) && (renderX * renderX + renderZ * renderZ + RADIUS_Expansion) <= (radius * radius)))
					{
						if (i % 2 == 0)
						{
							Vector3 vector = new Vector3(renderX, renderY, renderZ);
							GL11.glTranslated(vector.x, vector.y, vector.z);
							ModelCube.INSTNACE.render();
							GL11.glTranslated(-vector.x, -vector.y, -vector.z);
						}

						i++;
					}
				}
			}
		}
	}
}
