package resonantinduction.mechanical.machine.edit

import java.util.ArrayList
import cpw.mods.fml.common.network.ByteBufUtils
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
import resonant.api.IRotatable
import resonant.content.prefab.java.TileAdvanced
import resonant.engine.network.discriminator.PacketTile
import resonant.engine.network.discriminator.PacketType
import resonant.engine.network.handle.IPacketReceiver
import resonant.lib.utility.inventory.InternalInventoryHandler
import resonantinduction.core.ResonantInduction
import resonant.lib.transform.vector.Vector3
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import resonant.lib.transform.vector.VectorWorld

/**
 * @author tgame14
 * @since 18/03/14
 */
object TileBreaker
{
    @SideOnly(Side.CLIENT) private var iconFront: IIcon = null
    @SideOnly(Side.CLIENT) private var iconBack: IIcon = null
}

class TileBreaker extends TileAdvanced(Material.iron) with IRotatable with IPacketReceiver
{
    private var _doWork : Boolean = false
    private var invHandler: InternalInventoryHandler = null
    private var place_delay: Int = 0

    def getInvHandler: InternalInventoryHandler =
    {
        if (invHandler == null)
        {
            invHandler = new InternalInventoryHandler(this)
        }
        return invHandler
    }

    override def onAdded
    {
        work
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

    def work
    {
        if (isIndirectlyPowered)
        {
            _doWork = true
            place_delay = 0
        }
    }

    def doWork
    {
        if (isIndirectlyPowered)
        {
            val dir: ForgeDirection = getDirection
            val check: Vector3 = toVector3.add(dir)
            val put: VectorWorld = toVector3.add(dir.getOpposite).asInstanceOf[VectorWorld]
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
                ResonantInduction.proxy.renderBlockParticle(worldObj, check.xi, check.yi, check.zi, new Vector3((Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3), Block.getIdFromBlock(block), 1)
                getWorldObj.setBlockToAir(check.xi, check.yi, check.zi)
                getWorldObj.playAuxSFX(1012, check.xi, check.yi, check.zi, 0)
            }
        }
    }

    override def getDescPacket: PacketTile =
    {
        val nbt: NBTTagCompound = new NBTTagCompound
        writeToNBT(nbt)
        return new PacketTile(this, nbt)
    }

    @SideOnly(Side.CLIENT) override def getIcon(access: IBlockAccess, side: Int): IIcon =
    {
        val meta: Int = access.getBlockMetadata(xi, yi, zi)
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