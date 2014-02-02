package resonantinduction.electrical.charger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.block.BlockRI;

/** Block that is used to charge an item on its surface
 * 
 * @author Darkguardsman */
public class BlockCharger extends BlockRI
{
    public BlockCharger()
    {
        super("BlockItemCharger");
        setTextureName(Reference.PREFIX + "material_metal_side");
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta)
    {
        world.setBlockMetadataWithNotify(x, y, z, side, 3);
        return side;
    }

    @Override
    public void setBlockBoundsForItemRender()
    {
        this.setBlockBounds(.6f, 0f, 0f, 1f, 1f, 1f);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
    {
        TileEntity tile = world.getBlockTileEntity(x, y, z);
        if (tile instanceof TileCharger)
        {
            switch (((TileCharger) tile).getDirection())
            {
                case DOWN:
                    this.setBlockBounds(0f, .6f, 0f, 1f, 1f, 1f);
                    break;
                case WEST:
                    this.setBlockBounds(.6f, 0f, 0f, 1f, 1f, 1f);
                    break;
                case SOUTH:
                    this.setBlockBounds(0f, 0f, 0f, 1f, 1f, .4f);
                    break;
                case NORTH:
                    this.setBlockBounds(0f, 0f, .6f, 1f, 1f, 1f);
                    break;
                case UP:
                    this.setBlockBounds(0f, 0f, 0f, 1f, .4f, 1f);
                    break;
                case EAST:
                    this.setBlockBounds(0f, 0f, 0f, .4f, 1f, 1f);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        if (player != null)
        {
            if (player.inventory.getCurrentItem() != null)
            {

            }
            else
            {

            }
        }
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world)
    {
        return new TileCharger();
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public int damageDropped(int par1)
    {
        return 0;
    }

}
