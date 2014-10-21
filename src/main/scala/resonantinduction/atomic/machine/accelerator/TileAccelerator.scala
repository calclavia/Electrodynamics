package resonantinduction.atomic.machine.accelerator

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.{IElectromagnet, IRotatable}
import resonant.engine.ResonantEngine
import resonant.lib.content.prefab.java.TileElectricInventory
import resonant.lib.network.Synced
import resonant.lib.network.discriminator.PacketAnnotation
import resonant.lib.utility.BlockUtility
import resonantinduction.atomic.AtomicContent
import resonantinduction.atomic.items.ItemAntimatter
import resonantinduction.core.{Reference, Settings}
import universalelectricity.core.transform.vector.Vector3
class TileAccelerator extends TileElectricInventory(Material.iron) with IElectromagnet with IRotatable
{
    /**
     * Multiplier that is used to give extra anti-matter based on density (hardness) of a given ore.
     */
    private var antiMatterDensityMultiplyer: Int = Settings.ACCELERATOR_ANITMATTER_DENSITY_MULTIPLIER
    /**
     * The total amount of energy consumed by this particle. In Joules.
     */
    @Synced var totalEnergyConsumed: Float = 0
    /**
     * The amount of anti-matter stored within the accelerator. Measured in milligrams.
     */
    @Synced var antimatter: Int = 0
    var entityParticle: EntityParticle = null
    @Synced var velocity: Float = 0
    @Synced private var clientEnergy: Double = 0
    private var lastSpawnTick: Int = 0

    //Constructor
    this.setSizeInventory(4)

    override def update
    {
        super.update
        if (!worldObj.isRemote)
        {
            clientEnergy = energy.getEnergy
            velocity = 0
            if (entityParticle != null)
            {
                velocity = entityParticle.getParticleVelocity.asInstanceOf[Float]
            }
            if (AtomicContent.isItemStackEmptyCell(getStackInSlot(1)))
            {
                if (getStackInSlot(1).stackSize > 0)
                {
                    if (antimatter >= 125)
                    {
                        if (getStackInSlot(2) != null)
                        {
                            if (getStackInSlot(2).getItem eq AtomicContent.itemAntimatter)
                            {
                                val newStack: ItemStack = getStackInSlot(2).copy
                                if (newStack.stackSize < newStack.getMaxStackSize)
                                {
                                    decrStackSize(1, 1)
                                    antimatter -= 125
                                    newStack.stackSize += 1
                                    setInventorySlotContents(2, newStack)
                                }
                            }
                        }
                        else
                        {
                            antimatter -= 125
                            decrStackSize(1, 1)
                            setInventorySlotContents(2, new ItemStack(AtomicContent.itemAntimatter))
                        }
                    }
                }
            }
            if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
            {
                if (energy.checkExtract)
                {
                    if (entityParticle == null)
                    {
                        if (getStackInSlot(0) != null && lastSpawnTick >= 40)
                        {
                            val spawn_vec: Vector3 = asVector3
                            spawn_vec.add(getDirection.getOpposite)
                            spawn_vec.add(0.5f)
                            if (EntityParticle.canSpawnParticle(worldObj, spawn_vec))
                            {
                                totalEnergyConsumed = 0
                                entityParticle = new EntityParticle(worldObj, spawn_vec, asVector3, getDirection.getOpposite)
                                worldObj.spawnEntityInWorld(entityParticle)
                                CalculateParticleDensity
                                decrStackSize(0, 1)
                                lastSpawnTick = 0
                            }
                        }
                    }
                    else
                    {
                        if (entityParticle.isDead)
                        {
                            if (entityParticle.didParticleCollide)
                            {
                                if (worldObj.rand.nextFloat <= Settings.darkMatterSpawnChance)
                                {
                                    incrStackSize(3, new ItemStack(AtomicContent.itemDarkMatter))
                                }
                            }
                            entityParticle = null
                        }
                        else if (velocity > EntityParticle.clientParticleVelocity)
                        {
                            worldObj.playSoundEffect(xCoord, yCoord, zCoord, Reference.prefix + "antimatter", 2f, 1f - worldObj.rand.nextFloat * 0.3f)
                            val generatedAntimatter: Int = 5 + worldObj.rand.nextInt(antiMatterDensityMultiplyer)
                            antimatter += generatedAntimatter
                            totalEnergyConsumed = 0
                            entityParticle.setDead
                            entityParticle = null
                        }
                        if (entityParticle != null)
                        {
                            worldObj.playSoundEffect(xCoord, yCoord, zCoord, Reference.prefix + "accelerator", 1.5f, (0.6f + (0.4 * (entityParticle.getParticleVelocity) / EntityParticle.clientParticleVelocity)).asInstanceOf[Float])
                        }
                    }
                    energy.extractEnergy
                }
                else
                {
                    if (entityParticle != null)
                    {
                        entityParticle.setDead
                    }
                    entityParticle = null
                }
            }
            else
            {
                if (entityParticle != null)
                {
                    entityParticle.setDead
                }
                entityParticle = null
            }
            if (ticks % 5 == 0)
            {
            }
            lastSpawnTick += 1
        }
    }

    override def activate(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
    {
        if (!world.isRemote)
        {
            player.openGui(AtomicContent, 0, world, xi, yi, zi)
        }
        return true
    }

    private def CalculateParticleDensity
    {
        val itemToAccelerate: ItemStack = this.getStackInSlot(0)
        if (itemToAccelerate != null)
        {
            antiMatterDensityMultiplyer = Settings.ACCELERATOR_ANITMATTER_DENSITY_MULTIPLIER
            try
            {
                val potentialBlock: Block = Block.getBlockFromItem(itemToAccelerate.getItem)
                if (potentialBlock != null)
                {
                    antiMatterDensityMultiplyer = Math.abs(BlockUtility.getBlockHardness(potentialBlock)).asInstanceOf[Int]
                    if (antiMatterDensityMultiplyer <= 0)
                    {
                        antiMatterDensityMultiplyer = 1
                    }
                }
            }
            catch
                {
                    case err: Exception =>
                    {
                        antiMatterDensityMultiplyer = Settings.ACCELERATOR_ANITMATTER_DENSITY_MULTIPLIER
                    }
                }
        }
    }

    override def getDescriptionPacket: Packet =
    {
        return ResonantEngine.instance.packetHandler.toMCPacket(new PacketAnnotation(this))
    }

    /**
     * Reads a tile entity from NBT.
     */
    override def readFromNBT(par1NBTTagCompound: NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound)
        totalEnergyConsumed = par1NBTTagCompound.getFloat("totalEnergyConsumed")
        antimatter = par1NBTTagCompound.getInteger("antimatter")
    }

    /**
     * Writes a tile entity to NBT.
     */
    override def writeToNBT(par1NBTTagCompound: NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound)
        par1NBTTagCompound.setFloat("totalEnergyConsumed", totalEnergyConsumed)
        par1NBTTagCompound.setInteger("antimatter", antimatter)
    }

    override def getAccessibleSlotsFromSide(side: Int): Array[Int] =
    {
        return Array[Int](0, 1, 2, 3)
    }

    override def canInsertItem(slotID: Int, itemStack: ItemStack, j: Int): Boolean =
    {
        return isItemValidForSlot(slotID, itemStack) && slotID != 2 && slotID != 3
    }

    override def canExtractItem(slotID: Int, itemstack: ItemStack, j: Int): Boolean =
    {
        return slotID == 2 || slotID == 3
    }

    override def isItemValidForSlot(i: Int, itemStack: ItemStack): Boolean =
    {
        i match
        {
            case 0 =>
                return true
            case 1 =>
                return AtomicContent.isItemStackEmptyCell(itemStack)
            case 2 =>
                return itemStack.getItem.isInstanceOf[ItemAntimatter]
            case 3 =>
                return itemStack.getItem != null && itemStack.getItem.getUnlocalizedName.contains("DarkMatter")
        }
        return false
    }

    def isRunning: Boolean =
    {
        return true
    }

    override def getDirection: ForgeDirection =
    {
        return ForgeDirection.getOrientation(getBlockMetadata)
    }

    override def setDirection(direction: ForgeDirection)
    {
        world.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, direction.ordinal, 3)
    }
}