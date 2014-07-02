package mffs.security.access

import com.mojang.authlib.GameProfile
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import resonant.lib.access.{Permission, Permissions}

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
    var permissions = new mutable.HashSet[Permission]

    def this(nbt: NBTTagCompound)
    {
      this()
      val userList = nbt.getTagList("users", 10)
      users = (0 until userList.tagCount()).map(new User(nbtList.getCompoundTagAt(_)))
      val permList = nbt.getTagList("permissions", 10)
      permissions = (0 until permList.tagCount()).map(Permissions.find(permList.getStringTagAt()))
    }

    def toNBT: NBTTagCompound =
    {
      val nbt = new NBTTagCompound()

      val userList = new NBTTagList()
      users.foreach(x => userList.appendTag(x.toNBT))
      nbt.setTag("users", userList)

      val permList = new NBTTagList()
      permissions.foreach(x => permList.appendTag(x.toString))
      nbt.setTag("permissions", permList)
      return nbt
    }
  }

  class User(profile: GameProfile)
  {
    var permissions = new mutable.HashSet[Permission]

    def this(nbt: NBTTagCompound)
    {
      this(proifle = new GameProfile(nbt.getString("UUID"), nbt.getString("username")))

      val permList = nbt.getTagList("permissions", 10)
      permissions = (0 until permList.tagCount()).map(Permissions.find(permList.getStringTagAt()))
    }

    def toNBT: NBTTagCompound =
    {
      val nbt = new NBTTagCompound()
      nbt.setString("UUID", profile.getId)
      nbt.setString("username", profile.getName)

      val permList = new NBTTagList()
      permissions.foreach(x => permList.appendTag(x.toString))
      nbt.setTag("permissions", permList)

      return nbt
    }
  }

}
