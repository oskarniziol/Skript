package org.skriptlang.skript.localization;

import org.jetbrains.annotations.Nullable;

class LocalizerImpl {

	static final class UnmodifiableLocalizer implements Localizer {

		private final Localizer localizer;

		UnmodifiableLocalizer(Localizer localizer) {
			this.localizer = localizer;
		}

		@Override
		public void load(String languageFileDirectory, @Nullable String dataFileDirectory) {
			throw new UnsupportedOperationException("An unmodifiable localizer cannot have translations added.");
		}

	}

}
