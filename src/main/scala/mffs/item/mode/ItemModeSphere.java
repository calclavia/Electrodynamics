package mffs.item.mode;

import resonant.api.mffs.IFieldInteraction;
import resonant.api.mffs.IProjector;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.ModularForceFieldSystem;
import mffs.render.model.ModelCube;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import universalelectricity.core.transform.vector.Vector3;

import java.util.HashSet;
import java.util.Set;

public class ItemModeSphere extends ItemMode
{
	public ItemModeSphere(int i)
	{
		super(i, "modeSphere");
	}

	@Override
	public Set<Vector3> getExteriorPoints(IFieldInteraction projector)
	{
		Set<Vector3> fieldBlocks = new HashSet();
		int radius = projector.getModuleCount(ModularForceFieldSystem.itemModuleScale);

		int steps = (int) Math.ceil(Math.PI / Math.atan(1.0D / radius / 2));

		for (int phi_n = 0; phi_n < 2 * steps; phi_n++)
		{
			for (int theta_n = 0; theta_n < steps; theta_n++)
			{
				double phi = Math.PI * 2 / steps * phi_n;
				double theta = Math.PI / steps * theta_n;

				Vector3 point = new Vector3(Math.sin(theta) * Math.cos(phi), Math.cos(theta), Math.sin(theta) * Math.sin(phi)).scale(radius);
				fieldBlocks.add(point);
			}
		}

		return fieldBlocks;
	}

	@Override
	public Set<Vector3> getInteriorPoints(IFieldInteraction projector)
	{
		Set<Vector3> fieldBlocks = new HashSet<Vector3>();
		final Vector3 translation = projector.getTranslation();

		int radius = projector.getModuleCount(ModularForceFieldSystem.itemModuleScale);

		for (int x = -radius; x <= radius; x++)
		{
			for (int z = -radius; z <= radius; z++)
			{
				for (int y = -radius; y <= radius; y++)
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
		return new Vector3((TileEntity) projector).add(projector.getTranslation()).distance(position) < projector.getModuleCount(ModularForceFieldSystem.itemModuleScale);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void render(IProjector projector, double x1, double y1, double z1, float f, long ticks)
	{
		float scale = 0.15f;
		GL11.glScalef(scale, scale, scale);

		float radius = 1.5f;
		int steps = (int) Math.ceil(Math.PI / Math.atan(1.0D / radius / 2));

		for (int phi_n = 0; phi_n < 2 * steps; phi_n++)
		{
			for (int theta_n = 0; theta_n < steps; theta_n++)
			{
				double phi = Math.PI * 2 / steps * phi_n;
				double theta = Math.PI / steps * theta_n;

				Vector3 vector = new Vector3(Math.sin(theta) * Math.cos(phi), Math.cos(theta), Math.sin(theta) * Math.sin(phi));
				vector.scale(radius);
				GL11.glTranslated(vector.x, vector.y, vector.z);
				ModelCube.INSTNACE.render();
				GL11.glTranslated(-vector.x, -vector.y, -vector.z);
			}
		}
	}
}