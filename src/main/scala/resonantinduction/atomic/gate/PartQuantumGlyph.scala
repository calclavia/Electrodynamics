package resonantinduction.atomic.gate

import java.util.{ArrayList, List, Set}

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.lib.vec.{Cuboid6, Vector3}
import codechicken.multipart.{JCuboidPart, JNormalOcclusion, TSlottedPart}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{ChatComponentText, MovingObjectPosition}
import resonant.api.blocks.IBlockFrequency
import resonant.api.mffs.fortron.FrequencyGridRegistry
import resonantinduction.electrical.Electrical
import universalelectricity.core.transform.vector.VectorWorld
import scala.collection.JavaConversions._

object PartQuantumGlyph {
  final val MAX_GLYPH: Int = 4
  private[gate] final val bounds: Array[Cuboid6] = new Array[Cuboid6](15)
}

class PartQuantumGlyph extends JCuboidPart with TSlottedPart with JNormalOcclusion with IQuantumGate {

  private var slot: Byte = 0
  private[gate] var number: Byte = 0
  private[gate] var ticks: Int = 0

  def preparePlacement(side: Int, itemDamage: Int) {
    this.slot = side.asInstanceOf[Byte]
    this.number = itemDamage.asInstanceOf[Byte]
  }

  override def onWorldJoin {
    if ((tile.asInstanceOf[IQuantumGate]).getFrequency != -1) {
      FrequencyGridRegistry.instance.add(tile.asInstanceOf[IQuantumGate])
    }
  }

  override def preRemove {
    FrequencyGridRegistry.instance.remove(tile.asInstanceOf[IQuantumGate])
  }

  override def onEntityCollision(entity: Entity) {
    if (!world.isRemote) {
      if (entity.isInstanceOf[EntityPlayer]) if (!(entity.asInstanceOf[EntityPlayer]).isSneaking) return
      transport(entity)
    }
  }

  def transport(`ob`: scala.Any)
  {
    if (ticks % 10 == 0 && (tile.asInstanceOf[IQuantumGate]).getFrequency != -1)
    {
      val frequencyBlocks: Set[IBlockFrequency] = FrequencyGridRegistry.instance.getNodes((tile.asInstanceOf[IQuantumGate]).getFrequency)
      val gates: List[IQuantumGate] = new ArrayList[IQuantumGate]


      for (frequencyBlock <- frequencyBlocks)
      {
        if (frequencyBlock.isInstanceOf[IQuantumGate])
        {
          gates.add(frequencyBlock.asInstanceOf[IQuantumGate])
        }
      }
      gates.remove(tile)

      if (gates.size > 0) {
        if (ob.isInstanceOf[Entity])
        {
          val gate: IQuantumGate = gates.get(if (gates.size > 1) ob.asInstanceOf[Entity].worldObj.rand.nextInt(gates.size - 1) else 0)
          val position: VectorWorld = new VectorWorld(gate.asInstanceOf[TileEntity]).add(0.5, 2, 0.5)

          if (QuantumGateManager.moveEntity(ob.asInstanceOf[Entity], position)) world.playSoundAtEntity(ob.asInstanceOf[Entity], "mob.endermen.portal", 1.0F, 1.0F)
        }
      }
    }
  }

  override def update {
    if (ticks == 0) FrequencyGridRegistry.instance.add(tile.asInstanceOf[IQuantumGate])
    ticks += 1
  }

  override def activate(player: EntityPlayer, hit: MovingObjectPosition, itemStack: ItemStack): Boolean = {
    if (player.isSneaking) {
      if (!world.isRemote) {
        transport(player)
        return true
      }
    }
    else {
      val frequency: Int = (tile.asInstanceOf[IBlockFrequency]).getFrequency
      if (frequency > -1) {
        if (!world.isRemote) {
          player.addChatMessage(new ChatComponentText("Quantum Gate Frequency: " + frequency))
        }
        return true
      }
    }
    return false
  }

  def getType: String = {
    return "resonant_induction_quantum_glyph"
  }

  @SideOnly(Side.CLIENT) override def renderDynamic(pos: Vector3, frame: Float, pass: Int) {
    RenderQuantumGlyph.INSTANCE.render(this, pos.x, pos.y, pos.z)
  }

  def getBounds: Cuboid6 = {
    if (slot < PartQuantumGlyph.bounds.length) if (PartQuantumGlyph.bounds(slot) != null) return PartQuantumGlyph.bounds(slot)
    return new Cuboid6(0, 0, 0, 0.5, 0.5, 0.5)
  }

  def getOcclusionBoxes: Iterable[Cuboid6] = {
    return Array[Cuboid6](getBounds)
  }

  def getSlotMask: Int = {
    return 1 << slot
  }

  protected def getItem: ItemStack = {
    return new ItemStack(Electrical.itemQuantumGlyph, 1, number)
  }

  override def getDrops: Iterable[ItemStack] = {
    val drops: Array[ItemStack] = new Array[ItemStack](1)
    drops(0) = getItem
    return drops
  }

  override def pickItem(hit: MovingObjectPosition): ItemStack = {
    return getItem
  }

  /** Packet Code. */
  override def readDesc(packet: MCDataInput) {
    load(packet.readNBTTagCompound)
  }

  override def writeDesc(packet: MCDataOutput) {
    val nbt: NBTTagCompound = new NBTTagCompound
    save(nbt)
    packet.writeNBTTagCompound(nbt)
  }

  override def load(nbt: NBTTagCompound) {
    slot = nbt.getByte("side")
    number = nbt.getByte("number")
    if (nbt.hasKey("frequency")) {
      val frequency: Int = nbt.getInteger("frequency")
    }
  }

  override def save(nbt: NBTTagCompound) {
    nbt.setByte("side", slot)
    nbt.setByte("number", number)
    if (tile != null) {
      nbt.setInteger("frequency", (tile.asInstanceOf[IQuantumGate]).getFrequency)
    }
  }

  override def setFrequency(frequency: Int): Unit = ???

  override def getFrequency: Int = ???
}