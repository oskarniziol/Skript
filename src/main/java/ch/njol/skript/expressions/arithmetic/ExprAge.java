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
package ch.njol.skript.expressions.arithmetic;


import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("test")
@Description("test")
@Examples("test")
@Since("test")
public class ExprAge extends SimplePropertyExpression<Object, Integer> {

	static {
		register(ExprAge.class, Integer.class, "test", "blocks/entities");
	}

	private boolean isMax;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	@Nullable
	public Integer convert(Object obj) {
		if (obj instanceof Block) {
			BlockData bd = ((Block) obj).getBlockData();
			if (!(bd instanceof Ageable))
				return null;
			Ageable ageable = (Ageable) bd;
			return isMax ? ageable.getMaximumAge() : ageable.getAge();
		} else if (obj instanceof org.bukkit.entity.Ageable) {
			return isMax ? null : ((org.bukkit.entity.Ageable) obj).getAge();
		}
		return null;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (isMax || mode == ChangeMode.REMOVE_ALL || mode == ChangeMode.DELETE)
			return null;
		return CollectionUtils.array(Number.class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (mode != ChangeMode.RESET && delta == null)
			return;

		int newValue = mode != ChangeMode.RESET ? ((Number) delta[0]).intValue() : 0;

		for (Object obj : getExpr().getArray(event)) {
			Number oldValue = convert(obj);
			if (oldValue == null && mode != ChangeMode.RESET)
				continue;

			switch (mode) {
				case REMOVE:
					setAge(obj, oldValue.intValue() - newValue);
					break;
				case ADD:
					setAge(obj, oldValue.intValue() + newValue);
					break;
				case SET:
					setAge(obj, newValue);
					break;
				case RESET:
					// baby animals takes 20 minutes to grow up - ref: https://minecraft.fandom.com/wiki/Breeding
					if (obj instanceof org.bukkit.entity.Ageable)
						// it might change later on so removing entity age reset would be better unless
						// bukkit adds a method returning the default age
						newValue = -24000;
					setAge(obj, newValue);
					break;
			}
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return (isMax ? "maximum " : "") + "age";
	}

	private static void setAge(Object obj, int value) {
		if (obj instanceof Block) {
			Block block = (Block) obj;
			BlockData bd = block.getBlockData();
			if (bd instanceof Ageable) {
				((Ageable) bd).setAge(Math.max(Math.min(value, ((Ageable) bd).getMaximumAge()), 0));
				block.setBlockData(bd);
			}
		} else if (obj instanceof org.bukkit.entity.Ageable) {
			// Bukkit accepts higher values than 0, they will keep going down to 0 though (some Animals type might be using that - not sure)
			((org.bukkit.entity.Ageable) obj).setAge(value);
		}
	}

}