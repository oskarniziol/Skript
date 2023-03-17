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
package org.skriptlang.skript.test.tests.lang;

import java.util.function.BiConsumer;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.effects.base.SetEffect;

@NoDoc
public class SetEffectTest extends SetEffect<ItemType> {

	static {
		registerMake(SetEffectTest.class, "itemtypes", "all", "(return all|(have|be) all)");
	}

	@Override
	protected BiConsumer<ItemType, Boolean> apply() {
		return (item, boo) -> item.setAll(boo);
	}

	@Override
	protected String getPropertyName() {
		return "all items";
	}

}
