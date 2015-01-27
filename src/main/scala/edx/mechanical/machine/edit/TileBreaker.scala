package edx.mechanical.machine.edit

import java.util.ArrayList

import cpw.mods.fml.common.network.ByteBufUtils
import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Electrodynamics
import io.netty.buffer.ByteBuf
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.IIcon
import net.minecraft.world.IBlockAccess
import net.minecraftforge.common.util.ForgeDirection
import resonantengine.api.network.IPacketReceiver
import resonantengine.core.network.discriminator.{PacketTile, PacketType}
import resonantengine.lib.modcontent.block.ResonantTile
import resonantengine.lib.transform.vector.{Vector3, VectorWorld}
import resonantengine.lib.utility.inventory.InternalInventoryHandler
import resonantengine.prefab.block.impl.TRotatable

/**
 * @author tgame14
 * @since 18/03/14
 */
object TileBreaker
{
  @SideOnly(Side.CLIENT) private var iconFront: IIcon = null
  @SideOnly(Side.CLIENT) private var iconBack: IIcon = null
}

class TileBreaker extends ResonantTile(Material.iron) with TRotatable with IPacketReceiver
{
  private var _doWork: Boolean = false
  private var invHandler: InternalInventoryHandler = null
  private var place_delay: Int = 0

  override def onAdded
  {
    work
  }

  def work
  {
    if (isIndirectlyPowered)
    {
      _doWork = true
      place_delay = 0
    }
  }

  override def onNeighborChanged(block: Block)
  {
    work
  }

  override def update
  {
    if (_doWork)
    {
      if (place_delay < java.lang.Byte.MAX_VALUE)
      {
        place_delay += 1
      }
      if (place_delay >= 10)
      {
        _doWork = false
        place_delay = 0
      }
    }
  }

  def doWork
  {
    if (isIndirectlyPowered)
    {
      val dir: ForgeDirection = getDirection
      val check: Vector3 = position.add(dir)
      val put: VectorWorld = position.add(dir.getOpposite)
      val block: Block = check.getBlock(world)
      if (block != null)
      {
        val candidateMeta: Int = world.getBlockMetadata(check.xi, check.yi, check.zi)
        val flag: Boolean = true
        val drops: ArrayList[ItemStack] = block.getDrops(getWorldObj, check.xi, check.yi, check.zi, candidateMeta, 0)
        import scala.collection.JavaConversions._
        for (stack <- drops)
        {
          var insert: ItemStack = stack.copy
          insert = getInvHandler.storeItem(insert, this.getDirection.getOpposite)
          if (insert != null)
          {
            getInvHandler.throwItem(this.getDirection.getOpposite, insert)
          }
        }
        Electrodynamics.proxy.renderBlockParticle(worldObj, check.xi, check.yi, check.zi, new Vector3((Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3), Block.getIdFromBlock(block), 1)
        getWorldObj.setBlockToAir(check.xi, check.yi, check.zi)
        getWorldObj.playAuxSFX(1012, check.xi, check.yi, check.zi, 0)
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

  override def getDescPacket: PacketTile =
  {
    val nbt: NBTTagCompound = new NBTTagCompound
    writeToNBT(nbt)
    return new PacketTile(this, nbt)
  }

  @SideOnly(Side.CLIENT) override def getIcon(access: IBlockAccess, side: Int): IIcon =
  {
    val meta: Int = access.getBlockMetadata(x, y, z)
    if (side == meta)
    {
      return TileBreaker.iconFront
    }
    else if (side == (meta ^ 1))
    {
      return TileBreaker.iconBack
    }
    return getIcon
  }

  @SideOnly(Side.CLIENT)
  override def getIcon(side: Int, meta: Int): IIcon =
  {
    if (side == (meta ^ 1))
    {
      return TileBreaker.iconFront
    }
    else if (side == meta)
    {
      return TileBreaker.iconBack
    }
    return getIcon
  }

  @SideOnly(Side.CLIENT)
  override def registerIcons(iconRegister: IIconRegister)
  {
    super.registerIcons(iconRegister)
    TileBreaker.iconFront = iconRegister.registerIcon(getTextureName + "_front")
    TileBreaker.iconBack = iconRegister.registerIcon(getTextureName + "_back")
  }

  def read(data: ByteBuf, player: EntityPlayer, `type`: PacketType)
  {
    try
    {
      readFromNBT(ByteBufUtils.readTag(data))
    }
    catch
      {
        case e: Exception =>
        {
          e.printStackTrace
        }
      }
  }
}