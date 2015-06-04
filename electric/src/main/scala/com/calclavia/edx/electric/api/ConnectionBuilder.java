package com.calclavia.edx.electric.api;

import com.calclavia.microblock.micro.MicroblockContainer;
import nova.core.block.Block;
import nova.core.util.Direction;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Builds connections
 *
 * @author Calclavia
 */
public class ConnectionBuilder<T> {

	private final Class<T> componentType;
	private Block block;
	public int connectMask = 0x3f;

	public ConnectionBuilder(Class<T> componentType) {
		this.componentType = componentType;
	}

	public ConnectionBuilder setConnectMask(int connectMask) {
		this.connectMask = connectMask;
		return this;
	}

	public ConnectionBuilder setBlock(Block block) {
		this.block = block;
		return this;
	}

	/**
	 * @return A supplier that provides adjacent nodes as connections.
	 */
	public Supplier<Set<T>> adjacentSupplier() {
		return this::adjacentNodes;
	}

	protected Set<T> adjacentNodes() {
		Map<Direction, Optional<Block>> masked = maskedAdjacentBlocks();

		return masked
			.values()
			.stream()
			.filter(Optional::isPresent)
			.map(Optional::get)
			.map(block -> block.getOp(componentType))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toSet());
	}

	public Supplier<Set<T>> adjacentWireSupplier() {
		return this::adjacentWires;
	}

	protected Set<T> adjacentWires() {
		Map<Direction, Optional<Block>> masked = maskedAdjacentBlocks();

		Map<Direction, Block> blocks = masked
			.entrySet()
			.stream()
			.filter(entry -> entry.getValue().isPresent())
			.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));

		Set<T> direct = blocks
			.values()
			.stream()
			.map(block -> block.getOp(componentType))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toSet());

		if (direct.size() > 0) {
			return direct;
		}

		return blocks
			.values()
			.stream()
			.map(block -> block.getOp(MicroblockContainer.class))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.flatMap(container ->
					//Check the sides of the microblock instead. We only want to connect to appropriate sides
					Arrays.stream(Direction.DIRECTIONS)
						.filter(
							//Find all directions except the facing dir
							dir ->
								!dir.toVector().toDouble().abs().equals(
									//TODO: Use HashBiMap
									blocks.entrySet()
										.stream()
										.filter(entry -> entry.getValue() == container.block)
										.map(Map.Entry::getKey)
										.findFirst()
										.get()
										.toVector()
										.toDouble()
										.abs()
								)
						)
						.map(container::get)
						.filter(Optional::isPresent)
						.map(Optional::get)
			)
			.map(microblock -> microblock.block)
			.map(block -> block.getOp(componentType))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toSet());
	}

	protected Map<Direction, Optional<Block>> maskedAdjacentBlocks() {
		return adjacentBlocks()
			.entrySet()
			.stream()
			.filter(entry -> connectMask == -1 || (connectMask & (1 << entry.getKey().ordinal())) != 0)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	protected Map<Direction, Optional<Block>> adjacentBlocks() {
		return Arrays.stream(Direction.DIRECTIONS).collect(Collectors.toMap(Function.identity(), dir -> block.world().getBlock(block.position().add(dir.toVector()))));
	}
}
