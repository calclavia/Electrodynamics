package mffs.render;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
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
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.ResourceLocation;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import universalelectricity.core.vector.Vector2;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * All thanks to Briman for the ID card face rendering! Check out the mod MineForver!
 * 
 * @author Briman, Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderIDCard implements IItemRenderer
{
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
				// Prevent Z fighting.
				glTranslatef(0f, 0f, -0.0005f);
			}

			this.renderPlayerFace(this.getSkinFace(card.getUsername(itemStack)));

			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
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

	private ResourceLocation getSkinFace(String name)
	{
		try
		{
			ResourceLocation resourcelocation = AbstractClientPlayer.field_110314_b;

			if (name != null && !name.isEmpty())
			{
				resourcelocation = AbstractClientPlayer.func_110305_h(name);
				AbstractClientPlayer.func_110304_a(resourcelocation, name);
				return resourcelocation;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private void renderPlayerFace(ResourceLocation resourcelocation)
	{
		if (resourcelocation != null)
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

			FMLClientHandler.instance().getClient().renderEngine.func_110577_a(resourcelocation);
			// glBindTexture(GL_TEXTURE_2D, texID);

			glColor4f(1, 1, 1, 1);
			// Face
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
	}

	private void renderItemIcon(Icon icon)
	{
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
		ItemRenderer.renderItemIn2D(tessellator, f1, f2, f, f3, icon.getOriginX(), icon.getOriginY(), 0.0625F);
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