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
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import universalelectricity.core.vector.Vector2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * All thanks to Briman for the ID card face rendering! Check out the mod MineForver!
 * 
 * @author Calclavia, Briman
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderIDCard implements IItemRenderer
{
	private Minecraft mc;

	public RenderIDCard()
	{
		this.mc = Minecraft.getMinecraft();
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack itemStack, Object... data)
	{
		/*
		 * if (type == ItemRenderType.EQUIPPED) { glPushMatrix();
		 * 
		 * this.renderItem3D((EntityLiving) data[1], itemStack, 0); glPopMatrix(); }
		 */
		if (itemStack.getItem() instanceof ICardIdentification)
		{
			ICardIdentification card = (ICardIdentification) itemStack.getItem();

			glPushMatrix();
			glDisable(GL_CULL_FACE);

			this.transform(type);
			this.renderItemIcon(ModularForceFieldSystem.itemCardID.getIcon(itemStack, 0));

			if (type != ItemRenderType.INVENTORY)
			{
				// Prevent Z fighting.
				glTranslatef(0f, 0f, -0.0005f);
			}

			this.renderPlayerFace(this.getSkin(card.getUsername(itemStack)));

			if (type != ItemRenderType.INVENTORY)
			{
				glTranslatef(0f, 0f, 0.002f);
				this.renderItemIcon(ModularForceFieldSystem.itemCardID.getIcon(itemStack, 0));
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

	private void renderPlayerFace(int texID)
	{
		Vector2 translation = new Vector2(9, 5);
		int xSize = 4;
		int ySize = 4;

		int topLX = translation.intX();
		int topRX = translation.intX() + xSize;
		int botLX = translation.intX();
		int botRX = translation.intX() + xSize;

		int topLY = translation.intY();
		int topRY = translation.intY();
		int botLY = translation.intY() + ySize;
		int botRY = translation.intY() + ySize;

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

	private void renderItem3D(EntityLiving par1EntityLiving, ItemStack par2ItemStack, int par3)
	{
		Icon icon = par1EntityLiving.getItemIcon(par2ItemStack, par3);

		if (icon == null)
		{
			GL11.glPopMatrix();
			return;
		}

		if (par2ItemStack.getItemSpriteNumber() == 0)
		{
			this.mc.renderEngine.bindTexture("/terrain.png");
		}
		else
		{
			this.mc.renderEngine.bindTexture("/gui/items.png");
		}

		Tessellator tessellator = Tessellator.instance;
		float f = icon.getMinU();
		float f1 = icon.getMaxU();
		float f2 = icon.getMinV();
		float f3 = icon.getMaxV();
		float f4 = 0.0F;
		float f5 = 0.3F;
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glTranslatef(-f4, -f5, 0.0F);
		float f6 = 1.5F;
		GL11.glScalef(f6, f6, f6);
		GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
		GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
		ItemRenderer.renderItemIn2D(tessellator, f1, f2, f, f3, icon.getSheetWidth(), icon.getSheetHeight(), 0.0625F);

		if (par2ItemStack != null && par2ItemStack.hasEffect() && par3 == 0)
		{
			GL11.glDepthFunc(GL11.GL_EQUAL);
			GL11.glDisable(GL11.GL_LIGHTING);
			this.mc.renderEngine.bindTexture("%blur%/misc/glint.png");
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
			float f7 = 0.76F;
			GL11.glColor4f(0.5F * f7, 0.25F * f7, 0.8F * f7, 1.0F);
			GL11.glMatrixMode(GL11.GL_TEXTURE);
			GL11.glPushMatrix();
			float f8 = 0.125F;
			GL11.glScalef(f8, f8, f8);
			float f9 = Minecraft.getSystemTime() % 3000L / 3000.0F * 8.0F;
			GL11.glTranslatef(f9, 0.0F, 0.0F);
			GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
			ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
			GL11.glPopMatrix();
			GL11.glPushMatrix();
			GL11.glScalef(f8, f8, f8);
			f9 = Minecraft.getSystemTime() % 4873L / 4873.0F * 8.0F;
			GL11.glTranslatef(-f9, 0.0F, 0.0F);
			GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
			ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
			GL11.glPopMatrix();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDepthFunc(GL11.GL_LEQUAL);
		}

		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return false;
	}

}