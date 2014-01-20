package resonantinduction.core.resource;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import calclavia.lib.ore.OreGenReplaceStone;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockOre extends Block
{
    Icon[] icons = new Icon[EnumTierMaterial.values().length];

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
        this.loadOreNames();
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

    public void loadOreNames()
    {
        for (OreData data : OreData.values())
        {
            OreDictionary.registerOre(data.oreName, data.stack);
        }
    }

    public static enum OreData
    {
        TIN(EnumTierMaterial.TIN, "tin", "oreTin", 20, 8, 128),
        COPPER(EnumTierMaterial.COPPER, "copper", "copperOre", 20, 8, 128),
        SILVER(EnumTierMaterial.SILVER, "silver", "silverOre", 3, 8, 45),
        LEAD(EnumTierMaterial.LEAD, "lead", "leadOre", 1, 6, 30),
        Bauxite(EnumTierMaterial.ALUMINIUM, "bauxite", "bauxiteOre", 4, 6, 128);

        public String name, oreName;
        public ItemStack stack;
        public EnumTierMaterial mat;

        @SideOnly(Side.CLIENT)
        public Icon oreIcon;

        /* ORE GENERATOR OPTIONS */
        public boolean doWorldGen = true;
        public int ammount, branch, maxY;

        private OreData(EnumTierMaterial mat, String name, String oreName, int ammount, int branch, int maxY)
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
                ItemStack stack = new ItemStack(ResonantInduction.blockOre, 1, this.ordinal());
                return (OreGenReplaceStone) new OreGenReplaceStone(this.name, this.name + "Ore", stack, this.maxY, this.ammount, this.branch).enable(Settings.CONFIGURATION);
            }
            return null;
        }
    }
}
