package org.egordorichev.lasttry.item;

import org.egordorichev.lasttry.injection.CoreRegistry;
import org.egordorichev.lasttry.injection.InjectionHelper;

public class Tile extends Item {
	public Tile(String id) {
		super(id);
	}

	/**
	 * Checks if the current block has a texture that connects to the other
	 * item.
	 *
	 * @param other
	 * @return
	 */
	protected boolean canConnect(Item other) {
		if (other == null) {
			return false;
		}

		return true;
	}

	/**
	 * Determines if the current texture can be connected to the texture of the
	 * item indicated by the given item ID.
	 *
	 * @param itemID
	 * @return
	 */
	protected boolean canConnect(String itemID) {
		if (itemID == null) {
			return false;
		}

		Item i1 = CoreRegistry.get(ItemManager.class).getItem(itemID);

		if (i1 == null) {
			return false;
		}

		return canConnect(i1);
	}
}
