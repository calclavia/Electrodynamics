package com.dark.access;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.builtbroken.common.Group;

/** Used by a terminal to track what users are part of each group. As well used to setup access
 * points to the terminal.
 * 
 * @author DarkGuardsman */
public class AccessGroup extends Group<AccessUser>
{
    protected List<String> nodes = new ArrayList<String>();
    protected AccessGroup extendGroup;

    public AccessGroup(String name, AccessUser... js)
    {
        super(name, js);
    }

    public void setToExtend(AccessGroup group)
    {
        this.extendGroup = group;
    }

    @Override
    public boolean addMemeber(AccessUser obj)
    {
        if (obj != null)
        {
            for (AccessUser user : this.memebers)
            {
                if (user.getName().equalsIgnoreCase(obj.getName()))
                {
                    return false;
                }
            }
            if (super.addMemeber(obj))
            {
                obj.setGroup(this);
                return true;
            }
        }
        return false;
    }

    public AccessUser getMember(String name)
    {
        for (AccessUser user : this.memebers)
        {
            if (user.getName().equalsIgnoreCase(name))
            {
                return user;
            }
        }
        return null;
    }

    public NBTTagCompound save(NBTTagCompound nbt)
    {
        nbt.setString("groupName", this.getName());
        NBTTagList usersTag = new NBTTagList();
        for (AccessUser user : this.memebers)
        {
            if (!user.isTempary)
            {
                NBTTagCompound accessData = new NBTTagCompound();
                user.save(accessData);
                usersTag.appendTag(accessData);
            }
        }
        nbt.setTag("users", usersTag);
        NBTTagList nodesTag = new NBTTagList();
        for (String str : this.nodes)
        {
            NBTTagCompound accessData = new NBTTagCompound();
            accessData.setString("name", str);
            nodesTag.appendTag(accessData);
        }
        nbt.setTag("nodes", nodesTag);
        return nbt;
    }

    public void load(NBTTagCompound nbt)
    {
        this.setName(nbt.getString("groupName"));
        NBTTagList userList = nbt.getTagList("users");
        for (int i = 0; i < userList.tagCount(); ++i)
        {
            this.addMemeber(AccessUser.loadFromNBT((NBTTagCompound) userList.tagAt(i)));
        }
        NBTTagList nodeList = nbt.getTagList("nodes");
        for (int i = 0; i < nodeList.tagCount(); ++i)
        {
            this.nodes.add(((NBTTagCompound) nodeList.tagAt(i)).getString("name"));
        }
    }

    public boolean hasNode(String node)
    {
        return this.nodes.contains(node);
    }

    public void addNode(String node)
    {
        this.nodes.add(node);
    }

    public void removeNode(String node)
    {
        if (this.nodes.contains(node))
        {
            this.nodes.remove(node);
        }
    }

    public boolean isMemeber(String string)
    {
        for (AccessUser user : this.memebers)
        {
            if (user.getName().equalsIgnoreCase(string))
            {
                return true;
            }
        }
        return false;
    }
}
