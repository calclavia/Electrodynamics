package resonantinduction.core;

import net.minecraft.util.EnumMovingObjectType;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.ForgeSubscribe;

import org.lwjgl.opengl.GL11;

import calclavia.lib.render.RenderUtility;
import resonantinduction.mechanical.Mechanical;
import resonantinduction.mechanical.gear.RenderGear;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.FacePlacementGrid$;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MicroblockHighlightHandler
{
	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void drawBlockHighlight(DrawBlockHighlightEvent event)
	{
		if (event.currentItem != null && event.currentItem.getItem() == Mechanical.itemGear && event.target != null && event.target.typeOfHit == EnumMovingObjectType.TILE)
		{
			GL11.glPushMatrix();
			RenderUtils.translateToWorldCoords(event.player, event.partialTicks);
			Vector3 hit = new Vector3(event.target.hitVec);
			FacePlacementGrid$.MODULE$.render(hit, event.target.sideHit);
			event.setCanceled(true);
			GL11.glPopMatrix();
		}
	}
}
