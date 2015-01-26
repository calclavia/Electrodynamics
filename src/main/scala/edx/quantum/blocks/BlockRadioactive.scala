package edx.quantum.blocks

import java.util.{List, Random}

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.EntitySmokeFX
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.{Entity, EntityLiving, EntityLivingBase}
import net.minecraft.init.Blocks
import net.minecraft.util.{AxisAlignedBB, IIcon}
import net.minecraft.world.World
import resonantengine.lib.prefab.poison.PoisonRadiation
import resonantengine.lib.transform.vector.Vector3

import scala.collection.JavaConversions._

class BlockRadioactive(material: Material) extends Block(material)
{
  var canSpread: Boolean = true
  var radius: Float = 5
  var amplifier: Int = 2
  var canWalkPoison: Boolean = true
  var isRandomlyRadioactive: Boolean = true
  var spawnParticle: Boolean = true
  private var iconTop: IIcon = null
  private var iconBottom: IIcon = null

  //Constructor
  this.setTickRandomly(true)
  this.setHardness(0.2F)

  override def getIcon(side: Int, metadata: Int): IIcon =
  {
    return if (side == 1) this.iconTop else (if (side == 0) this.iconBottom else this.blockIcon)
  }

  @SideOnly(Side.CLIENT) override def registerBlockIcons(iconRegister: IIconRegister)
  {
    super.registerBlockIcons(iconRegister)
    this.iconTop = iconRegister.registerIcon(this.getUnlocalizedName.replace("tile.", "") + "_top")
    this.iconBottom = iconRegister.registerIcon(this.getUnlocalizedName.replace("tile.", "") + "_bottom")
  }

  /**
   * Ticks the block if it's been scheduled
   */
  override def updateTick(world: World, x: Int, y: Int, z: Int, rand: Random)
  {
    if (!world.isRemote)
    {
      if (this.isRandomlyRadioactive)
      {
        val bounds: AxisAlignedBB = AxisAlignedBB.getBoundingBox(x - this.radius, y - this.radius, z - this.radius, x + this.radius, y + this.radius, z + this.radius)
        val entitiesNearby: List[_] = world.getEntitiesWithinAABB(classOf[EntityLivingBase], bounds)
        for (entity <- entitiesNearby)
        {
          PoisonRadiation.INSTANCE.poisonEntity(new Vector3(x, y, z), entity.asInstanceOf[EntityLiving], amplifier)
        }
      }
      if (this.canSpread)
      {
        for (i <- 0 to 4)
        {
          val newX: Int = x + rand.nextInt(3) - 1
          val newY: Int = y + rand.nextInt(5) - 3
          val newZ: Int = z + rand.nextInt(3) - 1
          val block: Block = world.getBlock(newX, newY, newZ)
          if (rand.nextFloat > 0.4 && (block == Blocks.farmland || block == Blocks.grass))
          {
            world.setBlock(newX, newY, newZ, this)
          }
        }
        if (rand.nextFloat > 0.85)
        {
          world.setBlock(x, y, z, Blocks.dirt)
        }
      }
    }
  }

  /**
   * Called whenever an entity is walking on top of this block. Args: world, x, y, z, entity
   */
  override def onEntityWalking(par1World: World, x: Int, y: Int, z: Int, par5Entity: Entity)
  {
    if (par5Entity.isInstanceOf[EntityLiving] && this.canWalkPoison)
    {
      PoisonRadiation.INSTANCE.poisonEntity(new Vector3(x, y, z), par5Entity.asInstanceOf[EntityLiving])
    }
  }

  override def quantityDropped(par1Random: Random): Int =
  {
    return 0
  }

  @SideOnly(Side.CLIENT) override def randomDisplayTick(world: World, x: Int, y: Int, z: Int, par5Random: Random)
  {
    if (this.spawnParticle)
    {
      if (Minecraft.getMinecraft.gameSettings.particleSetting == 0)
      {
        val radius: Int = 3
        for (i <- 0 to 2)
        {
          val pos: Vector3 = new Vector3(x, y, z)
          pos.add(Math.random * radius - radius / 2, Math.random * radius - radius / 2, Math.random * radius - radius / 2)
          val fx: EntitySmokeFX = new EntitySmokeFX(world, pos.x, pos.y, pos.z, (Math.random - 0.5) / 2, (Math.random - 0.5) / 2, (Math.random - 0.5) / 2)
          fx.setRBGColorF(0.2f, 0.8f, 0)
          Minecraft.getMinecraft.effectRenderer.addEffect(fx)

        }
      }
    }
  }

}