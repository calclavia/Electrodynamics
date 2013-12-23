package resonantinduction.multimeter;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.base.ItemMultipartBase;
import resonantinduction.wire.EnumWireMaterial;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;

public class ItemMultimeter extends ItemMultipartBase
{
	private Icon[] icons = new Icon[EnumWireMaterial.values().length];

	public ItemMultimeter(int id)
	{
		super("multimeter", id);
	}

	@Override
	public TMultiPart newPart(ItemStack itemStack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 hit)
	{
		pos.offset(side ^ 1, -1);
		PartMultimeter part = (PartMultimeter) MultiPartRegistry.createPart("resonant_induction_multimeter", false);

		if (part != null)
		{
			part.preparePlacement(side, itemStack.getItemDamage());
		}

		return part;
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
	{
		par3List.add("Shift-right click to place,");
		par3List.add("Right click to scan data.");

		float detection = this.getDetection(itemStack);

		if (detection != -1)
		{
			par3List.add("Last Detection: " + detection + " KJ");
		}
		else
		{
			par3List.add("No detection saved.");
		}
	}

	@Override
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
	{
		if (!par2EntityPlayer.isSneaking())
		{
			if (!world.isRemote)
			{
				par2EntityPlayer.addChatMessage("Energy: " + PartMultimeter.getDetectedEnergy(ForgeDirection.getOrientation(par7), world.getBlockTileEntity(x, y, z)) + " J");
			}

			return true;
		}

		return super.onItemUse(par1ItemStack, par2EntityPlayer, world, x, y, z, par7, par8, par9, par10);
	}

	public float getDetection(ItemStack itemStack)
	{
		if (itemStack.stackTagCompound == null || !itemStack.getTagCompound().hasKey("detection"))
		{
			return -1;
		}

		return itemStack.stackTagCompound.getFloat("detection");
	}

	public void setDetection(ItemStack itemStack, float detection)
	{
		if (itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setFloat("detection", detection);
	}
}
