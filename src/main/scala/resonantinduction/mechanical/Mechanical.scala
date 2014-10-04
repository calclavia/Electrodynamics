package resonantinduction.mechanical

import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.{Mod, ModMetadata, SidedProxy}
import net.minecraft.item.ItemStack
import net.minecraftforge.common.MinecraftForge
import resonant.engine.content.debug.TileCreativeBuilder
import resonant.lib.loadable.LoadableHandler
import resonant.lib.network.discriminator.PacketAnnotationManager
import resonant.lib.schematic.SchematicPlate
import resonantinduction.core.interfaces.IMechanicalNode
import resonantinduction.core.{Reference, ResonantPartFactory, ResonantTab}
import resonantinduction.mechanical.fluid.pipe.PartPipe
import resonantinduction.mechanical.mech.MechanicalNode
import resonantinduction.mechanical.mech.gear.PartGear
import resonantinduction.mechanical.mech.gearshaft.PartGearShaft
import resonantinduction.mechanical.mech.turbine._
import universalelectricity.api.core.grid.NodeRegistry

/**
 * Resonant Induction Mechanical Module
 *
 * @author DarkCow, Calclavia
 */
@Mod(modid = Mechanical.ID, name = "Resonant Induction Mechanical", version = "", modLanguage = "scala", dependencies = "before:ThermalExpansion;required-after:ResonantInductionCore;after:ResonantInduction|Archaic") object Mechanical
{
    /** Mod Information */
    final val ID: String = "ResonantInduction|Mechanical"
    final val NAME: String = Reference.name + " Mechanical"

    @SidedProxy(clientSide = "resonantinduction.mechanical.ClientProxy", serverSide = "resonantinduction.mechanical.CommonProxy")
    var proxy: CommonProxy = null

    @Mod.Metadata(ID)
    var metadata: ModMetadata = null

    val loadables = new LoadableHandler

    @EventHandler
    def preInit(evt: FMLPreInitializationEvent)
    {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy)
        MinecraftForge.EVENT_BUS.register(new MicroblockHighlightHandler)

        loadables.applyModule(proxy)
        loadables.applyModule(MechContent)

        TileCreativeBuilder.register(new SchematicPlate("schematic.waterTurbine.name", MechContent.blockWaterTurbine))
        TileCreativeBuilder.register(new SchematicPlate("schematic.windTurbine.name", MechContent.blockWindTurbine))
        TileCreativeBuilder.register(new SchematicPlate("schematic.electricTurbine.name", MechContent.blockElectricTurbine))

        NodeRegistry.register(classOf[IMechanicalNode], classOf[MechanicalNode])

        ResonantTab.itemStack(new ItemStack(MechContent.blockGrinderWheel))

        PacketAnnotationManager.INSTANCE.register(classOf[TileWindTurbine])
        PacketAnnotationManager.INSTANCE.register(classOf[TileWaterTurbine])

        ResonantPartFactory.register(classOf[PartGear])
        ResonantPartFactory.register(classOf[PartGearShaft])
        ResonantPartFactory.register(classOf[PartPipe])

        loadables.preInit()
    }

    @EventHandler
    def init(evt: FMLInitializationEvent)
    {
        loadables.init()
    }

    @EventHandler
    def postInit(evt: FMLPostInitializationEvent)
    {
        loadables.postInit()
    }
}