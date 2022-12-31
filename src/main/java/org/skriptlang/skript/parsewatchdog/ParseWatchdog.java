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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.HandlerList;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.LogHandler;
import ch.njol.skript.log.RedirectingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class ParseWatchdog {

	private static final String THREAD_NAME = "Skript Parsing Watchdog";

	// TODO TP add to config.sk
	// Delay settings variables
	private static long SCRIPT_WARNING_MILLIS = 5_000;
	private static long SCRIPT_DISABLE_MILLIS = 10_000;
	private static long LINE_WARNING_MILLIS = 1_000;
	private static long LINE_DISABLE_MILLIS = 5_000;

	/**
	 * The parse watchdog instance
	 */
	@Nullable
	private static ParseWatchdog instance;

	/**
	 * The parser thread this parse watchdog is monitoring.
	 */
	private final Set<ParserThread> parserThreads = new HashSet<>();

	/**
	 * Creates the parse watchdog instance and starts the thread.
	 * Throws an exception if an instance of the watchdog already exists.
	 */
	private ParseWatchdog() throws IllegalStateException {
		synchronized (ParseWatchdog.class) {
			if (instance != null) {
				throw new IllegalStateException("Singleton class ParseWatchdog has already been instantiated before");
			}

			new WatchdogThread().start();

			instance = this;
		}
	}

	/**
	 * Creates and starts the parse watchdog.
	 *
	 * @throws IllegalStateException if the parse watchdog
	 * 									has been created already.
	 */
	public synchronized static void start() throws IllegalStateException {
		new ParseWatchdog();
	}

	/**
	 * Adds a thread to be monitored by the watchdog.
	 */
	public synchronized static void addThread(Thread thread, boolean isMainServer, ParserInstance parserInstance) {
		if (instance == null) {
			start();
		}

		synchronized (instance.parserThreads) {
			// Check if this thread is not already being monitored
			for (ParserThread parserThread : instance.parserThreads) {
				if (parserThread.thread == thread) {
					return;
				}
			}

			instance.parserThreads.add(new ParserThread(thread, isMainServer, parserInstance));
		}
	}

	/**
	 * Gets the parse watchdog instance, creates it if it does not yet exist.
	 */
	public synchronized static ParseWatchdog getInstance() {
		if (instance == null) {
			start();
		}

		return instance;
	}

	/**
	 * Checks whether the {@link ParserInstance} of the current thread
	 * was interrupted, and throws a {@link WatchdogInterruptedException} if so.
	 */
	public static void checkInterrupt() throws WatchdogInterruptedException {
		ParserInstance parserInstance = ParserInstance.get();
		if (parserInstance.interrupted()) {
			throw new WatchdogInterruptedException();
		}
	}

	/**
	 * Checks if the watchdog should take action on the given parser thread.
	 */
	private void check(ParserThread parserThread) {
		long currentTime = System.currentTimeMillis();

		ParserInstance parserInstance = parserThread.parserInstance;

		// Reset last seen script data and skip checks if ParserInstance inactive
		if (!parserInstance.isActive()) {
			parserThread.resetScript();

			return;
		}

		Script script = parserInstance.getCurrentScript();
		Script lastScript = parserThread.lastScript;

		// Script changed, reset script related variables and set last
		if (script != lastScript) {
			parserThread.resetScript();
			parserThread.lastScript = script;
			parserThread.lastScriptChange = currentTime;

			return;
		}

		// Skip script if we've interrupted it
		if (parserThread.scriptInterrupted) {
			return;
		}

		// If not warned for this script before, and at least SCRIPT_WARNING_MILLIS passed
		if (SCRIPT_WARNING_MILLIS != -1
				&& !parserThread.lastScriptWarned
				&& currentTime - parserThread.lastScriptChange >= SCRIPT_WARNING_MILLIS) {
			// Skip warning if already warned for a line
			if (!parserThread.lastLineWarned) {
				warnScript(parserThread, script);
			}

			parserThread.lastScriptWarned = true;
		}
		if (SCRIPT_DISABLE_MILLIS != -1
				&& currentTime - parserThread.lastScriptChange >= SCRIPT_DISABLE_MILLIS) {
			disableScript(parserThread, script);
			return;
		}

		Node node = parserInstance.getNode();
		if (node == null) {
			parserThread.resetLine();

			return;
		}

		int lineNumber = node.getLine();
		int lastLineNumber = parserThread.lastLineNumber;
		if (lineNumber != lastLineNumber) {
			parserThread.resetLine();
			parserThread.lastLineNumber = lineNumber;
			parserThread.lastLineNumberChangeTime = currentTime;

			return;
		}

		// Check if at least `LINE_WARNING_MILLIS` ms on the same line
		if (LINE_WARNING_MILLIS != -1
				&& !parserThread.lastLineWarned
				&& !parserThread.lastScriptWarned
				&& currentTime - parserThread.lastLineNumberChangeTime >= LINE_WARNING_MILLIS) {
			warnLine(parserThread, script, node);

			parserThread.lastLineWarned = true;
		}
		if (LINE_DISABLE_MILLIS != -1
				&& currentTime - parserThread.lastLineNumberChangeTime >= LINE_DISABLE_MILLIS) {
			disableLine(parserThread, script, node);
		}
	}

	private void warnScript(ParserThread parserThread, Script script) {
		CommandSender recipient = getLogRecipient(parserThread);

		error(recipient, "");
		error(recipient, "The script '" + script.getConfig().getFileName() + "' is taking a long time to load");
		error(recipient, "If it takes longer, Skript will stop loading and disable the script");
		error(recipient, "");
	}

	private void warnLine(ParserThread parserThread, Script script, Node node) {
		CommandSender recipient = getLogRecipient(parserThread);
		int lineNumber = node.getLine();

		error(recipient, "");
		error(recipient, "Line " + lineNumber + " of script '" +
				script.getConfig().getFileName() + "' is taking a long time to load");
		error(recipient, "If it takes longer, Skript will stop loading and disable the script");
		error(recipient, "");
	}

	private void disableScript(ParserThread parserThread, Script script) {
		CommandSender recipient = getLogRecipient(parserThread);

		error(recipient, "");
		error(recipient, "The script '" + script.getConfig().getFileName() + "' took too long to load");
		error(recipient, "It will now be disabled, please optimize your script and re-enable it.");
		error(recipient, "One option for this is to split it up into multiple scripts.");
		error(recipient, "");

		// Stop parsing and disable script
		stop(parserThread, script);
		ScriptLoader.unloadScript(script);
	}

	private void disableLine(ParserThread parserThread, Script script, Node node) {
		CommandSender recipient = getLogRecipient(parserThread);
		int lineNumber = node.getLine();

		error(recipient, "");
		error(recipient, "Line " + lineNumber + " of script '" + script.getConfig().getFileName()
				+ "' took too long to load");
		error(recipient, "The script will now be disabled, please optimize your script and re-enable it.");
		error(recipient, "One way to optimize the line is to split it up into multiple lines, " +
				"for example using intermediate variables.");
		error(recipient, "");

		// Stop parsing and disable script
		stop(parserThread, script);
	}

	/**
	 * Stops the parsing of the given parser thread.
	 */
	private void stop(ParserThread parserThread, Script script) {
		// TODO TP mb also regular Thread interrupt in case of blocking call in init
		parserThread.parserInstance.interrupt(script);

		parserThread.scriptInterrupted = true;
	}

	/**
	 * @return the logging recipient of the given parser thread,
	 * i.e. the command sender receiving logging messages (errors,
	 * warnings and the like) during the load.
	 */
	private static CommandSender getLogRecipient(ParserThread parserThread) {
		HandlerList handlers = parserThread.parserInstance.getHandlers();

		for (LogHandler logHandler : handlers) {
			if (logHandler instanceof RedirectingLogHandler) {
				return ((RedirectingLogHandler) logHandler).getRecipient();
			}
		}

		// No RedirectingLogHandlers found, return console command sender
		return Bukkit.getConsoleSender();
	}

	/**
	 * Sends the given error message to the given recipient.
	 */
	private void error(CommandSender recipient, String message) {
		LogEntry logEntry = new LogEntry(Level.SEVERE, message, null);
		SkriptLogger.sendFormatted(recipient, logEntry.toFormattedString());
	}

	/**
	 * The thread the watchdog runs on.
	 */
	private class WatchdogThread extends Thread {
		public WatchdogThread() {
			super(THREAD_NAME);
		}

		@Override
		public void run() {
			while (Skript.getInstance().isEnabled()) {
				// Make a copy, synchronized
				ParserThread[] parserThreadsCopy;
				synchronized (parserThreads) {
					// Remove dead threads
					parserThreads.removeIf(parserThread -> !parserThread.thread.isAlive());

					parserThreadsCopy = parserThreads.toArray(new ParserThread[0]);
				}

				// Check every parser thread
				for (ParserThread parserThread : parserThreadsCopy) {
					check(parserThread);
				}

				// Sleep for a bit, no need to run too often
				try {
					//noinspection BusyWait
					sleep(100);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

}
