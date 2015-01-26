package edx.mechanical.machine.edit

import java.util.EnumSet

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{ChatComponentText, IIcon}
import net.minecraft.world.IBlockAccess
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11
import resonantengine.lib.content.prefab.TInventory
import resonantengine.lib.network.discriminator.PacketTile
import resonantengine.prefab.network.TPacketSender
import resonantengine.lib.prefab.tile.spatial.ResonantTile
import resonantengine.lib.prefab.tile.traits.TRotatable
import resonantengine.lib.render.RenderItemOverlayUtility
import resonantengine.lib.transform.vector.Vector3
import resonantengine.lib.utility.LanguageUtility
import resonantengine.lib.utility.inventory.{InternalInventoryHandler, InventoryUtility}

/**
 * @author tgame14
 * @since 18/03/14
 */
object TilePlacer
{
  @SideOnly(Side.CLIENT) private var iconFront: IIcon = null
  @SideOnly(Side.CLIENT) private var iconBack: IIcon = null
}

class TilePlacer extends ResonantTile(Material.rock) with TInventory with TRotatable with TPacketSender
{
  private var _doWork: Boolean = false
  private var autoPullItems: Boolean = false
  private var placeDelay: Int = 0
  private var invHandler: InternalInventoryHandler = null

  //Constructor
  normalRender = false
  forceItemToRenderAsBlock = true
  renderStaticBlock = true
  this.rotationMask = 63

  override def getSizeInventory = 1

  override def onAdded
  {
    work
  }

  def work
  {
    if (isIndirectlyPowered)
    {
      _doWork = true
      placeDelay = 0
    }
  }

  override def onNeighborChanged(block: Block)
  {
    work
  }

  override def start
  {
    super.start
  }

  override def update
  {
    super.update
    if (autoPullItems && this.ticks % 5 == 0)
    {
      if (getStackInSlot(0) == null)
      {
        this.setInventorySlotContents(0, this.getInvHandler.tryGrabFromPosition(this.getDirection.getOpposite, 1))
      }
    }
    if (_doWork)
    {
      if (placeDelay < java.lang.Byte.MAX_VALUE)
      {
        placeDelay += 1
      }
      if (placeDelay >= 5)
      {
        doWork
        _doWork = false
      }
    }
  }

  def getInvHandler: InternalInventoryHandler =
  {
    if (invHandler == null)
    {
      invHandler = new InternalInventoryHandler(this)
    }
    return invHandler
  }

  def doWork
  {
    val side: Int = 0
    val placePos: Vector3 = toVector3.add(getDirection)
    val placeStack: ItemStack = getStackInSlot(0)
    if (InventoryUtility.placeItemBlock(world, placePos.xi, placePos.yi, placePos.zi, placeStack, side))
    {
      if (placeStack.stackSize <= 0)
      {
        setInventorySlotContents(0, null)
      }
      markUpdate
      _doWork = false
    }
  }

  override def use(player: EntityPlayer, hitSide: Int, hit: Vector3): Boolean =
  {
    interactCurrentItem(this, 0, player)
    return true
  }

  override def configure(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (player.isSneaking)
    {
      this.autoPullItems = !this.autoPullItems
      player.addChatComponentMessage(new ChatComponentText("AutoExtract: " + this.autoPullItems))
      return true
    }
    return super.configure(player, side, hit)
  }

  override def getDescPacket: PacketTile =
  {
    val nbt: NBTTagCompound = new NBTTagCompound
    writeToNBT(nbt)
    return new PacketTile(this, nbt)
  }

  /**
   * Writes a tile entity to NBT.
   */
  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setBoolean("autoPull", this.autoPullItems)
  }

  override def onInventoryChanged
  {
    sendDescPacket()
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    this.autoPullItems = nbt.getBoolean("autoPull")
  }

  override def canStore(stack: ItemStack, slot: Int, side: ForgeDirection): Boolean =
  {
    return side == this.getDirection.getOpposite && slot == 0
  }

  @SideOnly(Side.CLIENT) override def getIcon(access: IBlockAccess, side: Int): IIcon =
  {
    val meta: Int = access.getBlockMetadata(xi, yi, zi)
    if (side == meta)
    {
      return TilePlacer.iconFront
    }
    else if (side == (meta ^ 1))
    {
      return TilePlacer.iconBack
    }
    return getIcon
  }

  @SideOnly(Side.CLIENT) override def getIcon(side: Int, meta: Int): IIcon =
  {
    if (side == (meta ^ 1))
    {
      return TilePlacer.iconFront
    }
    else if (side == meta)
    {
      return TilePlacer.iconBack
    }
    return getIcon
  }

  @SideOnly(Side.CLIENT) override def registerIcons(iconRegister: IIconRegister)
  {
    super.registerIcons(iconRegister)
    TilePlacer.iconFront = iconRegister.registerIcon(getTextureName + "_front")
    TilePlacer.iconBack = iconRegister.registerIcon(getTextureName + "_back")
  }

  override def renderDynamic(position: Vector3, frame: Float, pass: Int)
  {
    if (world != null)
    {
      val set: EnumSet[ForgeDirection] = EnumSet.allOf(classOf[ForgeDirection])
      set.remove(getDirection)
      set.remove(getDirection.getOpposite)
      set.remove(ForgeDirection.UP)
      set.remove(ForgeDirection.DOWN)
      GL11.glPushMatrix
      RenderItemOverlayUtility.renderItemOnSides(this, getStackInSlot(0), position.x, position.y, position.z, LanguageUtility.getLocal("tooltip.noOutput"), set)
      GL11.glPopMatrix
    }
  }
}