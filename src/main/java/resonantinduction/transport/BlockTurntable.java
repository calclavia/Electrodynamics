package resonantinduction.transport;

import java.util.Random;

import resonantinduction.AssemblyLine;
import resonantinduction.blocks.BlockAssembly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.minecraft.interfaces.IRotatable;
import com.builtbroken.minecraft.interfaces.IRotatableBlock;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockTurntable extends BlockAssembly
{
    private Icon top;

    public BlockTurntable()
    {
        super("turntable", Material.piston);
        this.setTickRandomly(true);
    }

    @Override
    public int tickRate(World par1World)
    {
        return 5;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister iconReg)
    {
        super.registerIcons(iconReg);
        this.top = iconReg.registerIcon(ResonantInductionTransport.PREFIX + "turntable");
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random par5Random)
    {
        this.updateTurntableState(world, x, y, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int meta)
    {
        if (side == meta)
        {
            return this.top;
        }
        return this.blockIcon;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack stack)
    {
        int angle = MathHelper.floor_double((entityLiving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int change = 3;

        switch (angle)
        {
            case 0:
                change = 2;
                break;

            case 1:
                change = 5;
                break;

            case 2:
                change = 3;
                break;

            case 3:
                change = 4;
                break;
        }

        world.setBlockMetadataWithNotify(x, y, z, change, 3);
        world.scheduleBlockUpdate(x, y, z, this.blockID, 20);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int side)
    {
        world.scheduleBlockUpdate(x, y, z, this.blockID, 20);
    }

    private void updateTurntableState(World world, int x, int y, int z)
    {
        if (world.isBlockIndirectlyGettingPowered(x, y, z))
        {
            try
            {
                ForgeDirection direction = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z));
                ForgeDirection blockRotation = null;

                Vector3 position = new Vector3(x, y, z).modifyPositionFromSide(direction);
                TileEntity tileEntity = position.getTileEntity(world);
                int blockID = position.getBlockID(world);

                if (tileEntity instanceof IRotatable)
                {
                    blockRotation = ((IRotatable) tileEntity).getDirection();
                }
                else if (Block.blocksList[blockID] instanceof IRotatableBlock)
                {
                    blockRotation = ((IRotatableBlock) Block.blocksList[blockID]).getDirection(world, position.intX(), position.intY(), position.intZ());
                }
                else if (Block.blocksList[blockID] != null)
                {
                    Block.blocksList[blockID].rotateBlock(world, position.intX(), position.intY(), position.intZ(), direction.getOpposite());
                }
                if (direction != null)
                {
                    if (direction == ForgeDirection.UP || direction == ForgeDirection.DOWN)
                    {
                        if (blockRotation == ForgeDirection.NORTH)
                        {
                            blockRotation = ForgeDirection.EAST;
                        }
                        else if (blockRotation == ForgeDirection.EAST)
                        {
                            blockRotation = ForgeDirection.SOUTH;
                        }
                        else if (blockRotation == ForgeDirection.SOUTH)
                        {
                            blockRotation = ForgeDirection.WEST;
                        }
                        else if (blockRotation == ForgeDirection.WEST)
                        {
                            blockRotation = ForgeDirection.NORTH;
                        }
                    }
                    else if (direction == ForgeDirection.EAST || direction == ForgeDirection.WEST)
                    {
                        if (blockRotation == ForgeDirection.NORTH)
                        {
                            blockRotation = ForgeDirection.UP;
                        }
                        else if (blockRotation == ForgeDirection.UP)
                        {
                            blockRotation = ForgeDirection.SOUTH;
                        }
                        else if (blockRotation == ForgeDirection.SOUTH)
                        {
                            blockRotation = ForgeDirection.DOWN;
                        }
                        else if (blockRotation == ForgeDirection.DOWN)
                        {
                            blockRotation = ForgeDirection.NORTH;
                        }
                    }
                    else if (direction == ForgeDirection.NORTH || direction == ForgeDirection.SOUTH)
                    {
                        if (blockRotation == ForgeDirection.EAST)
                        {
                            blockRotation = ForgeDirection.UP;
                        }
                        else if (blockRotation == ForgeDirection.UP)
                        {
                            blockRotation = ForgeDirection.WEST;
                        }
                        else if (blockRotation == ForgeDirection.WEST)
                        {
                            blockRotation = ForgeDirection.DOWN;
                        }
                        else if (blockRotation == ForgeDirection.DOWN)
                        {
                            blockRotation = ForgeDirection.EAST;
                        }
                    }
                    world.markBlockForUpdate(position.intX(), position.intY(), position.intZ());
                    world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "tile.piston.in", 0.5F, world.rand.nextFloat() * 0.15F + 0.6F);
                    if (tileEntity instanceof IRotatable)
                    {
                        ((IRotatable) tileEntity).setDirection(blockRotation);
                        world.scheduleBlockUpdate(position.intX(), position.intY(), position.intZ(), this.blockID, 20);

                    }
                    else if (Block.blocksList[blockID] instanceof IRotatableBlock)
                    {
                        ((IRotatableBlock) Block.blocksList[blockID]).setDirection(world, position.intX(), position.intY(), position.intZ(), blockRotation);
                        world.scheduleBlockUpdate(position.intX(), position.intY(), position.intZ(), this.blockID, 20);
                    }
                }
            }
            catch (Exception e)
            {
                System.out.println("Error while rotating a block near " + x + "x " + y + "y " + z + "z " + (world != null && world.provider != null ? world.provider.dimensionId + "d" : "null:world"));
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onSneakMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote)
        {
            return true;
        }
        world.setBlockMetadataWithNotify(x, y, z, side, 3);
        return true;
    }

    @Override
    public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote)
        {
            return true;
        }
        ForgeDirection currentDirection = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z));
        if (currentDirection == ForgeDirection.NORTH)
        {
            currentDirection = ForgeDirection.UP;
        }
        else if (currentDirection == ForgeDirection.UP)
        {
            currentDirection = ForgeDirection.DOWN;
        }
        else if (currentDirection == ForgeDirection.DOWN)
        {
            currentDirection = ForgeDirection.EAST;
        }
        else if (currentDirection == ForgeDirection.EAST)
        {
            currentDirection = ForgeDirection.SOUTH;
        }
        else if (currentDirection == ForgeDirection.SOUTH)
        {
            currentDirection = ForgeDirection.WEST;
        }
        else if (currentDirection == ForgeDirection.WEST)
        {
            currentDirection = ForgeDirection.NORTH;
        }
        world.setBlockMetadataWithNotify(x, y, z, currentDirection.ordinal(), 3);
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
