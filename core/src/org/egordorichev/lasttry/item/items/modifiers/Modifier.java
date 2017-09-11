package org.egordorichev.lasttry.item.items.modifiers;

import org.egordorichev.lasttry.LastTry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Modifier {
	private static final Logger logger = LoggerFactory.getLogger(Modifier.class);

	protected String name;
	protected String id;
	protected int damage;
	protected int speed;
	protected int criticalStrikeChance;
	protected int manaCost;
	protected int size;
	protected int velocity;
	protected int knockback;
	protected int mana;
	protected int movementSpeed;
	protected int defense;

	public Modifier(String id, String name, int damage, int speed, int criticalStrikeChance, int manaCost, int size,
	                int velocity, int knockback, int mana, int movementSpeed, int defense) {

		if (Modifiers.MODIFIER_CACHE.get(id) != null) {
			logger.error("Modifier with id " + id + " is already defined!");
			LastTry.abort();
		}

		Modifiers.MODIFIER_CACHE.put(id, this);

		this.id = id;
		this.name = name;
		this.damage = damage;
		this.speed = speed;
		this.criticalStrikeChance = criticalStrikeChance;
		this.manaCost = manaCost;
		this.size = size;
		this.velocity = velocity;
		this.knockback = knockback;
		this.mana = mana;
		this.movementSpeed = movementSpeed;
		this.defense = defense;
	}

	public int getDamage() {
		return this.damage;
	}

	public int getSpeed() {
		return this.speed;
	}

	public int getCriticalStrikeChance() {
		return this.criticalStrikeChance;
	}

	public int getManaCost() {
		return this.manaCost;
	}

	public int getSize() {
		return this.size;
	}

	public int getVelocity() {
		return this.velocity;
	}

	public int getKnockback() {
		return this.knockback;
	}

	public int getMana() {
		return this.mana;
	}

	public int getMovementSpeed() {
		return this.movementSpeed;
	}

	public int getDefense() {
		return this.defense;
	}

	public String getID() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public static Modifier fromID(String id) {
		if (id == null || id == "") {
			return null;
		}

		return Modifiers.MODIFIER_CACHE.get(id);
	}
}