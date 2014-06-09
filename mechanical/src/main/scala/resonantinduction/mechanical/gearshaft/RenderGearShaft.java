package resonantinduction.mechanical.gearshaft;

import static org.lwjgl.opengl.GL11.glRotatef;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonant.api.items.ISimpleItemRenderer;
import resonant.lib.render.RenderUtility;
import resonantinduction.core.Reference;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGearShaft implements ISimpleItemRenderer
{
	public static final RenderGearShaft INSTANCE = new RenderGearShaft();
	public final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "gears.obj");

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		GL11.glRotatef(90, 1, 0, 0);

		switch (itemStack.getItemDamage())
		{
			default:
				RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "planks_oak.png");
				break;
			case 1:
				RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "cobblestone.png");
				break;
			case 2:
				RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "iron_block.png");
				break;
		}

		MODEL.renderOnly("Shaft");
	}

	public void renderDynamic(PartGearShaft part, double x, double y, double z, float frame)
	{
		GL11.glPushMatrix();
		// Center the model first.
		GL11.glTranslatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);
		GL11.glPushMatrix();

		ForgeDirection dir = part.placementSide;

		switch (dir)
		{
			default:
				break;
			case NORTH:
				glRotatef(90, 1, 0, 0);
				break;
			case WEST:
				glRotatef(90, 0, 0, 1);
				break;
		}

		GL11.glRotatef((float) Math.toDegrees(part.node.renderAngle), 0, 1, 0);

		switch (part.tier)
		{
			default:
				RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "planks_oak.png");
				break;
			case 1:
				RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "cobblestone.png");
				break;
			case 2:
				RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "iron_block.png");
				break;
		}

		MODEL.renderOnly("Shaft");

		GL11.glPopMatrix();
		GL11.glPopMatrix();
	}
}