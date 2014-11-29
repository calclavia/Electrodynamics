package resonantinduction.electrical.generator

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.material.Material
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{ChatComponentText, ResourceLocation}
import net.minecraftforge.client.model.AdvancedModelLoader
import org.lwjgl.opengl.GL11
import resonant.api.IRotatable
import resonant.content.prefab.java.TileAdvanced
import resonant.lib.content.prefab.TElectric
import resonant.lib.grid.node.TSpatialNodeProvider
import resonant.lib.render.RenderUtility
import resonant.lib.transform.vector.Vector3
import resonantinduction.core.Reference
import resonantinduction.mechanical.mech.grid.NodeMechanical

/**
 * A kinetic energy to electrical energy converter.
 *
 * @author Calclavia
 */
object TileMotor
{
  @SideOnly(Side.CLIENT)
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "motor.tcn"))
  @SideOnly(Side.CLIENT)
  val texture = new ResourceLocation(Reference.domain, Reference.modelPath + "motor.png")
}

class TileMotor extends TileAdvanced(Material.iron) with TElectric with TSpatialNodeProvider with IRotatable
{
  var mechNode = new NodeMechanical(this)

  private var gearRatio = 0

  textureName = "material_wood_surface"
  normalRender = false
  isOpaqueCube = false
  nodes.add(dcNode)
  nodes.add(mechNode)

  def toggleGearRatio() = (gearRatio + 1) % 3

  /**
   * Called when the block is placed by a living entity
   * @param entityLiving - entity who placed the block
   * @param itemStack - ItemStack the entity used to place the block
   */
  override def onPlaced(entityLiving: EntityLivingBase, itemStack: ItemStack)
  {
    super.onPlaced(entityLiving, itemStack)
    mechNode.connectionMask = 1 << getDirection.getOpposite.ordinal
  }

  override def update()
  {
    //TODO: Debug with free energy
    val deltaPower = 100d //Math.abs(mechNode.power - dcNode.power)

    if (false && mechNode.power > dcNode.power)
    {
      //Produce electricity
      dcNode.buffer(deltaPower)
      //TODO: Resist mech energy
    }
    //    else if (dcNode.power > mechNode.power)
    else
    {
      //Produce mechanical energy
      val mechRatio = Math.pow(gearRatio + 1, 3) * 400

      if (mechRatio > 0)
      {
        mechNode.rotate(deltaPower, deltaPower / mechRatio)
        //TODO: Resist DC energy
      }
    }
  }

  override protected def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (!world.isRemote)
    {
      gearRatio = (gearRatio + 1) % 3
      player.addChatComponentMessage(new ChatComponentText("Toggled gear ratio: " + gearRatio))
      return true
    }

    return false
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int): Unit =
  {
    GL11.glPushMatrix()
    GL11.glTranslatef(pos.x.toFloat + 0.5f, pos.y.toFloat + 0.5f, pos.z.toFloat + 0.5f)
    GL11.glRotatef(90, 0, 1, 0)
    RenderUtility.rotateBlockBasedOnDirection(getDirection)
    RenderUtility.bind(TileMotor.texture)
    TileMotor.model.renderAll()
    GL11.glPopMatrix()
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    gearRatio = nbt.getByte("gear")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setByte("gear", gearRatio.toByte)
  }

  override def toString: String = "[TileMotor]" + x + "x " + y + "y " + z + "z "
}