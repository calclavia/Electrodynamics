package resonantinduction.mechanical.logistic.belt;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.content.module.TileBase;
import calclavia.lib.content.module.TileRender;
import calclavia.lib.render.RenderUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileSorter extends TileBase
{
	public TileSorter()
	{
		super(UniversalElectricity.machine);
		textureName = "material_metal_side";
		normalRender = false;
		isOpaqueCube = false;
	}

	@SideOnly(Side.CLIENT)
	protected TileRender renderer()
	{System.out.println("TEST");
		return new TileRender()
		{
			final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "sorter.tcn");
			final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "sorter.png");

			@Override
			public void renderDynamic(Vector3 position, float frame)
			{
				GL11.glPushMatrix();
				RenderUtility.enableBlending();
				GL11.glTranslated(position.x + 0.5, position.y + 0.5, position.z + 0.5);
				RenderUtility.bind(TEXTURE);
				MODEL.renderAll();
				RenderUtility.disableBlending();
				GL11.glPopMatrix();
			}

		};
	}
}
