package resonantinduction.electrical.encoder;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInductionTabs;
import resonantinduction.core.Settings;
import resonantinduction.core.prefab.item.ItemRI;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemDisk extends ItemRI
{
	public ItemDisk()
	{
		super("disk");
		this.setHasSubtypes(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.itemIcon = par1IconRegister.registerIcon(Reference.PREFIX + "disk");
	}

	@Override
	public int getItemStackLimit()
	{
		return 1;
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer par2EntityPlayer, List list, boolean par4)
	{
		List<String> commands = getCommands(itemStack);

		if (commands.size() > 0)
		{
			if (commands.size() == 1)
				list.add(commands.size() + " command");
			else
				list.add(commands.size() + " commands");
		}
		else
		{
			list.add("No commands");
		}
	}

	/** Saves the list of items to filter out inside. */
	public static void setCommands(ItemStack itemStack, ArrayList<String> commands)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		NBTTagList nbt = new NBTTagList();

		for (int i = 0; i < commands.size(); ++i)
		{
			if (commands.get(i) != null)
			{
				NBTTagCompound newCompound = new NBTTagCompound();
				newCompound.setString("command", commands.get(i));
				nbt.appendTag(newCompound);
			}
		}

		itemStack.getTagCompound().setTag("Commands", nbt);
	}

	public static ArrayList<String> getCommands(ItemStack itemStack)
	{
		ArrayList<String> commands = new ArrayList<String>();

		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		NBTTagCompound nbt = itemStack.getTagCompound();
		NBTTagList tagList = nbt.getTagList("Commands");

		for (int i = 0; i < tagList.tagCount(); ++i)
		{
			NBTTagCompound curTag = (NBTTagCompound) tagList.tagAt(i);
			String cmd = curTag.getString("command");
			commands.add(cmd);
		}

		return commands;
	}
}
