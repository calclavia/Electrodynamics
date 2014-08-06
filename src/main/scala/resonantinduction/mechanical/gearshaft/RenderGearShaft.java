package resonantinduction.mechanical.gearshaft;

import static org.lwjgl.opengl.GL11.glRotatef;

import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonant.content.prefab.scala.render.ISimpleItemRenderer;
import resonant.lib.render.RenderUtility;
import resonantinduction.core.Reference;
import cpw.mods.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class RenderGearShaft implements ISimpleItemRenderer
{
	public static final RenderGearShaft INSTANCE = new RenderGearShaft();
	public final IModelCustom MODEL = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain(), "gears.obj"));

	@Override
	public void renderInventoryItem(IItemRenderer.ItemRenderType type, ItemStack itemStack, Object... data)
	{
		GL11.glRotatef(90, 1, 0, 0);

		switch (itemStack.getItemDamage())
		{
			default:
				RenderUtility.bind(Reference.blockTextureDirectory() + "planks_oak.png");
				break;
			case 1:
				RenderUtility.bind(Reference.blockTextureDirectory() + "cobblestone.png");
				break;
			case 2:
				RenderUtility.bind(Reference.blockTextureDirectory() + "iron_block.png");
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
				RenderUtility.bind(Reference.blockTextureDirectory() + "planks_oak.png");
				break;
			case 1:
				RenderUtility.bind(Reference.blockTextureDirectory() + "cobblestone.png");
				break;
			case 2:
				RenderUtility.bind(Reference.blockTextureDirectory() + "iron_block.png");
				break;
		}

		MODEL.renderOnly("Shaft");

		GL11.glPopMatrix();
		GL11.glPopMatrix();
	}
}