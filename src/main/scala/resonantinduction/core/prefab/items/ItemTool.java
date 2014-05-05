package resonantinduction.core.prefab.items;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import calclavia.components.tool.ToolMode;
import calclavia.lib.utility.LanguageUtility;
import calclavia.lib.utility.nbt.NBTUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Prefab for all tool based items
 * 
 * @author Darkguardsman */
public class ItemTool extends Item
{
    protected boolean hasModes = false;
    protected String[] toolModes = new String[] { "Remove" };

    public ItemTool(int par1)
    {
        super(par1);
    }

    @Override
    public void onCreated(ItemStack stack, World par2World, EntityPlayer entityPlayer)
    {
        //Save who crafted the tool
        if (entityPlayer != null)
        {
            NBTUtility.getNBTTagCompound(stack).setString("Creator", entityPlayer.username);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
    {
        super.addInformation(stack, player, list, par4);

        //Item creator
        String creator = NBTUtility.getNBTTagCompound(stack).getString("Creator");
        if (!creator.equalsIgnoreCase("creative") && creator != "")
        {
            list.add("Created by: " + creator);
        }
        else if (creator.equalsIgnoreCase("creative"))
        {
            list.add("Created by Magic Dwarfs");
        }

    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
    {
        if (hasModes && toolModes != null && toolModes.length > 1)
        {
            //TODO replace with middle mouse wheel
            //Changes the mod of the tool
            if (player.isSneaking())
            {
                setMode(itemStack, (getMode(itemStack) + 1) % toolModes.length);
                String modeKey = toolModes[getMode(itemStack)];
                if (!world.isRemote && modeKey != null && !modeKey.isEmpty())
                {                    
                    player.addChatMessage(LanguageUtility.getLocal("tool.mode.set") + LanguageUtility.getLocal(modeKey));
                }

            }
        }
        return itemStack;
    }

    @Override
    public boolean shouldPassSneakingClickToBlock(World world, int x, int y, int z)
    {
        return true;
    }

    public int getMode(ItemStack itemStack)
    {
        return NBTUtility.getNBTTagCompound(itemStack).getInteger("mode");
    }

    public void setMode(ItemStack itemStack, int mode)
    {
        NBTUtility.getNBTTagCompound(itemStack).setInteger("mode", mode);
    }
}
