package mffs.base

import java.util.{Set => JSet}

import mffs.Content
import mffs.api.modules.IModuleProvider
import mffs.util.TCache

abstract class TileModuleAcceptor extends BlockFortron with IModuleProvider with TCache
{
	var startModuleIndex = 1
	var endModuleIndex = getSizeInventory - 1
	/**
	 * Used for client-side only.
	 */
	var clientFortronCost = 0
	protected var capacityBase = 500
	protected var capacityBoost = 5

	override def write(buf: Packet, id: Int)
	{
		super.write(buf, id)

		if (id == PacketBlock.description.id)
		{
			buf <<< getFortronCost
		}
	}

	/**
	 * Returns Fortron cost in ticks.
	 */
	final def getFortronCost: Int =
	{
		if (this.worldObj.isRemote)
		{
			return this.clientFortronCost
		}

		val cacheID = "getFortronCost"

		if (hasCache(classOf[Integer], cacheID)) return getCache(classOf[Integer], cacheID)

		val result = doGetFortronCost

		cache(cacheID, result)

		return result
	}

	protected def doGetFortronCost: Int = Math.round((getModuleStacks() foldLeft 0f)((a: Float, b: Item) => a + b.stackSize * b.getItem.asInstanceOf[IModule].getFortronCost(getAmplifier)))

	protected def getAmplifier: Float = 1f

	override def read(buf: Packet, id: Int, packetType: PacketType) {
		super.read(buf, id, packetType)

		if (id == PacketBlock.description.id) {
			clientFortronCost = buf.readInt()
		}
	}

	override def start() {
		super.start()
		fortronTank.setCapacity((this.getModuleCount(Content.moduleCapacity) * this.capacityBoost + this.capacityBase) * FluidContainerRegistry.BUCKET_VOLUME)
	}

	@unchecked
	override def getModuleCount(module: IModule, slots: Int*): Int = {
		var cacheID = "getModuleCount_" + module.hashCode

		if (slots != null) {
			cacheID += "_" + slots.hashCode()
		}

		if (hasCache(classOf[Integer], cacheID)) return getCache(classOf[Integer], cacheID)

		var count = 0

		if (slots != null && slots.length > 0) {
			count = (slots.view map (getStackInSlot(_)) filter (_ != null) filter (_.getItem == module) foldLeft 0)(_ + _.stackSize)
		}
		else {
			count = (getModuleStacks() filter (_.getItem == module) foldLeft 0)(_ + _.stackSize)
		}

		cache(cacheID, count)

		return count
	}

	def consumeCost() {
		if (getFortronCost() > 0) {
			addFortron(getFortronCost(), true)
		}
	}

	override def getModule(module: IModule): Item =
	{
		val cacheID = "getModule_" + module.hashCode

		if (hasCache(classOf[Item], cacheID)) {
			return getCache(classOf[Item], cacheID)
		}

		val returnStack = new Item(module.asInstanceOf[Item], getModuleStacks().count(_.getItem == module))

		cache(cacheID, returnStack.copy)
		return returnStack
	}

	def getModuleStacks(slots: Int*): JSet[Item] = {
		var cacheID: String = "getModuleStacks_"

		if (slots != null) {
			cacheID += slots.hashCode()
		}

		if (hasCache(classOf[Set[Item]], cacheID)) return getCache(classOf[Set[Item]], cacheID)

		var modules: Set[Item] = null

		if (slots == null || slots.length <= 0) {
			modules = ((startModuleIndex until endModuleIndex) map (getStackInSlot(_)) filter (_ != null) filter (_.getItem.isInstanceOf[IModule])).toSet
		}
		else {
			modules = (slots map (getStackInSlot(_)) filter (_ != null) filter (_.getItem.isInstanceOf[IModule])).toSet
		}

		cache(cacheID, modules)

		return modules
	}

	@SuppressWarnings(Array("unchecked"))
	def getModules(slots: Int*): JSet[IModule] =
	{
		var cacheID: String = "getModules_"
		if (slots != null)
		{
			cacheID += slots.hashCode()
		}

		if (hasCache(classOf[Set[IModule]], cacheID)) return getCache(classOf[Set[IModule]], cacheID)

		var modules: Set[IModule] = null

		if (slots == null || slots.length <= 0)
			modules = ((startModuleIndex until endModuleIndex) map (getStackInSlot(_)) filter (_ != null) filter (_.getItem.isInstanceOf[IModule]) map (_.getItem.asInstanceOf[IModule])).toSet
		else
			modules = (slots map (getStackInSlot(_)) filter (_ != null) filter (_.getItem.isInstanceOf[IModule]) map (_.getItem.asInstanceOf[IModule])).toSet

		cache(cacheID, modules)
		return modules
	}

	override def markDirty()
	{
		super.markDirty()
		this.fortronTank.setCapacity((this.getModuleCount(Content.moduleCapacity) * this.capacityBoost + this.capacityBase) * FluidContainerRegistry.BUCKET_VOLUME)
		clearCache()
	}

	override def readFromNBT(nbt: NBTTagCompound)
	{
		clearCache()
		super.readFromNBT(nbt)
		this.clientFortronCost = nbt.getInteger("fortronCost")
	}

	override def writeToNBT(nbt: NBTTagCompound)
	{
		super.writeToNBT(nbt)
		nbt.setInteger("fortronCost", this.clientFortronCost)
	}

}