package edx.mechanical.mech.turbine

import java.util.List

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.{Reference, Settings}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import net.minecraft.world.biome.{BiomeGenBase, BiomeGenOcean, BiomeGenPlains}
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{Fluid, FluidStack, FluidTank, FluidTankInfo}
import org.lwjgl.opengl.GL11
import resonant.api.tile.IBoilHandler
import resonant.lib.prefab.tile.item.ItemBlockMetadata
import resonant.lib.render.RenderUtility
import resonant.lib.transform.vector.Vector3
import resonant.lib.utility.MathUtility
import resonant.lib.utility.inventory.InventoryUtility
import resonant.lib.wrapper.CollectionWrapper._
import resonant.lib.wrapper.NBTWrapper._

/**
 * The vertical wind turbine collects airflow.
 *
 * The horizontal wind turbine collects steam from steam power plants.
 *
 * @author Calclavia
 */
object TileWindTurbine
{
  @SideOnly(Side.CLIENT)
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "windTurbines.obj"))
}

class TileWindTurbine extends TileTurbine with IBoilHandler
{
  /**
   * Steam simulations
   */
  private val gasTank = new FluidTank(1000)
  /**
   * Wind simulations
   */
  private var openBlockCache = new Array[Byte](224)
  private var checkCount = 0
  private var efficiency = 0f
  private var windPower = 0d
  private var nextWindPower = 0d

  //Constructor
  this.itemBlock = classOf[ItemBlockMetadata]

  override def update()
  {
    super.update()

    if (!worldObj.isRemote)
    {
      if (tier == 0 && getDirection.offsetY == 0 && worldObj.isRaining && worldObj.isThundering && worldObj.rand.nextFloat < 0.00000008)
      {
        //Break under storm
        InventoryUtility.dropItemStack(worldObj, new Vector3(x, y, z), new ItemStack(Blocks.wool, 1 + worldObj.rand.nextInt(2)))
        InventoryUtility.dropItemStack(worldObj, new Vector3(x, y, z), new ItemStack(Items.stick, 3 + worldObj.rand.nextInt(8)))
        toVectorWorld.setBlockToAir()
      }
      else if (getMultiBlock.isPrimary)
      {
        //Only execute code in the primary block
        if (getDirection.offsetY == 0)
        {
          //This is a vertical wind turbine, generate from airflow
          if (ticks % 20 == 0)
            computePower()

          windPower = MathUtility.lerp(windPower, nextWindPower, ticks % 20 / 20d)
          getMultiBlock.get.mechanicalNode.rotate(windPower * multiBlockRadius / 20, windPower / multiBlockRadius / 20)
        }

        //Generate from steam
        val steamPower = if (gasTank.getFluid != null) gasTank.drain(gasTank.getFluidAmount, true).amount else 0 * 1000 * Settings.steamMultiplier
        getMultiBlock.get.mechanicalNode.rotate(steamPower * multiBlockRadius / 20, steamPower / multiBlockRadius / 20)
      }
    }
  }

  private def computePower()
  {
    val checkSize = 10
    val height = yCoord + checkCount / 28
    val deviation = checkCount % 7
    var checkDir: ForgeDirection = null
    var check: Vector3 = null
    val cc = checkCount / 7 % 4

    cc match
    {
      case 0 =>
        checkDir = ForgeDirection.NORTH
        check = new Vector3(xCoord - 3 + deviation, height, zCoord - 4)
      case 1 =>
        checkDir = ForgeDirection.WEST
        check = new Vector3(xCoord - 4, height, zCoord - 3 + deviation)
      case 2 =>
        checkDir = ForgeDirection.WEST
        check = new Vector3(xCoord - 4, height, zCoord - 3 + deviation)
      case 3 =>
        checkDir = ForgeDirection.EAST
        check = new Vector3(xCoord + 4, height, zCoord - 3 + deviation)
    }

    var openAirBlocks = 0

    while (openAirBlocks < checkSize && world.isAirBlock(check.xi, check.yi, check.zi))
    {
      check.add(checkDir)
      openAirBlocks += 1
    }

    efficiency = efficiency - openBlockCache(checkCount) + openAirBlocks
    openBlockCache(checkCount) = openAirBlocks.toByte
    checkCount = (checkCount + 1) % (openBlockCache.length - 1)
    val multiblockMultiplier = (multiBlockRadius / 2) * (multiBlockRadius / 2)

    val materialMultiplier = tier match
    {
      case 0 => 1.1f
      case 1 => 0.9f
      case 2 => 1f
    }

    val biome = worldObj.getBiomeGenForCoords(xCoord, zCoord)
    val hasBonus = biome.isInstanceOf[BiomeGenOcean] || biome.isInstanceOf[BiomeGenPlains] || biome == BiomeGenBase.river
    val windSpeed = (worldObj.rand.nextFloat / 5) + (yCoord / 256f) * (if (hasBonus) 1.2f else 1) + worldObj.getRainStrength(0.5f)

    nextWindPower = 10 * materialMultiplier * multiblockMultiplier * windSpeed * efficiency * Settings.WIND_POWER_RATIO / 20
  }

  override def getSubBlocks(par1: Item, par2CreativeTabs: CreativeTabs, par3List: List[_])
  {
    for (i <- 0 to 2)
    {
      par3List.add(new ItemStack(par1, 1, i))
    }
  }

  override def canFill(from: ForgeDirection, fluid: Fluid): Boolean = from == ForgeDirection.DOWN && fluid.getName.contains("steam")

  override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int = gasTank.fill(resource, doFill)

  override def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack = null

  override def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack = null

  override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean = false

  override def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] = Array(gasTank.getInfo)

  /** Reads a tile entity from NBT. */
  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    checkCount = nbt.getInteger("checkCount")
    efficiency = nbt.getFloat("efficiency")
    openBlockCache = nbt.getArray[Byte]("openBlockCache")
  }

  /** Writes a tile entity to NBT. */
  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setInteger("checkCount", checkCount)
    nbt.setFloat("efficiency", efficiency)
    nbt.setArray("openBlockCache", openBlockCache)
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int): Unit =
  {
    if (getMultiBlock.isPrimary)
    {
      GL11.glPushMatrix()
      GL11.glTranslated(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
      GL11.glPushMatrix()
      RenderUtility.rotateBlockBasedOnDirectionUp(getDirection)
      GL11.glTranslatef(0, 0.35f, 0)
      GL11.glRotatef(180, 1, 0, 0)
      GL11.glRotatef(Math.toDegrees(mechanicalNode.angle).asInstanceOf[Float], 0, 1, 0)
      render(tier, multiBlockRadius, getMultiBlock.isConstructed)
      GL11.glPopMatrix()
      GL11.glPopMatrix()
    }
  }

  @SideOnly(Side.CLIENT)
  def render(tier: Int, size: Int, isConstructed: Boolean)
  {
    if (tier == 0)
    {
      RenderUtility.bind(Reference.blockTextureDirectory + "planks_oak.png")
    }
    else if (tier == 1)
    {
      RenderUtility.bind(Reference.blockTextureDirectory + "cobblestone.png")
    }
    else if (tier == 2)
    {
      RenderUtility.bind(Reference.blockTextureDirectory + "iron_block.png")

    }
    if (isConstructed)
    {
      GL11.glScalef(0.3f, 1, 0.3f)
      GL11.glScalef(size * 2 + 1, Math.min(size, 2), size * 2 + 1)
      if (tier == 2)
      {
        GL11.glTranslatef(0, -0.11f, 0)
        TileWindTurbine.model.renderOnly("LargeMetalBlade")
        TileWindTurbine.model.renderOnly("LargeMetalHub")
      }
      else
      {
        TileWindTurbine.model.renderOnly("LargeBladeArm")
        GL11.glScalef(1f, 2f, 1f)
        GL11.glTranslatef(0, -0.05f, 0)
        TileWindTurbine.model.renderOnly("LargeHub")
        RenderUtility.bind(Reference.blockTextureDirectory + "wool_colored_white.png")
        TileWindTurbine.model.renderOnly("LargeBlade")
      }
    }
    else
    {
      TileWindTurbine.model.renderOnly("SmallBlade")
      RenderUtility.bind(Reference.blockTextureDirectory + "log_oak.png")
      TileWindTurbine.model.renderOnly("SmallHub")
    }
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(itemStack: ItemStack)
  {
    GL11.glPushMatrix()
    GL11.glTranslatef(0.5f, 0.5f, 0.5f)
    render(itemStack.getItemDamage, 1, false)
    GL11.glPopMatrix()
  }
}