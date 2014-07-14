package resonantinduction.archaic.engineering;

import java.util.ArrayList;
import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

import resonant.api.IRotatable;
import resonant.api.ISlotPickResult;
import resonant.api.recipe.MachineRecipes;
import resonant.api.recipe.RecipeResource;
import resonant.lib.content.module.TileRender;
import resonant.lib.content.module.prefab.TileInventory;
import resonant.lib.gui.ContainerDummy;
import resonant.lib.network.IPacketReceiver;
import resonant.lib.network.PacketHandler;
import resonant.lib.prefab.item.ItemBlockSaved;
import resonant.lib.prefab.vector.Cuboid;
import resonant.lib.render.RenderItemOverlayUtility;
import resonant.lib.type.Pair;
import resonant.lib.utility.LanguageUtility;
import resonant.lib.utility.WorldUtility;
import resonant.lib.utility.inventory.AutoCraftingManager;
import resonant.lib.utility.inventory.AutoCraftingManager.IAutoCrafter;
import resonant.lib.utility.inventory.InventoryUtility;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.ResonantInduction.RecipeType;
import resonantinduction.core.prefab.imprint.ItemImprint;
import universalelectricity.api.vector.Vector2;
import universalelectricity.api.vector.Vector3;
import codechicken.multipart.ControlKeyModifer;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Advanced crafting table that stores its crafting grid, can craft out of the player's inv, and be
 * configed to auto craft.
 * 
 * @author DarkGuardsman, Calclavia */
public class TileEngineeringTable extends TileInventory implements IPacketReceiver, IRotatable, ISidedInventory, ISlotPickResult, IAutoCrafter
{
    public static final int CRAFTING_MATRIX_END = 9;
    public static final int CRAFTING_OUTPUT_END = CRAFTING_MATRIX_END + 1;
    public static final int PLAYER_OUTPUT_END = CRAFTING_OUTPUT_END + 40;
    public static final int CENTER_SLOT = 4;

    // Relative slot IDs
    public static final int CRAFTING_OUTPUT_SLOT = 0;

    private AutoCraftingManager craftManager;

    /** 9 slots for crafting, 1 slot for a output. */
    public static final int CRAFTING_MATRIX_SIZE = 9;
    public ItemStack[] craftingMatrix = new ItemStack[CRAFTING_MATRIX_SIZE];
    public static final int[] craftingSlots = { 0, 1, 2, 3, 4, 5, 6, 7, 8 };

    /** The output inventory containing slots. */
    public ItemStack[] outputInventory = new ItemStack[1];

    /** The ability for the engineering table to search nearby inventories. */
    public boolean searchInventories = true;

    /** Temporary player inventory stored to draw the player's items. */
    private InventoryPlayer invPlayer = null;
    private int[] playerSlots;

    @SideOnly(Side.CLIENT)
    private static Icon iconTop, iconFront, iconSide;

    public TileEngineeringTable()
    {
        super(Material.wood);
        bounds = new Cuboid(0, 0, 0, 1, 0.9f, 1);
        isOpaqueCube = false;
        normalRender = false;
        itemBlock = ItemBlockSaved.class;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int meta)
    {
        return side == 1 ? iconTop : (side == meta ? iconFront : iconSide);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister iconRegister)
    {
        iconTop = iconRegister.registerIcon(getTextureName() + "_top");
        iconFront = iconRegister.registerIcon(getTextureName() + "_front");
        iconSide = iconRegister.registerIcon(getTextureName() + "_side");
    }

    @Override
    public void click(EntityPlayer player)
    {
        if (!world().isRemote && ControlKeyModifer.isControlDown(player))
        {
            // Don't drop the output, so subtract by one.
            for (int i = 0; i < getSizeInventory() - 1; ++i)
            {
                if (getStackInSlot(i) != null)
                {
                    InventoryUtility.dropItemStack(world(), new Vector3(player), getStackInSlot(i));
                    setInventorySlotContents(i, null);
                }
            }
        }
    }

    @Override
    protected boolean use(EntityPlayer player, int hitSide, Vector3 hit)
    {
        if (player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof ItemHammer)
        {
            for (int slot = 0; slot < TileEngineeringTable.CRAFTING_OUTPUT_END; slot++)
            {
                ItemStack inputStack = getStackInSlot(slot);

                if (inputStack != null)
                {
                    String oreName = OreDictionary.getOreName(OreDictionary.getOreID(inputStack));

                    if (oreName != null && !oreName.equals("Unknown"))
                    {
                        RecipeResource[] outputs = MachineRecipes.INSTANCE.getOutput(RecipeType.CRUSHER.name(), oreName);

                        if (outputs != null && outputs.length > 0)
                        {
                            if (!world().isRemote && world().rand.nextFloat() < 0.2)
                            {
                                for (RecipeResource resource : outputs)
                                {
                                    ItemStack outputStack = resource.getItemStack().copy();

                                    if (outputStack != null)
                                    {
                                        InventoryUtility.dropItemStack(world(), new Vector3(player), outputStack, 0);
                                        setInventorySlotContents(slot, --inputStack.stackSize <= 0 ? null : inputStack);
                                    }
                                }
                            }
                            
                            ResonantInduction.proxy.renderBlockParticle(world(), new Vector3(x() + 0.5, y() + 0.5, z() + 0.5), new Vector3((Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3), inputStack.itemID, 1);
                            world().playSoundEffect(x() + 0.5, y() + 0.5, z() + 0.5, Reference.PREFIX + "hammer", 0.5f, 0.8f + (0.2f * world().rand.nextFloat()));
                            player.addExhaustion(0.1f);
                            player.getCurrentEquippedItem().damageItem(1, player);
                            return true;
                        }
                    }
                }
            }
            return true;
        }

        if (hitSide == 1)
        {
            if (!world().isRemote)
            {
                Vector3 hitVector = new Vector3(hit.x, 0, hit.z);
                final double regionLength = 1d / 3d;

                // Rotate the hit vector based on direction of the tile.
                hitVector.translate(new Vector3(-0.5, 0, -0.5));
                hitVector.rotate(WorldUtility.getAngleFromForgeDirection(getDirection()), Vector3.UP());
                hitVector.translate(new Vector3(0.5, 0, 0.5));

                /** Crafting Matrix */
                matrix:
                for (int j = 0; j < 3; j++)
                {
                    for (int k = 0; k < 3; k++)
                    {
                        Vector2 check = new Vector2(j, k).scale(regionLength);

                        if (check.distance(hitVector.toVector2()) < regionLength)
                        {
                            int slotID = j * 3 + k;
                            interactCurrentItem(this, slotID, player);
                            break matrix;
                        }
                    }
                }

                onInventoryChanged();
            }

            return true;
        }
        else if (hitSide != 0)
        {
            /** Take out of engineering table. */
            if (!world().isRemote)
            {
                setPlayerInventory(player.inventory);

                ItemStack output = getStackInSlot(9);
                boolean firstLoop = true;

                while (output != null && (firstLoop || ControlKeyModifer.isControlDown(player)))
                {
                    onPickUpFromSlot(player, 9, output);

                    if (output.stackSize > 0)
                    {
                        InventoryUtility.dropItemStack(world(), new Vector3(player), output, 0);
                    }

                    setInventorySlotContents(9, null);
                    onInventoryChanged();

                    output = getStackInSlot(9);
                    firstLoop = false;
                }

                setPlayerInventory(null);
            }

            return true;

        }
        return false;
    }

    @Override
    protected boolean configure(EntityPlayer player, int side, Vector3 hit)
    {
        if (player.isSneaking())
        {
            searchInventories = !searchInventories;
            if (!world().isRemote)
            {
                if (searchInventories)
                    player.addChatMessage(LanguageUtility.getLocal("engineerTable.config.inventory.true"));
                else
                    player.addChatMessage(LanguageUtility.getLocal("engineerTable.config.inventory.false"));
            }

            markUpdate();
            return true;
        }

        return super.configure(player, side, hit);
    }

    @Override
    public ArrayList<ItemStack> getDrops(int metadata, int fortune)
    {
        return new ArrayList<ItemStack>();
    }

    @Override
    public void onRemove(int par5, int par6)
    {
        ItemStack stack = ItemBlockSaved.getItemStackWithNBT(this.getBlockType(), world(), x(), y(), z());
        InventoryUtility.dropItemStack(world(), center(), stack);
    }

    /** Creates a "fake inventory" and hook the player up to the crafter to use the player's items. */
    public void setPlayerInventory(InventoryPlayer invPlayer)
    {
        if (searchInventories)
        {
            if (invPlayer != null)
            {
                playerSlots = new int[invPlayer.getSizeInventory()];
                for (int i = 0; i < playerSlots.length; i++)
                    playerSlots[i] = i + CRAFTING_OUTPUT_END;
            }
            else
            {
                playerSlots = null;
            }

            this.invPlayer = invPlayer;
        }
    }

    @Override
    public boolean canUpdate()
    {
        return false;
    }

    /** Gets the AutoCraftingManager that does all the crafting results */
    public AutoCraftingManager getCraftingManager()
    {
        if (craftManager == null)
        {
            craftManager = new AutoCraftingManager(this);
        }
        return craftManager;
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        return ResonantInduction.PACKET_TILE.getPacket(this, nbt);
    }

    @Override
    public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
    {
        try
        {
            readFromNBT(PacketHandler.readNBTTagCompound(data));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public int getSizeInventory()
    {
        return 10 + (this.invPlayer != null ? this.invPlayer.getSizeInventory() : 0);
    }

    /** DO NOT USE THIS INTERNALLY. FOR EXTERNAL USE ONLY! */
    @Override
    public ItemStack getStackInSlot(int slot)
    {
        if (slot < CRAFTING_MATRIX_END)
        {
            return this.craftingMatrix[slot];
        }
        else if (slot < CRAFTING_OUTPUT_END)
        {
            return outputInventory[slot - CRAFTING_MATRIX_END];
        }
        else if (slot < PLAYER_OUTPUT_END && invPlayer != null)
        {
            return this.invPlayer.getStackInSlot(slot - CRAFTING_OUTPUT_END);
        }
        else if (searchInventories)
        {
            int idDisplacement = PLAYER_OUTPUT_END;

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            {
                TileEntity tile = new Vector3(this).translate(dir).getTileEntity(worldObj);

                if (tile instanceof IInventory)
                {
                    IInventory inventory = (IInventory) tile;
                    int slotID = slot - idDisplacement;

                    if (slotID >= 0 && slotID < inventory.getSizeInventory())
                        return inventory.getStackInSlot(slotID);

                    idDisplacement += inventory.getSizeInventory();
                }
            }
        }

        return null;
    }

    @Override
    public ItemStack decrStackSize(int i, int amount)
    {
        if (getStackInSlot(i) != null)
        {
            ItemStack stack;

            if (getStackInSlot(i).stackSize <= amount)
            {
                stack = getStackInSlot(i);
                setInventorySlotContents(i, null);
                return stack;
            }
            else
            {
                stack = getStackInSlot(i).splitStack(amount);

                if (getStackInSlot(i).stackSize == 0)
                {
                    setInventorySlotContents(i, null);
                }

                return stack;
            }
        }
        else
        {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack itemStack)
    {
        if (slot < CRAFTING_MATRIX_END)
        {
            craftingMatrix[slot] = itemStack;
        }
        else if (slot < CRAFTING_OUTPUT_END)
        {
            outputInventory[slot - CRAFTING_MATRIX_END] = itemStack;
        }
        else if (slot < PLAYER_OUTPUT_END && this.invPlayer != null)
        {
            this.invPlayer.setInventorySlotContents(slot - CRAFTING_OUTPUT_END, itemStack);
            EntityPlayer player = this.invPlayer.player;

            if (player instanceof EntityPlayerMP)
            {
                ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
            }
        }
        else if (searchInventories)
        {
            int idDisplacement = PLAYER_OUTPUT_END;

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            {
                TileEntity tile = new Vector3(this).translate(dir).getTileEntity(worldObj);

                if (tile instanceof IInventory)
                {
                    IInventory inventory = (IInventory) tile;
                    int slotID = slot - idDisplacement;

                    if (slotID >= 0 && slotID < inventory.getSizeInventory())
                        inventory.setInventorySlotContents(slotID, itemStack);

                    idDisplacement += inventory.getSizeInventory();
                }
            }
        }

        onInventoryChanged();
    }

    /** When some containers are closed they call this on each slot, then drop whatever it returns as
     * an EntityItem - like when you close a workbench GUI. */
    @Override
    public ItemStack getStackInSlotOnClosing(int slot)
    {
        if (this.getStackInSlot(slot) != null)
        {
            ItemStack var2 = this.getStackInSlot(slot);
            this.setInventorySlotContents(slot, null);
            return var2;
        }
        else
        {
            return null;
        }
    }

    @Override
    public String getInvName()
    {
        return this.getBlockType().getLocalizedName();
    }

    @Override
    public void openChest()
    {
        this.onInventoryChanged();
    }

    @Override
    public void closeChest()
    {
        this.onInventoryChanged();
    }

    /** Construct an InventoryCrafting Matrix on the fly.
     * 
     * @return */
    public InventoryCrafting getCraftingMatrix()
    {
        InventoryCrafting inventoryCrafting = new InventoryCrafting(new ContainerDummy(this), 3, 3);

        for (int i = 0; i < this.craftingMatrix.length; i++)
        {
            inventoryCrafting.setInventorySlotContents(i, this.craftingMatrix[i]);
        }

        return inventoryCrafting;
    }

    /** Updates all the output slots. Call this to update the Engineering Table. */
    @Override
    public void onInventoryChanged()
    {
        if (worldObj != null)
        {
            if (!worldObj.isRemote)
            {
                this.outputInventory[CRAFTING_OUTPUT_SLOT] = null;

                /** Try to craft from crafting grid. If not possible, then craft from imprint. */
                boolean didCraft = false;

                /** Simulate an Inventory Crafting Instance */
                InventoryCrafting inventoryCrafting = this.getCraftingMatrix();

                ItemStack matrixOutput = CraftingManager.getInstance().findMatchingRecipe(inventoryCrafting, this.worldObj);

                if (matrixOutput != null && this.getCraftingManager().getIdealRecipe(matrixOutput) != null)
                {
                    this.outputInventory[CRAFTING_OUTPUT_SLOT] = matrixOutput;
                    didCraft = true;
                }

                /** If output does not exist, try using the filter. */
                if (!didCraft)
                {
                    ItemStack filterStack = craftingMatrix[CENTER_SLOT];

                    if (filterStack != null && filterStack.getItem() instanceof ItemImprint)
                    {
                        Set<ItemStack> filters = ItemImprint.getFilters(filterStack);

                        for (ItemStack outputStack : filters)
                        {
                            if (outputStack != null)
                            {
                                Pair<ItemStack, ItemStack[]> idealRecipe = this.getCraftingManager().getIdealRecipe(outputStack);

                                if (idealRecipe != null)
                                {
                                    ItemStack recipeOutput = idealRecipe.left();
                                    if (recipeOutput != null & recipeOutput.stackSize > 0)
                                    {
                                        this.outputInventory[CRAFTING_OUTPUT_SLOT] = recipeOutput;
                                        didCraft = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }
        }
    }

    @Override
    public void onPickUpFromSlot(EntityPlayer entityPlayer, int slotID, ItemStack itemStack)
    {
        if (!worldObj.isRemote)
        {
            if (itemStack != null)
            {
                Pair<ItemStack, ItemStack[]> idealRecipeItem = getCraftingManager().getIdealRecipe(itemStack);

                if (idealRecipeItem != null)
                {
                    getCraftingManager().consumeItems(idealRecipeItem.right().clone());
                }
                else
                {
                    itemStack.stackSize = 0;
                }
            }
        }
    }

    // ///////////////////////////////////////
    // // Save And Data processing //////
    // ///////////////////////////////////////
    /** NBT Data */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        NBTTagList nbtList = nbt.getTagList("Items");
        this.craftingMatrix = new ItemStack[9];
        this.outputInventory = new ItemStack[1];

        for (int i = 0; i < nbtList.tagCount(); ++i)
        {
            NBTTagCompound stackTag = (NBTTagCompound) nbtList.tagAt(i);
            byte id = stackTag.getByte("Slot");

            if (id >= 0 && id < this.getSizeInventory())
            {
                this.setInventorySlotContents(id, ItemStack.loadItemStackFromNBT(stackTag));
            }
        }

        this.searchInventories = nbt.getBoolean("searchInventories");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        NBTTagList nbtList = new NBTTagList();

        for (int i = 0; i < this.getSizeInventory(); ++i)
        {
            if (this.getStackInSlot(i) != null)
            {
                NBTTagCompound var4 = new NBTTagCompound();
                var4.setByte("Slot", (byte) i);
                this.getStackInSlot(i).writeToNBT(var4);
                nbtList.appendTag(var4);
            }
        }

        nbt.setTag("Items", nbtList);
        nbt.setBoolean("searchInventories", this.searchInventories);
    }

    // ///////////////////////////////////////
    // // Inventory Access side Methods //////
    // ///////////////////////////////////////

    @Override
    public boolean isInvNameLocalized()
    {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        return true;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer)
    {
        return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : entityplayer.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;

    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        return new int[0];
    }

    /** Auto-crafting methods. */
    @Override
    public boolean canInsertItem(int slot, ItemStack itemstack, int side)
    {
        if (getStackInSlot(4) != null && getStackInSlot(4).getItem() instanceof ItemImprint)
            return true;

        int minSize = 64;
        int optimalSlot = -1;

        for (int i = 0; i < craftingMatrix.length; i++)
        {
            ItemStack checkStack = getStackInSlot(i);

            if (checkStack != null && checkStack.isItemEqual(itemstack))
            {
                if (checkStack.stackSize < minSize || optimalSlot < 0)
                {
                    optimalSlot = i;
                    minSize = checkStack.stackSize;
                }
            }
        }

        return slot == optimalSlot;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemstack, int side)
    {
        ItemStack outputStack = getStackInSlot(CRAFTING_MATRIX_END);

        if (outputStack != null)
        {
            /** Only allow take out crafting result when it can be crafted twice! */
            Pair<ItemStack, ItemStack[]> idealRecipeItem = this.getCraftingManager().getIdealRecipe(outputStack);
            ItemStack[] doubleResults = ArrayUtils.addAll(idealRecipeItem.right(), idealRecipeItem.right());

            if (!getCraftingManager().consumeItems(false, doubleResults))
            {
                return false;
            }
        }

        return slot == CRAFTING_MATRIX_END;
    }

    @Override
    public int[] getCraftingInv()
    {
        int[] slots = craftingSlots;

        if (playerSlots != null)
        {
            slots = ArrayUtils.addAll(playerSlots, slots);
        }

        if (searchInventories)
        {
            int temporaryInvID = PLAYER_OUTPUT_END;

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            {
                TileEntity tile = new Vector3(this).translate(dir).getTileEntity(worldObj);

                if (tile instanceof IInventory)
                {
                    IInventory inventory = (IInventory) tile;
                    int[] nearbySlots = new int[inventory.getSizeInventory()];

                    for (int i = 0; i < inventory.getSizeInventory(); i++)
                    {
                        nearbySlots[i] = temporaryInvID++;
                    }

                    slots = ArrayUtils.addAll(nearbySlots, slots);
                }
            }
        }

        return slots;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected TileRender newRenderer()
    {
        return new TileRender()
        {
            @Override
            public boolean renderDynamic(Vector3 position, boolean isItem, float frame)
            {
                if (!isItem)
                {
                    GL11.glPushMatrix();
                    RenderItemOverlayUtility.renderItemOnSides(TileEngineeringTable.this, getStackInSlot(9), position.x, position.y, position.z);
                    RenderItemOverlayUtility.renderTopOverlay(TileEngineeringTable.this, craftingMatrix, getDirection(), position.x, position.y - 0.1, position.z);
                    GL11.glPopMatrix();
                }

                return false;
            }
        };
    }
}
