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
	public ItemModeCube(int i)
	{
		super(i, "modeCube");
	}

	@Override
	public void calculateField(IProjector projector, Set<Vector3> blockDef)
	{
		Vector3 posScale = projector.getPositiveScale();
		Vector3 negScale = projector.getNegativeScale();

		for (float x = -negScale.intX(); x <= posScale.intX(); x += 1)
		{
			for (float z = -negScale.intZ(); z <= posScale.intZ(); z += 1)
			{
				for (float y = -negScale.intY(); y <= posScale.intY(); y += 1)
				{
					if (y == -negScale.intY() || y == posScale.intY() || x == -negScale.intX() || x == posScale.intX() || z == -negScale.intZ() || z == posScale.intZ())
					{
						blockDef.add(new Vector3(x, y, z));
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

		if (((TileEntity) projector).worldObj.isRemote)
		{
			((TileEntity) projector).worldObj.spawnParticle("smoke", relativePosition.x+projectorPos.x, relativePosition.y+projectorPos.y, relativePosition.z+projectorPos.z, 0, -0.1, 0);
		}

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