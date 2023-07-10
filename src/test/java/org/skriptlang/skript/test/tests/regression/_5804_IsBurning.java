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
package org.skriptlang.skript.test.tests.regression;

import org.bukkit.entity.Pig;
import org.junit.Before;
import org.junit.Test;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import net.kyori.adventure.text.Component;

/**
 * JUnit test to ensure that CondIsBurning goes before CondCompare.
 * Skript was selecting CondCompare and using the DamageCause to EntityData comparator.
 */
public class _5804_IsBurning extends SkriptJUnitTest {

	private Pig piggy;

	static {
		setShutdownDelay(1);
	}

	@Before
	public void spawnPig() {
		piggy = spawnTestPig();
		piggy.customName(Component.text("_5804_IsBurning"));
	}

	@Test
	public void startBurning() {
		piggy.setFireTicks(9000);
	}

}
