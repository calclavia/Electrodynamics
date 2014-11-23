package resonantinduction.mechanical.mech.process.grinder

import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.IRotatable
import resonant.api.recipe.{MachineRecipes, RecipeResource}
import resonant.content.factory.resources.RecipeType
import resonant.lib.prefab.damage.CustomDamageSource
import resonant.lib.transform.region.Cuboid
import resonant.lib.transform.vector.Vector3
import resonant.lib.utility.Timer
import resonantinduction.core.{Reference, ResonantInduction}
import resonantinduction.mechanical.mech.TileMechanical

/**
 * @author Calclavia
 */
object TileGrindingWheel
{
  final val processTime: Int = 20 * 20
  /**
   * A map of ItemStacks and their remaining grind-time left.
   */
  final val grindingTimer: Timer[EntityItem] = new Timer[EntityItem]
}

class TileGrindingWheel extends TileMechanical(Material.rock)
{
  private var grindingItem: EntityItem = null
  private final val requiredTorque: Long = 250
  private var counter = 0d

  mechanicalNode = new NodeGrinder(this)
  bounds = new Cuboid(0.05f, 0.05f, 0.05f, 0.95f, 0.95f, 0.95f)
  isOpaqueCube = false
  normalRender = false
  textureName = "material_steel_dark"

  override def update()
  {
    super.update()
    counter = Math.max(counter + Math.abs(mechanicalNode.torque), 0)
    doWork()

    if(!world.isRemote)
    {
      println(mechanicalNode)
      sendPacket(1)
    }
  }

  override def collide(entity: Entity)
  {
    if (entity.isInstanceOf[EntityItem])
    {
      entity.asInstanceOf[EntityItem].age -= 1
    }
    if (canWork)
    {
      if (entity.isInstanceOf[EntityItem])
      {
        if (canGrind((entity.asInstanceOf[EntityItem]).getEntityItem))
        {
          if (grindingItem == null)
          {
            grindingItem = entity.asInstanceOf[EntityItem]
          }
          if (!TileGrindingWheel.grindingTimer.containsKey(entity.asInstanceOf[EntityItem]))
          {
            TileGrindingWheel.grindingTimer.put(entity.asInstanceOf[EntityItem], TileGrindingWheel.processTime)
          }
        }
        else
        {
          entity.setPosition(entity.posX, entity.posY - 1.2, entity.posZ)
        }
      }
      else
      {
        entity.attackEntityFrom(new CustomDamageSource("grinder", this), 2)
      }
    }
    if (mechanicalNode.angularVelocity != 0)
    {
      var dir: ForgeDirection = getDirection
      dir = ForgeDirection.getOrientation(if (!(dir.ordinal % 2 == 0)) dir.ordinal - 1 else dir.ordinal).getOpposite
      val speed: Double = mechanicalNode.angularVelocity / 20
      var speedX: Double = dir.offsetX * speed
      var speedZ: Double = dir.offsetZ * speed
      var speedY: Double = Math.random * speed
      if (Math.abs(speedX) > 1)
      {
        speedX = if (speedX > 0) 1 else -1
      }
      if (Math.abs(speedZ) > 1)
      {
        speedZ = if (speedZ > 0) 1 else -1
      }
      if (Math.abs(speedZ) > 1)
      {
        speedY = if (speedY > 0) 1 else -1
      }
      entity.addVelocity(speedX, speedY, speedZ)
    }
  }

  /**
   * Can this machine work this tick?
   *
   * @return
   */
  def canWork: Boolean = counter >= requiredTorque

  def doWork()
  {
    if (canWork)
    {
      var didWork: Boolean = false
      if (grindingItem != null)
      {
        if (TileGrindingWheel.grindingTimer.containsKey(grindingItem) && !grindingItem.isDead && toVector3.add(0.5).distance(new Vector3(grindingItem)) < 1)
        {
          val timeLeft: Int = TileGrindingWheel.grindingTimer.decrease(grindingItem)
          if (timeLeft <= 0)
          {
            if (this.doGrind(grindingItem))
            {
              grindingItem.getEntityItem.stackSize -= 1;
              if (grindingItem.getEntityItem.stackSize <= 0)
              {
                grindingItem.setDead()
                TileGrindingWheel.grindingTimer.remove(grindingItem)
                grindingItem = null
              }
              else
              {
                grindingItem.setEntityItemStack(grindingItem.getEntityItem)
                TileGrindingWheel.grindingTimer.put(grindingItem, TileGrindingWheel.processTime)
              }
            }
          }
          else
          {
            grindingItem.delayBeforeCanPickup = 20
            if (grindingItem.getEntityItem.getItem.isInstanceOf[ItemBlock])
            {
              ResonantInduction.proxy.renderBlockParticle(worldObj, new Vector3(grindingItem), new Vector3((Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3), 3, 1)
            }
            else
            {
              worldObj.spawnParticle("crit", grindingItem.posX, grindingItem.posY, grindingItem.posZ, (Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3, (Math.random - 0.5f) * 3)
            }
          }
          didWork = true
        }
        else
        {
          TileGrindingWheel.grindingTimer.remove(grindingItem)
          grindingItem = null
        }
      }
      if (didWork)
      {
        if (this.ticks % 8 == 0)
        {
          worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, Reference.prefix + "grinder", 0.5f, 1)
        }
        counter -= requiredTorque
      }
    }
  }

  def canGrind(itemStack: ItemStack): Boolean =
  {
    return MachineRecipes.INSTANCE.getOutput(RecipeType.GRINDER.name, itemStack).length > 0
  }

  private def doGrind(entity: EntityItem): Boolean =
  {
    val itemStack: ItemStack = entity.getEntityItem
    val results: Array[RecipeResource] = MachineRecipes.INSTANCE.getOutput(RecipeType.GRINDER.name, itemStack)
    for (resource <- results)
    {
      val outputStack: ItemStack = resource.getItemStack
      if (!this.worldObj.isRemote)
      {
        val entityItem: EntityItem = new EntityItem(this.worldObj, entity.posX, entity.posY - 1.2, entity.posZ, outputStack)
        entityItem.delayBeforeCanPickup = 20
        entityItem.motionX = 0
        entityItem.motionY = 0
        entityItem.motionZ = 0
        this.worldObj.spawnEntityInWorld(entityItem)
      }
    }
    return results.length > 0
  }
}