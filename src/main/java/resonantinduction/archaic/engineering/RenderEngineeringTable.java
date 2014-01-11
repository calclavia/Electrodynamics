package resonantinduction.archaic.engineering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureCompass;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEngineeringTable extends TileEntitySpecialRenderer
{
	private final RenderBlocks renderBlocks = new RenderBlocks();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float var8)
	{
		Vector3 vec = new Vector3(x, y, z);
		double distance = vec.distance(new Vector3(0, 0, 0));

		if (tileEntity instanceof TileEngineeringTable)
		{
			TileEngineeringTable tile = (TileEngineeringTable) tileEntity;

			RenderItem renderItem = ((RenderItem) RenderManager.instance.getEntityClassRenderObject(EntityItem.class));

			/**
			 * Render the Crafting Matrix
			 */
			for (int i = 0; i < tile.craftingMatrix.length; i++)
			{
				if (tile.craftingMatrix[i] != null)
				{
					GL11.glPushMatrix();
					GL11.glTranslated(x + (double) (i / 3) / 3d + (0.5 / 3d), y + 1.1, z + (double) (i % 3) / 3d + (0.5 / 3d));
					GL11.glScalef(0.7f, 0.7f, 0.7f);
					this.renderItem(tileEntity.worldObj, ForgeDirection.UP, tile.craftingMatrix[i], new Vector3(), 0, 0);
					GL11.glPopMatrix();
				}
			}

			/**
			 * Render the Output
			 */
			String itemName = "No Output";
			String amount = "";
			ItemStack itemStack = tile.getStackInSlot(9);

			if (itemStack != null)
			{
				itemName = itemStack.getDisplayName();
				amount = Integer.toString(itemStack.stackSize);
			}

			for (int side = 2; side < 6; side++)
			{
				ForgeDirection direction = ForgeDirection.getOrientation(side);

				if (tile.worldObj.isBlockSolidOnSide(tile.xCoord + direction.offsetX, tile.yCoord, tile.zCoord + direction.offsetZ, direction.getOpposite()))
				{
					continue;
				}

				this.setupLight(tile, direction.offsetX, direction.offsetZ);
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

				if (itemStack != null)
				{
					GL11.glPushMatrix();

					switch (side)
					{
						case 2:
							GL11.glTranslated(x + 0.65, y + 0.9, z - 0.01);
							break;
						case 3:
							GL11.glTranslated(x + 0.35, y + 0.9, z + 1.01);
							GL11.glRotatef(180, 0, 1, 0);
							break;
						case 4:
							GL11.glTranslated(x - 0.01, y + 0.9, z + 0.35);
							GL11.glRotatef(90, 0, 1, 0);
							break;
						case 5:
							GL11.glTranslated(x + 1.01, y + 0.9, z + 0.65);
							GL11.glRotatef(-90, 0, 1, 0);
							break;
					}

					float scale = 0.03125F;
					GL11.glScalef(0.6f * scale, 0.6f * scale, 0);
					GL11.glRotatef(180, 0, 0, 1);

					TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;

					GL11.glDisable(2896);
					if (!ForgeHooksClient.renderInventoryItem(this.renderBlocks, renderEngine, itemStack, true, 0.0F, 0.0F, 0.0F))
					{
						renderItem.renderItemIntoGUI(this.getFontRenderer(), renderEngine, itemStack, 0, 0);
					}
					GL11.glEnable(2896);

					GL11.glPopMatrix();
				}

				this.renderText(itemName, side, 0.02f, x, y - 0.35f, z);
				this.renderText(amount, side, 0.02f, x, y - 0.15f, z);
			}
		}
	}

	private void setupLight(TileEntity tileEntity, int xDifference, int zDifference)
	{
		World world = tileEntity.worldObj;

		if (world.isBlockOpaqueCube(tileEntity.xCoord + xDifference, tileEntity.yCoord, tileEntity.zCoord + zDifference))
		{
			return;
		}

		int br = world.getLightBrightnessForSkyBlocks(tileEntity.xCoord + xDifference, tileEntity.yCoord, tileEntity.zCoord + zDifference, 0);
		int var11 = br % 65536;
		int var12 = br / 65536;
		float scale = 0.6F;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, var11 * scale, var12 * scale);
	}

	private void renderText(String text, int side, float maxScale, double x, double y, double z)
	{
		GL11.glPushMatrix();

		GL11.glPolygonOffset(-10, -10);
		GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);

		float displayWidth = 1 - (2 / 16);
		float displayHeight = 1 - (2 / 16);
		GL11.glTranslated(x, y, z);

		switch (side)
		{
			case 3:
				GL11.glTranslatef(0, 1, 0);
				GL11.glRotatef(0, 0, 1, 0);
				GL11.glRotatef(90, 1, 0, 0);

				break;
			case 2:
				GL11.glTranslatef(1, 1, 1);
				GL11.glRotatef(180, 0, 1, 0);
				GL11.glRotatef(90, 1, 0, 0);

				break;
			case 5:
				GL11.glTranslatef(0, 1, 1);
				GL11.glRotatef(90, 0, 1, 0);
				GL11.glRotatef(90, 1, 0, 0);

				break;
			case 4:
				GL11.glTranslatef(1, 1, 0);
				GL11.glRotatef(-90, 0, 1, 0);
				GL11.glRotatef(90, 1, 0, 0);
				break;
		}

		// Find Center
		GL11.glTranslatef(displayWidth / 2, 1F, displayHeight / 2);
		GL11.glRotatef(-90, 1, 0, 0);

		FontRenderer fontRenderer = this.getFontRenderer();

		int requiredWidth = Math.max(fontRenderer.getStringWidth(text), 1);
		int lineHeight = fontRenderer.FONT_HEIGHT + 2;
		int requiredHeight = lineHeight * 1;
		float scaler = 0.8f;
		float scaleX = (displayWidth / requiredWidth);
		float scaleY = (displayHeight / requiredHeight);
		float scale = scaleX * scaler;

		if (maxScale > 0)
		{
			scale = Math.min(scale, maxScale);
		}

		GL11.glScalef(scale, -scale, scale);
		GL11.glDepthMask(false);

		int offsetX;
		int offsetY;
		int realHeight = (int) Math.floor(displayHeight / scale);
		int realWidth = (int) Math.floor(displayWidth / scale);

		offsetX = (realWidth - requiredWidth) / 2;
		offsetY = (realHeight - requiredHeight) / 2;

		GL11.glDisable(GL11.GL_LIGHTING);
		fontRenderer.drawString("\u00a7f" + text, offsetX - (realWidth / 2), 1 + offsetY - (realHeight / 2), 1);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);

		GL11.glPopMatrix();
	}

	private void renderItem(World world, ForgeDirection dir, ItemStack itemStack, Vector3 position, float rotationYaw, int angle)
	{
		if (itemStack != null)
		{
			EntityItem entityitem = new EntityItem(world, 0.0D, 0.0D, 0.0D, itemStack);
			entityitem.getEntityItem().stackSize = 1;
			entityitem.hoverStart = 0.0F;
			GL11.glPushMatrix();
			GL11.glTranslatef(-0.453125F * (float) dir.offsetX, -0.18F, -0.453125F * (float) dir.offsetZ);
			GL11.glRotatef(180.0F + rotationYaw, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef((float) (-90 * angle), 0.0F, 0.0F, 1.0F);

			switch (angle)
			{
				case 1:
					GL11.glTranslatef(-0.16F, -0.16F, 0.0F);
					break;
				case 2:
					GL11.glTranslatef(0.0F, -0.32F, 0.0F);
					break;
				case 3:
					GL11.glTranslatef(0.16F, -0.16F, 0.0F);
			}

			RenderItem.renderInFrame = true;
			RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
			RenderItem.renderInFrame = false;

			GL11.glPopMatrix();
		}
	}

}
