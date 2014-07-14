package resonantinduction.electrical.armbot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonant.api.items.ISimpleItemRenderer;
import resonant.lib.render.RenderUtility;
import resonantinduction.core.Reference;
import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderArmbot extends TileEntitySpecialRenderer implements ISimpleItemRenderer
{
	public static final ModelArmbot MODEL = new ModelArmbot();
	public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "armbot.png");

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f)
	{
		if (tileEntity instanceof TileArmbot)
		{
			String cmdText = ((TileArmbot) tileEntity).getCommandDisplayText();
			if (cmdText != null && !cmdText.isEmpty())
			{
				EntityPlayer player = Minecraft.getMinecraft().thePlayer;
				MovingObjectPosition objectPosition = player.rayTrace(8, 1);

				if (objectPosition != null)
				{
					if (objectPosition.blockX == tileEntity.xCoord && (objectPosition.blockY == tileEntity.yCoord || objectPosition.blockY == tileEntity.yCoord + 1) && objectPosition.blockZ == tileEntity.zCoord)
					{
						RenderUtility.renderFloatingText(cmdText, new Vector3(x, y, z).add(0.5), 0xFFFFFF);
					}
				}
			}

			bindTexture(TEXTURE);

			GL11.glPushMatrix();
			GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
			GL11.glScalef(1.0F, -1F, -1F);

			MODEL.render(0.0625f, (float) ((TileArmbot) tileEntity).getRotation().x, (float) ((TileArmbot) tileEntity).getRotation().y);

			GL11.glPopMatrix();

			Vector3 handPosition = ((TileArmbot) tileEntity).getDeltaHandPosition();
			handPosition.add(0.5);
			handPosition.add(new Vector3(x, y, z));
			RenderItem renderItem = ((RenderItem) RenderManager.instance.getEntityClassRenderObject(EntityItem.class));
			TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;

			// Items don't move right, so we render them manually. Client side this can only be one
			// object so the bot should return its preferred render item client side
			if (((TileArmbot) tileEntity).getHeldObject() instanceof ItemStack)
			{
				ItemStack itemStack = (ItemStack) ((TileArmbot) tileEntity).getHeldObject();
				if (((TileArmbot) tileEntity).renderEntityItem == null)
				{
					((TileArmbot) tileEntity).renderEntityItem = new EntityItem(tileEntity.worldObj, 0, 0, 0, itemStack);
				}
				else if (!itemStack.isItemEqual(((TileArmbot) tileEntity).renderEntityItem.getEntityItem()))
				{
					((TileArmbot) tileEntity).renderEntityItem = new EntityItem(tileEntity.worldObj, 0, 0, 0, itemStack);
				}
				renderItem.doRenderItem(((TileArmbot) tileEntity).renderEntityItem, handPosition.x, handPosition.y, handPosition.z, 0, f);
			}
		}
	}

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		GL11.glPushMatrix();
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderArmbot.TEXTURE);
		GL11.glTranslatef(0.5f, 1.3f, 0.5f);
		GL11.glRotatef(180f, 0f, 0f, 1f);
		GL11.glScalef(0.8f, 0.8f, 0.8f);
		RenderArmbot.MODEL.render(0.0625F, 0, 0);
		GL11.glPopMatrix();
	}
}