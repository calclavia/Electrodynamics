package resonantinduction.archaic.crate;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import resonant.lib.prefab.block.BlockTile;
import resonant.lib.utility.LanguageUtility;
import resonant.lib.utility.inventory.InventoryUtility;
import resonantinduction.core.Reference;
import universalelectricity.api.UniversalElectricity;
import codechicken.multipart.ControlKeyModifer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** A block that allows the placement of mass amount of a specific item within it. It will be allowed
 * to go on Conveyor Belts.
 * 
 * NOTE: Crates should be upgraded with an item.
 * 
 * @author DarkGuardsman */
public class BlockCrate extends BlockTile
{
    Icon advanced, elite;

    public BlockCrate(int id)
    {
        super(id, UniversalElectricity.machine);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IconRegister iconReg)
    {
        this.blockIcon = iconReg.registerIcon(Reference.PREFIX + "crate_wood");
        this.advanced = iconReg.registerIcon(Reference.PREFIX + "crate_iron");
        this.elite = iconReg.registerIcon(Reference.PREFIX + "crate_steel");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int meta)
    {
        if (meta == 1)
        {
            return advanced;
        }
        else if (meta == 2)
        {
            return elite;
        }
        return this.blockIcon;
    }

    @Override
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
    {
        if (!world.isRemote && world.getBlockTileEntity(x, y, z) instanceof TileCrate)
        {
            TileCrate tileEntity = (TileCrate) world.getBlockTileEntity(x, y, z);
            this.tryEject(tileEntity, player, (System.currentTimeMillis() - tileEntity.prevClickTime) < 200);
            tileEntity.prevClickTime = System.currentTimeMillis();
        }
    }

    @Override
    public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote && world.getBlockTileEntity(x, y, z) instanceof TileCrate)
        {
            TileCrate tile = (TileCrate) world.getBlockTileEntity(x, y, z);
            tile.buildSampleStack();
            ItemStack sampleStack = tile.getSampleStack();
            int oreID = OreDictionary.getOreID(sampleStack);

            if (ControlKeyModifer.isControlDown(player))
            {
                tile.oreFilterEnabled = !tile.oreFilterEnabled;
                player.sendChatToPlayer(ChatMessageComponent.createFromText(LanguageUtility.getLocal("crate.orefilter." + tile.oreFilterEnabled)));
            }
            else if (oreID != -1)
            {
                /* Switches ore itemStack around */
                ArrayList<ItemStack> ores = OreDictionary.getOres(oreID);

                for (int oreIndex = 0; oreIndex < ores.size(); oreIndex++)
                {
                    if (ores.get(oreIndex).isItemEqual(sampleStack))
                    {
                        int nextIndex = (oreIndex + 1) % ores.size();
                        ItemStack desiredStack = ores.get(nextIndex).copy();
                        desiredStack.stackSize = sampleStack.stackSize;

                        for (int index = 0; index < tile.getSizeInventory(); index++)
                            tile.setInventorySlotContents(index, null);

                        tile.addStackToStorage(desiredStack);
                        break;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote && world.getBlockTileEntity(x, y, z) instanceof TileCrate)
        {
            TileCrate tile = (TileCrate) world.getBlockTileEntity(x, y, z);

            if (ControlKeyModifer.isControlDown(player))
            {
                if (player.getCurrentEquippedItem() != null && (!player.getCurrentEquippedItem().getItem().isDamageable() || player.getCurrentEquippedItem().getItem().getDamage(player.getCurrentEquippedItem()) > 0))
                {
                    ItemStack filter = player.getCurrentEquippedItem().copy();
                    filter.stackSize = 0;
                    tile.setFilter(filter);
                }
                else
                {
                    tile.setFilter(null);
                }
            }
            else
            {
                /* Creative mode way to fill crates to max in one click */
                ItemStack current = player.inventory.getCurrentItem();
                if (player.capabilities.isCreativeMode)
                {
                    if (side == 1)
                    {
                        if (current != null && tile.getSampleStack() == null)
                        {
                            ItemStack cStack = current.copy();
                            cStack.stackSize = TileCrate.getSlotCount(world.getBlockMetadata(x, y, z)) * 64;
                            addStackToCrate(tile, cStack);
                        }
                    }
                    else if (hitY <= 0.5)
                    {
                        tryEject(tile, player, System.currentTimeMillis() - tile.prevClickTime < 250);
                    }
                    else
                    {
                        tryInsert(tile, player, System.currentTimeMillis() - tile.prevClickTime < 250);
                    }
                }
                else
                {
                    tryInsert(tile, player, System.currentTimeMillis() - tile.prevClickTime < 250);
                }

            }
            tile.prevClickTime = System.currentTimeMillis();
        }
        return true;
    }

    /** Try to inject it into the crate. Otherwise, look around for nearby crates and try to put them
     * in. */
    public void tryInsert(TileCrate tileEntity, EntityPlayer player, boolean allMode, boolean doSearch)
    {
        boolean success = allMode ? this.insertAllItems(tileEntity, player) : this.insertCurrentItem(tileEntity, player);

        if (!success && doSearch)
        {
            PathfinderCrate pathfinder = new PathfinderCrate().init(tileEntity);

            for (TileEntity checkTile : pathfinder.iteratedNodes)
            {
                if (checkTile instanceof TileCrate)
                {
                    this.tryInsert(((TileCrate) checkTile), player, allMode, false);
                }
            }
        }
    }

    public void tryInsert(TileCrate tileEntity, EntityPlayer player, boolean allMode)
    {
        tryInsert(tileEntity, player, allMode, true);
    }

    public void tryEject(TileCrate tileEntity, EntityPlayer player, boolean allMode)
    {
        if (tileEntity.getSampleStack() == null)
        {
            return;
        }
        if (allMode && !player.isSneaking())
        {
            this.ejectItems(tileEntity, player, tileEntity.getSlotCount() * 64);
        }
        else
        {
            if (player.isSneaking())
            {
                this.ejectItems(tileEntity, player, 64);
            }
            else
            {
                this.ejectItems(tileEntity, player, tileEntity.getSampleStack().getMaxStackSize());
            }
        }
    }

    /** Inserts a the itemStack the player is holding into the crate. */
    public boolean insertCurrentItem(TileCrate tileEntity, EntityPlayer player)
    {
        ItemStack currentStack = player.getCurrentEquippedItem();

        if (currentStack != null)
        {
            if (currentStack.getItem().itemID == blockID)
            {
                ItemStack containedStack = ItemBlockCrate.getContainingItemStack(currentStack);
                ItemStack crateStack = tileEntity.getSampleStack();

                if (containedStack != null && (crateStack == null || ItemStack.areItemStacksEqual(containedStack, crateStack)))
                {
                    ItemStack returned = BlockCrate.addStackToCrate(tileEntity, containedStack);
                    ItemBlockCrate.setContainingItemStack(currentStack, returned);
                    return true;
                }
            }
            else
            {
                if (tileEntity.getSampleStack() != null)
                {
                    if (!(tileEntity.getSampleStack().isItemEqual(currentStack) || (tileEntity.oreFilterEnabled && !OreDictionary.getOreName(OreDictionary.getOreID(tileEntity.getSampleStack())).equals("Unknown") && OreDictionary.getOreID(tileEntity.getSampleStack()) == OreDictionary.getOreID(currentStack))))
                    {
                        return false;
                    }
                }

                player.inventory.setInventorySlotContents(player.inventory.currentItem, BlockCrate.addStackToCrate(tileEntity, currentStack));
                return true;
            }
        }

        return false;
    }

    /** Inserts all items of the same type this player has into the crate.
     * 
     * @return True on success */
    public boolean insertAllItems(TileCrate tileEntity, EntityPlayer player)
    {
        ItemStack requestStack = null;

        if (tileEntity.getSampleStack() != null)
        {
            requestStack = tileEntity.getSampleStack().copy();
        }

        if (requestStack == null)
        {
            requestStack = player.getCurrentEquippedItem();
        }

        if (requestStack != null && requestStack.itemID != this.blockID)
        {
            boolean success = false;

            for (int i = 0; i < player.inventory.getSizeInventory(); i++)
            {
                ItemStack currentStack = player.inventory.getStackInSlot(i);

                if (currentStack != null)
                {
                    if (requestStack.isItemEqual(currentStack))
                    {
                        player.inventory.setInventorySlotContents(i, BlockCrate.addStackToCrate(tileEntity, currentStack));

                        if (player instanceof EntityPlayerMP)
                        {
                            ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
                        }

                        success = true;
                    }
                }
            }
            return success;
        }
        return false;
    }

    /** Ejects and item out of the crate and spawn it under the player entity.
     * 
     * @param tileEntity
     * @param player
     * @param requestSize - The maximum stack size to take out. Default should be 64.
     * @return True on success */
    public boolean ejectItems(TileCrate tileEntity, EntityPlayer player, int requestSize)
    {
        World world = tileEntity.worldObj;
        if (!world.isRemote)
        {
            ItemStack sampleStack = tileEntity.getSampleStack();
            int ammountEjected = 0;
            if (sampleStack != null && requestSize > 0)
            {
                for (int slot = 0; slot < tileEntity.getInventory().getSizeInventory(); slot++)
                {
                    ItemStack slotStack = tileEntity.getInventory().getStackInSlot(slot);

                    if (slotStack != null && slotStack.stackSize > 0)
                    {
                        int amountToTake = Math.min(slotStack.stackSize, requestSize);
                        ItemStack dropStack = slotStack.copy();
                        dropStack.stackSize = amountToTake;                        

                        if (!player.inventory.addItemStackToInventory(dropStack))
                        {
                            tileEntity.getInventory().setInventorySlotContents(slot, slotStack);
                            ammountEjected += amountToTake - slotStack.stackSize;
                            break;
                        }
                        else
                        {
                            tileEntity.getInventory().setInventorySlotContents(slot, null);
                            ammountEjected += amountToTake;
                        }

                    }
                    if (ammountEjected >= requestSize)
                    {
                        return true;
                    }
                }
                player.inventory.onInventoryChanged();
                tileEntity.onInventoryChanged();
                return true;
            }
        }
        return false;
    }

    /** Puts an itemStack into the crate.
     * 
     * @param tileEntity
     * @param itemStack */
    public static ItemStack addStackToCrate(TileCrate tileEntity, ItemStack itemStack)
    {
        if (itemStack == null || itemStack.getItem().isDamageable() && itemStack.getItem().getDamage(itemStack) > 0)
        {
            return itemStack;
        }

        ItemStack containingStack = tileEntity.getSampleStack();

        if (containingStack == null || (containingStack.isItemEqual(itemStack) || (tileEntity.oreFilterEnabled && OreDictionary.getOreID(containingStack) == OreDictionary.getOreID(itemStack))))
        {
            int room = Math.max((tileEntity.getInventory().getSizeInventory() * 64) - (containingStack != null ? containingStack.stackSize : 0), 0);
            if (itemStack.stackSize <= room)
            {
                tileEntity.addToStack(itemStack);
                itemStack = null;
            }
            else
            {
                tileEntity.addToStack(itemStack, room);
                itemStack.stackSize -= room;
            }
            return itemStack;

        }

        if (itemStack.stackSize <= 0)
        {
            return null;
        }

        return itemStack;
    }

    @Override
    public int damageDropped(int metadata)
    {
        return metadata;
    }

    @Override
    public TileEntity createNewTileEntity(World var1)
    {
        return new TileCrate();
    }

    @Override
    public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        par3List.add(new ItemStack(par1, 1, 0));
        par3List.add(new ItemStack(par1, 1, 1));
        par3List.add(new ItemStack(par1, 1, 2));
    }
}
