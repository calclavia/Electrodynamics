package resonantinduction.mechanical.belt;

import java.util.HashMap;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonant.api.items.ISimpleItemRenderer;
import resonantinduction.core.Reference;
import resonantinduction.mechanical.belt.TileConveyorBelt.BeltType;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderConveyorBelt extends TileEntitySpecialRenderer implements ISimpleItemRenderer
{
    public static final ModelConveyorBelt MODEL = new ModelConveyorBelt();
    public static final ModelAngledBelt MODEL2 = new ModelAngledBelt();
    public static HashMap<Integer, ResourceLocation> beltTextureFrames = new HashMap<Integer, ResourceLocation>();
    public static HashMap<Integer, ResourceLocation> slantedBeltTextureFrames = new HashMap<Integer, ResourceLocation>();

    @Override
    public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
    {
        final TileConveyorBelt beltTile = (TileConveyorBelt) t;
        final BeltType slantType = beltTile.getBeltType();
        final int face = beltTile.getDirection().ordinal();
        final int frame = beltTile.getAnimationFrame();

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        GL11.glRotatef(180f, 0f, 0f, 1f);

        /* Assign texture */
        bindTexture(getTexture(slantType == BeltType.SLANT_UP || slantType == BeltType.SLANT_DOWN, frame));
        
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
                GL11.glTranslatef(0f, 0.8f, -0.8f);
                GL11.glRotatef(180f, 0f, 1f, 1f);
                TileEntity test = beltTile.worldObj.getBlockTileEntity(beltTile.xCoord + beltTile.getDirection().offsetX, beltTile.yCoord, beltTile.zCoord + beltTile.getDirection().offsetZ);
                if (test != null)
                {
                    if (test instanceof TileConveyorBelt)
                    {
                        if (((TileConveyorBelt) test).getBeltType() == BeltType.RAISED)
                        {
                            GL11.glRotatef(10f, 1f, 0f, 0f);
                        }
                    }
                }
                MODEL2.render(0.0625F, true);
            }
            else if (slantType == BeltType.SLANT_DOWN)
            {
                boolean slantAdjust = false;
                TileEntity test = beltTile.worldObj.getBlockTileEntity(beltTile.xCoord - beltTile.getDirection().offsetX, beltTile.yCoord, beltTile.zCoord - beltTile.getDirection().offsetZ);
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
                GL11.glRotatef(180, 0f, 1f, 0f);
                GL11.glTranslatef(0f, -0.68f, 0f);
                MODEL.render(0.0625f, 0 /* TODO add rotation */, false, false, false, false);
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
            MODEL.render(0.0625F, 0 /* TODO add rotation */, false, false, false, true);

        }
        GL11.glPopMatrix();
    }

    /** Gets the texture for the belt at the given frame of animation
     * 
     * @param slant - is the belt slanted up or down
     * @param frame - frame of animation
     * @return ResourceLocation for the frame of animation */
    public static ResourceLocation getTexture(boolean slant, int frame)
    {
        ResourceLocation texture = null;
        if (slant)
        {
            /* Store slanted belt textures to make retrieving them faster */
            if (slantedBeltTextureFrames.containsKey(frame))
            {
                texture = slantedBeltTextureFrames.get(frame);
            }
            if (texture == null)
            {
                texture = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "slantedbelt/frame" + frame + ".png");
                slantedBeltTextureFrames.put(frame, texture);
            }
        }
        else
        {
            /* Store belt textures to make retrieving them faster */
            if (beltTextureFrames.containsKey(frame))
            {
                texture = beltTextureFrames.get(frame);
            }
            if (texture == null)
            {
                texture = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "belt/frame" + frame + ".png");
                beltTextureFrames.put(frame, texture);
            }
        }
        return texture;
    }

    @Override
    public void renderInventoryItem(ItemStack itemStack)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef(0.5f, 1.7F, 0.5f);
        GL11.glRotatef(180f, 0f, 0f, 1f);
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(getTexture(false, 0));
        MODEL.render(0.0625F, 0, false, false, false, false);
        GL11.glPopMatrix();
    }
}