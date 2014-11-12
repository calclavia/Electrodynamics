package resonantinduction.electrical.generator

import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ChatComponentText
import resonant.api.IRotatable
import resonant.content.prefab.java.TileAdvanced
import resonant.lib.content.prefab.TElectric
import resonant.lib.grid.node.TSpatialNodeProvider
import resonant.lib.transform.vector.Vector3
import resonantinduction.mechanical.mech.grid.MechanicalNode

/**
 * A kinetic energy to electrical energy converter.
 *
 * @author Calclavia
 */
class TileMotor extends TileAdvanced(Material.iron) with TElectric with TSpatialNodeProvider with IRotatable
{
  var mechNode = new MechanicalNode(this)

  private var gearRatio = 0

  normalRender = false
  isOpaqueCube = false
  nodes.add(dcNode)
  nodes.add(mechNode)

  def toggleGearRatio() = (gearRatio + 1) % 3

  override def update()
  {
    //TODO: Debug
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
        mechNode.rotate(this, deltaPower * mechRatio, deltaPower / mechRatio)
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