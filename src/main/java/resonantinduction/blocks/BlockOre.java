package resonantinduction.blocks;

import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.Reference;
import resonantinduction.core.Settings;
import resonantinduction.core.recipe.RecipeLoader;
import resonantinduction.lib.EnumMaterial;
import resonantinduction.lib.IExtraInfo.IExtraBlockInfo;
import calclavia.lib.ore.OreGenReplaceStone;

import com.builtbroken.common.Pair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockOre extends Block implements IExtraBlockInfo
{
    Icon[] icons = new Icon[EnumMaterial.values().length];

    public BlockOre()
    {
        super(Settings.CONFIGURATION.getBlock("Ore", Settings.getNextBlockID()).getInt(), Material.rock);
        this.setCreativeTab(CreativeTabs.tabBlock);
        this.setUnlocalizedName(Reference.PREFIX + "Ore");
        this.setHardness(2.5f);
        this.setResistance(5.0f);

        for (OreData data : OreData.values())
        {
            data.stack = new ItemStack(this.blockID, 1, data.ordinal());
        }
    }

    @Override
    public int damageDropped(int par1)
    {
        return par1;
    }

    @Override
    public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (OreData data : OreData.values())
        {
            par3List.add(data.stack);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister)
    {
        for (OreData data : OreData.values())
        {
            data.oreIcon = par1IconRegister.registerIcon(Reference.PREFIX + data.name + "Ore");
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int metadata)
    {
        if (metadata < OreData.values().length)
        {
            return OreData.values()[metadata].oreIcon;
        }
        return Block.stone.getIcon(side, metadata);
    }

    @Override
    public void loadOreNames()
    {
        for (OreData data : OreData.values())
        {
            OreDictionary.registerOre(data.oreName, data.stack);
        }
    }

    public static enum OreData
    {
        TIN(EnumMaterial.TIN, "tin", "oreTin", 20, 8, 128),
        COPPER(EnumMaterial.COPPER, "copper", "copperOre", 20, 8, 128),
        SILVER(EnumMaterial.SILVER, "silver", "silverOre", 3, 8, 45),
        LEAD(EnumMaterial.LEAD, "lead", "leadOre", 1, 6, 30),
        Bauxite(EnumMaterial.ALUMINIUM, "bauxite", "bauxiteOre", 4, 6, 128);

        public String name, oreName;
        public ItemStack stack;
        public EnumMaterial mat;

        @SideOnly(Side.CLIENT)
        public Icon oreIcon;

        /* ORE GENERATOR OPTIONS */
        public boolean doWorldGen = true;
        public int ammount, branch, maxY;

        private OreData(EnumMaterial mat, String name, String oreName, int ammount, int branch, int maxY)
        {
            this.name = name;
            this.oreName = oreName;
            this.mat = mat;

            this.maxY = maxY;
            this.ammount = ammount;
            this.branch = branch;
        }

        public OreGenReplaceStone getGeneratorSettings()
        {
            if (this.doWorldGen)
            {
                ItemStack stack = new ItemStack(RecipeLoader.blockOre, 1, this.ordinal());
                return (OreGenReplaceStone) new OreGenReplaceStone(this.name, this.name + "Ore", stack, this.maxY, this.ammount, this.branch).enable(Settings.CONFIGURATION);
            }
            return null;
        }
    }

    @Override
    public boolean hasExtraConfigs()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void loadExtraConfigs(Configuration config)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public TileEntity createNewTileEntity(World world)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void getClientTileEntityRenderers(List<Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>> list)
    {
        // TODO Auto-generated method stub

    }
}
