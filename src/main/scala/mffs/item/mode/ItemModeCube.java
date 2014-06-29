package mffs.item.mode;

import resonant.api.mffs.IFieldInteraction;
import resonant.api.mffs.IProjector;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.render.model.ModelCube;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import resonant.lib.prefab.vector.Cuboid;
import universalelectricity.core.transform.vector.Vector3;

import java.util.HashSet;
import java.util.Set;

public class ItemModeCube extends ItemMode
{
	public ItemModeCube(int i, String name)
	{
		super(i, name);
	}

	public ItemModeCube(int i)
	{
		this(i, "modeCube");
	}

	@Override
	public Set<Vector3> getExteriorPoints(IFieldInteraction projector)
	{
		Set<Vector3> fieldBlocks = new HashSet<Vector3>();
		Vector3 posScale = projector.getPositiveScale();
		Vector3 negScale = projector.getNegativeScale();

		for (float x = -negScale.xi(); x <= posScale.xi(); x += 0.5f)
		{
			for (float z = -negScale.zi(); z <= posScale.zi(); z += 0.5f)
			{
				for (float y = -negScale.yi(); y <= posScale.yi(); y += 0.5f)
				{
					if (y == -negScale.yi() || y == posScale.yi() || x == -negScale.xi() || x == posScale.xi() || z == -negScale.zi() || z == posScale.zi())
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
		Set<Vector3> fieldBlocks = new HashSet<Vector3>();
		Vector3 posScale = projector.getPositiveScale();
		Vector3 negScale = projector.getNegativeScale();

		for (float x = -negScale.xi(); x <= posScale.xi(); x += 0.5f)
		{
			for (float z = -negScale.zi(); z <= posScale.zi(); z += 0.5f)
			{
				for (float y = -negScale.yi(); y <= posScale.yi(); y += 0.5f)
				{
					fieldBlocks.add(new Vector3(x, y, z));
				}
			}
		}

		return fieldBlocks;
	}

	@Override
	public boolean isInField(IFieldInteraction projector, Vector3 position)
	{
		Vector3 projectorPos = new Vector3((TileEntity) projector);
		projectorPos.add(projector.getTranslation());
		Vector3 relativePosition = position.clone().subtract(projectorPos);
		relativePosition.rotate(-projector.getRotationYaw(), -projector.getRotationPitch());
		Cuboid region = new Cuboid(projector.getNegativeScale().clone().scale(-1), projector.getPositiveScale());
		return region.intersects(relativePosition);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void render(IProjector projector, double x, double y, double z, float f, long ticks)
	{
		GL11.glScalef(0.5f, 0.5f, 0.5f);
		ModelCube.INSTNACE.render();
	}

}