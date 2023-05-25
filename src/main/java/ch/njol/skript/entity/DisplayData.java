/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.entity;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;

public class DisplayData extends EntityData<Display> {

	private static enum DisplayType {

		ANY(Display.class, "display"),
		BLOCK(BlockDisplay.class, "block display"),
		ITEM(ItemDisplay.class, "item display"),
		TEXT(TextDisplay.class, "text display");

		@Nullable
		final Class<? extends Display> c;
		private final String codeName;
		
		DisplayType(@Nullable Class<? extends Display> c, String codeName) {
			this.c = c;
			this.codeName = codeName;
		}

		@Override
		public String toString() {
			return codeName;
		}

		public static String[] codeNames;
		static {
			List<String> cn = new ArrayList<>();
			for (DisplayType t : values()) {
				if (t.c != null)
					cn.add(t.codeName);
			}
			codeNames = cn.toArray(new String[0]);
		}
	}

	static {
		EntityData.register(DisplayData.class, "display", Display.class, 0, DisplayType.codeNames);
		Variables.yggdrasil.registerSingleClass(DisplayType.class, "DisplayType");
	}

	private DisplayType type = DisplayType.ANY;

	@Nullable
	private BlockData blockData;

	@Nullable
	private ItemStack item;

	public DisplayData() {}

	public DisplayData(DisplayType type) {
		this.type = type;
		this.matchedPattern = type.ordinal();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		type = DisplayType.values()[matchedPattern];
		if (exprs.length > 0 && exprs[0] != null) {
			if (type == DisplayType.BLOCK && exprs[0] != null) {
				Object object = ((Literal<Object>) exprs[0]).getSingle();
				if (object instanceof ItemType) {
					Material material = ((ItemType) object).getMaterial();
					if (!material.isBlock()) {
						Skript.error("A block display must be a block item. " + Classes.toString(material) + " is not a block. If you want to spawn an item, use an 'item display'");
						return false;
					}
					blockData = Bukkit.createBlockData(material);
				} else {
					blockData = (BlockData) object;
				}
			} else if (type == DisplayType.ITEM) {
				item = ((Literal<ItemType>) exprs[0]).getSingle().getRandom();
			}
		}
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Display> c, @Nullable Display entity) {
		DisplayType[] types = DisplayType.values();
		for (int i = types.length - 1; i >= 0; i--) {
			Class<?> display = types[i].c;
			if (display == null)
				continue;
			if (entity == null ? display.isAssignableFrom(c) : display.isInstance(entity)) {
				type = types[i];
				if (entity != null) {
					switch (types[i]) {
						case ANY:
							break;
						case BLOCK:
							blockData = ((BlockDisplay) entity).getBlock();
							break;
						case ITEM:
							item = ((ItemDisplay) entity).getItemStack();
							break;
						case TEXT:
							break;
						default:
							break;
					}
				}
				return true;
			}
		}
		assert false;
		return false;
	}

	@Override
	public void set(Display entity) {
		switch (type) {
			case ANY:
				break;
			case BLOCK:
				if (blockData != null)
					((BlockDisplay) entity).setBlock(blockData);
				break;
			case ITEM:
				if (item != null)
					((ItemDisplay) entity).setItemStack(item);
				break;
			case TEXT:
				break;
			default:
				break;
		}
	}

	@Override
	public boolean match(Display entity) {
		switch (type) {
			case ANY:
				break;
			case BLOCK:
				if (blockData != null && !((BlockDisplay) entity).getBlock().equals(blockData))
					return false;
				break;
			case ITEM:
				if (item != null && !((ItemDisplay) entity).getItemStack().isSimilar(item))
					return false;
				break;
			case TEXT:
				break;
			default:
				break;
		}
		return type.c != null && type.c.isInstance(entity);
	}

	@Override
	public Class<? extends Display> getType() {
		return type.c != null ? type.c : Display.class;
	}

	@Override
	protected int hashCode_i() {
		return type.hashCode();
	}

	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (!(obj instanceof DisplayData))
			return false;
		DisplayData other = (DisplayData) obj;
		return type == other.type;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> e) {
		if (e instanceof DisplayData)
			return type == DisplayType.ANY || ((DisplayData) e).type == type;
		return false;
	}

	@Override
	public EntityData<?> getSuperType() {
		return new DisplayData(type);
	}

}
