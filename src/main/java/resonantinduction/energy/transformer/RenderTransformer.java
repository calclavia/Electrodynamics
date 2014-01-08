package resonantinduction.energy.transformer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.ResonantInduction;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.render.CalclaviaRenderHelper;
import calclavia.lib.utility.LanguageUtility;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTransformer
{
    public static final ModelTransformer MODEL = new ModelTransformer();
    public static final ResourceLocation TEXTURE = new ResourceLocation(ResonantInduction.DOMAIN, ResonantInduction.MODEL_TEXTURE_DIRECTORY + "transformer.png");

    public static void render(PartTransformer part, double x, double y, double z)
    {
        String status = LanguageUtility.getLocal((part.stepUp() ? "tooltip.transformer.stepUp" : "tooltip.transformer.stepDown"));

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        MovingObjectPosition movingPosition = player.rayTrace(5, 1f);

        if (movingPosition != null)
        {
            if (new Vector3(part.x(), part.y(), part.z()).equals(new Vector3(movingPosition)))
            {
                CalclaviaRenderHelper.renderFloatingText(status, (float) (x + 0.5), (float) (y - 1), (float) (z + 0.5));
            }
        }

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);

        switch (part.placementSide)
        {
            case DOWN:

                switch (part.face)
                {
                    case 0:
                        GL11.glRotatef(90, 0, 1, 0);
                        break;
                    case 1:
                        GL11.glRotatef(180, 0, 1, 0);
                        break;
                    case 2:
                        GL11.glRotatef(-90, 0, 1, 0);
                        break;
                    case 3:
                        break;
                }
                break;
            case UP:
                GL11.glRotatef(180, 0, 0, 1);
                GL11.glTranslatef(0, -2, 0);
                switch (part.face)
                {
                    case 0:
                        GL11.glRotatef(90, 0, 1, 0);
                        break;
                    case 1:
                        GL11.glRotatef(180, 0, 1, 0);
                        break;
                    case 2:
                        GL11.glRotatef(-90, 0, 1, 0);
                        break;
                    case 3:
                        break;
                }
                break;
            case NORTH:
                GL11.glRotatef(90, 1, 0, 0);
                GL11.glTranslatef(0, -1, -1);
                switch (part.face)
                {
                    case 0:
                        GL11.glRotatef(90, 0, 1, 0);
                        break;
                    case 1:
                        GL11.glRotatef(180, 0, 1, 0);
                        break;
                    case 2:
                        GL11.glRotatef(-90, 0, 1, 0);
                        break;
                    case 3:
                        break;
                }
                break;
            case SOUTH:
                GL11.glRotatef(-90, 1, 0, 0);
                GL11.glTranslatef(0, -1, 1);
                switch (part.face)
                {
                    case 0:
                        GL11.glRotatef(-90, 0, 1, 0);
                        break;
                    case 1:
                        break;
                    case 2:
                        GL11.glRotatef(90, 0, 1, 0);
                        break;
                    case 3:
                        GL11.glRotatef(180, 0, 1, 0);
                        break;

                }
                break;
            case WEST:
                GL11.glRotatef(90, 0, 0, 1);
                GL11.glTranslatef(1, -1, 0);
                switch (part.face)
                {
                    case 0:
                        break;
                    case 1:
                        GL11.glRotatef(90, 0, 1, 0);
                        break;
                    case 2:
                        GL11.glRotatef(180, 0, 1, 0);
                        break;
                    case 3:
                        GL11.glRotatef(-90, 0, 1, 0);
                        break;
                }
                break;
            case EAST:
                GL11.glRotatef(-90, 0, 0, 1);
                GL11.glTranslatef(-1, -1, 0);
                switch (part.face)
                {
                    case 0:
                        GL11.glRotatef(180, 0, 1, 0);
                        break;
                    case 1:
                        GL11.glRotatef(-90, 0, 1, 0);
                        break;
                    case 2:
                        break;
                    case 3:
                        GL11.glRotatef(90, 0, 1, 0);
                        break;
                }
                break;
        }

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);
        MODEL.render(0.0625F);
        GL11.glPopMatrix();
    }
}