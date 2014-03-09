package resonantinduction.quantum.gate;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import resonantinduction.core.Reference;
import resonantinduction.electrical.Electrical;
import calclavia.lib.render.RenderUtility;
import calclavia.lib.render.item.ISimpleItemRenderer;
import cpw.mods.fml.client.FMLClientHandler;

public class RenderQuantumGlyph implements ISimpleItemRenderer
{
	public static final RenderQuantumGlyph INSTANCE = new RenderQuantumGlyph();

	public void render(PartQuantumGlyph part, double x, double y, double z)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		Cuboid6 bound = part.getBounds().copy();
		bound.expand(-0.02);
		RenderUtility.bind(TextureMap.locationBlocksTexture);
		RenderUtility.renderCube(bound.min.x, bound.min.y, bound.min.z, bound.max.x, bound.max.y, bound.max.z, Block.stone, RenderUtility.getIcon(Reference.PREFIX + "glyph_" + part.number));
		GL11.glPopMatrix();
	}

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		GL11.glPushMatrix();
		RenderUtility.bind(TextureMap.locationBlocksTexture);
		RenderUtility.renderCube(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5, Block.stone, RenderUtility.getIcon(Reference.PREFIX + "glyph_" + itemStack.getItemDamage()));
		GL11.glPopMatrix();
	}
}
