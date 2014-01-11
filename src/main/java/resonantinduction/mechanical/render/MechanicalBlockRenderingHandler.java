package resonantinduction.mechanical.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.mechanical.Mechanical;
import resonantinduction.mechanical.fluid.tank.RenderTank;
import resonantinduction.old.client.model.ModelConveyorBelt;
import resonantinduction.old.client.model.ModelCrusher;
import resonantinduction.old.client.model.ModelGrinder;
import resonantinduction.old.client.model.ModelManipulator;
import resonantinduction.old.client.model.ModelRejectorPiston;
import resonantinduction.old.core.recipe.RecipeLoader;
import resonantinduction.old.transport.hopper.BlockAdvancedHopper;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MechanicalBlockRenderingHandler implements ISimpleBlockRenderingHandler
{
	public static MechanicalBlockRenderingHandler INSTANCE = new MechanicalBlockRenderingHandler();
	public static final int ID = RenderingRegistry.getNextAvailableRenderId();
	private ModelConveyorBelt modelConveyorBelt = new ModelConveyorBelt();
	private ModelRejectorPiston modelEjector = new ModelRejectorPiston();
	private ModelManipulator modelInjector = new ModelManipulator();
	private ModelCrusher modelCrushor = new ModelCrusher();
	private ModelGrinder grinderModel = new ModelGrinder();

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		if (block == Mechanical.blockConveyorBelt)
		{
			GL11.glPushMatrix();
			GL11.glTranslatef(0.0F, 1.3F, 0.0F);
			GL11.glRotatef(180f, 0f, 0f, 1f);
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "belt/frame0.png"));
			modelConveyorBelt.render(0.0625F, 0, false, false, false, false);
			GL11.glPopMatrix();
		}
		else if (block == Mechanical.blockTank)
		{
			GL11.glPushMatrix();
			GL11.glTranslatef(0.0F, 1.3F, 0.0F);
			GL11.glRotatef(180f, 0f, 0f, 1f);
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderTank.getTexture(0, 0));
			RenderTank.MODEL.render(0.0625F, false, false, false, false);
			GL11.glPopMatrix();
		}
		else if (RecipeLoader.blockRejector != null && block.blockID == RecipeLoader.blockRejector.blockID)
		{
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "rejector.png"));
			GL11.glPushMatrix();
			GL11.glTranslatef(0.6F, 1.5F, 0.6F);
			GL11.glRotatef(180f, 0f, 0f, 1f);
			GL11.glRotatef(-90f, 0f, 1f, 0f);
			modelEjector.render(0.0625F);
			modelEjector.renderPiston(0.0625F, 1);
			GL11.glPopMatrix();
		}
		else if (RecipeLoader.blockManipulator != null && block.blockID == RecipeLoader.blockManipulator.blockID)
		{
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "manipulator1.png"));
			GL11.glPushMatrix();
			GL11.glTranslatef(0.6F, 1.5F, 0.6F);
			GL11.glRotatef(180f, 0f, 0f, 1f);
			GL11.glRotatef(-90f, 0f, 1f, 0f);
			modelInjector.render(0.0625F, true, 0);
			GL11.glPopMatrix();
		}
		/*
		 * else if (RecipeLoader.blockArmbot != null && block.blockID ==
		 * RecipeLoader.blockArmbot.blockID)
		 * {
		 * FMLClientHandler.instance().getClient().renderEngine.bindTexture(new
		 * ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + RenderArmbot.TEXTURE));
		 * GL11.glPushMatrix();
		 * GL11.glTranslatef(0.4f, 0.8f, 0f);
		 * GL11.glScalef(0.7f, 0.7f, 0.7f);
		 * GL11.glRotatef(180f, 0f, 0f, 1f);
		 * GL11.glRotatef(-90f, 0f, 1f, 0f);
		 * RenderArmbot.MODEL.render(0.0625F, 0, 0);
		 * GL11.glPopMatrix();
		 * }
		 */
		else if (RecipeLoader.processorMachine != null && block.blockID == RecipeLoader.processorMachine.blockID && metadata == 0)
		{
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "CrusherBlock.png"));
			GL11.glPushMatrix();
			GL11.glTranslatef(0f, 1f, 0f);
			GL11.glRotatef(180f, 0f, 0f, 1f);
			GL11.glRotatef(-90f, 0f, 1f, 0f);
			this.modelCrushor.renderBody(0.0625f);
			this.modelCrushor.renderPiston(0.0625f, 4);
			GL11.glPopMatrix();
		}
		else if (RecipeLoader.processorMachine != null && block.blockID == RecipeLoader.processorMachine.blockID && metadata == 4)
		{
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "GrinderBlock.png"));
			GL11.glPushMatrix();
			GL11.glTranslatef(0f, 1f, 0f);
			GL11.glRotatef(180f, 0f, 0f, 1f);
			GL11.glRotatef(-90f, 0f, 1f, 0f);
			this.grinderModel.renderBody(0.0625f);
			this.grinderModel.renderRotation(0.0625f, 0);
			GL11.glPopMatrix();
		}
		/*
		 * else if (RecipeLoader.blockSteamGen != null && block.blockID ==
		 * RecipeLoader.blockSteamGen.blockID)
		 * {
		 * ModelMachine model = RenderSteamGen.getModel(metadata);
		 * FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderSteamGen.getTexture
		 * (metadata));
		 * GL11.glTranslatef(0.0F, 1.1F, 0.0F);
		 * GL11.glRotatef(180f, 0f, 0f, 1f);
		 * model.render(0.0625F);
		 * }
		 */
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		if (block instanceof BlockAdvancedHopper)
		{
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory()
	{
		return true;
	}

	@Override
	public int getRenderId()
	{
		return ID;
	}
}
