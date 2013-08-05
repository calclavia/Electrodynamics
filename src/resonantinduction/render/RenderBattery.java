/**
 * 
 */
package resonantinduction.render;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonantinduction.ResonantInduction;
import resonantinduction.base.Vector3;
import resonantinduction.model.ModelBattery;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderBattery extends TileEntitySpecialRenderer
{
	public static final ResourceLocation TEXTURE = new ResourceLocation(ResonantInduction.DOMAIN, ResonantInduction.MODEL_TEXTURE_DIRECTORY + "battery.png");
	public static final ModelBattery MODEL = new ModelBattery();
	private EntityItem fakeBattery;
	private Random random = new Random();
	protected RenderManager renderManager;

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		if (this.fakeBattery == null)
		{
			this.fakeBattery = new EntityItem(t.worldObj, 0, 0, 0, new ItemStack(ResonantInduction.itemCapacitor));
			this.fakeBattery.age = 10;
		}

		if (this.renderManager == null)
		{
			this.renderManager = RenderManager.instance;
		}

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		this.func_110628_a(TEXTURE);
		MODEL.render(0.0625f);
		GL11.glPopMatrix();

		int renderAmount = 16;

		itemRender:
		for (int i = 2; i < 6; i++)
		{
			ForgeDirection direction = ForgeDirection.getOrientation(i);
			Block block = Block.blocksList[new Vector3(t).translate(new Vector3(direction)).getBlockID(t.worldObj)];
			if (block == null || (block != null && block.isOpaqueCube()))
			{
				for (int slot = 0; slot < 4; slot++)
				{
					GL11.glPushMatrix();
					GL11.glTranslatef((float) x + 0.5f, (float) y + 0.7f, (float) z + 0.5f);

					float translateX = 0;
					float translateY = 0;

					switch (slot)
					{
						case 0:
							translateX = 0.25f;
							break;
						case 1:
							translateX = 0.25f;
							translateY = -0.5f;
							break;
						case 2:
							translateX = -0.25f;
							translateY = -0.5f;
							break;
						case 3:
							translateX = -0.25f;
							break;
					}

					switch (direction)
					{
						case NORTH:
							GL11.glTranslatef(-0.5f, 0, 0);
							GL11.glTranslatef(0, translateY, translateX);
							GL11.glRotatef(90, 0, 1, 0);
							break;
						case SOUTH:
							GL11.glTranslatef(0, 0, -0.5f);
							GL11.glTranslatef(translateX, translateY, 0);
							break;
						case WEST:
							GL11.glTranslatef(0.5f, 0, 0);
							GL11.glTranslatef(0, translateY, translateX);
							GL11.glRotatef(90, 0, 1, 0);
							break;
						case EAST:
							GL11.glTranslatef(0, 0, 0.5f);
							GL11.glTranslatef(translateX, translateY, 0);
							break;
					}

					GL11.glScalef(0.5f, 0.5f, 0.5f);
					this.renderItemSimple(this.fakeBattery);
					GL11.glPopMatrix();

					if (renderAmount-- <= 0)
					{
						break itemRender;
					}
				}
			}
		}
	}

	public void renderItemSimple(EntityItem entityItem)
	{
		Tessellator tessellator = Tessellator.instance;
		ItemStack itemStack = entityItem.getEntityItem();

		for (int k = 0; k < itemStack.getItem().getRenderPasses(itemStack.getItemDamage()); ++k)
		{
			Icon icon = itemStack.getItem().getIcon(itemStack, k);

			if (icon == null)
			{
				TextureManager texturemanager = Minecraft.getMinecraft().func_110434_K();
				ResourceLocation resourcelocation = texturemanager.func_130087_a(entityItem.getEntityItem().getItemSpriteNumber());
				icon = ((TextureMap) texturemanager.func_110581_b(resourcelocation)).func_110572_b("missingno");
			}

			float f4 = ((Icon) icon).getMinU();
			float f5 = ((Icon) icon).getMaxU();
			float f6 = ((Icon) icon).getMinV();
			float f7 = ((Icon) icon).getMaxV();
			float f8 = 1.0F;
			float f9 = 0.5F;
			float f10 = 0.25F;
			float f11;

			GL11.glPushMatrix();

			float f12 = 0.0625F;
			f11 = 0.021875F;
			ItemStack itemstack = entityItem.getEntityItem();
			int j = itemstack.stackSize;
			byte b0 = getMiniItemCount(itemstack);

			GL11.glTranslatef(-f9, -f10, -((f12 + f11) * (float) b0 / 2.0F));

			for (int kj = 0; kj < b0; ++kj)
			{
				// Makes items offset when in 3D, like when in 2D, looks much better. Considered a
				// vanilla bug...
				if (kj > 0)
				{
					float x = (random.nextFloat() * 2.0F - 1.0F) * 0.3F / 0.5F;
					float y = (random.nextFloat() * 2.0F - 1.0F) * 0.3F / 0.5F;
					float z = (random.nextFloat() * 2.0F - 1.0F) * 0.3F / 0.5F;
					GL11.glTranslatef(x, y, f12 + f11);
				}
				else
				{
					GL11.glTranslatef(0f, 0f, f12 + f11);
				}

				if (itemstack.getItemSpriteNumber() == 0)
				{
					this.func_110776_a(TextureMap.field_110575_b);
				}
				else
				{
					this.func_110776_a(TextureMap.field_110576_c);
				}

				GL11.glColor4f(1, 1, 1, 1.0F);
				ItemRenderer.renderItemIn2D(tessellator, f5, f6, f4, f7, ((Icon) icon).getOriginX(), ((Icon) icon).getOriginY(), f12);
			}

			GL11.glPopMatrix();
		}

	}

	protected void func_110776_a(ResourceLocation par1ResourceLocation)
	{
		this.renderManager.renderEngine.func_110577_a(par1ResourceLocation);
	}

	public byte getMiniItemCount(ItemStack stack)
	{
		byte ret = 1;
		if (stack.stackSize > 1)
			ret = 2;
		if (stack.stackSize > 15)
			ret = 3;
		if (stack.stackSize > 31)
			ret = 4;
		return ret;
	}
}
