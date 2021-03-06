package org.egordorichev.lasttry.item.block;

import com.badlogic.gdx.utils.JsonValue;
import org.egordorichev.lasttry.Globals;
import org.egordorichev.lasttry.entity.drop.DroppedItem;
import org.egordorichev.lasttry.graphics.Graphics;
import org.egordorichev.lasttry.inventory.ItemHolder;
import org.egordorichev.lasttry.item.block.helpers.BlockHelper;

public class MultiTileBlock extends Block {
	public MultiTileBlock(String id, boolean loadIcon) {
		super(id, loadIcon);
	}

	@Override
	public void renderBlock(int x, int y, byte binary) {
		byte hp = Globals.getWorld().blocks.getHP(x, y);
		Graphics.batch.draw(this.tiles[BlockHelper.mtb.getY(hp)][BlockHelper.mtb.getX(hp)], x * Block.SIZE, y * Block.SIZE);
	}

	@Override
	protected void loadFields(JsonValue root) {
		super.loadFields(root);

		if (root.has("size")) {
			JsonValue size = root.get("size");

			this.width = size.get(0).asByte();
			this.height = size.get(1).asByte();
		} else {
			this.width = 2;
			this.height = 2;
		}
	}

	public int getGridWidth() {
		return this.width;
	}

	public int getGridHeight() {
		return this.height;
	}

	@Override
	public boolean canBeUsed(short x, short y) {
		if (!super.canBeUsed(x, y)) {
			return false;
		}

		for (int j = y; j < y + this.height; j++) {
			for (int i = x; i < x + this.width; i++) {
				if (Globals.getWorld().blocks.get(i, j) != null) {
					return false;
				}
			}
		}

		// todo: no placing in air

		return true;
	}

	@Override
	public boolean use(short x, short y) {
		for (int j = y; j < y + this.height; j++) {
			for (int i = x; i < x + this.width; i++) {
				Globals.getWorld().blocks.set(this.id, i, j);

				byte hp = 0;

				hp = BlockHelper.mtb.setX(hp, (byte) (i - x));
				hp = BlockHelper.mtb.setY(hp, (byte) (this.height - (j - y) - 1));

				Globals.getWorld().blocks.setHP(hp, i, j);
			}
		}

		return true;
	}

	@Override
	public void die(short x, short y) { // TODO: kill other
		Globals.getWorld().blocks.set(null, x, y);
		Globals.entityManager.spawnBlockDrop(new DroppedItem(new ItemHolder(this, 1)), Block.SIZE * x, Block.SIZE * y);
	}
}