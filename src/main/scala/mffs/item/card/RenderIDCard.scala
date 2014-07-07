package mffs.item.card

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import mffs.ModularForceFieldSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.{ItemRenderer, Tessellator}
import net.minecraft.entity.EntityLiving
import net.minecraft.item.ItemStack
import net.minecraft.util.{IIcon, ResourceLocation}
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.IItemRenderer.ItemRenderType
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.{GL11, GL12}
import resonant.api.mffs.card.ICardIdentification
import universalelectricity.core.transform.vector.Vector2

/**
 * All thanks to Briman for the ID card face rendering! Check out the mod MineForver!
 *
 * @author Briman, Calclavia
 */
@SideOnly(Side.CLIENT)
class RenderIDCard extends IItemRenderer
{
  def renderItem(renderType: IItemRenderer.ItemRenderType, itemStack: ItemStack, data: AnyRef*)
  {
    if (itemStack.getItem.isInstanceOf[ICardIdentification])
    {
      val card: ICardIdentification = itemStack.getItem.asInstanceOf[ICardIdentification]
      glPushMatrix
      glDisable(GL_CULL_FACE)
      transform(renderType)
      renderItemIcon(ModularForceFieldSystem.Items.cardID.getIcon(itemStack, 0))

      if (renderType != ItemRenderType.INVENTORY)
      {
        glTranslatef(0f, 0f, -0.0005f)
      }
      renderPlayerFace(getSkinFace(card.getProfile(itemStack).getName))
      glEnable(GL_CULL_FACE)
      glPopMatrix
    }
  }

  private def transform(renderType: IItemRenderer.ItemRenderType)
  {
    val scale: Float = 0.0625f
    if (renderType ne ItemRenderType.INVENTORY)
    {
      glScalef(scale, -scale, -scale)
      glTranslatef(20f, -16f, 0f)
      glRotatef(180f, 1f, 1f, 0f)
      glRotatef(-90f, 0f, 0f, 1f)
    }
    if (renderType eq ItemRenderType.ENTITY)
    {
      glTranslatef(20f, 0f, 0f)
      glRotatef(Minecraft.getSystemTime / 12f % 360f, 0f, 1f, 0f)
      glTranslatef(-8f, 0f, 0f)
      glTranslated(0.0, 2.0 * Math.sin(Minecraft.getSystemTime / 512.0 % 360.0), 0.0)
    }
  }

  private def getSkinFace(name: String): ResourceLocation =
  {
    try
    {
      var resourcelocation: ResourceLocation = Minecraft.getMinecraft.thePlayer.getLocationSkin

      if (name != null && !name.isEmpty)
      {
        resourcelocation = AbstractClientPlayer.getLocationSkin(name)
        AbstractClientPlayer.getDownloadImageSkin(resourcelocation, name)
        return resourcelocation
      }
    }
    catch
      {
        case e: Exception =>
        {
          e.printStackTrace
        }
      }
    return null
  }

  private def renderPlayerFace(resourcelocation: ResourceLocation)
  {
    if (resourcelocation != null)
    {
      val translation: Vector2 = new Vector2(9, 5)
      val xSize: Int = 4
      val ySize: Int = 4
      val topLX: Int = translation.xi
      val topRX: Int = translation.xi + xSize
      val botLX: Int = translation.xi
      val botRX: Int = translation.xi + xSize
      val topLY: Int = translation.yi
      val topRY: Int = translation.yi
      val botLY: Int = translation.yi + ySize
      val botRY: Int = translation.yi + ySize
      FMLClientHandler.instance.getClient.renderEngine.bindTexture(resourcelocation)
      glColor4f(1, 1, 1, 1)
      glBegin(GL_QUADS)

      glTexCoord2f(1f / 8f, 1f / 4f)
      glVertex2f(topLX, topLY)
      glTexCoord2f(1f / 8f, 2f / 4f)
      glVertex2f(botLX, botLY)
      glTexCoord2f(2f / 8f, 2f / 4f)
      glVertex2f(botRX, botRY)
      glTexCoord2f(2f / 8f, 1f / 4f)
      glVertex2f(topRX, topRY)

      glEnd
      glBegin(GL_QUADS)

      glTexCoord2f(5f / 8f, 1f / 4f)
      glVertex2f(topLX, topLY)
      glTexCoord2f(5f / 8f, 2f / 4f)
      glVertex2f(botLX, botLY)
      glTexCoord2f(6f / 8f, 2f / 4f)
      glVertex2f(botRX, botRY)
      glTexCoord2f(6f / 8f, 1f / 4f)
      glVertex2f(topRX, topRY)

      glEnd
    }
  }

  private def renderItemIcon(icon: IIcon)
  {
    glBegin(GL_QUADS)
    
      glTexCoord2f(icon.getMinU, icon.getMinV)
      glVertex2f(0, 0)
      glTexCoord2f(icon.getMinU, icon.getMaxV)
      glVertex2f(0, 16)
      glTexCoord2f(icon.getMaxU, icon.getMaxV)
      glVertex2f(16, 16)
      glTexCoord2f(icon.getMaxU, icon.getMinV)
      glVertex2f(16, 0)
    
    glEnd
  }

  private def renderItem3D(par1EntityLiving: EntityLiving, par2ItemStack: ItemStack, par3: Int)
  {
    val icon: IIcon = par1EntityLiving.getItemIcon(par2ItemStack, par3)
    if (icon == null)
    {
      glPopMatrix
      return
    }
    
    val tessellator: Tessellator = Tessellator.instance
    val f: Float = icon.getMinU
    val f1: Float = icon.getMaxU
    val f2: Float = icon.getMinV
    val f3: Float = icon.getMaxV
    val f4: Float = 0.0F
    val f5: Float = 0.3F
    glEnable(GL12.GL_RESCALE_NORMAL)
    glTranslatef(-f4, -f5, 0.0F)
    val f6: Float = 1.5F
    glScalef(f6, f6, f6)
    glRotatef(50.0F, 0.0F, 1.0F, 0.0F)
    glRotatef(335.0F, 0.0F, 0.0F, 1.0F)
    glTranslatef(-0.9375F, -0.0625F, 0.0F)
    ItemRenderer.renderItemIn2D(tessellator, f1, f2, f, f3, icon.getIconWidth, icon.getIconHeight, 0.0625F)
    glDisable(GL12.GL_RESCALE_NORMAL)
  }

  def handleRenderType(item: ItemStack, `type`: IItemRenderer.ItemRenderType): Boolean =
  {
    return true
  }

  def shouldUseRenderHelper(`type`: IItemRenderer.ItemRenderType, item: ItemStack, helper: IItemRenderer.ItemRendererHelper): Boolean =
  {
    return false
  }
}