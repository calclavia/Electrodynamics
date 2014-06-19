package resonantinduction.mechanical.process.crusher;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.IRotatable;
import resonant.api.recipe.MachineRecipes;
import resonant.api.recipe.RecipeResource;
import resonant.lib.config.Config;
import resonant.lib.utility.MovementUtility;
import resonant.lib.utility.inventory.InventoryUtility;
import resonantinduction.core.ResonantInduction;
import resonantinduction.mechanical.energy.grid.MechanicalNode;
import resonantinduction.mechanical.energy.grid.TileMechanical;
import universalelectricity.api.vector.Vector3;

import java.lang.reflect.Method;

public class TileMechanicalPiston extends TileMechanical implements IRotatable
{
    @Config(comment = "Outdated, not used anymore. use mechanicalPistonMultiplier as its based on block hardness now")
    @Deprecated
    private static int mechanicalPistonBreakCount = 5;

    @Config
    private static int mechanicalPistonMultiplier = 2;

    private boolean markRevolve = false;

    public TileMechanicalPiston()
    {
        super(Material.piston);

        mechanicalNode = new MechanicalNode(this)
        {
            @Override
            protected void revolve()
            {
                markRevolve = true;
            }

            @Override
            public boolean canConnect(ForgeDirection from, Object source)
            {
                return from != getDirection();
            }

        }.setLoad(0.5f);

        isOpaqueCube = false;
        normalRender = false;
        customItemRender = true;
        rotationMask = Byte.parseByte("111111", 2);
        textureName = "material_steel_dark";
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();

        if (markRevolve)
        {
            Vector3 movePosition = new Vector3(TileMechanicalPiston.this).translate(getDirection());

            if (!hitOreBlock(movePosition))
            {
                if (!worldObj.isRemote)
                {
                    Vector3 moveNewPosition = movePosition.clone().translate(getDirection());

                    if (canMove(movePosition, moveNewPosition))
                    {
                        move(movePosition, moveNewPosition);
                    }
                }
            }

            markRevolve = false;
        }
    }

    public boolean hitOreBlock(Vector3 blockPos)
    {
        Block block = Block.blocksList[blockPos.getBlockID(world())];

        if (block != null)
        {
            int breakCount = (int) (mechanicalPistonMultiplier * block.blockHardness);
            final int startBreakCount = breakCount;

            ItemStack blockStack = new ItemStack(block);
            RecipeResource[] resources = MachineRecipes.INSTANCE.getOutput(ResonantInduction.RecipeType.CRUSHER.name(), blockStack);

            if (resources.length > 0)
            {
                if (!worldObj.isRemote)
                {
                    int breakStatus = (int) (((float) (startBreakCount - breakCount) / (float) startBreakCount) * 10f);
                    world().destroyBlockInWorldPartially(0, blockPos.intX(), blockPos.intY(), blockPos.intZ(), breakStatus);
                    ResonantInduction.LOGGER.info("[Mechanical Piston] Break Count: " + breakCount);
                    
                    if (breakCount >= mechanicalPistonMultiplier)
                    {
                        for (RecipeResource recipe : resources)
                        {
                            if (Math.random() <= recipe.getChance())
                            {
                                InventoryUtility.dropItemStack(world(), blockPos.clone().translate(0.5), recipe.getItemStack(), 10, 0);
                            }
                        }

                        getWorldObj().destroyBlock(blockPos.intX(), blockPos.intY(), blockPos.intZ(), false);
                    }
                }

                ResonantInduction.proxy.renderBlockParticle(worldObj, blockPos.clone().translate(0.5), new Vector3((Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3), block.blockID, 1);
                breakCount--;
                return true;
            }
        }

        if (!worldObj.isRemote)
        {
            world().destroyBlockInWorldPartially(0, blockPos.intX(), blockPos.intY(), blockPos.intZ(), -1);
        }
        
        return false;
    }

    @Override
    public void onRemove(int par5, int par6)
    {
        super.onRemove(par5, par6);
    }

    public boolean canMove(Vector3 from, Vector3 to)
    {
        TileEntity tileEntity = from.getTileEntity(worldObj);

        if (this.equals(to.getTileEntity(getWorldObj())))
        {
            return false;
        }

        /** Check Target */
        int targetBlockID = to.getBlockID(worldObj);

        if (!(worldObj.isAirBlock(to.intX(), to.intY(), to.intZ()) || (targetBlockID > 0 && (Block.blocksList[targetBlockID].isBlockReplaceable(worldObj, to.intX(), to.intY(), to.intZ())))))
        {
            return false;
        }

        return true;
    }

    public void move(Vector3 from, Vector3 to)
    {
        int blockID = from.getBlockID(worldObj);
        int blockMetadata = from.getBlockMetadata(worldObj);

        TileEntity tileEntity = from.getTileEntity(worldObj);

        NBTTagCompound tileData = new NBTTagCompound();

        if (tileEntity != null)
        {
            tileEntity.writeToNBT(tileData);
        }

        MovementUtility.setBlockSneaky(worldObj, from, 0, 0, null);

        if (tileEntity != null && tileData != null)
        {
            /** Forge Multipart Support. Use FMP's custom TE creator. */
            boolean isMultipart = tileData.getString("id").equals("savedMultipart");

            TileEntity newTile = null;

            if (isMultipart)
            {
                try
                {
                    Class multipart = Class.forName("codechicken.multipart.MultipartHelper");
                    Method m = multipart.getMethod("createTileFromNBT", World.class, NBTTagCompound.class);
                    newTile = (TileEntity) m.invoke(null, worldObj, tileData);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                newTile = TileEntity.createAndLoadEntity(tileData);
            }

            MovementUtility.setBlockSneaky(worldObj, to, blockID, blockMetadata, newTile);

            if (newTile != null && isMultipart)
            {
                try
                {
                    // Send the description packet of the TE after moving it.
                    Class multipart = Class.forName("codechicken.multipart.MultipartHelper");
                    multipart.getMethod("sendDescPacket", World.class, TileEntity.class).invoke(null, worldObj, newTile);

                    // Call onMoved event.
                    Class tileMultipart = Class.forName("codechicken.multipart.TileMultipart");
                    tileMultipart.getMethod("onMoved").invoke(newTile);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            MovementUtility.setBlockSneaky(worldObj, to, blockID, blockMetadata, null);
        }

        notifyChanges(from);
        notifyChanges(to);
    }

    public void notifyChanges(Vector3 pos)
    {
        worldObj.notifyBlocksOfNeighborChange(pos.intX(), pos.intY(), pos.intZ(), pos.getBlockID(worldObj));

        TileEntity newTile = pos.getTileEntity(worldObj);

        if (newTile != null)
        {
            if (Loader.isModLoaded("BuildCraft|Factory"))
            {
                /** Special quarry compatibility code. */
                try
                {
                    Class clazz = Class.forName("buildcraft.factory.TileQuarry");

                    if (newTile.equals(clazz))
                    {
                        ReflectionHelper.setPrivateValue(clazz, newTile, true, "isAlive");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

}
