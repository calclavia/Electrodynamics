package resonantinduction.mechanical.belt;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonant.lib.prefab.block.BlockTile;
import resonant.lib.render.block.BlockRenderingHandler;
import resonantinduction.core.Reference;
import resonantinduction.mechanical.belt.TileConveyorBelt.BeltType;
import universalelectricity.api.UniversalElectricity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** The block for the actual conveyor belt!
 * 
 * @author Calclavia, DarkGuardsman */
public class BlockConveyorBelt extends BlockTile
{
    public BlockConveyorBelt(int id)
    {
        super(id, UniversalElectricity.machine);
        setTextureName(Reference.PREFIX + "material_metal_side");
        setBlockBounds(0, 0, 0, 1, 0.3f, 1);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z)
    {
        //TileEntity tile = world.getBlockTileEntity(x, y, z);
        //if (tile instanceof TileConveyorBelt)
           // ((TileConveyorBelt) tile).node.reconstruct();
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
    {
        if (world.getBlockTileEntity(x, y, z) instanceof TileConveyorBelt)
        {
            TileConveyorBelt tileEntity = (TileConveyorBelt) world.getBlockTileEntity(x, y, z);

            if (tileEntity.getBeltType() == BeltType.SLANT_UP || tileEntity.getBeltType() == BeltType.SLANT_DOWN)
            {
                this.setBlockBounds(0f, 0f, 0f, 1f, 0.96f, 1f);
                return;
            }
            if (tileEntity.getBeltType() == BeltType.RAISED)
            {
                this.setBlockBounds(0f, 0.68f, 0f, 1f, 0.96f, 1f);
                return;
            }
        }

        this.setBlockBounds(0f, 0f, 0f, 1f, 0.3f, 1f);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
    {
        TileEntity t = world.getBlockTileEntity(x, y, z);

        if (t != null && t instanceof TileConveyorBelt)
        {
            TileConveyorBelt tileEntity = (TileConveyorBelt) t;

            if (tileEntity.getBeltType() == BeltType.SLANT_UP || tileEntity.getBeltType() == BeltType.SLANT_DOWN)
            {
                return AxisAlignedBB.getAABBPool().getAABB(x + this.minX, y + this.minY, z + this.minZ, (double) x + 1, (double) y + 1, (double) z + 1);
            }
            if (tileEntity.getBeltType() == BeltType.RAISED)
            {
                return AxisAlignedBB.getAABBPool().getAABB(x + this.minX, (double) y + 0.68f, z + this.minZ, x + this.maxX, (double) y + 0.98f, z + this.maxZ);
            }
        }

        return AxisAlignedBB.getAABBPool().getAABB(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB boundBox, List boxList, Entity entity)
    {
        TileEntity t = world.getBlockTileEntity(x, y, z);

        if (t != null && t instanceof TileConveyorBelt)
        {
            TileConveyorBelt tile = (TileConveyorBelt) t;

            if (tile.getBeltType() == BeltType.SLANT_UP || tile.getBeltType() == BeltType.SLANT_DOWN)
            {
                AxisAlignedBB boundBottom = AxisAlignedBB.getAABBPool().getAABB(x, y, z, x + 1, y + 0.3, z + 1);
                AxisAlignedBB boundTop = null;

                ForgeDirection direction = tile.getDirection();

                if (tile.getBeltType() != BeltType.NORMAL)
                {
                    AxisAlignedBB newBounds = AxisAlignedBB.getAABBPool().getAABB(x, y, z, x + 1, y + 0.1, z + 1);

                    if (newBounds != null && boundBox.intersectsWith(newBounds))
                    {
                        boxList.add(newBounds);
                    }

                    return;
                }

                if (tile.getBeltType() == BeltType.SLANT_UP)
                {
                    if (direction.offsetX > 0)
                    {
                        boundTop = AxisAlignedBB.getAABBPool().getAABB(x + (float) direction.offsetX / 2, y, z, x + 1, y + 0.8, z + 1);
                    }
                    else if (direction.offsetX < 0)
                    {
                        boundTop = AxisAlignedBB.getAABBPool().getAABB(x, y, z, x + (float) direction.offsetX / -2, y + 0.8, z + 1);
                    }
                    else if (direction.offsetZ > 0)
                    {
                        boundTop = AxisAlignedBB.getAABBPool().getAABB(x, y, z + (float) direction.offsetZ / 2, x + 1, y + 0.8, z + 1);
                    }
                    else if (direction.offsetZ < 0)
                    {
                        boundTop = AxisAlignedBB.getAABBPool().getAABB(x, y, z, x + 1, y + 0.8, z + (float) direction.offsetZ / -2);
                    }
                }
                else if (tile.getBeltType() == BeltType.SLANT_DOWN)
                {
                    if (direction.offsetX > 0)
                    {
                        boundTop = AxisAlignedBB.getAABBPool().getAABB(x, y, z, x + (float) direction.offsetX / 2, y + 0.8, z + 1);
                    }
                    else if (direction.offsetX < 0)
                    {
                        boundTop = AxisAlignedBB.getAABBPool().getAABB(x + (float) direction.offsetX / -2, y, z, x + 1, y + 0.8, z + 1);
                    }
                    else if (direction.offsetZ > 0)
                    {
                        boundTop = AxisAlignedBB.getAABBPool().getAABB(x, y, z, x + 1, y + 0.8, z + (float) direction.offsetZ / 2);
                    }
                    else if (direction.offsetZ < 0)
                    {
                        boundTop = AxisAlignedBB.getAABBPool().getAABB(x, y, z + (float) direction.offsetZ / -2, x + 1, y + 0.8, z + 1);
                    }
                }

                if (boundBox.intersectsWith(boundBottom))
                {
                    boxList.add(boundBottom);
                }
                if (boundTop != null && boundBox.intersectsWith(boundTop))
                {
                    boxList.add(boundTop);
                }

                return;
            }

            if (tile.getBeltType() == BeltType.RAISED)
            {
                AxisAlignedBB newBounds = AxisAlignedBB.getAABBPool().getAABB(x, y + 0.68, z, x + 1, y + 0.98, z + 1);

                if (newBounds != null && boundBox.intersectsWith(newBounds))
                {
                    boxList.add(newBounds);
                }

                return;
            }
        }

        AxisAlignedBB newBounds = AxisAlignedBB.getAABBPool().getAABB(x, y, z, x + 1, y + 0.1, z + 1);

        if (newBounds != null && boundBox.intersectsWith(newBounds))
        {
            boxList.add(newBounds);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack stack)
    {
        super.onBlockPlacedBy(world, x, y, z, entityLiving, stack);
        int angle = MathHelper.floor_double((entityLiving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int change = 2;

        switch (angle)
        {
            case 0:
                change = 3;
                break;
            case 1:
                change = 4;
                break;
            case 2:
                change = 2;
                break;
            case 3:
                change = 5;
                break;

        }
        world.setBlockMetadataWithNotify(x, y, z, change, 3);
    }

    @Override
    public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        int original = world.getBlockMetadata(x, y, z);
        int change = 2;

        switch (original)
        {
            case 2:
                change = 4;
                break;
            case 3:
                change = 5;
                break;
            case 4:
                change = 3;
                break;
            case 5:
                change = 2;
                break;

        }

        world.setBlockMetadataWithNotify(x, y, z, change, 3);

        return true;
    }

    @Override
    public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        TileConveyorBelt tileEntity = (TileConveyorBelt) world.getBlockTileEntity(x, y, z);

        int slantOrdinal = tileEntity.getBeltType().ordinal() + 1;

        if (slantOrdinal >= BeltType.values().length)
        {
            slantOrdinal = 0;
        }

        tileEntity.setBeltType(BeltType.values()[slantOrdinal]);

        return true;
    }

    /** Moves the entity if the belt is powered. */
    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity instanceof TileConveyorBelt)
        {
            TileConveyorBelt tile = (TileConveyorBelt) tileEntity;

            if (tile.ignoreList.contains(entity))
            {
                return;
            }

            if (!world.isBlockIndirectlyGettingPowered(x, y, z))
            {
                double maxSpeed = TileConveyorBelt.ACCELERATION;

                if (maxSpeed > 0)
                {
                    BeltType slantType = tile.getBeltType();
                    ForgeDirection direction = tile.getDirection();

                    if (slantType != BeltType.NORMAL)
                    {
                        entity.onGround = false;
                    }

                    if (slantType == BeltType.SLANT_UP)
                    {
                        // We need at least 0.25 to move items up.
                        entity.motionY = maxSpeed * 3;// Math.max(0.25, maxSpeed);
                    }
                    else if (slantType == BeltType.SLANT_DOWN)
                    {
                        entity.motionY = -maxSpeed;
                    }

                    if (direction.offsetX != 0)
                    {
                        entity.motionX = direction.offsetX * maxSpeed;
                        entity.motionZ /= 2;
                    }

                    if (direction.offsetZ != 0)
                    {
                        entity.motionZ = direction.offsetZ * maxSpeed;
                        entity.motionX /= 2;
                    }

                    if (entity instanceof EntityItem)
                    {
                        if (direction.offsetX != 0)
                        {
                            double difference = (z + 0.5) - entity.posZ;
                            entity.motionZ += difference * 0.1;
                        }
                        else if (direction.offsetZ != 0)
                        {
                            double difference = (x + 0.5) - entity.posX;
                            entity.motionX += difference * 0.1;
                        }

                        ((EntityItem) entity).age = 0;

                        boolean foundSneaking = false;
                        for (EntityPlayer player : (List<EntityPlayer>) world.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1)))
                        {
                            if (player.isSneaking())
                                foundSneaking = true;
                        }

                        if (foundSneaking)
                            ((EntityItem) entity).delayBeforeCanPickup = 0;
                        else
                            ((EntityItem) entity).delayBeforeCanPickup = 20;
                        entity.onGround = false;
                    }
                }
            }
        }
    }

    /** Returns the TileEntity used by this block. */
    @Override
    public TileEntity createNewTileEntity(World world)
    {
        return new TileConveyorBelt();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderType()
    {
        return BlockRenderingHandler.ID;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    public int damageDropped(int par1)
    {
        return 0;
    }
}
