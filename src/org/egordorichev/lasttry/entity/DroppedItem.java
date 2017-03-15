package org.egordorichev.lasttry.entity;

import org.egordorichev.lasttry.item.ItemHolder;
import org.egordorichev.lasttry.util.Assets;
import org.egordorichev.lasttry.util.Direction;
import org.newdawn.slick.Animation;

public class DroppedItem extends Entity {
	/**
	 * Item contained by the item entity.
	 */
	private final ItemHolder holder;

	public DroppedItem(ItemHolder holder) {
		super(EntityID.droppedItem, true);
		this.holder = holder;
		this.state = State.FALLING;
		this.texture = Assets.boxTexture;
		this.shouldUpdate = true;

		Animation anim = new Animation();
		anim.addFrame(this.texture, 1);
		anim.setLooping(false);

		this.animations[State.JUMPING.getId()] = anim;
		this.animations[State.MOVING.getId()] = anim;
		this.animations[State.IDLE.getId()] = anim;
		this.animations[State.DEAD.getId()] = anim;
		this.animations[State.FALLING.getId()] = anim;
		this.animations[State.FLYING.getId()] = anim;
		this.renderBounds.width = 14;
		this.renderBounds.height = 16;
		this.hitbox = renderBounds;
	}
	
	@Override
	public void update(int dt) {
		super.update(dt);
		// TODO: On collision with player, destroy this entity, and add the item
		// to the player's inventory.
	}

}
