package resonantinduction.quantum.gate;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import resonant.api.items.ISimpleItemRenderer;
import resonant.lib.render.RenderUtility;
import resonantinduction.core.Reference;
import codechicken.lib.vec.Cuboid6;

public class RenderQuantumGlyph implements ISimpleItemRenderer
{
	public static final RenderQuantumGlyph INSTANCE = new RenderQuantumGlyph();

	public void render(PartQuantumGlyph part, double x, double y, double z)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		Cuboid6 bound = part.getBounds();
		RenderUtility.bind(TextureMap.locationBlocksTexture);
		RenderUtility.renderCube(bound.min.x, bound.min.y, bound.min.z, bound.max.x, bound.max.y, bound.max.z, Block.stone, RenderUtility.getIcon(Reference.PREFIX + "glyph_" + part.number));
		GL11.glPopMatrix();
	}

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(-0.25, -0.25, -0.25);
		RenderUtility.bind(TextureMap.locationBlocksTexture);
		RenderUtility.renderCube(0, 0, 0, 0.5, 0.5, 0.5, Block.stone, RenderUtility.getIcon(Reference.PREFIX + "glyph_" + itemStack.getItemDamage()));
		GL11.glPopMatrix();
	}
}
