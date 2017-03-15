package org.egordorichev.lasttry.world;

import org.egordorichev.lasttry.LastTry;
import org.egordorichev.lasttry.entity.Drop;
import org.egordorichev.lasttry.entity.DroppedItem;
import org.egordorichev.lasttry.entity.Enemy;
import org.egordorichev.lasttry.entity.Entity;
import org.egordorichev.lasttry.item.*;
import org.egordorichev.lasttry.util.FileReader;
import org.egordorichev.lasttry.util.FileWriter;
import org.egordorichev.lasttry.util.Rectangle;
import org.egordorichev.lasttry.world.tile.TileData;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class World {
	/**
	 * Current world version generated by the game. Future versions may
	 * increment this number.
	 */
	public static final int CURRENT_VERSION = 1;
	/**
	 * Value indicating if the world has been fully loaded <i>(Entities placed,
	 * tiles added, dimensions set, etc.)</i>
	 */
	private boolean loaded;
	/**
	 * World width in tiles.
	 */
	private int width;
	/**
	 * World height in tiles.
	 */
	private int height;
	/**
	 * World version.
	 */
	private int version;
	/**
	 * World name.
	 */
	private String name;
	/**
	 * Array of tile data. 2D coordinates are encoded by:
	 * 
	 * <pre>
	 * index = x + y * world - width
	 * </pre>
	 */
	private TileData[] tiles;
	/**
	 * List of entities in the world.
	 */
	private List<Entity> entities = new ArrayList<>();;
	/**
	 * List of entities to be deleted in the next tick.
	 */
	private List<Entity> deadEntities = new ArrayList<>();
	/**
	 * List of items in the world
	 */
	private List<ItemHolder> items = new ArrayList<>();
	/**
	 * The type of terrain generated by entering expert-mode.
	 * {@link org.egordorichev.lasttry.world.World.ExpertType ExpertType}.
	 */
	private ExpertType expertType;
	/**
	 * Value indicating if the world has entered expert-mode.
	 */
	private boolean expert;

	public World(String name) {
		this.loaded = false;
		this.name = name;

		Item.preload();

		this.load();
	}

	/**
	 * Render the world.
	 */
	public void render() {
		int windowWidth = LastTry.getWindowWidth();
		int windowHeight = LastTry.getWindowHeight();
		int tww = windowWidth / Block.TEX_SIZE;
		int twh = windowHeight / Block.TEX_SIZE;
		int tcx = (int) LastTry.camera.getX() / Block.TEX_SIZE;
		int tcy = (int) LastTry.camera.getY() / Block.TEX_SIZE;

		int minY = Math.max(0, tcy - 2);
		int maxY = Math.min(this.height - 1, tcy + twh + 2);
		int minX = Math.max(0, tcx - 2);
		int maxX = Math.min(this.width - 1, tcx + tww + 2);

		// Iterate coordinates, exclude ones not visible to the camera
		for (int y = minY; y < maxY; y++) {
			for (int x = minX; x < maxX; x++) {
				TileData tileData = this.getTile(x, y);
				tileData.render(x, y);
			}
		}

		// Render entities, exclude ones not visible to the camera
		for (Entity entity : this.entities) {
			int gx = entity.getGridX();
			int gy = entity.getGridY();
			int w = entity.getGridWidth();
			int h = entity.getGridHeight();
			if ((gx > minX - w && gx < maxX + w) && (gy > minY - h && gy < maxY + h)) {
				entity.render();
			}
		}

		// Render items
		for (ItemHolder item : this.items) {
			int gx = item.getGridX();
			int gy = item.getGridY();
			// Make sure that items are fully off-screen before skipping
			// rendering
			if ((gx > minX - 5 && gx < maxX + 5) && (gy > minY - 5 && gy < maxY + 5)) {
				item.render();
			}
		}
	}

	/**
	 * Update the world.
	 * 
	 * @param dt
	 *            The milliseconds passed since the last update.
	 */
	public void update(int dt) {
		// Remove dead entities
		for (Entity entity : this.deadEntities) {
			this.entities.remove(entity);
		}
		this.deadEntities.clear();
		// Update alive entities.
		for (Entity entity : this.entities) {
			entity.update(dt);
		}
	}

	/**
	 * Spawn an enemy in the world <i>(Type dictated by the id)</i>
	 * 
	 * @param name
	 *            Name of the type of entity to spawn.
	 * @param x
	 *            X-position to spawn entity at.
	 * @param y
	 *            Y-position to spawn entity at.
	 * @return
	 */
	public Enemy spawnEnemy(int id, int x, int y) {
		Enemy enemy = Enemy.create(id);

		if (enemy != null) {
			enemy.spawn(x, y);
			this.entities.add(enemy);
		}

		return enemy;
	}

	/**
	 * Spawn a dropped item in the world.
	 * 
	 * @param drop
	 *            Drop data.
	 * @param x
	 *            X-position to spawn dropped item at.
	 * @param y
	 *            Y-position to spawn dropped item at.
	 */
	public void spawnDrop(Drop drop, float x, float y) {
		DroppedItem de = new DroppedItem(drop.createHolder());
		de.spawn(x / Block.TEX_SIZE, y / Block.TEX_SIZE);
		this.entities.add(de);
		int vel = 10;
		de.setVelocity((LastTry.random.nextFloat() * (vel * 2)) - vel, -3);
	}

	/**
	 * Add an entity to the removal list. They will be removed the next tick.
	 * 
	 * @param entity
	 *            Entity to remove.
	 */
	public void remove(Entity entity) {
		this.deadEntities.add(entity);
	}

	/**
	 * Return the TileData for the given position.
	 * 
	 * @param x
	 *            X-position of the world.
	 * @param y
	 *            Y-position of the world.
	 * @return
	 */
	public TileData getTile(int x, int y) {
		return this.tiles[x + y * this.width];
	}

	/**
	 * Set a block in the world at the given position.
	 * 
	 * @param block
	 *            Block to place.
	 * @param x
	 *            X-position of the world.
	 * @param y
	 *            Y-position of the world.
	 */
	public void setBlock(Block block, int x, int y) {
		TileData data = this.getTile(x, y);

		data.block = block;
		data.blockHp = data.maxHp;
		data.data = 0;
	}

	/**
	 * Set a wall in the world at the given position.
	 * 
	 * @param wall
	 *            Wall to place.
	 * @param x
	 *            X-position of the world.
	 * @param y
	 *            Y-position of the world.
	 */
	public void setWall(Wall wall, int x, int y) {
		TileData data = this.getTile(x, y);

		data.wall = wall;
		data.wallHp = data.maxHp;
		data.data = 0;
	}

	/**
	 * Set the data tag of the tile in the world at the given position.
	 * 
	 * @param data
	 *            Tag value.
	 * @param x
	 *            X-position of the world.
	 * @param y
	 *            Y-position of the world.
	 */
	public void setData(byte data, int x, int y) {
		TileData tileData = this.getTile(x, y);
		tileData.data = data;
	}

	/**
	 * Return the data tag of the tile in the world at the given position.
	 * 
	 * @param x
	 *            X-position of the world.
	 * @param y
	 *            Y-position of the world.
	 * @returnd Data tag value.
	 */
	public byte getData(int x, int y) {
		TileData tileData = this.getTile(x, y);
		return tileData.data;
	}

	/**
	 * Return the block in the world at the given position.
	 * 
	 * @param x
	 *            X-position of the world.
	 * @param y
	 *            Y-position of the world.
	 * @return Block in world.
	 */
	public Block getBlock(int x, int y) {
		TileData data = this.getTile(x, y);
		return data.block;
	}

	/**
	 * Return the wall in the world at the given position.
	 * 
	 * @param x
	 *            X-position of the world.
	 * @param y
	 *            Y-position of the world.
	 * @return Wall in world.
	 */
	public Wall getWall(int x, int y) {
		TileData data = this.getTile(x, y);
		return data.wall;
	}

	/**
	 * Return the ID of the block in the world at the given position.
	 * 
	 * @param x
	 *            X-position of the world.
	 * @param y
	 *            Y-position of the world.
	 * @return Block ID in world.
	 */
	public int getBlockId(int x, int y) {
		if (!this.isInside(x, y)) {
			return 0;
		}

		TileData data = this.getTile(x, y);

		if (data.block == null) {
			return 0;
		}

		return data.block.getId();
	}

	/**
	 * Return the ID of the wall in the world at the given position.
	 * 
	 * @param x
	 *            X-position of the world.
	 * @param y
	 *            Y-position of the world.
	 * @return Wall ID in world.
	 */
	public int getWallId(int x, int y) {
		if (!this.isInside(x, y)) {
			return 0;
		}

		TileData data = this.getTile(x, y);

		if (data.wall == null) {
			return 0;
		}

		return data.wall.getId();
	}

	/**
	 * Check if the given bounds collide with blocks in the world.
	 * 
	 * @param bounds
	 *            Bounds to check collision for.
	 * @return If bounds collide with world's blocks.
	 */
	public boolean isColliding(Rectangle bounds) {
		Rectangle gridBounds = new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);

		gridBounds.x /= Block.TEX_SIZE;
		gridBounds.y /= Block.TEX_SIZE;
		gridBounds.width /= Block.TEX_SIZE;
		gridBounds.height /= Block.TEX_SIZE;

		for (int y = (int) gridBounds.y - 1; y < gridBounds.y + gridBounds.height + 1; y++) {
			for (int x = (int) gridBounds.x - 1; x < gridBounds.x + gridBounds.width + 1; x++) {
				if (!this.isInside(x, y)) {
					return true;
				}

				TileData data = this.getTile(x, y);

				if (data.block == null || !data.block.isSolid()) {
					continue;
				}

				Rectangle blockRect = new Rectangle(x * Block.TEX_SIZE, y * Block.TEX_SIZE, Block.TEX_SIZE,
						Block.TEX_SIZE);

				if (blockRect.intersects(bounds)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Check if the world has been loaded.
	 * 
	 * @return World loaded.
	 */
	public boolean isLoaded() {
		return this.loaded;
	}

	/**
	 * Check if the given position resides within the world's bounds.
	 * 
	 * @param x
	 *            X-position to check.
	 * @param y
	 *            Y-position to check.
	 * @return Position is inside world.
	 */
	public boolean isInside(int x, int y) {
		return (x >= 0 && x < this.width && y >= 0 && y < this.height);
	}

	/**
	 * Return the world's width.
	 * 
	 * @return World width.
	 */
	public int getWidth() {
		return this.width;
	}

	/**
	 * Return the world's height.
	 * 
	 * @return World height.
	 */
	public int getHeight() {
		return this.height;
	}

	/**
	 * Return the world's name.
	 * 
	 * @return World name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Return the world's file path.
	 * 
	 * @return World file path.
	 */
	public String getFilePath() {
		return "assets/worlds/" + this.name + ".wld";
	}

	public ExpertType getEvil() {
		return this.expertType;
	}

	public boolean isExpert() {
		return this.expert;
	}

	/**
	 * Generate the world.
	 */
	private void generate() {
		this.width = 500;
		this.height = 500;
		this.version = World.CURRENT_VERSION;
		this.expert = true; // TODO: replace

		if (LastTry.random.nextBoolean()) {
			this.expertType = ExpertType.CORRUPTION;
		} else {
			this.expertType = ExpertType.CRIMSON;
		}

		int totalSize = this.width * this.height;
		this.tiles = new TileData[totalSize];

		int tiles[][] = new int[this.width][this.height];

		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < this.width; x++) {
				if (y == 120) {
					tiles[x][y] = ItemID.grassBlock;
				} else if (y > 120) {
					tiles[x][y] = ItemID.dirtBlock;
				} else {
					tiles[x][y] = 0;
				}
			}
		}

		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < this.width; x++) {
				int id = tiles[x][y];
				this.tiles[x + y * this.width] = new TileData((Block) Item.fromId(id), Wall.getForBlockId(id));
			}
		}

		System.out.println("done generating!");

		this.loaded = true;
	}

	private void load() {
		try {
			FileReader stream = new FileReader(this.getFilePath());

			// Header
			int version = stream.readInt32();
			if (version > World.CURRENT_VERSION) {
				throw new RuntimeException("Unsupported version");
			}

			String worldName = stream.readString();

			this.name = worldName;
			this.width = stream.readInt32();
			this.height = stream.readInt32();
			this.expert = stream.readBoolean();
			this.expertType = (stream.readBoolean()) ? ExpertType.CRIMSON : ExpertType.CORRUPTION;

			// Tile data
			int totalSize = this.width * this.height;
			this.tiles = new TileData[totalSize];

			for (int i = 0; i < totalSize; i++) {
				this.tiles[i] = new TileData((Block) Item.fromId(stream.readInt32()),
						(Wall) Item.fromId(stream.readInt32()));

				// TODO: RLE
			}

			if (stream.readBoolean() == false) {
				throw new RuntimeException("Verification failed");
			}

			worldName = stream.readString();

			if (!worldName.equals(this.name)) {
				throw new RuntimeException("Verification failed");
			}

			// Verification
		} catch (FileNotFoundException exception) {
			this.generate();
			// this.save();
		}

		System.out.println("done loading!");
		this.loaded = true;
	}

	public void save() {
		FileWriter stream = new FileWriter(this.getFilePath());

		// Header
		stream.writeInt32(this.version);
		stream.writeString(this.name);
		stream.writeInt32(this.width);
		stream.writeInt32(this.height);
		stream.writeBoolean(this.expert);
		stream.writeBoolean((this.expertType == ExpertType.CRIMSON) ? true : false);

		// Tile data
		int totalSize = this.width * this.height;
		for (int i = 0; i < totalSize; i++) {
			TileData data = this.tiles[i];

			int blockId = 0;
			int wallId = 0;

			if (data.block != null) {
				blockId = data.block.getId();
			}

			if (data.wall != null) {
				wallId = data.wall.getId();
			}

			stream.writeInt32(blockId);
			stream.writeInt32(wallId);

			// TODO: RLE
		}

		// Verification
		stream.writeBoolean(true);
		stream.writeString(this.name);
		stream.close();

		System.out.println("done saving!");
	}

	/**
	 * Enumeration for the type of terrain to generate when the world is set to
	 * expert mode.
	 */
	public enum ExpertType {
		CORRUPTION, CRIMSON
	}

}
