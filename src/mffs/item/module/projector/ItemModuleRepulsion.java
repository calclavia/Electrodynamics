package mffs.item.module.projector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mffs.api.IProjector;
import mffs.item.module.ItemModule;
import mffs.tileentity.TileEntityForceFieldProjector;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import universalelectricity.core.vector.Vector3;

public class ItemModuleRepulsion extends ItemModule
{
	public static final Set<Vector3> repulsionFields = new HashSet<Vector3>();

	public ItemModuleRepulsion(int id)
	{
		super(id, "moduleRepulsion");
		this.setMaxStackSize(1);
		this.setCost(20);
	}

	@Override
	public boolean onProject(IProjector projector, Set<Vector3> fields)
	{
		Set<Vector3> field = projector.getCalculatedField();

		for (Vector3 fieldPos : field)
		{
			List<Entity> entities = ((TileEntity) projector).getWorldObj().getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(fieldPos.intX(), fieldPos.intY(), fieldPos.intZ(), fieldPos.intX() + 1, fieldPos.intY() + 1, fieldPos.intZ() + 1));

			for (Entity entity : entities)
			{
				entity.posX = entity.lastTickPosX;
				entity.posY = entity.lastTickPosY;
				entity.posZ = entity.lastTickPosZ;
				entity.motionX *= -1;
				entity.motionY *= -1;
				entity.motionZ *= -1;
				entity.moveEntity(entity.motionX, entity.motionY, entity.motionZ);
			}
		}

		return true;
	}

	@Override
	public boolean onDestroy(IProjector projector, Set<Vector3> field)
	{
		((TileEntityForceFieldProjector) projector).sendFieldToClient();
		return false;
	}
}
