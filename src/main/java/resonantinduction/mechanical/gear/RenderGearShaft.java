package resonantinduction.mechanical.gear;

import static org.lwjgl.opengl.GL11.glRotatef;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import calclavia.lib.render.RenderUtility;
import calclavia.lib.utility.WorldUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGearShaft
{
	public static final RenderGearShaft INSTANCE = new RenderGearShaft();
	public final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "gears.obj");

	public void renderInventory(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		GL11.glRotatef(90, 1, 0, 0);
		RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "planks_oak.png");
		MODEL.renderOnly("Shaft");
	}

	public void renderDynamic(PartGearShaft part, double x, double y, double z, float frame)
	{
		GL11.glPushMatrix();
		// Center the model first.
		GL11.glTranslatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);
		GL11.glPushMatrix();

		ForgeDirection dir = part.placementSide;
		dir = ForgeDirection.getOrientation(!(dir.ordinal() % 2 == 0) ? dir.ordinal() - 1 : dir.ordinal());

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

		GL11.glRotatef((float) Math.toDegrees(part.angle), 0, 1, 0);

		RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "planks_oak.png");
		MODEL.renderOnly("Shaft");

		GL11.glPopMatrix();
		GL11.glPopMatrix();
	}
}