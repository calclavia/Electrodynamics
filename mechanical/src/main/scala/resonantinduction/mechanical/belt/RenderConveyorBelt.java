package resonantinduction.mechanical.belt;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.mechanical.belt.TileConveyorBelt.BeltType;
import calclavia.lib.render.item.ISimpleItemRenderer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderConveyorBelt extends TileEntitySpecialRenderer implements ISimpleItemRenderer
{
	public static final ModelConveyorBelt MODEL = new ModelConveyorBelt();
	public static final ModelAngledBelt MODEL2 = new ModelAngledBelt();

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		TileConveyorBelt tileEntity = (TileConveyorBelt) t;
		BeltType slantType = tileEntity.getBeltType();
		int face = tileEntity.getDirection().ordinal();

		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GL11.glRotatef(180f, 0f, 0f, 1f);

		int frame = tileEntity.getAnimationFrame();

		if (slantType != null && slantType != BeltType.NORMAL)
		{
			switch (face)
			{
				case 2:
					GL11.glRotatef(180f, 0f, 1f, 0f);
					break;
				case 3:
					GL11.glRotatef(0f, 0f, 1f, 0f);
					break;
				case 4:
					GL11.glRotatef(90f, 0f, 1f, 0f);
					break;
				case 5:
					GL11.glRotatef(-90f, 0f, 1f, 0f);
					break;
			}

			if (slantType == BeltType.SLANT_UP)
			{
				ResourceLocation name = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "slantedbelt/frame" + frame + ".png");
				bindTexture(name);

				GL11.glTranslatef(0f, 0.8f, -0.8f);
				GL11.glRotatef(180f, 0f, 1f, 1f);
				boolean slantAdjust = false;
				TileEntity test = tileEntity.worldObj.getBlockTileEntity(tileEntity.xCoord + tileEntity.getDirection().offsetX, tileEntity.yCoord, tileEntity.zCoord + tileEntity.getDirection().offsetZ);
				if (test != null)
				{
					if (test instanceof TileConveyorBelt)
					{
						if (((TileConveyorBelt) test).getBeltType() == BeltType.RAISED)
						{
							GL11.glRotatef(10f, 1f, 0f, 0f);
							slantAdjust = true;
						}
					}
				}
				MODEL2.render(0.0625F, true);
			}
			else if (slantType == BeltType.SLANT_DOWN)
			{
				ResourceLocation name = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "slantedbelt/frame" + frame + ".png");
				bindTexture(name);
				GL11.glRotatef(180f, 0f, 1f, 0f);
				boolean slantAdjust = false;
				TileEntity test = tileEntity.worldObj.getBlockTileEntity(tileEntity.xCoord - tileEntity.getDirection().offsetX, tileEntity.yCoord, tileEntity.zCoord - tileEntity.getDirection().offsetZ);
				if (test != null)
				{
					if (test instanceof TileConveyorBelt)
					{
						if (((TileConveyorBelt) test).getBeltType() == BeltType.RAISED)
						{
							GL11.glRotatef(-10f, 1f, 0f, 0f);
							GL11.glTranslatef(0f, 0.25f, 0f);
							slantAdjust = true;
						}
					}
				}
				MODEL2.render(0.0625F, slantAdjust);
			}
			else
			{
				ResourceLocation name = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "belt/frame" + frame + ".png");
				bindTexture(name);
				GL11.glRotatef(180, 0f, 1f, 0f);
				GL11.glTranslatef(0f, -0.68f, 0f);
				MODEL.render(0.0625f, (float) Math.toRadians(tileEntity.node.angle), false, false, false, false);
			}
		}
		else
		{
			switch (face)
			{
				case 2:
					GL11.glRotatef(0f, 0f, 1f, 0f);
					break;
				case 3:
					GL11.glRotatef(180f, 0f, 1f, 0f);
					break;
				case 4:
					GL11.glRotatef(-90f, 0f, 1f, 0f);
					break;
				case 5:
					GL11.glRotatef(90f, 0f, 1f, 0f);
					break;
			}
			ResourceLocation name = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "belt/frame" + frame + ".png");
			bindTexture(name);
			MODEL.render(0.0625F, (float) Math.toRadians(tileEntity.node.angle), false, false, false, true);

		}

		int ent = tileEntity.worldObj.getBlockId(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);

		GL11.glPopMatrix();
	}

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef(0.5f, 1.7F, 0.5f);
		GL11.glRotatef(180f, 0f, 0f, 1f);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "belt/frame0.png"));
		MODEL.render(0.0625F, 0, false, false, false, false);
		GL11.glPopMatrix();
	}
}