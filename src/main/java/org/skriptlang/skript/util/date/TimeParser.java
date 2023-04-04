package org.skriptlang.skript.util.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Optional;

import ch.njol.skript.SkriptConfig;

/**
 * Future implementation will be added to this class in a 'real time' pull request enhancement.
 */
public class TimeParser {

	private static final DateFormat format = SkriptConfig.dateFormat.value();

	/**
	 * Attempts to parse the input under the time format that the scripter has set.
	 * Otherwise will use DateFormat.FULL
	 * 
	 * @param input The string input to parse against.
	 * @return The Date value other null if the input didn't match.
	 */
	public static Date parseDate(String input) {
		try {
			Optional<Date> date = Optional.of(format.parse(input))
					.map(java.util.Date::getTime)
					.map(Date::new);
			// Would have thrown already.
			assert date.isPresent();
			return date.get();
		} catch (ParseException e) {}
		return null;
	}

}
