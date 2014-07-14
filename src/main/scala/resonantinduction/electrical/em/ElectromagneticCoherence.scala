package resonantinduction.electrical.em

import cpw.mods.fml.common.{SidedProxy, Mod}
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLPreInitializationEvent, FMLInitializationEvent}
import cpw.mods.fml.common.registry.{LanguageRegistry, GameRegistry}
import net.minecraft.util.{EnumChatFormatting, ResourceLocation}
import resonantinduction.electrical.em.laser.emitter.BlockLaserEmitter
import resonantinduction.electrical.em.laser.focus.mirror.BlockMirror
import resonantinduction.electrical.em.laser.receiver.BlockLaserReceiver
import net.minecraft.init.{Items, Blocks}
import net.minecraftforge.oredict.ShapedOreRecipe
import net.minecraftforge.common.MinecraftForge
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraft.item.ItemStack
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.{NBTTagString, NBTTagList, NBTTagCompound}
import resonantinduction.electrical.em.laser.focus.ItemFocusingMatrix
import resonantinduction.electrical.em.laser.focus.crystal.BlockFocusCrystal

/**
 * @author Calclavia
 */
@Mod(name = "Electromagnetic Coherence", modid = "electromagneticcoherence", version = "1.0.0", modLanguage = "scala")
object ElectromagneticCoherence
{
  val NAME = "Electromagnetic Coherence";
  val MOD_ID = "electromagneticcoherence";

  val DOMAIN = MOD_ID;
  val PREFIX = MOD_ID + ":";

  val DIRECTORY = "assets/" + DOMAIN + "/";
  val MODEL_PATH_NAME = "models/"
  val MODEL_PATH = DIRECTORY + MODEL_PATH_NAME
  val FX_DIRECTORY = "textures/fx/"

  val particleTextures = new ResourceLocation("textures/particle/particles.png")

  @SidedProxy(clientSide = "resonantinduction.electrical.em.ClientProxy", serverSide = "resonantinduction.electrical.em.CommonProxy")
  var proxy: CommonProxy = null

  var blockLaserEmitter: BlockLaserEmitter = null
  var blockLaserReceiver: BlockLaserReceiver = null
  var blockMirror: BlockMirror = null
  var blockFocusCrystal: BlockFocusCrystal = null

  var itemFocusingMatrix: ItemFocusingMatrix = null

  var guideBook: ItemStack = null

  @EventHandler
  def preInit(event: FMLPreInitializationEvent)
  {
    MinecraftForge.EVENT_BUS.register(this)
  }

  @EventHandler
  def init(event: FMLInitializationEvent)
  {

    /**
     * GuideBook
     */
    guideBook = new ItemStack(Items.written_book)
    val bookNBT = new NBTTagCompound()
    bookNBT.setString("title", "Electromagnetic Coherence Guide")
    bookNBT.setString("author", "Calclavia")

    val pages = new NBTTagList()
    pages.appendTag(new NBTTagString(EnumChatFormatting.RED + "Guidebook:\n\n" + EnumChatFormatting.BLACK + "Electromagnetic Coherence is a mod all about lasers.\n\nYou can find all the blocks in the mod's creative tab."))
    pages.appendTag(new NBTTagString("A laser can be focused through a  " + EnumChatFormatting.RED + "laser emitter" + EnumChatFormatting.BLACK + ". By default, the color of the laser is white. The color can be changed by placing stained glass in front of it. Different combinations of glass would result in mixed colors."))
    pages.appendTag(new NBTTagString("To create a laser beam, provide a redstone pulse to the laser emitter. The intensity of the redstone would determine the intensity of the laser. Lasers with high intensities can burn and melt through blocks, hurting entities."))
    pages.appendTag(new NBTTagString("A laser beam can also be reflected using a  " + EnumChatFormatting.RED + "mirror" + EnumChatFormatting.BLACK + " with reduced intensity. Mirrors can be rotated by right clicking on it. Shift-right clicking a mirror focuses it to a side. Mirrors can also be auto-rotated with a redstone signal based on the direction of the signal propagation."))
    pages.appendTag(new NBTTagString("A " + EnumChatFormatting.RED + "laser receiver" + EnumChatFormatting.BLACK + " outputs a redstone signal with a strength based on the laser incident on its front. Using this, laser trip-wires can be made as entities walking through a laser will block its path."))
    pages.appendTag(new NBTTagString("The " + EnumChatFormatting.RED + "focusing matrix" + EnumChatFormatting.BLACK + " allows the player to focus mirrors and focus crystals. First, right click on a mirror/crystal to select it. Then, right click on a point to focus. Clicking the point twice will aim the laser at that point instead of making the device look at the point."))
    pages.appendTag(new NBTTagString("The " + EnumChatFormatting.RED + "Focus Crystal" + EnumChatFormatting.BLACK + " allows you to focus multiple laser beams into a single one, adding their strength together. All beams aiming at the crystal will be sent in the direction the crystal is facing. Focus Crystals can be rotated the same way as mirrors can."))
    pages.appendTag(new NBTTagString(EnumChatFormatting.RED + "Usages\n\n" + EnumChatFormatting.BLACK + "- Light Shows\n- Mining\n- Killing\n- Burning\n- Redstone Detection\n- Smelting (Aim strong laser at furnace)\n\nComing Soon:\n- Energy Transfer\n- Crafting"))

    bookNBT.setTag("pages", pages)
    guideBook.setTagCompound(bookNBT)

    blockLaserEmitter = new BlockLaserEmitter()
    blockLaserReceiver = new BlockLaserReceiver()
    blockMirror = new BlockMirror()
    blockFocusCrystal = new BlockFocusCrystal()

    itemFocusingMatrix = new ItemFocusingMatrix()

    GameRegistry.registerBlock(blockLaserEmitter, "LaserEmitter")
    GameRegistry.registerBlock(blockLaserReceiver, "LaserReceiver")
    GameRegistry.registerBlock(blockMirror, "Mirror")
    GameRegistry.registerBlock(blockFocusCrystal, "FocusCrystal")

    GameRegistry.registerItem(itemFocusingMatrix, "FocusingMatrix")

    LanguageRegistry.instance.addStringLocalization("tile." + PREFIX + "laserEmitter.name", "Laser Emitter")
    LanguageRegistry.instance.addStringLocalization("tile." + PREFIX + "mirror.name", "Mirror")
    LanguageRegistry.instance.addStringLocalization("tile." + PREFIX + "laserReceiver.name", "Laser Receiver")
    LanguageRegistry.instance.addStringLocalization("tile." + PREFIX + "focusCrystal.name", "Focus Crystal")
    LanguageRegistry.instance.addStringLocalization("item." + PREFIX + "focusingMatrix.name", "Focusing Matrix")

    LanguageRegistry.instance.addStringLocalization("itemGroup.ec", NAME)

    GameRegistry.addRecipe(new ShapedOreRecipe(blockLaserEmitter, "IGI", "IDI", "III", 'G': Character, Blocks.glass, 'I': Character, Items.iron_ingot, 'D': Character, Items.diamond))
    GameRegistry.addRecipe(new ShapedOreRecipe(blockLaserReceiver, "IGI", "IRI", "III", 'G': Character, Blocks.glass, 'I': Character, Items.iron_ingot, 'R': Character, Blocks.redstone_block))
    GameRegistry.addRecipe(new ShapedOreRecipe(blockMirror, "GGG", "III", "GGG", 'G': Character, Blocks.glass, 'I': Character, Items.iron_ingot))
    GameRegistry.addRecipe(new ShapedOreRecipe(blockFocusCrystal, "GGG", "GDG", "GGG", 'G': Character, Blocks.glass, 'D': Character, Items.diamond))

    GameRegistry.addRecipe(new ShapedOreRecipe(itemFocusingMatrix, "GGG", "GNG", "GGG", 'G': Character, Items.redstone, 'N': Character, Items.quartz))

    proxy.init()
  }

  @SubscribeEvent
  def joinWorldEvent(evt: EntityJoinWorldEvent)
  {
    if (evt.entity.isInstanceOf[EntityPlayer])
    {
      val player = evt.entity.asInstanceOf[EntityPlayer]
      val nbt = player.getEntityData

      if (!nbt.getBoolean("EC_receiveBook"))
      {
        player.inventory.addItemStackToInventory(guideBook)
        nbt.setBoolean("EC_receiveBook", true)
      }
    }
  }
}
