package mffs.field.mode;

import resonant.api.mffs.IFieldInteraction;
import resonant.api.mffs.IProjector;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.render.model.ModelPlane;
import net.minecraftforge.common.ForgeDirection;
import org.lwjgl.opengl.GL11;
import universalelectricity.core.transform.vector.Vector3;

import java.util.HashSet;
import java.util.Set;

public class ItemModeTube extends ItemModeCube
{
	public ItemModeTube(int i)
	{
		super(i, "modeTube");
	}

	@Override
	public Set<Vector3> getExteriorPoints(IFieldInteraction projector)
	{
		Set<Vector3> fieldBlocks = new HashSet<Vector3>();
		ForgeDirection direction = projector.getDirection();
		Vector3 posScale = projector.getPositiveScale();
		Vector3 negScale = projector.getNegativeScale();

		for (float x = -negScale.xi(); x <= posScale.xi(); x += 0.5f)
		{
			for (float z = -negScale.zi(); z <= posScale.zi(); z += 0.5f)
			{
				for (float y = -negScale.yi(); y <= posScale.yi(); y += 0.5f)
				{
					if (!(direction == ForgeDirection.UP || direction == ForgeDirection.DOWN) && (y == -negScale.yi() || y == posScale.yi()))
					{
						fieldBlocks.add(new Vector3(x, y, z));
						continue;
					}

					if (!(direction == ForgeDirection.NORTH || direction == ForgeDirection.SOUTH) && (z == -negScale.zi() || z == posScale.zi()))
					{
						fieldBlocks.add(new Vector3(x, y, z));
						continue;
					}

					if (!(direction == ForgeDirection.WEST || direction == ForgeDirection.EAST) && (x == -negScale.xi() || x == posScale.xi()))
					{
						fieldBlocks.add(new Vector3(x, y, z));
						continue;
					}
				}
			}
		}
		return fieldBlocks;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void render(IProjector projector, double x, double y, double z, float f, long ticks)
	{
		GL11.glScalef(0.5f, 0.5f, 0.5f);
		GL11.glTranslatef(-0.5f, 0, 0);
		ModelPlane.INSTNACE.render();
		GL11.glTranslatef(1f, 0, 0);
		ModelPlane.INSTNACE.render();
		GL11.glTranslatef(-0.5f, 0f, 0);
		GL11.glRotatef(90, 0, 1, 0);
		GL11.glTranslatef(0.5f, 0f, 0f);
		ModelPlane.INSTNACE.render();
		GL11.glTranslatef(-1f, 0f, 0f);
		ModelPlane.INSTNACE.render();
	}
}