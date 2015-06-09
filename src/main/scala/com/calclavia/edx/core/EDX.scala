package com.calclavia.edx.core

import nova.core.block.BlockManager
import nova.core.component.ComponentManager
import nova.core.entity.EntityManager
import nova.core.event.GlobalEvents
import nova.core.fluid.FluidManager
import nova.core.game.{ClientManager, GameInfo}
import nova.core.gui.InputManager
import nova.core.item.{ItemDictionary, ItemManager}
import nova.core.loader.{Loadable, NovaMod}
import nova.core.nativewrapper.NativeManager
import nova.core.network.NetworkManager
import nova.core.recipes.RecipeManager
import nova.core.recipes.crafting.CraftingRecipeManager
import nova.core.render.RenderManager
import nova.core.util.{LanguageManager, RetentionManager}
import nova.core.world.WorldManager
import nova.internal.core.tick.UpdateTicker
import org.slf4j.Logger

/**
 * @author Calclavia
 */
@NovaMod(id = "edx", name = "Electrodynamics", version = Reference.version, novaVersion = Reference.novaVersion)
object EDX extends Loadable {
	val logger: Logger = null
	val gameInfo: GameInfo = null
	val clientManager: ClientManager = null
	val blocks: BlockManager = null
	val entityManager: EntityManager = null
	val items: ItemManager = null
	val fluids: FluidManager = null
	val worlds: WorldManager = null
	val renderManager: RenderManager = null
	val recipeManager: RecipeManager = null
	val craftingRecipeManager: CraftingRecipeManager = null
	val itemDictionary: ItemDictionary = null
	val events: GlobalEvents = null
	val network: NetworkManager = null
	val retention: RetentionManager = null
	val language: LanguageManager = null
	val input: InputManager = null
	val components: ComponentManager = null
	val nativeManager: NativeManager = null

	val syncTicker: UpdateTicker.SynchronizedTicker = null
	val threadTicker: UpdateTicker.ThreadTicker = null

	override def preInit() {

		//		CoreContent.preInit()

		/**
		 * Register GUI
		 */
		//Game.gui.register(classOf[GuiCreativeBuilder])
		//	ResourceFactory.preInit()
	}

	override def postInit() {
		/*
		Game.itemDictionary.add("ingotGold", Items.gold_ingot)
		Game.itemDictionary.add("ingotIron", Items.iron_ingot)
		Game.itemDictionary.add("oreGold", Blocks.gold_ore)
		Game.itemDictionary.add("oreIron", Blocks.iron_ore)
		Game.itemDictionary.add("oreLapis", Blocks.lapis_ore)


		MachineRecipes.instance.addRecipe(RecipeType.SMELTER.name, new FluidStack(FluidRegistry.LAVA, FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(Blocks.stone))
		MachineRecipes.instance.addRecipe(RecipeType.GRINDER.name, Blocks.cobblestone, Blocks.gravel)
		MachineRecipes.instance.addRecipe(RecipeType.GRINDER.name, Blocks.stone, Blocks.cobblestone)
		MachineRecipes.instance.addRecipe(RecipeType.GRINDER.name, Blocks.chest, new ItemStack(Blocks.planks, 7, 0))
		MachineRecipes.instance.addRecipe(RecipeType.SIFTER.name, Blocks.cobblestone, Blocks.sand)
		MachineRecipes.instance.addRecipe(RecipeType.SIFTER.name, Blocks.gravel, Blocks.sand)
		MachineRecipes.instance.addRecipe(RecipeType.SIFTER.name, Blocks.glass, Blocks.sand)
		*/
	}

	/**
	 * Default handler.

	def boilEventHandler(evt: BoilEvent) {
		val world: World = evt.world
		val position: Vector3d = evt.position

		for (height <- 1 until evt.maxSpread) {
			{
				val tileEntity: TileEntity = world.getTileEntity(position.xi, position.yi + height, position.zi)
				if (tileEntity.isInstanceOf[IBoilHandler]) {
					val handler: IBoilHandler = tileEntity.asInstanceOf[IBoilHandler]
					val fluid: FluidStack = evt.getRemainForSpread(height)
					if (fluid.amount > 0) {
						if (handler.canFill(Direction.DOWN, fluid.getFluid)) {
							fluid.amount -= handler.fill(Direction.DOWN, fluid, true)
						}
					}
				}
			}
		}

		evt.setResult(Event.Result.DENY)
	} */
}
