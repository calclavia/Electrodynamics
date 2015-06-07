package com.calclavia.edx.core

import nova.core.block.BlockManager
import nova.core.component.ComponentManager
import nova.core.entity.EntityManager
import nova.core.event.GlobalEvents
import nova.core.fluid.FluidManager
import nova.core.game.{ClientManager, GameInfo}
import nova.core.gui.InputManager
import nova.core.item.{ItemDictionary, ItemManager}
import nova.core.loader.NovaMod
import nova.core.nativewrapper.NativeManager
import nova.core.network.NetworkManager
import nova.core.recipes.RecipeManager
import nova.core.recipes.crafting.CraftingRecipeManager
import nova.core.render.RenderManager
import nova.core.util.{LanguageManager, RetentionManager}
import nova.core.world.WorldManager
import nova.internal.tick.UpdateTicker
import org.slf4j.Logger

/**
 * @author Calclavia
 */
@NovaMod(id = "edx", name = "Electrodynamics", version = Reference.version, novaVersion = Reference.novaVersion)
object EDX {
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
}
