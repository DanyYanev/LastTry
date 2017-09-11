package org.egordorichev.lasttry.item.block.plant;

import java.util.Arrays;

import org.egordorichev.lasttry.Globals;
import org.egordorichev.lasttry.entity.drop.DroppedItem;
import org.egordorichev.lasttry.graphics.Graphics;
import org.egordorichev.lasttry.injection.CoreRegistry;
import org.egordorichev.lasttry.injection.InjectionHelper;
import org.egordorichev.lasttry.inventory.ItemHolder;
import org.egordorichev.lasttry.item.Item;
import org.egordorichev.lasttry.item.ItemManager;
import org.egordorichev.lasttry.item.block.Block;
import org.egordorichev.lasttry.item.block.helpers.BlockHelper;
import org.egordorichev.lasttry.util.Util;

public class Plant extends Block {
	private static final String[] DEFAULT_GROUND = new String[] { "lt:dirt" };
	public static final byte GROW_THRESHOLD = 20;
	private String[] groundTypes;

	public Plant(String id) {
		this(id, DEFAULT_GROUND);
	}

	public Plant(String id, String... groundTypes) {
		super(id);
		this.groundTypes = groundTypes;
	}

	@Override
	public void onNeighborChange(short x, short y, short nx, short ny) {
		if (ny == y - 1 && x == nx) {
			this.die(x, y);
		}
	}

	@Override
	public void die(short x, short y) {
		byte hp = Globals.getWorld().blocks.getHP(x, y);

		if (BlockHelper.plant.isBlooming(hp)) {
			Globals.entityManager.spawnBlockDrop(new DroppedItem(new ItemHolder(this, Util.random(1, 3))),
					x * Block.SIZE - Block.SIZE / 2, y * Block.SIZE - Block.SIZE / 2);
		}

		if (BlockHelper.plant.hasGrown(hp)) {
			String seeds = getSeedsFor(this.id);

			ItemManager itemManager = CoreRegistry.get(ItemManager.class);

			if (!seeds.isEmpty()) {
				Globals.entityManager.spawnBlockDrop(
						new DroppedItem(new ItemHolder(itemManager.getItem(seeds), Util.random(1, 3))),
						x * Block.SIZE - Block.SIZE / 2, y * Block.SIZE - Block.SIZE / 2);
			}
		}

		Globals.getWorld().blocks.set(null, x, y);
	}

	public static String getSeedsFor(String id) {
		switch (id) {
		case "lt:day_bloom":
			return "lt:day_bloom_seeds";
		case "lt:blink_root":
			return "lt:blink_root_seeds";
		case "lt:moon_glow":
			return "lt:moon_glow_seeds";
		case "lt:death_weed":
			return "lt:death_weed_seeds";
		case "lt:fire_blossom":
			return "lt:fire_blossom_seeds";
		case "lt:water_leaf":
			return "lt:water_leaf_seeds";
		case "lt:silver_thorn":
			return "lt:silver_thorn_seeds";
		default:
			return null;
		}
	}

	@Override
	public void renderBlock(int x, int y, byte binary) {
		byte hp = Globals.getWorld().blocks.getHP(x, y);

		int tx = 0;

		if (BlockHelper.plant.isBlooming(hp)) {
			tx = 1;
		} else if (BlockHelper.plant.hasGrown(hp)) {
			tx = 2;
		}

		Graphics.batch.draw(this.tiles[0][tx], x * Block.SIZE, y * Block.SIZE);
	}

	@Override
	protected boolean renderCracks() {
		return false;
	}

	@Override
	public void updateBlock(int x, int y) {
		byte hp = Globals.getWorld().blocks.getHP(x, y);

		if (BlockHelper.plant.hasGrown(Plant.GROW_THRESHOLD)) {
			if (this.canBloom()) {
				Globals.getWorld().blocks.setHP(BlockHelper.plant.setGrowLevel(hp, (byte) (GROW_THRESHOLD + 1)), x, y);
			} else {
				Globals.getWorld().blocks.setHP(BlockHelper.plant.setGrowLevel(hp, GROW_THRESHOLD), x, y);
			}
		} else {
			Globals.getWorld().blocks
					.setHP(BlockHelper.plant.setGrowLevel(hp, (byte) (BlockHelper.plant.getGrowLevel(hp) + 1)), x, y);
		}
	}

	protected boolean canBloom() {
		return true;
	}

	public boolean canBeGrownAt(int x, int y) {
		// Check if space occupied
		String self = Globals.getWorld().blocks.getID(x, y - 1);
		if (self != null) {
			return false;
		}
		// Check if valid ground tile.
		String id = Globals.getWorld().blocks.getID(x, y - 1);
		if (id == null) {
			return false;
		}
		return Arrays.binarySearch(groundTypes, id) >= 0;
	}
}