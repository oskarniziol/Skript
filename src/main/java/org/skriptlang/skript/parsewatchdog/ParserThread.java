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
package org.skriptlang.skript.parsewatchdog;

import ch.njol.skript.lang.parser.ParserInstance;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

/**
 * Holds information about a thread that may be used for parsing.
 * Also holds information about the state of the watchdog.
 */
class ParserThread {

	/**
	 * The thread this ParserThread belongs to.
	 */
	final Thread thread;
	/**
	 * Whether {@link #thread} is the main server thread.
	 */
	// TODO TP special handling if not main thread (i.e. async loading)?
	final boolean isMainThread;
	/**
	 * The ParserInstance of this thread.
	 */
	final ParserInstance parserInstance;

	/**
	 * The Script that this parser thread is parsing,
	 * that was last seen by the watchdog.
	 */
	@Nullable
	Script lastScript;
	/**
	 * When {@link #lastScript} was last changed.
	 */
	long lastScriptChange;
	/**
	 * If {@link #lastScript} has generated a warning
	 * by taking too long to parse.
	 */
	boolean lastScriptWarned;

	/**
	 * The line number that this parser thread was busy with,
	 * that was last seen by the watchdog.
	 */
	int lastLineNumber;
	/**
	 * When {@link #lastLineNumber} was last changed.
	 */
	long lastLineNumberChangeTime;
	/**
	 * If {@link #lastLineNumber} has generated a warning
	 * by taking too long to parse.
	 */
	boolean lastLineWarned;

	/**
	 * If the parser thread has been called to stop parsing,
	 * and disable the last script.
	 */
	boolean scriptInterrupted;

	public ParserThread(Thread thread, boolean isMainThread, ParserInstance parserInstance) {
		this.thread = thread;
		this.isMainThread = isMainThread;
		this.parserInstance = parserInstance;

		resetScript();
	}

	/**
	 * Reset variables related to the last seen script.
	 */
	public void resetScript() {
		lastScript = null;
		lastScriptChange = -1;
		lastScriptWarned = false;

		resetLine();
	}

	/**
	 * Reset variables related to the last seen line.
	 */
	public void resetLine() {
		lastLineNumber = -1;
		lastLineNumberChangeTime = -1;
		lastLineWarned = false;

		scriptInterrupted = false;
	}

}
