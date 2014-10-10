/**
 *
 */
package resonantinduction.electrical.battery

import java.util.{ArrayList, Arrays, List}

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.model.{AdvancedModelLoader, IModelCustom}
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.{glPopMatrix, glPushMatrix, glRotatef}
import resonant.content.prefab.scala.render.ISimpleItemRenderer
import resonant.lib.render.RenderUtility
import resonantinduction.core.Reference
import universalelectricity.core.transform.vector.Vector3

/**
 * TODO: Make this more efficient.
 *
 * @author Calclavia
 */
@SideOnly(Side.CLIENT) object RenderBattery
{
    var INSTANCE: RenderBattery = new RenderBattery
    final val MODEL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "battery/battery.tcn"))
}

@SideOnly(Side.CLIENT) class RenderBattery extends TileEntitySpecialRenderer with ISimpleItemRenderer
{
    def renderInventoryItem(`type`: IItemRenderer.ItemRenderType, itemStack: ItemStack, data: AnyRef*)
    {
        glPushMatrix
        GL11.glTranslated(0, 0, 0)
        val energyLevel: Int = (((itemStack.getItem.asInstanceOf[ItemBlockBattery]).getEnergy(itemStack).asInstanceOf[Double] / (itemStack.getItem.asInstanceOf[ItemBlockBattery]).getEnergyCapacity(itemStack).asInstanceOf[Double]) * 8).asInstanceOf[Int]
        RenderUtility.bind(Reference.domain, Reference.modelPath + "battery/battery.png")
        val disabledParts: List[String] = new ArrayList[String]
        disabledParts.addAll(Arrays.asList(Array[String]("connector", "connectorIn", "connectorOut"): _*))
        disabledParts.addAll(Arrays.asList(Array[String]("coil1", "coil2", "coil3", "coil4", "coil5", "coil6", "coil7", "coil8"): _*))
        disabledParts.addAll(Arrays.asList(Array[String]("coil1lit", "coil2lit", "coil3lit", "coil4lit", "coil5lit", "coil6lit", "coil7lit", "coil8lit"): _*))
        disabledParts.addAll(Arrays.asList(Array[String]("frame1con", "frame2con", "frame3con", "frame4con"): _*))
        RenderBattery.MODEL.renderAllExcept(disabledParts.toArray(new Array[String](0)): _*)

        for (i <- 1 until 8)
        {
            if (i != 1 || !disabledParts.contains("coil1"))
            {
                if ((8 - i) <= energyLevel) RenderBattery.MODEL.renderOnly("coil" + i + "lit")
                else RenderBattery.MODEL.renderOnly("coil" + i)
            }
        }
        glPopMatrix
    }

    def renderTileEntityAt(t: TileEntity, x: Double, y: Double, z: Double, f: Float)
    {
        val partToDisable: Array[Array[String]] = Array[Array[String]](Array[String]("bottom"), Array[String]("top"), Array[String]("frame1", "frame2"), Array[String]("frame3", "frame4"), Array[String]("frame4", "frame1"), Array[String]("frame2", "frame3"))
        val connectionPartToEnable: Array[Array[String]] = Array[Array[String]](null, null, Array[String]("frame1con", "frame2con"), Array[String]("frame3con", "frame4con"), Array[String]("frame4con", "frame1con"), Array[String]("frame2con", "frame3con"))
        GL11.glPushMatrix
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
        val tile: TileBattery = t.asInstanceOf[TileBattery]
        val energyLevel: Int = Math.round((tile.energy.getEnergy.asInstanceOf[Double] / TileBattery.getEnergyForTier(tile.getBlockMetadata).asInstanceOf[Double]) * 8).asInstanceOf[Int]
        RenderUtility.bind(Reference.domain, Reference.modelPath + "battery/battery.png")
        val disabledParts: List[String] = new ArrayList[String]
        val enabledParts: List[String] = new ArrayList[String]
        for (check <- ForgeDirection.VALID_DIRECTIONS)
        {
            if (new Vector3(t).add(check).getTileEntity(t.getWorldObj).isInstanceOf[TileBattery])
            {
                disabledParts.addAll(Arrays.asList(partToDisable(check.ordinal): _*))
                if (check eq ForgeDirection.UP)
                {
                    enabledParts.addAll(Arrays.asList(partToDisable(check.ordinal): _*))
                    enabledParts.add("coil1")
                }
                else if (check eq ForgeDirection.DOWN)
                {
                    val connectionParts: List[String] = new ArrayList[String]
                    for (sideCheck <- ForgeDirection.VALID_DIRECTIONS) if (sideCheck.offsetY == 0) connectionParts.addAll(Arrays.asList(connectionPartToEnable(sideCheck.ordinal): _*))
                    for (sideCheck <- ForgeDirection.VALID_DIRECTIONS)
                    {
                        if (sideCheck.offsetY == 0)
                        {
                            if (new Vector3(t).add(sideCheck).getTileEntity(t.getWorldObj).isInstanceOf[TileBattery])
                            {
                                connectionParts.removeAll(Arrays.asList(connectionPartToEnable(sideCheck.ordinal)))
                            }
                        }
                    }
                    enabledParts.addAll(connectionParts)
                }
            }
            if (check.offsetY == 0)
            {
                GL11.glPushMatrix
                RenderUtility.rotateBlockBasedOnDirection(check)

                if (check == ForgeDirection.NORTH)
                {
                    glRotatef(0, 0, 1, 0)
                }
                if (check == ForgeDirection.SOUTH)
                {
                    glRotatef(0, 0, 1, 0)
                }
                else if (check == ForgeDirection.WEST)
                {
                    glRotatef(-180, 0, 1, 0)
                }
                else if (check == ForgeDirection.EAST)
                {
                    glRotatef(180, 0, 1, 0)
                }
                GL11.glRotatef(-90, 0, 1, 0)
                val io: Int = tile.getIO(check)
                if (io == 1)
                {
                    RenderBattery.MODEL.renderOnly("connectorIn")
                }
                else if (io == 2)
                {
                    RenderBattery.MODEL.renderOnly("connectorOut")
                }
                GL11.glPopMatrix
            }
        }
        enabledParts.removeAll(disabledParts)

        for (i <- 1 to 8)
        {
            if (i != 1 || enabledParts.contains("coil1"))
            {
                if ((8 - i) < energyLevel) RenderBattery.MODEL.renderOnly("coil" + i + "lit")
                else RenderBattery.MODEL.renderOnly("coil" + i)
            }
        }


        disabledParts.addAll(Arrays.asList(Array[String]("connector", "connectorIn", "connectorOut") :_*))
        disabledParts.addAll(Arrays.asList(Array[String]("coil1", "coil2", "coil3", "coil4", "coil5", "coil6", "coil7", "coil8"):_*))
        disabledParts.addAll(Arrays.asList(Array[String]("coil1lit", "coil2lit", "coil3lit", "coil4lit", "coil5lit", "coil6lit", "coil7lit", "coil8lit"):_*))
        disabledParts.addAll(Arrays.asList(Array[String]("frame1con", "frame2con", "frame3con", "frame4con"):_*))
        enabledParts.removeAll(Arrays.asList(Array[String]("coil1", "coil2", "coil3", "coil4", "coil5", "coil6", "coil7", "coil8")))
        RenderBattery.MODEL.renderAllExcept(disabledParts.toArray(new Array[String](0)) :_*)
        RenderBattery.MODEL.renderOnly(enabledParts.toArray(new Array[String](0)):_*)
        GL11.glPopMatrix
    }

}