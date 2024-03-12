package org.skriptlang.skript.addon;

import org.skriptlang.skript.localization.Localizer;
import org.skriptlang.skript.registration.SyntaxRegistry;

class SkriptAddonImpl {

	static class UnmodifiableAddon implements SkriptAddon {

		private final SkriptAddon addon;
		private final SyntaxRegistry unmodifiableRegistry;
		private final Localizer unmodifiableLocalizer;

		UnmodifiableAddon(SkriptAddon addon) {
			this.addon = addon;
			this.unmodifiableRegistry = SyntaxRegistry.unmodifiableView(addon.registry());
			this.unmodifiableLocalizer = Localizer.unmodifiableView(addon.localizer());
		}

		@Override
		public String name() {
			return addon.name();
		}

		@Override
		public SyntaxRegistry registry() {
			return unmodifiableRegistry;
		}

		@Override
		public Localizer localizer() {
			return unmodifiableLocalizer;
		}

	}

}
