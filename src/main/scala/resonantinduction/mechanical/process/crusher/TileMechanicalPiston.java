package resonantinduction.mechanical.process.crusher;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.ReflectionHelper;
import resonant.content.factory.resources.RecipeType;
import resonantinduction.mechanical.energy.grid.MechanicalNode;
import resonantinduction.mechanical.energy.grid.TileMechanical;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.api.IRotatable;
import resonant.api.recipe.MachineRecipes;
import resonant.api.recipe.RecipeResource;
import resonant.lib.config.Config;
import resonant.lib.utility.MovementUtility;
import resonant.lib.utility.inventory.InventoryUtility;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.core.grid.INode;
import universalelectricity.core.transform.vector.Vector3;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Mechanical driven piston that can be used to move basic blocks and crush ores
 * @author Calclavia
 */
public class TileMechanicalPiston extends TileMechanical
{
    @Config
    private static int mechanicalPistonMultiplier = 2;

    protected boolean markRevolve = false;

    public TileMechanicalPiston()
    {
        super(Material.piston);
        mechanicalNode = new NodeMechanicalPiston(this);
        isOpaqueCube(false);
        normalRender(false);
        customItemRender(true);
        rotationMask_$eq(Byte.parseByte("111111", 2));
        setTextureName("material_steel_dark");
    }

    @Override
    public void getNodes(List<INode> nodes)
    {
        if(mechanicalNode != null)
            nodes.add(this.mechanicalNode);
    }

    @Override
    public void update()
    {
        super.update();

        if (markRevolve)
        {
            Vector3 movePosition = new Vector3(TileMechanicalPiston.this).add(getDirection());

            if (!hitOreBlock(movePosition))
            {
                if (!worldObj.isRemote)
                {
                    Vector3 moveNewPosition = movePosition.clone().add(getDirection());

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
        //TODO add a crushing head to enforce block breaking on all block types
        Block block = blockPos.getBlock(world());

        if (block != null)
        {
            ItemStack blockStack = new ItemStack(block);
            RecipeResource[] resources = MachineRecipes.INSTANCE.getOutput(RecipeType.CRUSHER.name(), blockStack);

            if (resources.length > 0)
            {
                if (!worldObj.isRemote)
                {
                    for (RecipeResource recipe : resources)
                    {
                        if (Math.random() <= recipe.getChance())
                        {
                            InventoryUtility.dropItemStack(world(), blockPos.clone().add(0.5), recipe.getItemStack(), 10, 0);
                        }
                    }

                    blockPos.setBlockToAir(world());
                }

                ResonantInduction.proxy().renderBlockParticle(worldObj, blockPos.clone().add(0.5), new Vector3((Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3, (Math.random() - 0.5f) * 3), Block.getIdFromBlock(block), 1);
                return true;
            }
        }

        if (!worldObj.isRemote)
        {
            world().destroyBlockInWorldPartially(0, blockPos.xi(), blockPos.yi(), blockPos.zi(), -1);
        }

        return false;
    }

    public boolean canMove(Vector3 from, Vector3 to)
    {
        if (this.equals(to.getTileEntity(getWorldObj())))
        {
            return false;
        }

        /** Check Target */
        Block targetBlock = to.getBlock(worldObj);

        if (!(worldObj.isAirBlock(to.xi(), to.yi(), to.zi()) || (targetBlock != null && (targetBlock.canBeReplacedByLeaves(worldObj, to.xi(), to.yi(), to.zi())))))
        {
            return false;
        }

        return true;
    }

    public void move(Vector3 from, Vector3 to)
    {
        Block blockID = from.getBlock(worldObj);
        int blockMetadata = from.getBlockMetadata(worldObj);

        TileEntity tileEntity = from.getTileEntity(worldObj);

        NBTTagCompound tileData = new NBTTagCompound();

        if (tileEntity != null)
        {
            tileEntity.writeToNBT(tileData);
        }

        MovementUtility.setBlockSneaky(worldObj, from, null, 0, null);

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
        worldObj.notifyBlocksOfNeighborChange(pos.xi(), pos.yi(), pos.zi(), pos.getBlock(worldObj));

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
