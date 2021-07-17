package com.archyx.aureliumskills.loot;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LootTable {

	private final List<LootPool> pools;
	
	public LootTable(List<LootPool> pools) {
		this.pools = pools;
	}

	@Nullable
	public LootPool getPool(String name) {
		for (LootPool pool : pools) {
			if (pool.getName().equals(name)) {
				return pool;
			}
		}
		return null;
	}

	public List<LootPool> getPools() {
		return pools;
	}

}
