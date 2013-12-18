package com.dark.fluid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidHandler;
import universalelectricity.core.vector.Vector3;

import com.builtbroken.common.Pair;
import com.dark.helpers.AutoCraftingManager;

public class FluidHelper
{
    public static List<Pair<Integer, Integer>> replacableBlockMeta = new ArrayList<Pair<Integer, Integer>>();
    public static List<Block> replacableBlocks = new ArrayList<Block>();
    public static List<Block> nonBlockDropList = new ArrayList<Block>();

    static
    {

        /* Adds default fluid replaceable blocks */
        replacableBlocks.add(Block.crops);
        replacableBlocks.add(Block.deadBush);
        nonBlockDropList.add(Block.deadBush);
        //TODO have waterlily raise and lower when automaticly filling or draining a block rather than remove it
        replacableBlocks.add(Block.waterlily);
        replacableBlocks.add(Block.mushroomRed);
        replacableBlocks.add(Block.mushroomBrown);
        replacableBlocks.add(Block.netherStalk);
        replacableBlocks.add(Block.sapling);
        replacableBlocks.add(Block.melonStem);
        nonBlockDropList.add(Block.melonStem);
        replacableBlocks.add(Block.pumpkinStem);
        nonBlockDropList.add(Block.pumpkinStem);
        replacableBlocks.add(Block.tallGrass);
        replacableBlocks.add(Block.torchWood);
    }

    /** Gets the block's fluid if it has one
     *
     * @param world - world we are working in
     * @param vector - 3D location in world
     * @return @Fluid that the block is */
    public static Fluid getLiquidFromBlock(World world, Vector3 vector)
    {
        return FluidHelper.getFluidFromBlockID(vector.getBlockID(world));
    }

    /** Gets a fluid from blockID */
    public static Fluid getFluidFromBlockID(int id)
    {
        if (Block.blocksList[id] instanceof IFluidBlock)
        {
            return ((IFluidBlock) Block.blocksList[id]).getFluid();
        }
        else if (id == Block.waterStill.blockID || id == Block.waterMoving.blockID)
        {
            return FluidRegistry.getFluid("water");
        }
        else if (id == Block.lavaStill.blockID || id == Block.lavaMoving.blockID)
        {
            return FluidRegistry.getFluid("lava");
        }
        return null;
    }

    public static FluidStack getStack(FluidStack stack, int amount)
    {
        if (stack != null)
        {
            FluidStack newStack = stack.copy();
            newStack.amount = amount;
            return newStack;
        }
        return stack;
    }

    /** Drains a block of fluid
     *
     * @Note sets the block with a client update only. Doesn't tick the block allowing for better
     * placement of fluid that can flow infinitely
     *
     * @param doDrain - do the action
     * @return FluidStack drained from the block */
    public static FluidStack drainBlock(World world, Vector3 node, boolean doDrain)
    {
        return drainBlock(world, node, doDrain, 2);
    }

    /** Drains a block of fluid
     *
     * @param doDrain - do the action
     * @param update - block update flag to use
     * @return FluidStack drained from the block */
    public static FluidStack drainBlock(World world, Vector3 node, boolean doDrain, int update)
    {
        if (world == null || node == null)
        {
            return null;
        }

        int blockID = node.getBlockID(world);
        int meta = node.getBlockMetadata(world);
        Block block = Block.blocksList[blockID];
        if (block != null)
        {
            if (block instanceof IFluidBlock && ((IFluidBlock) block).canDrain(world, node.intX(), node.intY(), node.intZ()))
            {
                return ((IFluidBlock) block).drain(world, node.intX(), node.intY(), node.intZ(), doDrain);
            }
            else if ((block.blockID == Block.waterStill.blockID || block.blockID == Block.waterMoving.blockID) && node.getBlockMetadata(world) == 0)
            {
                if (doDrain)
                {
                    Vector3 vec = node.clone().modifyPositionFromSide(ForgeDirection.UP);
                    if (vec.getBlockID(world) == Block.waterlily.blockID)
                    {
                        vec.setBlock(world, 0, 0, update);
                        node.setBlock(world, blockID, meta);
                    }
                    else
                    {
                        node.setBlock(world, 0, 0, update);
                    }
                }
                return new FluidStack(FluidRegistry.getFluid("water"), FluidContainerRegistry.BUCKET_VOLUME);
            }
            else if ((block.blockID == Block.lavaStill.blockID || block.blockID == Block.lavaMoving.blockID) && node.getBlockMetadata(world) == 0)
            {
                if (doDrain)
                {
                    node.setBlock(world, 0, 0, update);
                }
                return new FluidStack(FluidRegistry.getFluid("lava"), FluidContainerRegistry.BUCKET_VOLUME);
            }
        }
        return null;
    }

    /** Checks to see if a non-fluid block is able to be filled with fluid */
    public static boolean isFillableBlock(World world, Vector3 node)
    {
        if (world == null || node == null)
        {
            return false;
        }

        int blockID = node.getBlockID(world);
        int meta = node.getBlockMetadata(world);
        Block block = Block.blocksList[blockID];
        if (drainBlock(world, node, false) != null)
        {
            return false;
        }
        else if (block == null || block.blockID == 0 || block.isAirBlock(world, node.intX(), node.intY(), node.intZ()))
        {
            return true;
        }
        else if (!(block instanceof IFluidBlock || block instanceof BlockFluid) && block.isBlockReplaceable(world, node.intX(), node.intY(), node.intZ()) || replacableBlockMeta.contains(new Pair<Integer, Integer>(blockID, meta)) || replacableBlocks.contains(block))
        {
            return true;
        }
        return false;
    }

    /** Checks to see if a fluid related block is able to be filled */
    public static boolean isFillableFluid(World world, Vector3 node)
    {
        if (world == null || node == null)
        {
            return false;
        }

        int blockID = node.getBlockID(world);
        int meta = node.getBlockMetadata(world);
        Block block = Block.blocksList[blockID];
        //TODO when added change this to call canFill and fill
        if (drainBlock(world, node, false) != null)
        {
            return false;
        }
        else if (block instanceof IFluidBlock || block instanceof BlockFluid)
        {
            return meta != 0;
        }
        return false;
    }

    /** Helper method to fill a location with a fluid
     *
     * Note: This does not update the block to prevent the liquid from flowing
     *
     * @return */
    public static int fillBlock(World world, Vector3 node, FluidStack stack, boolean doFill)
    {
        if ((isFillableBlock(world, node) || isFillableFluid(world, node)) && stack != null && stack.amount >= FluidContainerRegistry.BUCKET_VOLUME)
        {
            if (doFill)
            {
                int blockID = node.getBlockID(world);
                int meta = node.getBlockMetadata(world);
                Block block = Block.blocksList[blockID];
                Vector3 vec = node.clone().modifyPositionFromSide(ForgeDirection.UP);

                if (block != null)
                {
                    if (block.blockID == Block.waterlily.blockID && vec.getBlockID(world) == 0)
                    {
                        vec.setBlock(world, blockID, meta);
                    }
                    else if (block != null && replacableBlocks.contains(block) && !nonBlockDropList.contains(block))
                    {
                        block.dropBlockAsItem(world, node.intX(), node.intY(), node.intZ(), meta, 1);
                        block.breakBlock(world, node.intX(), node.intY(), node.intZ(), blockID, meta);
                    }
                }

                node.setBlock(world, stack.getFluid().getBlockID());
            }
            return FluidContainerRegistry.BUCKET_VOLUME;
        }
        return 0;
    }

    /** Fills all instances of IFluidHandler surrounding the origin
     *
     * @param stack - FluidStack that will be filled into the tanks
     * @param doFill - Actually perform the action or simulate action
     * @param ignore - ForgeDirections to ignore
     * @return amount of fluid that was used from the stack */
    public static int fillTanksAllSides(World world, Vector3 origin, FluidStack stack, boolean doFill, ForgeDirection... ignore)
    {
        int filled = 0;
        FluidStack fillStack = stack != null ? stack.copy() : null;
        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
        {
            if (fillStack == null || fillStack.amount <= 0)
            {
                return filled;
            }
            if (ignore != null)
            {
                for (int i = 0; i < ignore.length; i++)
                {
                    if (direction == ignore[i])
                    {
                        continue;
                    }
                }
            }
            filled += fillTankSide(world, origin, stack, doFill, direction);
            fillStack = getStack(stack, stack.amount - filled);

        }
        return filled;
    }

    /** Fills an instance of IFluidHandler in the given direction
     *
     * @param stack - FluidStack to fill the tank will
     * @param doFill - Actually perform the action or simulate action
     * @param direction - direction to fill in from the origin
     * @return amount of fluid that was used from the stack */
    public static int fillTankSide(World world, Vector3 origin, FluidStack stack, boolean doFill, ForgeDirection direction)
    {
        TileEntity entity = origin.clone().modifyPositionFromSide(direction).getTileEntity(world);
        if (entity instanceof IFluidHandler && ((IFluidHandler) entity).canFill(direction.getOpposite(), stack.getFluid()))
        {
            return ((IFluidHandler) entity).fill(direction.getOpposite(), stack, doFill);
        }
        return 0;
    }

    /** Does all the work needed to fill or drain an item of fluid when a player clicks on the block. */
    public static boolean playerActivatedFluidItem(World world, int x, int y, int z, EntityPlayer entityplayer, int side)
    {
        //TODO add double click support similar to the crates in assembly line
        ItemStack current = entityplayer.inventory.getCurrentItem();
        if (current != null && world.getBlockTileEntity(x, y, z) instanceof IFluidHandler)
        {

            FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(current);

            IFluidHandler tank = (IFluidHandler) world.getBlockTileEntity(x, y, z);

            if (liquid != null)
            {
                if (tank.fill(ForgeDirection.UNKNOWN, liquid, true) != 0 && !entityplayer.capabilities.isCreativeMode)
                {
                    entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, AutoCraftingManager.consumeItem(current, 1));
                }

                return true;
            }
            else
            {

                FluidStack available = tank.drain(ForgeDirection.getOrientation(side), Integer.MAX_VALUE, false);
                if (available != null)
                {
                    ItemStack filled = FluidContainerRegistry.fillFluidContainer(available, current);

                    liquid = FluidContainerRegistry.getFluidForFilledItem(filled);

                    if (liquid != null)
                    {
                        if (!entityplayer.capabilities.isCreativeMode)
                        {
                            if (current.stackSize > 1)
                            {
                                if (!entityplayer.inventory.addItemStackToInventory(filled))
                                {
                                    return false;
                                }
                                else
                                {
                                    entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, AutoCraftingManager.consumeItem(current, 1));
                                }
                            }
                            else
                            {
                                entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, AutoCraftingManager.consumeItem(current, 1));
                                entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, filled);
                            }
                        }
                        tank.drain(ForgeDirection.UNKNOWN, liquid.amount, true);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /** Drains an item of fluid and fills the tank with what was drained
     *
     * @param consumeItem - should it consume the item. Used mainly for creative mode players. This
     * does effect the return of the method
     * @return Item stack that would be returned if the item was drain of its fluid. Water bucket ->
     * empty bucket */
    public static ItemStack drainItem(ItemStack stack, IFluidHandler tank, ForgeDirection side)
    {
        if (stack != null && tank != null)
        {
            FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(stack);
            if (liquid != null)
            {
                if (tank.fill(side, liquid, true) > 0)
                {
                    return stack.getItem().getContainerItemStack(stack);
                }
            }
        }
        return stack;
    }

    /** Fills an item with fluid from the tank
     *
     * @param consumeItem - should it consume the item. Used mainly for creative mode players. This
     * does effect the return of the method
     * @return Item stack that would be returned if the item was filled with fluid. empty bucket ->
     * water bucket */
    public static ItemStack fillItem(ItemStack stack, IFluidHandler tank, ForgeDirection side)
    {
        if (stack != null && tank != null)
        {
            FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(stack);
            FluidStack drainStack = tank.drain(side, Integer.MAX_VALUE, false);
            if (liquid == null && drainStack != null)
            {
                ItemStack liquidItem = FluidContainerRegistry.fillFluidContainer(drainStack, stack);
                if (tank.drain(side, FluidContainerRegistry.getFluidForFilledItem(liquidItem), true) != null)
                {
                    return liquidItem;
                }
            }
        }
        return stack;
    }

    /** Builds a list of fluidStacks from FluidTankInfo general taken from an instanceof
     * IFluidHandler */
    public static List<FluidStack> getFluidList(FluidTankInfo... fluidTankInfos)
    {
        List<FluidStack> stackList = new ArrayList<FluidStack>();
        HashMap<FluidStack, Integer> map = new HashMap<FluidStack, Integer>();
        if (fluidTankInfos != null)
        {
            for (int i = 0; i < fluidTankInfos.length; i++)
            {
                FluidTankInfo info = fluidTankInfos[i];
                if (info != null && info.fluid != null)
                {
                    FluidStack stack = info.fluid;
                    if (map.containsKey(FluidHelper.getStack(stack, 0)))
                    {
                        map.put(FluidHelper.getStack(stack, 0), map.get(FluidHelper.getStack(stack, 0)) + stack.amount);
                    }
                    else
                    {
                        map.put(FluidHelper.getStack(stack, 0), stack.amount);
                    }
                }
            }
            Iterator<Entry<FluidStack, Integer>> it = map.entrySet().iterator();
            while (it.hasNext())
            {
                Entry<FluidStack, Integer> entry = it.next();
                stackList.add(FluidHelper.getStack(entry.getKey(), entry.getValue()));
            }
        }
        return stackList;

    }
}
