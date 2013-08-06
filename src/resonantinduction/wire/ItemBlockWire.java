package resonantinduction.wire;

import java.util.HashMap;
import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import resonantinduction.ResonantInduction;
import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class ItemBlockWire extends ItemBlock
{
	protected HashMap<String, Icon> icons = new HashMap<String, Icon>();

	public ItemBlockWire(int id)
	{
		super(id);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		return this.getUnlocalizedName() + "." + EnumWire.values()[itemStack.getItemDamage()].name().toLowerCase();
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List par3List, boolean par4)
	{
		par3List.add("Resistance: " + ElectricityDisplay.getDisplay(EnumWire.values()[itemstack.getItemDamage()].resistance, ElectricUnit.RESISTANCE));
		par3List.add("Max Amperage: " + ElectricityDisplay.getDisplay(EnumWire.values()[itemstack.getItemDamage()].maxAmps, ElectricUnit.AMPERE));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		for (int i = 0; i < EnumWire.values().length - 1; i++)
		{
			this.icons.put(this.getUnlocalizedName(new ItemStack(this.itemID, 1, i)), par1IconRegister.registerIcon(this.getUnlocalizedName(new ItemStack(this.itemID, 1, i)).replaceAll("tile.", ResonantInduction.PREFIX)));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int meta)
	{
		return this.icons.get(this.getUnlocalizedName(new ItemStack(this.itemID, 1, meta)));
	}
}