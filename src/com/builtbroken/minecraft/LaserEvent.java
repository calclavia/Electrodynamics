package com.builtbroken.minecraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.Event;
import universalelectricity.core.vector.Vector3;

import com.builtbroken.minecraft.helpers.DarksHelper;

/** An event triggered by entities or tiles that create lasers
 * 
 * @author DarkGuardsman */
public class LaserEvent extends Event
{
    public World world;
    public Vector3 spot;
    public Vector3 target;

    public LaserEvent(World world, Vector3 spot, Vector3 target)
    {
        this.world = world;
        this.spot = spot;
        this.target = target;
    }

    /** Called when a laser is fired */
    @Cancelable
    public static class LaserFireEvent extends LaserEvent
    {
        public Object shooter;

        public LaserFireEvent(World world, Vector3 spot, Vector3 target, Object shooter)
        {
            super(world, spot, target);
            this.shooter = shooter;
        }

        public LaserFireEvent(TileEntity tileEntity, MovingObjectPosition hit)
        {
            super(tileEntity.worldObj, new Vector3(tileEntity), new Vector3(hit));
            this.shooter = tileEntity;
        }
    }

    /** Called when a player fires a laser. Use this to cancel a laser hit event */
    @Cancelable
    public static class LaserFiredPlayerEvent extends LaserFireEvent
    {
        public ItemStack laserItem;
        public MovingObjectPosition hit;

        public LaserFiredPlayerEvent(EntityPlayer player, MovingObjectPosition hit, ItemStack stack)
        {
            super(player.worldObj, new Vector3(player), new Vector3(hit), player);
            this.laserItem = stack;
            this.hit = hit;
        }
    }

    /** Called when a laser is heating up a block to be mined */
    public static class LaserMeltBlockEvent extends LaserEvent
    {
        public Object shooter;

        public LaserMeltBlockEvent(World world, Vector3 spot, Vector3 hit, Object shooter)
        {
            super(world, spot, hit);
            this.shooter = shooter;
        }
    }

    /** Use this to change what drops when the laser finishes mining a block */
    public static class LaserDropItemEvent extends LaserEvent
    {
        public List<ItemStack> items;

        public LaserDropItemEvent(World world, Vector3 spot, Vector3 hit, List<ItemStack> items)
        {
            super(world, spot, hit);
            this.items = items;
        }
    }

    /** Called before a laser mines a block */
    @Cancelable
    public static class LaserMineBlockEvent extends LaserEvent
    {
        public Object shooter;

        public LaserMineBlockEvent(World world, Vector3 spot, Vector3 hit, Object shooter)
        {
            super(world, spot, hit);
            this.shooter = shooter;
        }

    }

    public static boolean doLaserHarvestCheck(World world, Vector3 pos, Object player, Vector3 hit)
    {
        LaserEvent event = new LaserMineBlockEvent(world, pos, hit, player);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    /** Called while the block is being mined */
    public static void onLaserHitBlock(World world, Object player, Vector3 vec, ForgeDirection side)
    {
        int id = vec.getBlockID(world);
        int meta = vec.getBlockID(world);
        Block block = Block.blocksList[id];

        Vector3 faceVec = vec.clone().modifyPositionFromSide(side);
        int id2 = faceVec.getBlockID(world);
        Block block2 = Block.blocksList[id2];

        Vector3 start = null;

        if (player instanceof Entity)
        {
            start = new Vector3((Entity) player);
        }
        else if (player instanceof TileEntity)
        {
            start = new Vector3((TileEntity) player);
        }
        if (block != null)
        {
            float chance = world.rand.nextFloat();

            int fireChance = block.getFlammability(world, vec.intX(), vec.intY(), vec.intZ(), meta, side);
            if ((fireChance / 300) >= chance && (block2 == null || block2.isAirBlock(world, vec.intX(), vec.intY(), vec.intZ())))
            {
                world.setBlock(vec.intX(), vec.intY(), vec.intZ(), Block.fire.blockID, 0, 3);
                return;
            }
            if (block.blockID == Block.grass.blockID && (block2 == null || block2.isAirBlock(world, vec.intX(), vec.intY() + 1, vec.intZ())))
            {
                world.setBlock(vec.intX(), vec.intY() + 1, vec.intZ(), Block.fire.blockID, 0, 3);
                world.setBlock(vec.intX(), vec.intY(), vec.intZ(), Block.dirt.blockID, 0, 3);
                return;
            }
            if (chance > 0.8f)
            {
                //TODO turn water into steam
                if (block.blockID == Block.sand.blockID)
                {
                    world.setBlock(vec.intX(), vec.intY(), vec.intZ(), Block.glass.blockID, 0, 3);
                    return;
                }
                else if (block.blockID == Block.cobblestone.blockID)
                {
                    world.setBlock(vec.intX(), vec.intY(), vec.intZ(), 1, 0, 3);
                    return;
                }
                else if (block.blockID == Block.ice.blockID)
                {
                    world.setBlock(vec.intX(), vec.intY(), vec.intZ(), Block.waterStill.blockID, 15, 3);
                    return;
                }
                else if (block.blockID == Block.obsidian.blockID)
                {
                    world.setBlock(vec.intX(), vec.intY(), vec.intZ(), Block.lavaStill.blockID, 15, 3);
                    return;
                }
            }
            MinecraftForge.EVENT_BUS.post(new LaserEvent.LaserMeltBlockEvent(world, start, vec, player));
        }
    }

    /** Called when the block is actually mined */
    public static void onBlockMinedByLaser(World world, Object player, Vector3 vec)
    {
        int id = vec.getBlockID(world);
        int meta = vec.getBlockID(world);
        Block block = Block.blocksList[id];

        Vector3 start = null;
        if (player instanceof Entity)
        {
            start = new Vector3((Entity) player);
        }
        else if (player instanceof TileEntity)
        {
            start = new Vector3((TileEntity) player);
        }

        //TODO make this use or call to the correct methods, and events so it can be canceled
        if (block != null && block.getBlockHardness(world, vec.intX(), vec.intY(), vec.intZ()) >= 0 && doLaserHarvestCheck(world, start, player, vec))
        {
            try
            {

                Block blockBellow = Block.blocksList[vec.clone().modifyPositionFromSide(ForgeDirection.DOWN).getBlockID(world)];
                if (block != null)
                {
                    if (block.blockID == Block.tnt.blockID)
                    {
                        world.setBlock(vec.intX(), vec.intY(), vec.intZ(), 0, 0, 3);
                        EntityTNTPrimed entitytntprimed = new EntityTNTPrimed(world, (vec.intX() + 0.5F), (vec.intY() + 0.5F), (vec.intZ() + 0.5F), player instanceof EntityLivingBase ? ((EntityLivingBase) player) : null);
                        entitytntprimed.fuse = world.rand.nextInt(entitytntprimed.fuse / 4) + entitytntprimed.fuse / 8;
                        world.spawnEntityInWorld(entitytntprimed);
                        return;
                    }
                    if (block.blockMaterial == Material.wood || block.blockMaterial == Material.plants || block.blockMaterial == Material.vine || block.blockMaterial == Material.plants || block.blockMaterial == Material.pumpkin || block.blockMaterial == Material.cloth || block.blockMaterial == Material.web)
                    {
                        if (blockBellow != null && blockBellow.blockID == Block.tilledField.blockID && block instanceof IPlantable)
                        {
                            vec.clone().translate(new Vector3(0, -1, 0)).setBlock(world, Block.dirt.blockID, 0, 3);
                        }
                        vec.setBlock(world, Block.fire.blockID, 0, 3);
                        return;
                    }
                    List<ItemStack> items = block.getBlockDropped(world, vec.intX(), vec.intY(), vec.intZ(), meta, 1);
                    if (items == null)
                    {
                        items = new ArrayList<ItemStack>();
                    }
                    //TODO have glass refract the laser causing it to hit random things
                    if (id == Block.glass.blockID)
                    {
                        items.add(new ItemStack(Block.glass, 1, meta));
                    }
                    if (id == Block.thinGlass.blockID)
                    {
                        items.add(new ItemStack(Block.thinGlass, 1));
                    }
                    List<ItemStack> removeList = new ArrayList<ItemStack>();
                    for (int i = 0; i < items.size(); i++)
                    {
                        if (items.get(i).itemID == Block.wood.blockID)
                        {
                            items.set(i, new ItemStack(Item.coal, 1, 1));
                        }
                        else if (items.get(i).itemID == Block.wood.blockID)
                        {
                            if (world.rand.nextFloat() < .25f)
                            {
                                items.set(i, new ItemStack(Item.coal, 1, 1));
                            }
                            else
                            {
                                removeList.add(items.get(i));
                            }
                        }
                    }
                    items.removeAll(removeList);
                    LaserEvent.LaserDropItemEvent event = new LaserEvent.LaserDropItemEvent(world, start, vec, items);
                    MinecraftForge.EVENT_BUS.post(event);
                    items = event.items;
                    for (ItemStack stack : items)
                    {
                        DarksHelper.dropItemStack(world, vec.translate(0.5), stack, false);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            world.setBlockToAir(vec.intX(), vec.intY(), vec.intZ());
        }
    }
}
