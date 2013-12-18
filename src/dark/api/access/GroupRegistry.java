package dark.api.access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/** @author DarkGuardsman */
public class GroupRegistry
{
    public static final List<String> nodes = new ArrayList<String>();
    public static final HashMap<String, List<String>> groupDefaultNodes = new HashMap();
    public static final HashMap<String, String> groupDefaultExtends = new HashMap();

    static
    {
        List<String> list = new ArrayList<String>();
        //Owner group defaults
        list.add(Nodes.GROUP_OWNER_NODE);
        list.add(Nodes.INV_DISABLE_NODE);
        list.add(Nodes.INV_ENABLE_NODE);
        createDefaultGroup("owner", "admin", list);
        //Admin group defaults
        List<String> list2 = new ArrayList<String>();
        list2.add(Nodes.GROUP_ADMIN_NODE);
        list2.add(Nodes.INV_EDIT_NODE);
        list2.add(Nodes.INV_LOCK_NODE);
        list2.add(Nodes.INV_UNLOCK_NODE);
        list2.add(Nodes.INV_CHANGE_NODE);
        createDefaultGroup("admin", "user", list2);
        //User group defaults
        List<String> list3 = new ArrayList<String>();
        list3.add(Nodes.GROUP_USER_NODE);
        list3.add(Nodes.INV_OPEN_NODE);
        list3.add(Nodes.INV_TAKE_NODE);
        list3.add(Nodes.INV_GIVE_NODE);
        createDefaultGroup("user", null, list3);
    }

    /** Creates a default group for all machines to use. Only add a group if there is no option to
     * really manage the group's settings
     *
     * @param name - group name
     * @param prefabGroup - group this should extend. Make sure it exists.
     * @param nodes - all commands or custom nodes */
    public static void createDefaultGroup(String name, String prefabGroup, List<String> nodes)
    {
        if (name != null)
        {
            groupDefaultNodes.put(name, nodes);
            groupDefaultExtends.put(name, prefabGroup);
        }
    }

    /** Creates a default group for all machines to use. Only add a group if there is no option to
     * really manage the group's settings
     *
     * @param name - group name
     * @param prefabGroup - group this should extend. Make sure it exists.
     * @param nodes - all commands or custom nodes */
    public static void createDefaultGroup(String name, String prefabGroup, String... nodes)
    {
        List<String> nodeList = new ArrayList<String>();
        if (nodes != null)
        {
            for (String node : nodes)
            {
                nodeList.add(node);
            }
        }
        createDefaultGroup(name, prefabGroup, nodeList);
    }

    /** Builds a new default group list for a basic machine */
    public static List<AccessGroup> getNewGroupSet()
    {
        List<AccessGroup> groups = new ArrayList<AccessGroup>();
        for (Entry<String, List<String>> entry : groupDefaultNodes.entrySet())
        {
            AccessGroup group = new AccessGroup(entry.getKey());
            if (entry.getValue() != null)
            {
                for (String string : entry.getValue())
                {
                    group.addNode(string);
                }
            }
            groups.add(group);
        }
        return groups;
    }

    /** Builds then loaded a new default group set into the terminal */
    public static void loadNewGroupSet(ISpecialAccess terminal)
    {
        if (terminal != null)
        {
            List<AccessGroup> groups = getNewGroupSet();
            for (AccessGroup group : groups)
            {
                terminal.addGroup(group);
            }
        }
    }

    public static void register(String node)
    {
        if (!nodes.contains(node))
        {
            nodes.add(node);
        }
    }

}
