package resonantinduction.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import resonantinduction.Reference;
import resonantinduction.client.model.ModelMiningLaserGun;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMiningLaserGun implements IItemRenderer
{

	private static final ModelMiningLaserGun MODEL = new ModelMiningLaserGun();
	private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "LaserGun.png");

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		GL11.glPushMatrix();

		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);

		if (type == ItemRenderType.EQUIPPED_FIRST_PERSON)
		{
			float scale = 3f;

			if (Minecraft.getMinecraft().thePlayer.getItemInUse() != item)
			{
				GL11.glScalef(scale, scale, scale);
				GL11.glTranslatef(0, 1.5f, .5f);
				GL11.glRotatef(180, 0, 0, 1);
				GL11.glRotatef(30, 0, 1, 0);
				GL11.glRotatef(10, 1, 0, 0);
			}
			else
			{
				scale = 8f;
				GL11.glScalef(scale, scale, scale);
				GL11.glTranslatef(-.01f, 1.0f, .8f);
				GL11.glRotatef(180, 0, 0, 1);
				GL11.glRotatef(40, 0, 1, 0);
				GL11.glRotatef(-20, 1, 0, 0);
			}
		}
		else if (type == ItemRenderType.EQUIPPED)
		{
			float scale = 3f;
			GL11.glScalef(scale, scale, scale);
			GL11.glRotatef(-105, 0, 0, 1);
			GL11.glRotatef(-70, 0, 1, 0);
			GL11.glTranslatef(0.3f, -0.9f, 0.42f);
		}
		else if (type == ItemRenderType.INVENTORY)
		{
			float scale = 2f;
			GL11.glScalef(scale, scale, scale);
			GL11.glRotatef(180, 0, 0, 1);
			GL11.glRotatef(-70, 0, 1, 0);
			GL11.glTranslatef(-0.07f, -1.2f, 0.52f);
		}
		MODEL.render(0.0625F);

		GL11.glPopMatrix();
	}
}
