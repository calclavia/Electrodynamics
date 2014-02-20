package resonantinduction.electrical.levitator;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import calclavia.lib.render.RenderUtility;
import calclavia.lib.render.block.ICustomBlockRenderer;
import calclavia.lib.render.item.ISimpleItemRenderer;
import cpw.mods.fml.client.FMLClientHandler;

public class RenderLevitator implements ISimpleItemRenderer
{
	public static final RenderLevitator INSTANCE = new RenderLevitator();

	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "levitator.tcn");
	public static final ResourceLocation TEXTURE_ON = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "levitator_on.png");
	public static final ResourceLocation TEXTURE_OFF = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "levitator_off.png");

	public void render(PartLevitator part, double x, double y, double z)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
		RenderUtility.rotateFaceToSideNoTranslate(part.placementSide);

		if (part.canFunction())
			RenderUtility.bind(TEXTURE_ON);
		else
			RenderUtility.bind(TEXTURE_OFF);

		GL11.glPushMatrix();
		GL11.glRotatef(part.renderRotation, 1, 0, 0);
		MODEL.renderOnly("ring1");
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glRotatef(-part.renderRotation, 1, 0, 0);
		MODEL.renderOnly("ring2");
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glRotatef(part.renderRotation, 0, 0, 1);
		MODEL.renderOnly("ring3");
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glRotatef(-part.renderRotation, 0, 0, 1);
		MODEL.renderOnly("ring4");
		GL11.glPopMatrix();

		MODEL.renderAllExcept("ring1", "ring2", "ring3", "ring4");

		GL11.glPopMatrix();
	}

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef(0.5f, 0.5f, 0.5f);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_OFF);
		MODEL.renderAll();
		GL11.glPopMatrix();
	}
}
