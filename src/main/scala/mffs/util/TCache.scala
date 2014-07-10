package mffs.util

import mffs.Settings

import scala.collection.mutable

/**
 * For objects that uses caching method to reduce CPU work.
 * @author Calclavia
 */
trait TCache
{
  /**
   * Caching for the module stack data. This is used to reduce calculation time. Cache gets reset
   * when inventory changes.
   */
  private val cache = new mutable.HashMap[String, Any]

  protected def cache(id: String, value: Any)
  {
    if (Settings.useCache)
    {
      cache.put(id, value)
    }
  }

  def getCache(cacheID: String): AnyRef =
  {
    if (Settings.useCache)
    {
      if (cache.contains(cacheID))
      {
        return cache.get(cacheID)
      }
    }

    return null
  }

  def getCache[C](clazz: Class[C], cacheID: String): C =
  {
    if (Settings.useCache)
    {
      if (cache.contains(cacheID))
      {
        if (cache.get(cacheID) != null && cache.get(cacheID).getClass.isAssignableFrom(clazz))
        {
          return cache.get(cacheID).asInstanceOf[C]
        }
      }
    }

    return null.asInstanceOf[C]
  }

  def hasCache[C](clazz: Class[C], cacheID: String): Boolean =
  {
    if (Settings.useCache)
    {
      if (cache.contains(cacheID))
      {
        return cache.get(cacheID) != null && clazz.isAssignableFrom(cache.get(cacheID).getClass)
      }
    }

    return false
  }

  def clearCache(cacheID: String)
  {
    cache.remove(cacheID)
  }

  def clearCache()
  {
    cache.clear()
  }
}