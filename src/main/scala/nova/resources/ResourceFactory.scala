package nova.resources

import java.util.function.{Function => JFunction}

import nova.core
import nova.core.block.{Block, BlockFactory}
import nova.core.item.{Item, ItemFactory}
import nova.core.render.texture.{BlockTexture, ItemTexture}
import nova.internal.core.Game
import nova.resources.block.TileOre
import nova.resources.item.ItemIngot
import nova.scala.modcontent.ContentLoader
import nova.scala.wrapper.StringWrapper._

/**
 * A factor class generates different types of resources based on its material
 *
 * type - Ore? Ingot? Dust? etc.
 *
 * @author Calclavia
 */
class ResourceFactory(domain: String) extends ContentLoader {

	val oreForeground = new BlockTexture(domain, "oreForeground")
	val oreBackground = new BlockTexture(domain, "oreBackground")
	val ingot = new ItemTexture(domain, "ingot")
	/**
	 * Reference to color of material
	 */
	private var materials = Set.empty[String]
	private var materialColorCache = Map.empty[String, Integer]
	private var resourceBlocks = Map.empty[String, Class[_ <: Block with Resource]]
	private var resourceItems = Map.empty[String, Class[_ <: Item with Resource]]
	private var generatedBlocks = Map.empty[(String, String), BlockFactory]
	private var generatedItems = Map.empty[(String, String), ItemFactory]

	override def id: String = domain

	/**
	 * Materials must be first registered before use
	 */
	def registerMaterial(material: String) {
		materials += material
	}

	def registerMaterialColor(material: String, color: Int) {
		if (!materialColorCache.contains(material)) {
			materialColorCache += material -> color
		}
	}

	def requestBlocks(material: String, except: String*): Map[String, BlockFactory] = {
		return resourceBlocks.keys.filterNot(except.contains).map(t => (t, requestBlock(t, material))).toMap
	}

	/**
	 * Requests a resource block to be generated. Example: dustIron
	 * @param material - E.g: iron
	 * @param resourceType - E.g: dust
	 */
	def requestBlock(resourceType: String, material: String): BlockFactory = {
		assert(materials.contains(material))

		val result = Game.blocks.register(new JFunction[Array[AnyRef], Block] {
			override def apply(args: Array[AnyRef]): Block = {
				val newResource = resourceBlocks(resourceType).newInstance()
				newResource.id = resourceType + material.capitalizeFirst
				newResource.asInstanceOf[Resource].material = material
				return newResource
			}
		})
		generatedBlocks += (resourceType, material) -> result

		//Register ore dictionary
		Game.itemDictionary.add(resourceType + material.capitalizeFirst, result.getID)
		return result
	}

	def requestItems(material: String, except: String*): Map[String, ItemFactory] = {
		return resourceItems.keys.filterNot(except.contains).map(t => (t, requestItem(t, material))).toMap
	}

	def requestItem(resourceType: String, material: String): ItemFactory = {
		assert(materials.contains(material))
		val result = Game.items.register(new JFunction[Array[AnyRef], core.item.Item] {
			override def apply(args: Array[AnyRef]): Item = {
				val newResource = resourceItems(resourceType).newInstance()
				newResource.id = resourceType + material.capitalizeFirst
				newResource.material = material
				return newResource
			}
		})

		generatedItems += (resourceType, material) -> result

		//Register ore dictionary
		Game.itemDictionary.add(resourceType + material.capitalizeFirst, result.getID)
		return result
	}

	def getBlock(resourceType: String, material: String) = generatedBlocks((resourceType, material))

	def getItem(resourceType: String, material: String) = generatedItems((resourceType, material))

	def getMaterial(block: BlockFactory) = generatedBlocks.map(keyVal => (keyVal._2, keyVal._1._2)).getOrElse(block, null)

	def getMaterial(item: ItemFactory) = generatedItems.map(keyVal => (keyVal._2, keyVal._1._2)).getOrElse(item, null)

	override def preInit() {
		//By default, we want to register ore resource type and ingot resource type
		registerResourceBlock("ore", classOf[TileOre])
		registerResourceItem("ingot", classOf[ItemIngot])

		//Register texture
		super.preInit()
	}

	def registerResourceBlock(name: String, clazz: Class[_ <: Block with Resource]) {
		resourceBlocks += name -> clazz
	}

	def registerResourceItem(name: String, clazz: Class[_ <: Item with Resource]) {
		resourceItems += name -> clazz
	}

	def getColor(name: String): Int = if (materialColorCache.contains(name)) materialColorCache(name) else 0xFFFFFF
}
