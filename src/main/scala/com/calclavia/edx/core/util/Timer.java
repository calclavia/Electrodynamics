package com.calclavia.edx.core.util;

import com.calclavia.edx.core.EDX;

import java.util.HashMap;

public class Timer<K> {
	private final HashMap<K, Integer> clientTimer = new HashMap<>();
	private final HashMap<K, Integer> serverTimer = new HashMap<>();

	public void put(K key, int defaultTime) {
		getTimeMap().put(key, defaultTime);
	}

	public boolean containsKey(K key) {
		return getTimeMap().containsKey(key);
	}

	public void remove(K key) {
		getTimeMap().remove(key);
	}

	public int decrease(K key) {
		return decrease(key, 1);
	}

	public int decrease(K key, int amount) {
		int timeLeft = getTimeMap().get(key) - amount;
		getTimeMap().put(key, timeLeft);
		return timeLeft;
	}

	public HashMap<K, Integer> getTimeMap() {
		if (EDX.network().isServer()) {
			return serverTimer;
		}

		return clientTimer;
	}

}
