package resonantinduction.archaic.process;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.api.recipe.MachineRecipes;
import resonant.api.recipe.RecipeResource;
import resonant.content.spatial.block.SpatialBlock;
import resonant.lib.network.discriminator.PacketTile;
import resonant.lib.network.discriminator.PacketType;
import resonant.lib.network.handle.IPacketReceiver;
import resonant.lib.utility.inventory.InventoryUtility;
import resonantinduction.core.Reference;
import resonant.content.factory.resources.RecipeType;
import resonantinduction.mechanical.gear.ItemHandCrank;
import universalelectricity.core.transform.vector.Vector3;
import resonant.lib.content.prefab.java.TileInventory;

public class TileMillstone extends TileInventory implements IPacketReceiver
{
	private int grindCount = 0;

    public TileMillstone() {
        super(Material.rock);
        setTextureName(Reference.prefix() + "millstone_side");
    }

    public void onInventoryChanged()
	{
		grindCount = 0;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public void doGrind(Vector3 spawnPos)
	{
		RecipeResource[] outputs = MachineRecipes.INSTANCE.getOutput(RecipeType.GRINDER.name(), getStackInSlot(0));

		if (outputs.length > 0)
		{
			if (++grindCount > 20)
			{
				for (RecipeResource res : outputs)
				{
					InventoryUtility.dropItemStack(worldObj, spawnPos, res.getItemStack().copy());
				}

				decrStackSize(0, 1);
				onInventoryChanged();
			}
		}
	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack)
	{
		return MachineRecipes.INSTANCE.getOutput(RecipeType.GRINDER.name(), itemStack).length > 0;
	}

	@Override
	public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
	{
		return true;
	}

	/**
	 * Packets
	 */
	@Override
	public PacketTile getDescPacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return new PacketTile(this, nbt);
	}

	@Override
	public void read(ByteBuf data, EntityPlayer player, PacketType type)
	{
		try
		{
			this.readFromNBT(ByteBufUtils.readTag(data));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconReg)
    {
        super.registerIcons(iconReg);
        SpatialBlock.icon().put("millstone_top", iconReg.registerIcon(Reference.prefix() + "millstone_top"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
        if (side == 0 || side == 1)
        {
            return SpatialBlock.icon().get("millstone_top");
        }

        return SpatialBlock.icon().get("millstone");
    }

    @Override
    public void click(EntityPlayer player)
    {
        if (!world().isRemote)
        {

                ItemStack output = getStackInSlot(0);

                if (output != null)
                {
                    InventoryUtility.dropItemStack(world(), new Vector3(player), output, 0);
                    setInventorySlotContents(0, null);
                }

                onInventoryChanged();
            }
    }

    @Override
    public boolean use(EntityPlayer player, int hitSide, Vector3 hit)
    {
            ItemStack current = player.inventory.getCurrentItem();
            ItemStack output = getStackInSlot(0);

            if (current != null && current.getItem() instanceof ItemHandCrank)
            {
                if (output != null)
                {
                    doGrind(new Vector3(player));
                    player.addExhaustion(0.3f);
                    return true;
                }
            }

            if (output != null)
            {
                InventoryUtility.dropItemStack(world(), new Vector3(player), output, 0);
                setInventorySlotContents(0, null);
            }
            else if (current != null && isItemValidForSlot(0, current))
            {
                setInventorySlotContents(0, current);
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }

            world().markBlockForUpdate(x(), y(), z());

        return false;
    }
}
