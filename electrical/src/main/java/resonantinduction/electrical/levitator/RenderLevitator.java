package resonantinduction.electrical.levitator;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import calclavia.lib.render.RenderUtility;
import calclavia.lib.render.block.ICustomBlockRenderer;
import cpw.mods.fml.client.FMLClientHandler;

public class RenderLevitator extends TileEntitySpecialRenderer implements ICustomBlockRenderer
{
	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "levitator.tcn");
	public static final ResourceLocation TEXTURE_ON = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "levitator_on.png");
	public static final ResourceLocation TEXTURE_OFF = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "levitator_off.png");

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
		RenderUtility.rotateFaceToSideNoTranslate(((TileLevitator) t).getDirection().getOpposite());
		TileLevitator tile = (TileLevitator) t;
		if (tile.canFunction())
			bindTexture(TEXTURE_ON);
		else
			bindTexture(TEXTURE_OFF);

		GL11.glPushMatrix();
		GL11.glRotatef(tile.renderRotation, 1, 0, 0);
		MODEL.renderOnly("ring1");
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glRotatef(-tile.renderRotation, 1, 0, 0);
		MODEL.renderOnly("ring2");
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glRotatef(tile.renderRotation, 0, 0, 1);
		MODEL.renderOnly("ring3");
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glRotatef(-tile.renderRotation, 0, 0, 1);
		MODEL.renderOnly("ring4");
		GL11.glPopMatrix();

		MODEL.renderAllExcept("ring1", "ring2", "ring3", "ring4");

		GL11.glPopMatrix();
	}

	@Override
	public void renderInventory(Block block, int metadata, int modelID, RenderBlocks renderer)
	{

	}

	@Override
	public boolean renderStatic(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		return false;
	}

	@Override
	public void renderDynamic(TileEntity tile, Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef(0.5f, 0.5f, 0.5f);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_OFF);
		MODEL.renderAll();
		GL11.glPopMatrix();
	}
}
