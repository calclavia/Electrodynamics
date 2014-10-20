package resonantinduction.atomic.machine.quantum

import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import resonant.api.recipe.QuantumAssemblerRecipes
import resonant.lib.content.prefab.java.TileElectricInventory
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.network.handle.IPacketReceiver
import resonantinduction.atomic.AtomicContent
import resonantinduction.core.Reference
import universalelectricity.core.transform.vector.Vector3

/**
 * Atomic assembler of items *
 *
 * @author Calclavia, Darkguardsman
 */
class TileQuantumAssembler extends TileElectricInventory(Material.iron) with IPacketReceiver
{
    private[quantum] var ENERGY: Long = 1000000000L
    private[quantum] var MAX_TIME: Int = 20 * 120
    private[quantum] var time: Int = 0
    /**
     * Used for rendering arm motion, X Y Z are not used as location data
     */
    private[quantum] var rotation: Vector3 = new Vector3
    /**
     * Used for rendering.
     */
    private[quantum] var entityItem: EntityItem = null

    //Constructor
    setSizeInventory(7)
    energy.setCapacity(ENERGY)
    energy.setMaxTransfer(ENERGY / 10)
    isOpaqueCube(false)
    normalRender(false)
    customItemRender(true)
    setTextureName("machine")

    /**
     * Called when the block is right clicked by the player
     */
    override def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
    {
        if (!world.isRemote)
        {
            player.openGui(AtomicContent, 0, world, xi, yi, zi)
        }
        return true
    }

    override def update
    {
        super.update
        if (!this.worldObj.isRemote)
        {
            if (this.canProcess)
            {
                if (energy.checkExtract)
                {
                    if (this.time == 0)
                    {
                        this.time = this.MAX_TIME
                    }
                    if (this.time > 0)
                    {
                        this.time -= 1
                        if (this.time < 1)
                        {
                            this.process
                            this.time = 0
                        }
                    }
                    else
                    {
                        this.time = 0
                    }
                    energy.extractEnergy(ENERGY, true)
                }
            }
            else
            {
                this.time = 0
            }
            if (this.ticks % 10 == 0)
            {
                //TODO send packets to each player with the GUI open
            }
        }
        else if (this.time > 0)
        {
            var middleStack: ItemStack = this.getStackInSlot(6)
            if (middleStack != null)
            {
                middleStack = middleStack.copy
                middleStack.stackSize = 1
                if (this.entityItem == null)
                {
                    this.entityItem = new EntityItem(this.worldObj, 0, 0, 0, middleStack)
                }
                else if (!middleStack.isItemEqual(this.entityItem.getEntityItem))
                {
                    this.entityItem = new EntityItem(this.worldObj, 0, 0, 0, middleStack)
                }
                this.entityItem.age += 1
            }
            else
            {
                this.entityItem = null
            }
            if (this.ticks % 600 == 0)
            {
                this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, Reference.prefix + "assembler", 0.7f, 1f)
            }
            this.rotation.add(3, 2, 1)
        }
    }

    def read(data: ByteBuf, player: EntityPlayer, `type`: PacketType)
    {
        this.time = data.readInt
        val itemID: Int = data.readInt
        val itemAmount: Int = data.readInt
        val itemMeta: Int = data.readInt
        if (itemID != -1 && itemAmount != -1 && itemMeta != -1)
        {
            this.setInventorySlotContents(6, new ItemStack(Item.getItemById(itemID), itemAmount, itemMeta))
        }
    }

    override def getDescPacket: PacketTile =
    {
        if (this.getStackInSlot(6) != null)
        {
            return new PacketTile(xi, yi, zi, Array(time, getStackInSlot(6)))
        }
        return new PacketTile(xi, yi, zi, Array(time, -1, -1, -1))
    }

    /**
     * Checks to see if the assembler can run
     */
    def canProcess: Boolean =
    {
        if (getStackInSlot(6) != null)
        {
            if (QuantumAssemblerRecipes.hasItemStack(getStackInSlot(6)))
            {
                for (i <- 0 to 6)
                {
                    if (getStackInSlot(i) == null)
                    {
                        return false
                    }
                    if (getStackInSlot(i).getItem ne AtomicContent.itemDarkMatter)
                    {
                        return false
                    }
                }
                return getStackInSlot(6).stackSize < 64
            }
        }
        return false
    }

    /**
     * Turn one item from the furnace source stack into the appropriate smelted item in the furnace
     * result stack
     */
    def process
    {
        if (this.canProcess)
        {
            for (i <- 0 to 6)
            {
                if (getStackInSlot(i) != null)
                {
                    decrStackSize(i, 1)
                }
            }
            if (getStackInSlot(6) != null)
            {
                getStackInSlot(6).stackSize += 1
            }
        }
    }

    override def readFromNBT(nbt: NBTTagCompound)
    {
        super.readFromNBT(nbt)
        this.time = nbt.getInteger("smeltingTicks")
    }

    override def writeToNBT(nbt: NBTTagCompound)
    {
        super.writeToNBT(nbt)
        nbt.setInteger("smeltingTicks", this.time)
    }

    override def isItemValidForSlot(slotID: Int, itemStack: ItemStack): Boolean =
    {
        if (slotID == 6)
        {
            return true
        }
        return itemStack.getItem eq AtomicContent.itemDarkMatter
    }
}