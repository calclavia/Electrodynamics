package mffs.security

import com.mojang.authlib.GameProfile
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}

import scala.collection.mutable

/**
 * @author Calclavia
 */
class Access
{

  class Profile
  {
    var groups = new mutable.HashSet[Group]()

    def this(nbt: NBTTagCompound)
    {
      this()
      val nbtList = nbt.getTagList("groups", 10)
      groups = (0 until nbtList.tagCount()).map(new Group(nbtList.getCompoundTagAt(_)))
    }

    def toNBT: NBTTagCompound =
    {
      val nbt = new NBTTagCompound()
      val nbtList = new NBTTagList()
      groups.foreach(group => nbtList.appendTag(user.toNBT))
      nbt.setTag("groups", nbtList)
      return nbt
    }
  }

  class Group
  {
    var users = new mutable.HashSet[User]()

    def this(nbt: NBTTagCompound)
    {
      this()
      val nbtList = nbt.getTagList("users", 10)
      groups = (0 until nbtList.tagCount()).map(new User(nbtList.getCompoundTagAt(_)))
    }

    def toNBT: NBTTagCompound =
    {
      val nbt = new NBTTagCompound()
      val nbtList = new NBTTagList()
      users.foreach(user => nbtList.appendTag(user.toNBT))
      nbt.setTag("users", nbtList)
      return nbt
    }
  }

  class User
  {
    val profile: GameProfile = _

    def this(nbt: NBTTagCompound)
    {
      this()
      proifle = new GameProfile(nbt.getString("UUID"), nbt.getString("username"))
    }

    def toNBT: NBTTagCompound =
    {
      val nbt = new NBTTagCompound()
      nbt.setString("UUID", profile.getId)
      nbt.setString("username", profile.getName)
      return nbt
    }
  }

}
