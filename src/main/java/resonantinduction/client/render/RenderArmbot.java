package resonantinduction.client.render;

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

import resonantinduction.Reference;
import resonantinduction.client.model.ModelArmbot;
import resonantinduction.mechanics.armbot.TileEntityArmbot;
import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderArmbot extends TileEntitySpecialRenderer
{
	public static final ModelArmbot MODEL = new ModelArmbot();
	public static final String TEXTURE = "armbot.png";

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f)
	{
		if (tileEntity instanceof TileEntityArmbot)
		{
			String cmdText = ((TileEntityArmbot) tileEntity).getCommandDisplayText();
			if (cmdText != null && !cmdText.isEmpty())
			{
				EntityPlayer player = Minecraft.getMinecraft().thePlayer;
				MovingObjectPosition objectPosition = player.rayTrace(8, 1);

				if (objectPosition != null)
				{
					if (objectPosition.blockX == tileEntity.xCoord && (objectPosition.blockY == tileEntity.yCoord || objectPosition.blockY == tileEntity.yCoord + 1) && objectPosition.blockZ == tileEntity.zCoord)
					{
						RenderHelper.renderFloatingText(cmdText, (float) x + 0.5f, ((float) y) + 0.25f, (float) z + 0.5f, 0xFFFFFF);
					}
				}
			}
			ResourceLocation name = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + TEXTURE);
			bindTexture(name);

			GL11.glPushMatrix();
			GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
			GL11.glScalef(1.0F, -1F, -1F);

			MODEL.render(0.0625f, (float) ((TileEntityArmbot) tileEntity).getRotation().x, (float) ((TileEntityArmbot) tileEntity).getRotation().y);

			GL11.glPopMatrix();

			Vector3 handPosition = ((TileEntityArmbot) tileEntity).getDeltaHandPosition();
			handPosition.add(0.5);
			handPosition.add(new Vector3(x, y, z));
			RenderItem renderItem = ((RenderItem) RenderManager.instance.getEntityClassRenderObject(EntityItem.class));
			TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;

			// Items don't move right, so we render them manually. Client side this can only be one
			// object so the bot should return its preferred render item client side
			if (((TileEntityArmbot) tileEntity).getHeldObject() instanceof ItemStack)
			{
				ItemStack itemStack = (ItemStack) ((TileEntityArmbot) tileEntity).getHeldObject();
				if (((TileEntityArmbot) tileEntity).renderEntityItem == null)
				{
					((TileEntityArmbot) tileEntity).renderEntityItem = new EntityItem(tileEntity.worldObj, 0, 0, 0, itemStack);
				}
				else if (!itemStack.isItemEqual(((TileEntityArmbot) tileEntity).renderEntityItem.getEntityItem()))
				{
					((TileEntityArmbot) tileEntity).renderEntityItem = new EntityItem(tileEntity.worldObj, 0, 0, 0, itemStack);
				}
				renderItem.doRenderItem(((TileEntityArmbot) tileEntity).renderEntityItem, handPosition.x, handPosition.y, handPosition.z, 0, f);
			}
		}
	}
}