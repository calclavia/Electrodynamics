package com.dark.access;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;

import com.dark.save.IVirtualObject;
import com.dark.save.NBTFileHelper;
import com.dark.save.SaveManager;

/** Designed to be used as a container for AccessGroups and AccessUser. If you plan to use this make
 * sure to use it correctly. This is designed to be saved separate from the world save if marked for
 * global access. Which means it can save/load at will from the world file.
 * 
 * @author DarkGuardsman */
public class AccessProfile implements ISpecialAccess, IVirtualObject
{
    /** A list of user access data. */
    protected List<AccessGroup> groups = new ArrayList<AccessGroup>();
    /** Display name */
    protected String profileName = "";
    /** Only used by global profiles that have no defined container. Also LocalHost means it was
     * created by a tileEntity */
    protected String profileID = "LocalHost";
    /** Is this profile global */
    protected boolean global = false;
    /** Save file by which this was loaded from. Mainly used to save it in the same location again. */
    protected File saveFile;

    private static final Set<AccessProfile> globalList = new HashSet<AccessProfile>();

    static
    {
        SaveManager.registerClass("AccessProfile", AccessProfile.class);
    }

    public AccessProfile()
    {
        if (global)
        {
            SaveManager.register(this);
            AccessProfile.globalList.add(this);
        }
    }

    public AccessProfile(NBTTagCompound nbt)
    {
        this(nbt, false);
    }

    public AccessProfile(NBTTagCompound nbt, boolean global)
    {
        this();
        this.load(nbt);
        if (this.profileName == null || this.profileID == null)
        {
            if (!global)
            {
                this.generateNew("Default", null);
            }
            else
            {
                this.generateNew("New Group", "global");
            }
        }
    }

    public static AccessProfile get(String name)
    {
        for (AccessProfile profile : globalList)
        {
            if (profile.getID().equalsIgnoreCase(name))
            {
                return profile;
            }
        }
        return null;
    }

    public AccessProfile generateNew(String name, Object object)
    {
        GroupRegistry.loadNewGroupSet(this);
        this.profileName = name;
        name.replaceAll(" ", "");
        String id = null;
        // Created by player for personal use
        if (object instanceof EntityPlayer)
        {
            id = ((EntityPlayer) object).username + "_" + System.currentTimeMillis();
        }//Created by a tile
        else if (object instanceof TileEntity || object == null)
        {
            id = "LocalHost:" + name;
        }//created by the game or player for global use
        else if (object instanceof String && ((String) object).equalsIgnoreCase("global"))
        {
            id = "P_" + name + "_" + System.currentTimeMillis();
            this.global = true;
        }
        this.profileID = id;
        return this;
    }

    public String getName()
    {
        return this.profileName;
    }

    public String getID()
    {
        return this.profileID;
    }

    public boolean isGlobal()
    {
        return this.global;
    }

    @Override
    public AccessUser getUserAccess(String username)
    {
        for (AccessGroup group : this.groups)
        {
            AccessUser user = group.getMember(username);
            if (user != null)
            {
                return user;
            }
        }
        return new AccessUser(username);
    }

    @Override
    public List<AccessUser> getUsers()
    {
        List<AccessUser> users = new ArrayList<AccessUser>();
        for (AccessGroup group : this.groups)
        {
            users.addAll(group.getMembers());
        }
        return users;
    }

    @Override
    public boolean setUserAccess(String player, AccessGroup g, boolean save)
    {
        return setUserAccess(new AccessUser(player).setTempary(save), g);
    }

    @Override
    public boolean setUserAccess(AccessUser user, AccessGroup group)
    {
        boolean bool = false;

        if (user != null && user.getName() != null)
        {
            bool = this.removeUserAccess(user.getName()) && group == null;
            if (group != null)
            {
                bool = group.addMemeber(user);
            }
            if (bool)
            {
                this.onProfileUpdate();
            }
        }
        return bool;
    }

    public boolean removeUserAccess(String player)
    {
        boolean re = false;
        for (AccessGroup group : this.groups)
        {
            AccessUser user = group.getMember(player);
            if (user != null && group.removeMemeber(user))
            {
                re = true;
            }
        }
        if (re)
        {
            this.onProfileUpdate();
        }
        return re;
    }

    public void onProfileUpdate()
    {

    }

    @Override
    public AccessGroup getGroup(String name)
    {
        for (AccessGroup group : this.getGroups())
        {
            if (group.getName().equalsIgnoreCase(name))
            {
                return group;
            }
        }
        return null;
    }

    @Override
    public boolean addGroup(AccessGroup group)
    {
        if (!this.groups.contains(group))
        {
            for (AccessGroup g : this.groups)
            {
                if (group.getName().equalsIgnoreCase(g.getName()))
                {
                    return false;
                }
            }
            if (this.groups.add(group))
            {
                this.onProfileUpdate();
                return true;
            }
        }
        return false;
    }

    @Override
    public AccessGroup getOwnerGroup()
    {
        return this.getGroup("owner");
    }

    @Override
    public List<AccessGroup> getGroups()
    {
        if (this.groups == null || this.groups.isEmpty())
        {
            GroupRegistry.loadNewGroupSet(this);
        }
        return this.groups;
    }

    @Override
    public void save(NBTTagCompound nbt)
    {
        this.profileName = nbt.getString("name");
        this.global = nbt.getBoolean("global");
        NBTTagList userList = nbt.getTagList("groups");
        if (userList != null && userList.tagCount() > 0)
        {
            this.groups.clear();
            for (int i = 0; i < userList.tagCount(); i++)
            {
                AccessGroup group = new AccessGroup("");
                group.load((NBTTagCompound) userList.tagAt(i));
                this.groups.add(group);
            }
        }
    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        nbt.setString("name", this.profileName);
        nbt.setBoolean("global", this.global);
        NBTTagList usersTag = new NBTTagList();
        for (AccessGroup group : this.getGroups())
        {
            usersTag.appendTag(group.save(new NBTTagCompound()));
        }
        nbt.setTag("groups", usersTag);
    }

    @Override
    public File getSaveFile()
    {
        if (this.saveFile == null)
        {
            this.saveFile = new File(NBTFileHelper.getWorldSaveDirectory(MinecraftServer.getServer().getFolderName()), "Access/Profile/" + this.getID() + ".dat");
        }
        return this.saveFile;
    }

    @Override
    public void setSaveFile(File file)
    {
        this.saveFile = file;

    }
}
