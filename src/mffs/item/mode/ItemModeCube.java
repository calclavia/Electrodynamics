package mffs.item.mode;

import java.util.Set;

import mffs.api.IProjector;
import mffs.render.model.ModelCube;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.vector.Region3;
import calclavia.lib.CalculationHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	public void calculateField(IProjector projector, Set<Vector3> fieldBlocks)
	{
		Vector3 posScale = projector.getPositiveScale();
		Vector3 negScale = projector.getNegativeScale();

		for (float x = -negScale.intX(); x <= posScale.intX(); x += 0.5f)
		{
			for (float z = -negScale.intZ(); z <= posScale.intZ(); z += 0.5f)
			{
				for (float y = -negScale.intY(); y <= posScale.intY(); y += 0.5f)
				{
					if (y == -negScale.intY() || y == posScale.intY() || x == -negScale.intX() || x == posScale.intX() || z == -negScale.intZ() || z == posScale.intZ())
					{
						fieldBlocks.add(new Vector3(x, y, z));
					}
				}
			}
		}
	}

	@Override
	public boolean isInField(IProjector projector, Vector3 position)
	{
		Vector3 projectorPos = new Vector3((TileEntity) projector);
		Vector3 relativePosition = position.clone().subtract(projectorPos);
		CalculationHelper.rotateXZByAngle(relativePosition, -projector.getRotationYaw());
		CalculationHelper.rotateYByAngle(relativePosition, -projector.getRotationPitch());
		Region3 region = new Region3(projector.getNegativeScale().clone().multiply(-1), projector.getPositiveScale());
		return region.isIn(relativePosition);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void render(IProjector projector, double x, double y, double z, float f, long ticks)
	{
		GL11.glScalef(0.5f, 0.5f, 0.5f);
		ModelCube.INSTNACE.render();
	}
}