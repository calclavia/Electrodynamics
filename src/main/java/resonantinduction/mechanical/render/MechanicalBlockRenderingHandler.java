package resonantinduction.mechanical.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.mechanical.Mechanical;
import resonantinduction.mechanical.fluid.pipe.RenderPipe;
import resonantinduction.mechanical.fluid.pump.RenderPump;
import resonantinduction.mechanical.fluid.tank.RenderTank;
import resonantinduction.old.client.model.ModelConveyorBelt;
import resonantinduction.old.client.model.ModelCrusher;
import resonantinduction.old.client.model.ModelGrinder;
import resonantinduction.old.client.model.ModelManipulator;
import resonantinduction.old.client.model.ModelRejectorPiston;
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
		else if (block == Mechanical.blockPipe)
		{
			GL11.glPushMatrix();
			GL11.glTranslatef(0.0F, 1F, 0.0F);
			GL11.glRotatef(180f, 0f, 0f, 1f);
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderPipe.getTexture(metadata));
			RenderPipe.MODEL_PIPE.renderFront();
			RenderPipe.MODEL_PIPE.renderMiddle();
			RenderPipe.MODEL_PIPE.renderBack();
			GL11.glPopMatrix();
		}
		else if (Mechanical.blockPump != null)
		{
			GL11.glTranslatef(0.0F, 1.3F, 0.0F);
			GL11.glRotatef(180f, 0f, 0f, 1f);

			FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderPump.TEXTURE);
			RenderPump.MODEL.render(0.0725F);
			RenderPump.MODEL.renderMotion(0.0725F, 0);
		}/*
		 * else if (RecipeLoader.blockRejector != null && block.blockID ==
		 * RecipeLoader.blockRejector.blockID)
		 * {
		 * FMLClientHandler.instance().getClient().renderEngine.bindTexture(new
		 * ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "rejector.png"));
		 * GL11.glPushMatrix();
		 * GL11.glTranslatef(0.6F, 1.5F, 0.6F);
		 * GL11.glRotatef(180f, 0f, 0f, 1f);
		 * GL11.glRotatef(-90f, 0f, 1f, 0f);
		 * modelEjector.render(0.0625F);
		 * modelEjector.renderPiston(0.0625F, 1);
		 * GL11.glPopMatrix();
		 * }
		 * else if (RecipeLoader.blockManipulator != null && block.blockID ==
		 * RecipeLoader.blockManipulator.blockID)
		 * {
		 * FMLClientHandler.instance().getClient().renderEngine.bindTexture(new
		 * ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "manipulator1.png"));
		 * GL11.glPushMatrix();
		 * GL11.glTranslatef(0.6F, 1.5F, 0.6F);
		 * GL11.glRotatef(180f, 0f, 0f, 1f);
		 * GL11.glRotatef(-90f, 0f, 1f, 0f);
		 * modelInjector.render(0.0625F, true, 0);
		 * GL11.glPopMatrix();
		 * }
		 * if (RecipeLoader.blockPumpMachine != null && block.blockID ==
		 * RecipeLoader.blockPumpMachine.blockID && metadata < 4)
		 * {
		 * GL11.glTranslatef(0.0F, 1.1F, 0.0F);
		 * GL11.glRotatef(180f, 0f, 0f, 1f);
		 * FMLClientHandler.instance().getClient().renderEngine.bindTexture(new
		 * ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "pumps/WaterPump.png"));
		 * modelPump.render(0.0725F);
		 * modelPump.renderMotion(0.0725F, 0);
		 * }
		 * else if (RecipeLoader.blockSink != null && block.blockID ==
		 * RecipeLoader.blockSink.blockID)
		 * {
		 * GL11.glTranslatef(0.0F, .8F, 0.0F);
		 * GL11.glRotatef(180f, 0f, 0f, 1f);
		 * FMLClientHandler.instance().getClient().renderEngine.bindTexture(new
		 * ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "Sink.png"));
		 * sink.render(0.0565F);
		 * }
		 * else if (RecipeLoader.blockRod != null && block.blockID == RecipeLoader.blockRod.blockID)
		 * {
		 * GL11.glTranslatef(0.0F, 1.5F, 0.0F);
		 * GL11.glRotatef(180f, 0f, 0f, 1f);
		 * FMLClientHandler.instance().getClient().renderEngine.bindTexture(new
		 * ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY +
		 * "mechanical/GearRod.png"));
		 * modelRod.render(0.0825F, 0);
		 * }
		 * else if (RecipeLoader.blockConPump != null && block.blockID ==
		 * RecipeLoader.blockConPump.blockID && metadata < 4)
		 * {
		 * GL11.glTranslatef(0.0F, 1.2F, 0.0F);
		 * GL11.glRotatef(180f, 0f, 0f, 1f);
		 * FMLClientHandler.instance().getClient().renderEngine.bindTexture(new
		 * ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "ConstructionPump.png"));
		 * conPump.render(0.0725F);
		 * conPump.renderMotor(0.0725F);
		 * }
		 * else if (RecipeLoader.frackingPipe != null && block.blockID ==
		 * RecipeLoader.frackingPipe.blockID)
		 * {
		 * FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderFrackingPipe.TEXTURE
		 * );
		 * GL11.glTranslatef(0, 1F, 0);
		 * GL11.glScalef(1.0F, -1F, -1F);
		 * RenderFrackingPipe.model.renderAll();
		 * }
		 * GL11.glPopMatrix();
		 */
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