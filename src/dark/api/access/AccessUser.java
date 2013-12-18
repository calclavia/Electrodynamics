package dark.api.access;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.builtbroken.common.User;

/** Used to define a users access to a terminal based object.
 * 
 * @author DarkGuardsman */
public class AccessUser extends User
{
    protected boolean isTempary = false;
    protected NBTTagCompound extraData;
    protected AccessGroup group;
    protected List<String> nodes = new ArrayList<String>();

    public AccessUser(String username)
    {
        super(username);
    }

    public AccessUser(EntityPlayer player)
    {
        super(player.username);
    }

    public AccessGroup getGroup()
    {
        return this.group;
    }

    public AccessUser setGroup(AccessGroup group)
    {
        this.group = group;
        return this;
    }

    public boolean hasNode(String node)
    {
        if (group != null && group.hasNode(node))
        {
            return true;
        }
        return nodes.contains(node);
    }

    public NBTTagCompound save(NBTTagCompound nbt)
    {
        nbt.setString("username", this.username);
        nbt.setCompoundTag("extraData", this.userData());
        NBTTagList usersTag = new NBTTagList();
        for (String str : this.nodes)
        {
            NBTTagCompound accessData = new NBTTagCompound();
            accessData.setString("name", str);
            usersTag.appendTag(accessData);
        }
        nbt.setTag("nodes", usersTag);
        return nbt;
    }

    public AccessUser load(NBTTagCompound nbt)
    {
        this.username = nbt.getString("username");
        this.extraData = nbt.getCompoundTag("extraData");
        NBTTagList userList = nbt.getTagList("nodes");
        for (int i = 0; i < userList.tagCount(); ++i)
        {
            this.nodes.add(((NBTTagCompound) userList.tagAt(i)).getString("name"));
        }
        return this;
    }

    public static AccessUser loadFromNBT(NBTTagCompound nbt)
    {
        return new AccessUser("").load(nbt);
    }

    public AccessUser setTempary(boolean si)
    {
        this.isTempary = si;
        return this;
    }

    /** Used to add other data to the user */
    public NBTTagCompound userData()
    {
        if (this.extraData == null)
        {
            this.extraData = new NBTTagCompound();
        }
        return this.extraData;
    }

}
