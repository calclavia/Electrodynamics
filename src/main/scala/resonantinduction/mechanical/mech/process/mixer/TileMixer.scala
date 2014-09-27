package resonantinduction.mechanical.mech.process.mixer

import java.util.{LinkedHashSet, List, Set}

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.IFluidBlock
import resonant.api.recipe.MachineRecipes
import resonant.content.factory.resources.RecipeType
import resonant.content.factory.resources.block.BlockFluidMixture
import resonant.engine.ResonantEngine
import resonant.lib.utility.Timer
import resonantinduction.core.Reference
import resonantinduction.mechanical.mech.TileMechanical
import universalelectricity.api.core.grid.INode
import universalelectricity.core.transform.rotation.Quaternion
import universalelectricity.core.transform.vector.Vector3
import scala.collection.JavaConversions._

/**
 * @author Calclavia
 */
object TileMixer
{
    final val PROCESS_TIME: Int = 12 * 20
    final val timer: Timer[EntityItem] = new Timer[EntityItem]
}

class TileMixer extends TileMechanical(Material.iron)
{
    //Constructor
    mechanicalNode = new MixerNode(this)
    isOpaqueCube(false)
    normalRender(false)
    customItemRender(true)
    setTextureName("material_metal_top")

    override def getNodes(nodes: List[INode])
    {
        if (mechanicalNode != null) nodes.add(this.mechanicalNode)
    }

    override def update
    {
        super.update
        if (!world.isRemote && ticks % 20 == 0)
        {
            this.areaBlockedFromMoving = checkIsBlocked


        }
        if (canWork)
        {
            doWork
        }
    }

    /** Checks to see if the area around the mixer is blocked (3x3 - excluding center)
      * @return true if there is a non-fluid block inside the bounds
      */
    def checkIsBlocked: Boolean =
    {
        for (x <- -1 to 1)
        {
            for (z <- -1 to 1)
            {
                if (x != 0 && z != 0)
                {
                    val block: Block = position.add(x, 0, z).getBlock(world)
                    if (block != null && !(block.isInstanceOf[IFluidBlock]))
                    {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Can this machine work this tick?
     *
     * @return
     */
    def canWork: Boolean =
    {
        return mechanicalNode.getAngularSpeed(ForgeDirection.UNKNOWN) != 0 && !areaBlockedFromMoving
    }

    def doWork
    {
        var didWork: Boolean = false
        val aabb: AxisAlignedBB = AxisAlignedBB.getBoundingBox(this.xCoord - 1, this.yCoord, this.zCoord - 1, this.xCoord + 2, this.yCoord + 1, this.zCoord + 2)
        val entities: List[_] = this.worldObj.getEntitiesWithinAABB(classOf[Entity], aabb)
        val processItems: Set[EntityItem] = new LinkedHashSet[EntityItem]
        for (obj <- entities)
        {
            val entity: Entity = obj.asInstanceOf[Entity]
            val originalPosition: Vector3 = new Vector3(entity)
            val relativePosition: Vector3 = originalPosition.clone.subtract(new Vector3(this).add(0.5))
            relativePosition.transform(new Quaternion(-mechanicalNode.getAngularSpeed(ForgeDirection.UNKNOWN), new Vector3(1, 0, 0)))
            val newPosition: Vector3 = new Vector3(this).add(0.5).add(relativePosition)
            val difference: Vector3 = newPosition.subtract(originalPosition).multiply(0.5)
            entity.addVelocity(difference.x, difference.y, difference.z)
            entity.onGround = false
            if (entity.isInstanceOf[EntityItem])
            {
                if (MachineRecipes.INSTANCE.getOutput(RecipeType.MIXER.name, (entity.asInstanceOf[EntityItem]).getEntityItem).length > 0)
                {
                    processItems.add(entity.asInstanceOf[EntityItem])
                }
            }
        }

        for (processingItem <- processItems)
        {
            if (!TileMixer.timer.containsKey(processingItem))
            {
                TileMixer.timer.put(processingItem, TileMixer.PROCESS_TIME)
            }
            if (!processingItem.isDead && new Vector3(this).add(0.5).distance(new Vector3(processingItem)) < 2)
            {
                val timeLeft: Int = TileMixer.timer.decrease(processingItem)
                if (timeLeft <= 0)
                {
                    if (doneWork(processingItem))
                    {
                        if (({
                            processingItem.getEntityItem.stackSize -= 1;
                            processingItem.getEntityItem.stackSize
                        }) <= 0)
                        {
                            processingItem.setDead
                            TileMixer.timer.remove(processingItem)
                        }
                        else
                        {
                            processingItem.setEntityItemStack(processingItem.getEntityItem)
                            TileMixer.timer.put(processingItem, TileMixer.PROCESS_TIME)
                        }
                    }
                }
                else
                {
                    processingItem.delayBeforeCanPickup = 20
                    this.worldObj.spawnParticle("bubble", processingItem.posX, processingItem.posY, processingItem.posZ, (Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3)
                }
                didWork = true
            }
            else
            {
                TileMixer.timer.remove(processingItem)
            }
        }
        if (didWork)
        {
            if (this.ticks % 20 == 0)
            {
                this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, Reference.prefix + "mixer", 0.5f, 1)
            }
        }
    }

    private def doneWork(entity: EntityItem): Boolean =
    {
        val mixPosition: Vector3 = new Vector3(entity.posX, yCoord, entity.posZ)
        if (mixPosition.getBlock(world) ne getBlockType)
        {
            val block: Block = mixPosition.getBlock(worldObj)
            val blockFluidFinite: Block = ResonantEngine.resourceFactory.getMixture(ResonantEngine.resourceFactory.getName(entity.getEntityItem))
            if (blockFluidFinite != null)
            {
                if (block.isInstanceOf[BlockFluidMixture])
                {
                    val itemStack: ItemStack = entity.getEntityItem.copy
                    if ((block.asInstanceOf[BlockFluidMixture]).mix(worldObj, mixPosition.xi, mixPosition.yi, mixPosition.zi, itemStack))
                    {
                        worldObj.notifyBlocksOfNeighborChange(mixPosition.xi, mixPosition.yi, mixPosition.zi, mixPosition.getBlock(worldObj))
                        return true
                    }
                }
                else if (block != null && (block == Blocks.water || block == Blocks.flowing_water))
                {
                    mixPosition.setBlock(worldObj, blockFluidFinite)
                }
            }
        }
        return false
    }

    private var areaBlockedFromMoving: Boolean = false
}