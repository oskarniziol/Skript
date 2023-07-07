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
package ch.njol.skript.expressions;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.Event;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;

@Name("Sign Text")
@Description("A line of text on a sign. Can be changed, but remember that there is a 16 character limit per line (including color codes that use 2 characters each).")
@Examples({
	"on rightclick on sign:",
		"\tline 2 of the clicked block is \"[Heal]\":",
			"\t\theal the player",
		"\tset line 3 to \"%player%\""
})
@Since("1.3, INSERT VERSION (all lines, back side, multiple blocks, and Skript's ChatFormat (hex, font, etc))")
public class ExprSignText extends SimpleExpression<String> {

	private static final boolean RUNNING_1_20 = Skript.isRunningMinecraft(1, 20);
	private static BungeeComponentSerializer serializer;

	static {
		// Adventure API coming from Paper.
		if (Skript.methodExists(SignChangeEvent.class, "lines"))
			serializer = BungeeComponentSerializer.get();

		String addition = RUNNING_1_20 ? "[[on [the] (front|:back) side] of [sign[s]] %blocks%]" : "[of [sign[s]] %blocks%]";
		Skript.registerExpression(ExprSignText.class, String.class, ExpressionType.PROPERTY,
				"[all [[of] the]|the] lines " + addition,
				RUNNING_1_20 ? "%blocks%'[s] [(front|:back) side] lines" : "%blocks%'[s] lines",

				"[the] line %number% " + addition,
				"[the] (1¦1st|1¦first|2¦2nd|2¦second|3¦3rd|3¦third|4¦4th|4¦fourth) line[s] " + addition);
	}

	@Nullable
	private Expression<Number> line;
	private Expression<Block> blocks;
	private boolean lines;

	@Nullable
	private Side side; // Only nullable due to older versions than 1.20.

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 2) {
			line = (Expression<Number>) exprs[0];
		} else if (matchedPattern == 3) {
			line = new SimpleLiteral<>(parseResult.mark, false);
		} else {
			lines = true;
		}
		if (RUNNING_1_20)
			side = parseResult.hasTag("back") ? Side.BACK : Side.FRONT;
		blocks = (Expression<Block>) exprs[matchedPattern <= 1 ? 0 : 1];
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		int line = 0;
		if (this.line == null && !lines) {
			return new String[0];
		} else if (!lines) {
			line = this.line.getOptionalSingle(event).orElse(1).intValue() - 1;
			if (line < 0 || line > 3)
				return new String[0];
		}
		if (getTime() >= 0 && event instanceof SignChangeEvent && !Delay.isDelayed(event)) {
			if (lines)
				return ((SignChangeEvent) event).getLines();
			return new String[] {((SignChangeEvent) event).getLine(line)};
		}
		int finalLine = line;
		return blocks.stream(event)
				.map(Block::getState)
				.filter(Sign.class::isInstance)
				.map(Sign.class::cast)
				.flatMap(sign -> {
					if (RUNNING_1_20) {
						if (lines)
							return Arrays.stream(sign.getSide(side).getLines());
						return Stream.of(sign.getSide(side).getLine(finalLine));
					}
					if (lines)
						return Arrays.stream(sign.getLines());
					return Stream.of(sign.getLine(finalLine));
				})
				.toArray(String[]::new);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		boolean acceptsMany = line == null;
		switch (mode) {
			case REMOVE:
			case REMOVE_ALL:
			case DELETE:
				acceptsMany = false;
			case SET:
			case ADD:
				return CollectionUtils.array(acceptsMany ? String[].class : String.class);
			case RESET:
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		int line = 0;
		if (this.line == null && !lines) {
			return;
		} else if (!lines) {
			line = this.line.getOptionalSingle(event).orElse(1).intValue() - 1;
			if (line < 0 || line > 3)
				return;
		}
		if (getTime() >= 0 && event instanceof SignChangeEvent && blocks.check(event, block -> block.equals(((SignChangeEvent) event).getBlock())) && !Delay.isDelayed(event)) {
			String[] stringDelta = delta == null ? null : Arrays.copyOf(delta, delta.length, String[].class);
			SignChangeEvent changeEvent = (SignChangeEvent) event;
			switch (mode) {
				case ADD:
					assert stringDelta != null;
					if (lines) {
						List<String> list = Lists.newArrayList(changeEvent.getLines());
						for (String string : stringDelta)
							list.add(string);

						stringDelta = list.toArray(new String[0]);
					} else {
						for (int i = 0; i < stringDelta.length; i++) {
							String value = stringDelta.length > i ? (String) delta[i] : "";
							stringDelta[i] = value + stringDelta[i];
						}
					}
					//$FALL-THROUGH$
				case REMOVE:
				case REMOVE_ALL:
					assert stringDelta != null;
					stringDelta = ExprLore.handleRemove(StringUtils.join(stringDelta, "\n"), stringDelta[0], mode == ChangeMode.REMOVE_ALL).split("\n");
					//$FALL-THROUGH$
				case DELETE:
					stringDelta = CollectionUtils.array("", "", "", "");
				case SET:
					// We need to ensure that it's clearing values without calling setLine twice.
					if (mode == ChangeMode.SET) {
						for (int i = 0; i < 4; i++)
							stringDelta[i] = stringDelta.length > i ? (String) stringDelta[i] : "";
					}
					assert stringDelta != null;
					if (lines) {
						for (int i = 0; i < 4; i++) {
							String value = stringDelta.length > i ? (String) stringDelta[i] : "";
							if (serializer != null) {
								if (value.isEmpty()) // Reduce callings.
									changeEvent.line(i, Component.empty());
								changeEvent.line(i, serializer.deserialize(BungeeConverter.convert(ChatMessages.parseToArray(value))));
								continue;
							}
							changeEvent.setLine(i, value);
						}
						break;
					}
					changeEvent.setLine(line, (String) delta[0]);
					break;
				default:
					break;
			}
			return;
		}
		int finalLine = line;
		blocks.stream(event)
				.map(Block::getState)
				.filter(Sign.class::isInstance)
				.map(Sign.class::cast)
				.forEach(sign -> {
					String[] stringDelta = delta == null ? null : Arrays.copyOf(delta, delta.length, String[].class);
					switch (mode) {
						case ADD:
							assert stringDelta != null;
							if (lines) {
								List<String> list = Lists.newArrayList(sign.getLines());
								for (String string : stringDelta)
									list.add(string);

								stringDelta = list.toArray(new String[0]);
							} else {
								for (int i = 0; i < stringDelta.length; i++) {
									String value = stringDelta.length > i ? (String) delta[i] : "";
									stringDelta[i] = value + stringDelta[i];
								}
							}
							//$FALL-THROUGH$
						case REMOVE:
						case REMOVE_ALL:
							assert stringDelta != null;
							stringDelta = ExprLore.handleRemove(StringUtils.join(stringDelta, "\n"), stringDelta[0], mode == ChangeMode.REMOVE_ALL).split("\n");
							//$FALL-THROUGH$
						case DELETE:
							stringDelta = CollectionUtils.array("", "", "", "");
						case SET:
							// We need to ensure that it's clearing values without calling setLine twice.
							if (mode == ChangeMode.SET) {
								for (int i = 0; i < 4; i++)
									stringDelta[i] = stringDelta.length > i ? (String) stringDelta[i] : "";
							}
							assert stringDelta != null;
							if (RUNNING_1_20) {
								if (lines) {
									for (int i = 0; i < 4; i++) {
										String value = stringDelta.length > i ? (String) stringDelta[i] : "";
										if (serializer != null) {
											if (value.isEmpty()) // Reduce callings.
												sign.getSide(side).line(i, Component.empty());
											sign.getSide(side).line(i, serializer.deserialize(BungeeConverter.convert(ChatMessages.parseToArray(value))));
											continue;
										}
										sign.setLine(i, value);
									}
								}
								sign.setLine(finalLine, (String) delta[0]);
								break;
							}
							if (lines) {
								for (int i = 0; i < 4; i++) {
									String value = stringDelta.length > i ? (String) stringDelta[i] : "";
									if (serializer != null) {
										if (value.isEmpty()) // Reduce callings.
											sign.line(i, Component.empty());
										sign.line(i, serializer.deserialize(BungeeConverter.convert(ChatMessages.parseToArray(value))));
										continue;
									}
									sign.setLine(i, value);
								}
							}
							sign.setLine(finalLine, (String) delta[0]);
							break;
						default:
							break;
					}
					sign.update(true, false);
				});
	}

	@Override
	public boolean setTime(int time) {
		return super.setTime(time, SignChangeEvent.class, blocks);
	}

	@Override
	public boolean isSingle() {
		return !lines;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (line == null)
			return "lines of " + blocks.toString(event, debug);
		return "line " + line.toString(event, debug) + " of " + blocks.toString(event, debug);
	}

}
