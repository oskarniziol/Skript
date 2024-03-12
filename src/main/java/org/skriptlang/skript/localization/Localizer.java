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
package org.skriptlang.skript.localization;

import ch.njol.skript.localization.Language;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

/**
 * A Localizer is used for the localization of translatable strings.
 *
 * This API is highly experimental and will be subject to change due to pending localization reworks.
 * In its current state, it acts as a bridge between old and new API.
 *
 * @see ch.njol.skript.localization.Language
 */
@ApiStatus.Experimental
public interface Localizer {

	/**
	 * @return A localizer with no default translations.
	 */
	@Contract("-> new")
	static Localizer empty() {
		return new Localizer(){};
	}

	/**
	 * Constructs an unmodifiable view of a localizer.
	 * That is, the localizer may not have any new translations added.
	 * @param localizer The localizer backing this unmodifiable view.
	 * @return An unmodifiable view of <code>localizer</code>.
	 */
	@Contract("_ -> new")
	@UnmodifiableView
	static Localizer unmodifiableView(Localizer localizer) {
		return new LocalizerImpl.UnmodifiableLocalizer(localizer);
	}

	/**
	 *
	 * @param languageFileDirectory The path to the directory containing language files.
	 * When searching for language files on the jar, this will be used as the path.
	 * When searching for language files on the disk, this will be used along with <code>dataFileDirectory</code>.
	 * That is, it is expected that the path <code>dataFileDirectory + languageFileDirectory</code> would
	 *  lead to language files on the disk.
	 * @param dataFileDirectory The path to the directory on disk containing data files.
	 * For example, this may include language files that have been saved to enable user customization.
	 */
	default void load(String languageFileDirectory, @Nullable String dataFileDirectory) {
		// TODO fix language loading :)
	}

	// TODO potentially different name. translate? render?
	default String localize(String key) {
		return Language.get(key);
	}

}
