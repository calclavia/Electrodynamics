package com.calclavia.edx.electric.grid.api;

import nova.core.block.Block;
import nova.core.util.Direction;
import nova.core.world.World;

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
	private final World world;
	private final Block block;

	public int connectMask = 0x3f;
	public int connectedMask = 0;

	public ConnectionBuilder(Class<T> componentType, Block block) {
		this.componentType = componentType;
		this.world = block.world();
		this.block = block;
	}

	public ConnectionBuilder setConnectMask(int connectMask) {
		this.connectMask = connectMask;
		return this;
	}

	/**
	 * @return A supplier that provides adjacent nodes as connections.
	 */
	public Supplier<Set<T>> adjacentSupplier() {
		return () -> adjacentNodes();
	}

	public Set<T> adjacentNodes() {
		Map<Direction, Optional<Block>> adjacentBlocks = adjacentBlocks();

		Map<Direction, Optional<Block>> filtered = adjacentBlocks
			.entrySet()
			.stream()
			.filter(entry -> connectMask == -1 || (connectMask & (1 << entry.getKey().ordinal())) != 0)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		connectedMask = filtered
			.keySet()
			.stream()
			.map(Direction::ordinal)
			.map(dir -> 1 << dir)
			.reduce(0, (a, b) -> a | b);

		return filtered
			.values()
			.stream()
			.filter(Optional::isPresent)
			.map(Optional::get)
			.map(block -> block.getOp(componentType))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toSet());
	}

	protected Map<Direction, Optional<Block>> adjacentBlocks() {
		return Arrays.stream(Direction.DIRECTIONS).collect(Collectors.toMap(Function.identity(), dir -> world.getBlock(block.position().add(dir.toVector()))));
	}
}
