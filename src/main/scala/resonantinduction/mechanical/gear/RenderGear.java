package resonantinduction.mechanical.gear;

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
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGear implements ISimpleItemRenderer
{
	public static final RenderGear INSTANCE = new RenderGear();
	public final IModelCustom MODEL = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain(), "gears.obj"));
	

	public void renderGear(int side, int tier, boolean isLarge, double angle)
	{
		switch (tier)
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
			case 10:
                RenderUtility.bind(Reference.blockTextureDirectory() + "pumpkin_top.png");
                break;
		}

		RenderUtility.rotateFaceBlockToSide(ForgeDirection.getOrientation(side));
		GL11.glRotated(angle, 0, 1, 0);

		if (isLarge)
		{
			MODEL.renderOnly("LargeGear");
		}
		else
		{
			MODEL.renderOnly("SmallGear");
		}
	}

	public void renderDynamic(PartGear part, double x, double y, double z, int tier)
	{
		if (part.getMultiBlock().isPrimary())
		{
			GL11.glPushMatrix();
			// Center the model first.
			GL11.glTranslatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);
			GL11.glPushMatrix();
			renderGear(part.placementSide.ordinal(), part.tier, part.getMultiBlock().isConstructed(), Math.toDegrees(part.node.renderAngle));
			GL11.glPopMatrix();
			GL11.glPopMatrix();
		}
	}

	@Override
	public void renderInventoryItem(IItemRenderer.ItemRenderType type, ItemStack itemStack, Object... data)
	{
		GL11.glRotatef(90, 1, 0, 0);
		renderGear(-1, itemStack.getItemDamage(), false, 0);
	}
}