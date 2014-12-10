package resonantinduction.archaic.process

import cpw.mods.fml.common.network.ByteBufUtils
import cpw.mods.fml.relauncher.{Side, SideOnly}
import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.IIcon
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.recipe.{MachineRecipes, RecipeResource}
import resonant.lib.factory.resources.RecipeType
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.network.handle.IPacketReceiver
import resonant.lib.prefab.tile.TileInventory
import resonant.lib.prefab.tile.spatial.SpatialBlock
import resonant.lib.utility.inventory.InventoryUtility
import resonantinduction.core.Reference
import resonantinduction.mechanical.mech.gear.ItemHandCrank
import resonant.lib.transform.vector.Vector3

class TileMillstone extends TileInventory(Material.rock) with IPacketReceiver
{

    private var grindCount: Int = 0

    //Constructor
    setTextureName(Reference.prefix + "millstone_side")

    override def onInventoryChanged
    {
        grindCount = 0
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
    }

    def doGrind(spawnPos: Vector3)
    {
        val outputs: Array[RecipeResource] = MachineRecipes.INSTANCE.getOutput(RecipeType.GRINDER.name, getStackInSlot(0))
        if (outputs.length > 0)
        {
            grindCount += 1;
            if ( grindCount > 20)
            {
                for (res <- outputs)
                {
                    InventoryUtility.dropItemStack(worldObj, spawnPos, res.getItemStack.copy)
                }
                decrStackSize(0, 1)
                onInventoryChanged
            }
        }
    }

    override def canUpdate: Boolean =
    {
        return false
    }

    override def isItemValidForSlot(i: Int, itemStack: ItemStack): Boolean =
    {
        return MachineRecipes.INSTANCE.getOutput(RecipeType.GRINDER.name, itemStack).length > 0
    }

    override def canStore(stack: ItemStack, slot: Int, side: ForgeDirection): Boolean =
    {
        return true
    }

    /**
     * Packets
     */
    override def getDescPacket: PacketTile =
    {
        val nbt: NBTTagCompound = new NBTTagCompound
        this.writeToNBT(nbt)
        return new PacketTile(this, nbt)
    }

    def read(data: ByteBuf, player: EntityPlayer, `type`: PacketType)
    {
        try
        {
            this.readFromNBT(ByteBufUtils.readTag(data))
        }
        catch
            {
                case e: Exception =>
                {
                    e.printStackTrace
                }
            }
    }

    @SideOnly(Side.CLIENT) override def registerIcons(iconReg: IIconRegister)
    {
        SpatialBlock.icon.put("millstone_side", iconReg.registerIcon(Reference.prefix + "millstone_side"))
        SpatialBlock.icon.put("millstone_top", iconReg.registerIcon(Reference.prefix + "millstone_top"))
    }

    @SideOnly(Side.CLIENT) override def getIcon(side: Int, meta: Int): IIcon =
    {
        if (side == 0 || side == 1)
        {
            return SpatialBlock.icon.get("millstone_top")
        }
        return SpatialBlock.icon.get("millstone_side")
    }

    override def click(player: EntityPlayer)
    {
        if (!world.isRemote)
        {
            val output: ItemStack = getStackInSlot(0)
            if (output != null)
            {
                InventoryUtility.dropItemStack(world, new Vector3(player), output, 0)
                setInventorySlotContents(0, null)
            }
            onInventoryChanged
        }
    }

    override def use(player: EntityPlayer, hitSide: Int, hit: Vector3): Boolean =
    {
        val current: ItemStack = player.inventory.getCurrentItem
        val output: ItemStack = getStackInSlot(0)
        if (current != null && current.getItem.isInstanceOf[ItemHandCrank])
        {
            if (output != null)
            {
                doGrind(new Vector3(player))
                player.addExhaustion(0.3f)
                return true
            }
        }
        if (output != null)
        {
            InventoryUtility.dropItemStack(world, new Vector3(player), output, 0)
            setInventorySlotContents(0, null)
        }
        else if (current != null && isItemValidForSlot(0, current))
        {
            setInventorySlotContents(0, current)
            player.inventory.setInventorySlotContents(player.inventory.currentItem, null)
        }
        world.markBlockForUpdate(xi, yi, zi)
        return false
    }
}