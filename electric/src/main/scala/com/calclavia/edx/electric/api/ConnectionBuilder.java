package com.calclavia.edx.electric.api;

import nova.core.block.Block;
import nova.core.util.Direction;
import nova.core.util.math.Vector3DUtil;
import nova.microblock.micro.MicroblockContainer;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Builds connections
 * @author Calclavia
 */
public class ConnectionBuilder<T> {

	private final Class<T> componentType;
	public Supplier<Integer> connectMask = () -> 0x3f;
	private Block block;

	public ConnectionBuilder(Class<T> componentType) {
		this.componentType = componentType;
	}

	public ConnectionBuilder setConnectMask(Supplier<Integer> connectMask) {
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
			.map(block -> block.components.getOp(componentType))
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
			.collect(
				Collectors.toMap(
				Map.Entry::getKey,
				entry -> entry.getValue().get()
			)
			);

		Set<T> direct = blocks
			.values()
			.stream()
			.map(block -> block.components.getOp(componentType))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toSet());

		if (direct.size() > 0) {
			return direct;
		}

		return blocks
			.values()
			.stream()
			.map(block -> block.components.getOp(MicroblockContainer.class))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.flatMap(container ->
					//Check the sides of the microblock instead. We only want to connect to appropriate sides
					Arrays.stream(Direction.VALID_DIRECTIONS)
						.filter(
							//Find all directions except the facing dir and its opposite
							dir ->
								!Vector3DUtil.abs(dir.toVector()).equals(
									Vector3DUtil.abs(
										//TODO: Use HashBiMap
										blocks.entrySet()
											.stream()
											.filter(entry -> entry.getValue() == container.block)
											.map(Map.Entry::getKey)
											.findFirst()
											.get()
											.toVector()
									)
								)
						)
						.map(container::get)
						.filter(Optional::isPresent)
						.map(Optional::get)
			)
			.map(microblock -> microblock.block)
			.map(block -> block.components.getOp(componentType))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toSet());
	}

	protected Map<Direction, Optional<Block>> maskedAdjacentBlocks() {
		return adjacentBlocks()
			.entrySet()
			.stream()
			.filter(entry -> (connectMask.get() & (1 << entry.getKey().ordinal())) != 0)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	protected Map<Direction, Optional<Block>> adjacentBlocks() {
		return Arrays.stream(Direction.VALID_DIRECTIONS).collect(Collectors.toMap(Function.identity(), dir -> block.world().getBlock(block.position().add(dir.toVector()))));
	}
}
