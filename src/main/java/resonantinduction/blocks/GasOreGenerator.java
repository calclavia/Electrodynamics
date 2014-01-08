package resonantinduction.blocks;

import java.util.Random;

import resonantinduction.ALRecipeLoader;
import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderEnd;
import net.minecraft.world.gen.ChunkProviderHell;
import net.minecraftforge.fluids.FluidStack;
import cpw.mods.fml.common.IWorldGenerator;

public class GasOreGenerator implements IWorldGenerator
{

    public int minGenerateLevel = 6;
    public int maxGenerateLevel = 50;
    public int amountPerChunk = 3;
    public int amountPerBranch = 15;
    public int replaceID = 1;

    public FluidStack stack;

    @Override
    public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
    {
        chunkX = chunkX << 4;
        chunkZ = chunkZ << 4;

        if (this.isOreGeneratedInWorld(world, chunkGenerator))
        {
            this.generate(world, rand, chunkX, chunkZ);
        }
    }

    public void generate(World world, Random random, int varX, int varZ)
    {
        try
        {
            //TODO get the biome of the chunk and generate more gas in swamp biomes
            // chunk = world.getChunkFromChunkCoords(varX, varZ);
            for (int i = 0; i < this.amountPerChunk; i++)
            {
                int x = varX + random.nextInt(16);
                int z = varZ + random.nextInt(16);
                int y = random.nextInt(Math.max(this.maxGenerateLevel - this.minGenerateLevel, 0)) + this.minGenerateLevel;
                this.generateReplace(world, random, x, y, z);
            }
        }
        catch (Exception e)
        {
            System.out.println("[CoreMachine]Error generating natural gas");
            e.printStackTrace();
        }
    }

    public boolean generateReplace(World world, Random random, int par3, int par4, int par5)
    {
        float var6 = random.nextFloat() * (float) Math.PI;
        double var7 = par3 + 8 + MathHelper.sin(var6) * this.amountPerBranch / 8.0F;
        double var9 = par3 + 8 - MathHelper.sin(var6) * this.amountPerBranch / 8.0F;
        double var11 = par5 + 8 + MathHelper.cos(var6) * this.amountPerBranch / 8.0F;
        double var13 = par5 + 8 - MathHelper.cos(var6) * this.amountPerBranch / 8.0F;
        double var15 = par4 + random.nextInt(3) - 2;
        double var17 = par4 + random.nextInt(3) - 2;

        for (int oreCount = 0; oreCount <= this.amountPerBranch; ++oreCount)
        {
            double var20 = var7 + (var9 - var7) * oreCount / this.amountPerBranch;
            double var22 = var15 + (var17 - var15) * oreCount / this.amountPerBranch;
            double var24 = var11 + (var13 - var11) * oreCount / this.amountPerBranch;
            double var26 = random.nextDouble() * this.amountPerBranch / 16.0D;
            double var28 = (MathHelper.sin(oreCount * (float) Math.PI / this.amountPerBranch) + 1.0F) * var26 + 1.0D;
            double var30 = (MathHelper.sin(oreCount * (float) Math.PI / this.amountPerBranch) + 1.0F) * var26 + 1.0D;
            int var32 = MathHelper.floor_double(var20 - var28 / 2.0D);
            int var33 = MathHelper.floor_double(var22 - var30 / 2.0D);
            int var34 = MathHelper.floor_double(var24 - var28 / 2.0D);
            int var35 = MathHelper.floor_double(var20 + var28 / 2.0D);
            int var36 = MathHelper.floor_double(var22 + var30 / 2.0D);
            int var37 = MathHelper.floor_double(var24 + var28 / 2.0D);

            for (int x = var32; x <= var35; ++x)
            {
                double var39 = (x + 0.5D - var20) / (var28 / 2.0D);

                if (var39 * var39 < 1.0D)
                {
                    for (int y = var33; y <= var36; ++y)
                    {
                        double var42 = (y + 0.5D - var22) / (var30 / 2.0D);

                        if (var39 * var39 + var42 * var42 < 1.0D)
                        {
                            for (int z = var34; z <= var37; ++z)
                            {
                                double var45 = (z + 0.5D - var24) / (var28 / 2.0D);

                                int blockid = world.getBlockId(x, y, z);
                                Block block = Block.blocksList[blockid];
                                if (var39 * var39 + var42 * var42 + var45 * var45 < 1.0D && (block == null || block.isAirBlock(world, x, y, z) || block.canBeReplacedByLeaves(world, x, y, z) || blockid == this.replaceID))
                                {
                                    world.setBlock(x, y, z, ALRecipeLoader.blockGas.blockID, 10 + world.rand.nextInt(5), 2);
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    public boolean isOreGeneratedInWorld(World world, IChunkProvider chunkGenerator)
    {
        if (chunkGenerator instanceof ChunkProviderHell)
        {
            return false;
        }
        if (chunkGenerator instanceof ChunkProviderEnd)
        {
            return false;
        }
        return true;
    }
}
