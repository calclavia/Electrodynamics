package com.calclavia.edx.optics.util

import com.calclavia.edx.optics.Settings

import scala.collection.mutable

/**
 * For objects that uses caching method to reduce CPU work.
 * @author Calclavia
 */
trait CacheHandler {
	/**
	 * Caching for the module stack data. This is used to reduce calculation time. Cache gets reset
	 * when inventory changes.
	 */
	private val cache = new mutable.HashMap[String, Any]

	def getOrSetCache[T](cacheID: String, f: () => T): T = {
		if (hasCache(cacheID)) {
			return getCache(cacheID)
		}

		val result = f()
		cache(cacheID, result)
		return result
	}

	protected def cache(id: String, value: Any) {
		if (Settings.useCache) {
			cache.put(id, value)
		}
	}

	def getCache[C](cacheID: String): C = {
		if (Settings.useCache) {
			if (hasCache(cacheID)) {
				return cache(cacheID).asInstanceOf[C]
			}
		}

		return null.asInstanceOf[C]
	}

	def hasCache[C](cacheID: String): Boolean = {
		if (Settings.useCache) {
			if (cache.contains(cacheID)) {
				return cache(cacheID) != null
			}
		}

		return false
	}

	def clearCache(cacheID: String) {
		cache.remove(cacheID)
	}

	def clearCache() {
		cache.clear()
	}
}