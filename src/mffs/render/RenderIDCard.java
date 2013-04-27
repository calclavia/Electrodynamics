package mffs.render;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2f;
import mffs.ModularForceFieldSystem;
import mffs.api.card.ICardIdentification;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.client.IItemRenderer;

/**
 * All thanks to Briman for the ID card face rendering! Check out the mod MineForver!
 * 
 * @author Briman
 * 
 */
public class RenderIDCard implements IItemRenderer
{
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return item.getItemDamage() == 0;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return false;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack itemStack, Object... data)
	{
		if (itemStack.getItem() instanceof ICardIdentification)
		{
			ICardIdentification card = (ICardIdentification) itemStack.getItem();

			glPushMatrix();
			glDisable(GL_CULL_FACE);

			this.transform(type);
			this.renderItemIcon(ModularForceFieldSystem.itemCardID.getIcon(itemStack, 0));

			if (type != ItemRenderType.INVENTORY)
			{
				glTranslatef(0f, 0f, -0.001f);
			}

			this.renderFace(this.getSkin(card.getUsername(itemStack)));

			if (type != ItemRenderType.INVENTORY)
			{
				glTranslatef(0f, 0f, 0.002f);
				renderItemIcon(ModularForceFieldSystem.itemCardID.getIcon(itemStack, 0));
				RenderManager.instance.itemRenderer.renderItemIn2D(Tessellator.instance, par1, par2, par3, par4, par5, par6, par7)
			}

			glEnable(GL_CULL_FACE);
			glPopMatrix();
		}
	}

	private void transform(ItemRenderType type)
	{
		float scale = 0.0625f;
		
		if (type != ItemRenderType.INVENTORY)
		{
			glScalef(scale, -scale, -scale);
			glTranslatef(20f, -16f, 0f);
			glRotatef(180f, 1f, 1f, 0f);
			glRotatef(-90f, 0f, 0f, 1f);
		}
		if (type == ItemRenderType.ENTITY)
		{
			glTranslatef(20f, 0f, 0f);
			glRotatef(Minecraft.getSystemTime() / 12f % 360f, 0f, 1f, 0f);
			glTranslatef(-8f, 0f, 0f);
			glTranslated(0.0, 2.0 * Math.sin(Minecraft.getSystemTime() / 512.0 % 360.0), 0.0);
		}
	}

	private int getSkin(String name)
	{
		try
		{
			String skin = "http://skins.minecraft.net/MinecraftSkins/" + name + ".png";
			Minecraft mc = Minecraft.getMinecraft();
			if (!mc.renderEngine.hasImageData(skin))
				mc.renderEngine.obtainImageData(skin, new ImageBufferDownload());
			return mc.renderEngine.getTextureForDownloadableImage(skin, "/mob/char.png");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return 0;
	}

	private void renderFace(int texID)
	{
		int topLX = 2;
		int topRX = 7;
		int botLX = 2;
		int botRX = 7;
		int topLY = 5;
		int topRY = 4;
		int botLY = 10;
		int botRY = 9;
		glBindTexture(GL_TEXTURE_2D, texID);
		glColor4f(1, 1, 1, 1);
		// face
		glBegin(GL_QUADS);
		{
			glTexCoord2f(1f / 8f, 1f / 4f);
			glVertex2f(topLX, topLY);

			glTexCoord2f(1f / 8f, 2f / 4f);
			glVertex2f(botLX, botLY);

			glTexCoord2f(2f / 8f, 2f / 4f);
			glVertex2f(botRX, botRY);

			glTexCoord2f(2f / 8f, 1f / 4f);
			glVertex2f(topRX, topRY);
		}
		glEnd();
		// mask
		glBegin(GL_QUADS);
		{
			glTexCoord2f(5f / 8f, 1f / 4f);
			glVertex2f(topLX, topLY);

			glTexCoord2f(5f / 8f, 2f / 4f);
			glVertex2f(botLX, botLY);

			glTexCoord2f(6f / 8f, 2f / 4f);
			glVertex2f(botRX, botRY);

			glTexCoord2f(6f / 8f, 1f / 4f);
			glVertex2f(topRX, topRY);
		}
		glEnd();
	}

	private void renderItemIcon(Icon icon)
	{
		Minecraft.getMinecraft().renderEngine.bindTexture("/gui/items.png");
		glBegin(GL_QUADS);
		{
			glTexCoord2f(icon.getMinU(), icon.getMinV());
			glVertex2f(0, 0);

			glTexCoord2f(icon.getMinU(), icon.getMaxV());
			glVertex2f(0, 16);

			glTexCoord2f(icon.getMaxU(), icon.getMaxV());
			glVertex2f(16, 16);

			glTexCoord2f(icon.getMaxU(), icon.getMinV());
			glVertex2f(16, 0);
		}
		glEnd();
	}
}