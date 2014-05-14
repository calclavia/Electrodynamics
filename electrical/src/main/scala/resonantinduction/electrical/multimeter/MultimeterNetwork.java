package resonantinduction.electrical.multimeter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import resonant.lib.utility.LanguageUtility;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import universalelectricity.api.net.IUpdate;
import universalelectricity.api.vector.Vector3;
import universalelectricity.core.net.Network;
import universalelectricity.core.net.NetworkTickHandler;

public class MultimeterNetwork extends Network<MultimeterNetwork, PartMultimeter> implements IUpdate
{
	public final List<String> displayInformation = new ArrayList<String>();

	/**
	 * Maximum data points a graph can store.
	 */
	private int maxData = 1;
	/**
	 * The available graphs to be handled.
	 */
	public final List<Graph> graphs = new ArrayList<Graph>();

	public final GraphL energyGraph = new GraphL("energy", maxData);
	public final GraphL powerGraph = new GraphL("power", maxData);
	public final GraphL energyCapacityGraph = new GraphL("capacity", 1);
	public final GraphL voltageGraph = new GraphL("voltage", maxData);
	public final GraphD torqueGraph = new GraphD("torque", maxData);
	public final GraphD angularVelocityGraph = new GraphD("speed", maxData);
	public final GraphI fluidGraph = new GraphI("fluid", maxData);
	public final GraphF thermalGraph = new GraphF("temperature", maxData);
	public final GraphI pressureGraph = new GraphI("pressure", maxData);

	/**
	 * The absolute center of the multimeter screens.
	 */
	public Vector3 center = new Vector3();

	/**
	 * The relative bound sizes.
	 */
	public Vector3 upperBound = new Vector3();
	public Vector3 lowerBound = new Vector3();

	/**
	 * The overall size of the multimeter
	 */
	public Vector3 size = new Vector3();

	private long queueGraphValue = 0;
	private long queueGraphCapacity = 0;
	private boolean doUpdate = false;

	/**
	 * If the screen is not a perfect rectangle, don't render.
	 */
	public boolean isEnabled = true;
	public PartMultimeter primaryMultimeter = null;

	public MultimeterNetwork()
	{
		super(PartMultimeter.class);
		graphs.add(energyGraph);
		graphs.add(powerGraph);
		graphs.add(energyCapacityGraph);
		graphs.add(voltageGraph);
		graphs.add(torqueGraph);
		graphs.add(angularVelocityGraph);
		graphs.add(fluidGraph);
		graphs.add(thermalGraph);
		graphs.add(pressureGraph);
	}

	public String getDisplay(int graphID)
	{
		Graph graph = graphs.get(graphID);

		String graphValue = "";

		if (graph == energyGraph)
			graphValue = UnitDisplay.getDisplay(energyGraph.get(), Unit.JOULES);

		if (graph == powerGraph)
			graphValue = UnitDisplay.getDisplay(powerGraph.get(), Unit.WATT);

		if (graph == energyCapacityGraph)
			graphValue = UnitDisplay.getDisplay(energyCapacityGraph.get(), Unit.JOULES);

		if (graph == voltageGraph)
			graphValue = UnitDisplay.getDisplay(voltageGraph.get(), Unit.VOLTAGE);

		if (graph == torqueGraph)
			graphValue = UnitDisplay.getDisplayShort(torqueGraph.get(), Unit.NEWTON_METER);

		if (graph == angularVelocityGraph)
			graphValue = UnitDisplay.roundDecimals(angularVelocityGraph.get()) + " rad/s";

		if (graph == fluidGraph)
			graphValue = UnitDisplay.getDisplay(fluidGraph.get(), Unit.LITER);

		if (graph == thermalGraph)
			graphValue = UnitDisplay.roundDecimals(thermalGraph.get()) + " K";

		if (graph == pressureGraph)
			graphValue = UnitDisplay.roundDecimals(pressureGraph.get()) + " Pa";

		return getLocalized(graph) + ": " + graphValue;

	}

	public String getLocalized(Graph graph)
	{
		return LanguageUtility.getLocal("tooltip.graph." + graph.name);
	}

	public boolean isPrimary(PartMultimeter check)
	{
		return primaryMultimeter == check;
	}

	@Override
	public void addConnector(PartMultimeter connector)
	{
		super.addConnector(connector);
		NetworkTickHandler.addNetwork(this);
	}

	@Override
	public void update()
	{
		for (Graph graph : graphs)
		{
			graph.doneQueue();
		}

		doUpdate = false;
	}

	public void markUpdate()
	{
		doUpdate = true;
	}

	@Override
	public boolean canUpdate()
	{
		return doUpdate && continueUpdate();
	}

	@Override
	public boolean continueUpdate()
	{
		return getConnectors().size() > 0;
	}

	@Override
	public boolean isValidConnector(Object node)
	{
		return node instanceof PartMultimeter && ((PartMultimeter) node).world() != null && ((PartMultimeter) node).tile() != null;
	}

	@Override
	public void reconstruct()
	{
		if (getConnectors().size() > 0)
		{
			primaryMultimeter = null;
			upperBound = null;
			lowerBound = null;
			super.reconstruct();
			center = upperBound.midPoint(lowerBound);

			/**
			 * Make bounds relative.
			 */
			upperBound.subtract(center);
			lowerBound.subtract(center);
			size = new Vector3(Math.abs(upperBound.x) + Math.abs(lowerBound.x), Math.abs(upperBound.y) + Math.abs(lowerBound.y), Math.abs(upperBound.z) + Math.abs(lowerBound.z));

			double area = (size.x != 0 ? size.x : 1) * (size.y != 0 ? size.y : 1) * (size.z != 0 ? size.z : 1);
			isEnabled = area == getConnectors().size();

			NetworkTickHandler.addNetwork(this);

			Iterator<PartMultimeter> it = this.getConnectors().iterator();

			while (it.hasNext())
			{
				PartMultimeter connector = it.next();
				connector.updateDesc();
				connector.updateGraph();
			}

			doUpdate = true;
		}
	}

	@Override
	protected void reconstructConnector(PartMultimeter node)
	{
		node.setNetwork(this);

		if (primaryMultimeter == null)
			primaryMultimeter = node;

		if (upperBound == null)
		{
			upperBound = node.getPosition().translate(1);
		}

		if (lowerBound == null)
		{
			lowerBound = node.getPosition();
		}

		upperBound = upperBound.max(node.getPosition().translate(1));
		lowerBound = lowerBound.min(node.getPosition());
	}

	@Override
	public MultimeterNetwork newInstance()
	{
		return new MultimeterNetwork();
	}

	public void load(NBTTagCompound nbt)
	{
		NBTTagList nbtList = nbt.getTagList("graphs");

		for (int i = 0; i < nbtList.tagCount(); ++i)
		{
			NBTTagCompound nbtCompound = (NBTTagCompound) nbtList.tagAt(i);
			graphs.get(i).load(nbtCompound);
		}
	}

	public NBTTagCompound save()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList data = new NBTTagList();

		for (Graph graph : graphs)
		{
			data.appendTag(graph.save());
		}

		nbt.setTag("graphs", data);

		return nbt;
	}
}
