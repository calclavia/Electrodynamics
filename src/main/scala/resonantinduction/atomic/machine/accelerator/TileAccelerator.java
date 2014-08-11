package resonantinduction.atomic.machine.accelerator;

import net.minecraft.block.material.Material;
import net.minecraft.network.Packet;
import resonant.engine.ResonantEngine;
import resonant.lib.network.discriminator.PacketAnnotation;
import resonant.lib.utility.BlockUtility;
import resonantinduction.atomic.Atomic;
import resonantinduction.atomic.AtomicContent;
import resonantinduction.atomic.items.ItemAntimatter;
import resonantinduction.atomic.items.ItemDarkMatter;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.api.IElectromagnet;
import resonant.api.IRotatable;
import resonant.lib.network.Synced;
import resonantinduction.core.Reference;
import resonantinduction.core.Settings;
import universalelectricity.core.transform.vector.Vector3;
import resonant.lib.content.prefab.java.TileElectricInventory;

/** Accelerator TileEntity */
public class TileAccelerator extends TileElectricInventory implements IElectromagnet, IRotatable
{
    /** Joules required per ticks. */
    public static final int energyPerTick = 4800000;

    /** User client side to determine the velocity of the particle. */
    public static final float clientParticleVelocity = 0.9f;

    /** The total amount of energy consumed by this particle. In Joules. */
    @Synced
    public float totalEnergyConsumed = 0;

    /** The amount of anti-matter stored within the accelerator. Measured in milligrams. */
    @Synced
    public int antimatter;
    public EntityParticle entityParticle;

    @Synced
    public float velocity;

    @Synced
    private double clientEnergy = 0;

    private int lastSpawnTick = 0;

    /** Multiplier that is used to give extra anti-matter based on density (hardness) of a given ore. */
    private int antiMatterDensityMultiplyer = DENSITY_MULTIPLYER_DEFAULT;
    private static final int DENSITY_MULTIPLYER_DEFAULT = 1;

    public TileAccelerator()
    {
        super(Material.iron);
        this.setSizeInventory(4);
        antiMatterDensityMultiplyer = DENSITY_MULTIPLYER_DEFAULT;
    }

    @Override
    public void update()
    {
        super.update();

        if (!worldObj.isRemote)
        {
            clientEnergy = electricNode().energy().getEnergy();
            velocity = 0;

            // Calculate accelerated particle velocity if it is spawned.
            if (entityParticle != null)
            {
                velocity = (float) entityParticle.getParticleVelocity();
            }

            // Check if item inside of empty cell slot is indeed an empty slot.
            if (Atomic.isItemStackEmptyCell(getStackInSlot(1)))
            {
                // Check if there are any empty cells we can store anti-matter in.
                if (getStackInSlot(1).stackSize > 0)
                {
                    // Craft anti-matter item if there is enough anti-matter to actually do so.
                    if (antimatter >= 125)
                    {
                        if (getStackInSlot(2) != null)
                        {
                            // Increase the existing amount of anti-matter if stack already exists.
                            if (getStackInSlot(2).getItem() == AtomicContent.itemAntimatter())
                            {
                                ItemStack newStack = getStackInSlot(2).copy();
                                if (newStack.stackSize < newStack.getMaxStackSize())
                                {
                                    // Remove an empty cell which we will put the anti-matter into.
                                    decrStackSize(1, 1);

                                    // Remove anti-matter from internal reserve and increase stack count.
                                    antimatter -= 125;
                                    newStack.stackSize++;
                                    setInventorySlotContents(2, newStack);
                                }
                            }
                        }
                        else
                        {
                            // Remove some of the internal reserves of anti-matter and use it to craft an individual item.
                            antimatter -= 125;
                            decrStackSize(1, 1);
                            setInventorySlotContents(2, new ItemStack(AtomicContent.itemAntimatter()));
                        }
                    }
                }
            }

            // Check if redstone signal is currently being applied.
            if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
            {
                if (electricNode().energy().checkExtract())
                {
                    if (entityParticle == null)
                    {
                        // Creates a accelerated particle if one needs to exist (on world load for example or player login).
                        if (getStackInSlot(0) != null && lastSpawnTick >= 40)
                        {
                            Vector3 spawn_vec = new Vector3(this);
                            spawn_vec.add(getDirection().getOpposite());
                            spawn_vec.add(0.5f);

                            // Only render the particle if container within the proper environment for it.
                            if (EntityParticle.canSpawnParticle(worldObj, spawn_vec))
                            {
                                // Spawn the particle.
                                totalEnergyConsumed = 0;
                                entityParticle = new EntityParticle(worldObj, spawn_vec, new Vector3(this), getDirection().getOpposite());
                                worldObj.spawnEntityInWorld(entityParticle);

                                // Grabs input block hardness if available, otherwise defaults are used.
                                CalculateParticleDensity();

                                // Decrease particle we want to collide.
                                decrStackSize(0, 1);
                                lastSpawnTick = 0;
                            }
                        }
                    }
                    else
                    {
                        if (entityParticle.isDead)
                        {
                            // On particle collision we roll the dice to see if dark-matter is generated.
                            if (entityParticle.didParticleCollide)
                            {
                                if (worldObj.rand.nextFloat() <= Settings.darkMatterSpawnChance())
                                {
                                    incrStackSize(3, new ItemStack(AtomicContent.itemDarkMatter()));
                                }
                            }

                            entityParticle = null;
                        }
                        else if (velocity > clientParticleVelocity)
                        {
                            // Play sound of anti-matter being created.
                            worldObj.playSoundEffect(xCoord, yCoord, zCoord, Reference.prefix() + "antimatter", 2f, 1f - worldObj.rand.nextFloat() * 0.3f);

                            // Create anti-matter in the internal reserve.
                            int generatedAntimatter = 5 + worldObj.rand.nextInt(antiMatterDensityMultiplyer);
                            antimatter += generatedAntimatter;
                            // AtomicScience.LOGGER.info("[Particle Accelerator] Generated " + String.valueOf(generatedAntimatter) + " mg of anti-matter.");

                            // Reset energy consumption levels and destroy accelerated particle.
                            totalEnergyConsumed = 0;
                            entityParticle.setDead();
                            entityParticle = null;
                        }

                        // Plays sound of particle accelerating past the speed based on total velocity at the time of anti-matter creation.
                        if (entityParticle != null)
                        {
                            worldObj.playSoundEffect(xCoord, yCoord, zCoord, Reference.prefix() + "accelerator", 1.5f, (float) (0.6f + (0.4 * (entityParticle.getParticleVelocity()) / TileAccelerator.clientParticleVelocity)));
                        }
                    }

                    electricNode().energy().extractEnergy();
                }
                else
                {
                    if (entityParticle != null)
                    {
                        entityParticle.setDead();
                    }

                    entityParticle = null;
                }
            }
            else
            {
                if (entityParticle != null)
                {
                    entityParticle.setDead();
                }

                entityParticle = null;
            }

            if (ticks() % 5 == 0)
            {
                //for (EntityPlayer player : getPlayersUsing())
                //{
                //    ResonantEngine.instance.packetHandler.sendtoPlayer(new PacketAnnotation(this), player);
                //}
            }

            lastSpawnTick++;
        }
    }
    
    @Override
    public boolean use(EntityPlayer player, int side, Vector3 hit)
    {
        if (!world().isRemote)
        {
            player.openGui(Atomic.INSTANCE(), 0, world(), x(), y(), z());
        }
        return true;
    }

    private void CalculateParticleDensity()
    {
        ItemStack itemToAccelerate = this.getStackInSlot(0);
        if (itemToAccelerate != null)
        {
            // Calculate block density multiplier if ore dictionary block.
            antiMatterDensityMultiplyer = DENSITY_MULTIPLYER_DEFAULT;
            try
            {
                Block potentialBlock = Block.getBlockFromItem(itemToAccelerate.getItem());
                if (potentialBlock != null)
                {
                    // Prevent negative numbers and disallow zero for density multiplier.
                    antiMatterDensityMultiplyer = (int) Math.abs(BlockUtility.getBlockHardness(potentialBlock));
                    if (antiMatterDensityMultiplyer <= 0)
                    {
                        antiMatterDensityMultiplyer = 1;
                    }

                    // AtomicScience.LOGGER.info("[Particle Accelerator] " + String.valueOf(potentialBlock.getUnlocalizedName()) + " Hardness: " + String.valueOf(antiMatterDensityMultiplyer));
                }
            }
            catch (Exception err)
            {
                antiMatterDensityMultiplyer = DENSITY_MULTIPLYER_DEFAULT;
                // AtomicScience.LOGGER.info("[Particle Accelerator] Attempted to query Minecraft block-list with value out of index.");
            }
        }
    }

    @Override
    public Packet getDescriptionPacket()
    {
        return ResonantEngine.instance.packetHandler.toMCPacket(new PacketAnnotation(this));
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        totalEnergyConsumed = par1NBTTagCompound.getFloat("totalEnergyConsumed");
        antimatter = par1NBTTagCompound.getInteger("antimatter");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setFloat("totalEnergyConsumed", totalEnergyConsumed);
        par1NBTTagCompound.setInteger("antimatter", antimatter);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        return new int[]
        { 0, 1, 2, 3 };
    }

    @Override
    public boolean canInsertItem(int slotID, ItemStack itemStack, int j)
    {
        return isItemValidForSlot(slotID, itemStack) && slotID != 2 && slotID != 3;
    }

    @Override
    public boolean canExtractItem(int slotID, ItemStack itemstack, int j)
    {
        return slotID == 2 || slotID == 3;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack)
    {
        switch (i)
        {
        case 0:
            return true;
        case 1:
            return Atomic.isItemStackEmptyCell(itemStack);
        case 2:
            return itemStack.getItem() instanceof ItemAntimatter;
        case 3:
            return itemStack.getItem() instanceof ItemDarkMatter;
        }

        return false;
    }

    @Override
    public boolean isRunning()
    {
        return true;
    }

    @Override
    public ForgeDirection getDirection()
    {
        return ForgeDirection.getOrientation(getBlockMetadata());
    }

    @Override
    public void setDirection(ForgeDirection direction)
    {
        world().setBlockMetadataWithNotify(xCoord, yCoord, zCoord, direction.ordinal(), 3);
    }
}
