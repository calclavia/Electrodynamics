package mffs.tileentity;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import mffs.ModularForceFieldSystem;
import mffs.api.card.ICard;
import mffs.api.card.ICardInfinite;
import mffs.api.card.ICardLink;
import mffs.api.fortron.IFortronCapacitor;
import mffs.api.fortron.IFortronFrequency;
import mffs.api.fortron.IFortronStorage;
import mffs.api.modules.IModule;
import mffs.base.TileEntityInventory;
import mffs.base.TileEntityModuleAcceptor;
import mffs.fortron.FortronGrid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import universalelectricity.core.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityFortronCapacitor extends TileEntityModuleAcceptor implements IFortronStorage, IFortronCapacitor
{
	public enum TransferMode
	{
		EQUALIZE, DISTRIBUTE, DRAIN, FILL;

		public TransferMode toggle()
		{
			int newOrdinal = this.ordinal() + 1;

			if (newOrdinal >= TransferMode.values().length)
			{
				newOrdinal = 0;
			}
			return TransferMode.values()[newOrdinal];
		}

	}

	private TransferMode transferMode = TransferMode.EQUALIZE;

	public TileEntityFortronCapacitor()
	{
		this.fortronTank.setCapacity(500 * LiquidContainerRegistry.BUCKET_VOLUME);
		this.startModuleIndex = 2;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.isDisabled())
		{
			/**
			 * Transmit Fortrons in frequency network, evenly distributing them.
			 */
			/**
			 * Gets the card.
			 */
			if (this.isActive() && this.ticks % 10 == 0)
			{
				Set<IFortronFrequency> machines = new HashSet<IFortronFrequency>();

				for (ItemStack itemStack : this.getCards())
				{
					if (itemStack != null)
					{
						if (itemStack.getItem() instanceof ICardInfinite)
						{
							this.setFortronEnergy(this.getFortronCapacity());
						}
						else if (itemStack.getItem() instanceof ICardLink)
						{
							Vector3 linkPosition = ((ICardLink) itemStack.getItem()).getLink(itemStack);

							if (linkPosition != null && linkPosition.getTileEntity(this.worldObj) instanceof IFortronFrequency)
							{
								machines.add(this);
								machines.add((IFortronFrequency) linkPosition.getTileEntity(this.worldObj));
							}

						}
					}
				}

				if (machines.size() < 1)
				{
					machines = this.getLinkedDevices();
				}

				if (machines.size() > 1)
				{
					/**
					 * Check spread mode. Equal, Give All, Take All
					 */
					int totalFortron = 0;
					int totalCapacity = 0;

					for (IFortronFrequency machine : machines)
					{
						if (machine != null)
						{
							totalFortron += machine.getFortronEnergy();
							totalCapacity += machine.getFortronCapacity();
						}
					}

					if (totalFortron > 0 && totalCapacity > 0)
					{
						/**
						 * Test each mode and based on the mode, spread Fortron energy.
						 */
						switch (this.transferMode)
						{
							case EQUALIZE:
							{
								for (IFortronFrequency machine : machines)
								{
									if (machine != null)
									{
										double capacityPercentage = (double) machine.getFortronCapacity() / (double) totalCapacity;
										int amountToSet = (int) (totalFortron * capacityPercentage);
										this.transferFortron(machine, amountToSet - machine.getFortronEnergy());
									}
								}

								break;
							}
							case DISTRIBUTE:
							{
								machines.remove(this);

								final int amountToSet = totalFortron / machines.size();

								for (IFortronFrequency machine : machines)
								{
									if (machine != null)
									{
										this.transferFortron(machine, amountToSet - machine.getFortronEnergy());
									}
								}

								break;
							}
							case DRAIN:
							{
								machines.remove(this);

								for (IFortronFrequency machine : machines)
								{
									if (machine != null)
									{
										double capacityPercentage = (double) machine.getFortronCapacity() / (double) totalCapacity;
										int amountToSet = (int) (totalFortron * capacityPercentage);

										if (amountToSet - machine.getFortronEnergy() > 0)
										{
											this.transferFortron(machine, amountToSet - machine.getFortronEnergy());
										}
									}
								}

								break;
							}
							case FILL:
							{
								machines.remove(this);

								/**
								 * Take total Fortron energy and consume it, then distribute the
								 * rest. Removes this capacitor from the list.
								 */
								totalFortron -= this.provideFortron(totalFortron, true);
								totalCapacity -= this.getFortronCapacity();

								for (IFortronFrequency machine : machines)
								{
									if (machine != null)
									{
										double capacityPercentage = (double) machine.getFortronCapacity() / (double) totalCapacity;
										int amountToSet = (int) (totalFortron * capacityPercentage);
										this.transferFortron(machine, amountToSet - machine.getFortronEnergy());
									}
								}

								break;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Tries to transfer Fortron to a specific machine from this capacitor. Renders an animation on
	 * the client side.
	 * 
	 * @param machine: The machine to be transfered to.
	 * @param joules: The amount of energy to be transfered.
	 */
	private void transferFortron(IFortronFrequency machine, int joules)
	{
		if (machine != null)
		{
			if (joules > 0)
			{
				// Transfer energy to machine.
				joules = Math.min(joules, this.getTransmissionRate());
				int toBeInjected = machine.provideFortron(this.requestFortron(joules, false), false);
				toBeInjected = this.requestFortron(machine.provideFortron(toBeInjected, true), true);

				// Draw Beam Effect
				if (this.worldObj.isRemote && toBeInjected > 0)
				{
					ModularForceFieldSystem.proxy.renderBeam(this.worldObj, Vector3.add(new Vector3(this), 0.5), Vector3.add(new Vector3((TileEntityInventory) machine), 0.5), 0.6f, 0.6f, 1, 20);
				}
			}
			else
			{
				// Take energy from machine.
				joules = Math.min(Math.abs(joules), this.getTransmissionRate());
				int toBeEjected = this.provideFortron(machine.requestFortron(joules, false), false);
				toBeEjected = machine.requestFortron(this.provideFortron(toBeEjected, true), true);

				// Draw Beam Effect
				if (this.worldObj.isRemote && toBeEjected > 0)
				{
					ModularForceFieldSystem.proxy.renderBeam(this.worldObj, Vector3.add(new Vector3((TileEntityInventory) machine), 0.5), Vector3.add(new Vector3(this), 0.5), 0.6f, 0.6f, 1, 20);
				}
			}
		}
	}

	@Override
	public void onInventoryChanged()
	{
		super.onInventoryChanged();
		this.fortronTank.setCapacity((this.getModuleCount(ModularForceFieldSystem.itemModuleCapacity) * 10 + 500) * LiquidContainerRegistry.BUCKET_VOLUME);
	}

	/**
	 * Packet Methods
	 */
	@Override
	public List getPacketUpdate()
	{
		List objects = new LinkedList();
		objects.addAll(super.getPacketUpdate());
		objects.add(this.transferMode.ordinal());
		return objects;
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		super.onReceivePacket(packetID, dataStream);

		if (packetID == TPacketType.DESCRIPTION.ordinal())
		{
			this.transferMode = TransferMode.values()[dataStream.readInt()];
		}
		else if (packetID == TPacketType.TOGGLE_MODE.ordinal())
		{
			this.transferMode = this.transferMode.toggle();
		}
	}

	@Override
	public int getSizeInventory()
	{
		return 5;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.transferMode = TransferMode.values()[nbt.getInteger("transferMode")];
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("transferMode", this.transferMode.ordinal());
	}

	@Override
	public Set<IFortronFrequency> getLinkedDevices()
	{
		return FortronGrid.instance().get(this.worldObj, new Vector3(this), this.getTransmissionRange(), this.getFrequency());
	}

	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemStack)
	{
		if (slotID == 0 || slotID == 1)
		{
			return itemStack.getItem() instanceof ICard;
		}
		else
		{
			return itemStack.getItem() instanceof IModule;
		}
	}

	@Override
	public Set<ItemStack> getCards()
	{
		Set<ItemStack> cards = new HashSet<ItemStack>();
		cards.add(super.getCard());
		cards.add(this.getStackInSlot(1));
		return cards;
	}

	public TransferMode getTransferMode()
	{
		return this.transferMode;
	}

	@Override
	public int getTransmissionRange()
	{
		return 15 + this.getModuleCount(ModularForceFieldSystem.itemModuleScale);
	}

	@Override
	public int getTransmissionRate()
	{
		return 200 + 50 * this.getModuleCount(ModularForceFieldSystem.itemModuleSpeed);
	}
}