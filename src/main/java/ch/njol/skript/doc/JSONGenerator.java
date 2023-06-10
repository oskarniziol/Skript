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
package ch.njol.skript.doc;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.SyntaxElementInfo;
import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.lang.structure.StructureInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

public class JSONGenerator extends Generator {

	public JSONGenerator(File templateDir, File outputDir) {
		super(templateDir, outputDir);
	}

	private static JsonArray convertToJsonArray(String[] strings) {
		JsonArray jsonArray = new JsonArray();
		for (String string : strings)
			jsonArray.add(new JsonPrimitive(string));
		return jsonArray;
	}

	private static @Nullable JsonObject generatedAnnotatedElement(Class<?> syntaxClass, String[] patterns) {
		if (syntaxClass.getAnnotation(NoDoc.class) != null)
			return null;

		JsonObject syntaxJsonObject = new JsonObject();

		// TODO: conditionJson.addProperty("id");
		Name nameAnnotation = syntaxClass.getAnnotation(Name.class);
		syntaxJsonObject.addProperty("name", nameAnnotation.value());

		Since sinceAnnotation = syntaxClass.getAnnotation(Since.class);
		syntaxJsonObject.addProperty("since", sinceAnnotation.value());

		Description descriptionAnnotation = syntaxClass.getAnnotation(Description.class);
		String description = null;
		if (descriptionAnnotation != null)
			description = Joiner.on('\n').join(descriptionAnnotation.value());
		syntaxJsonObject.addProperty("description", description);

		Examples examplesAnnotation = syntaxClass.getAnnotation(Examples.class);
		String examples = null;
		if (examplesAnnotation != null)
			examples = Joiner.on('\n').join(examplesAnnotation.value());

		syntaxJsonObject.addProperty("examples", examples);
		syntaxJsonObject.add("patterns", convertToJsonArray(patterns));

		return syntaxJsonObject;
	}

	private static JsonObject generateEventElement(SkriptEventInfo<?> eventInfo) {
		JsonObject syntaxJsonObject = new JsonObject();

		// TODO: conditionJson.addProperty("id");
		syntaxJsonObject.addProperty("name", eventInfo.name);

		syntaxJsonObject.addProperty("since", eventInfo.getSince());

		String[] descriptionLines = eventInfo.getDescription();
		String description = null;
		if (descriptionLines != null)
			description = Joiner.on('\n').join(descriptionLines);
		syntaxJsonObject.addProperty("description", description);

		String[] exampleLines = eventInfo.getExamples();
		String examples = null;
		if (exampleLines != null)
			examples = Joiner.on('\n').join(exampleLines);

		syntaxJsonObject.addProperty("examples", examples);
		syntaxJsonObject.add("patterns", convertToJsonArray(eventInfo.patterns));

		return syntaxJsonObject;
	}


	private static <T extends StructureInfo<? extends Structure>> JsonArray generateStructureElementArray(Iterator<T> infos) {
		JsonArray syntaxArray = new JsonArray();

		infos.forEachRemaining(info -> {
			if (info instanceof SkriptEventInfo) {
				syntaxArray.add(generateEventElement((SkriptEventInfo<?>) info));
			} else {
				JsonObject structureElementJsonObject = generatedAnnotatedElement(info.getElementClass(), info.patterns);
				if (structureElementJsonObject != null)
					syntaxArray.add(structureElementJsonObject);
			}
		});

		return syntaxArray;
	}

	private static <T extends SyntaxElementInfo<? extends SyntaxElement>> JsonArray generateSyntaxElementArray(Iterator<T> infos) {
		JsonArray syntaxArray = new JsonArray();

		infos.forEachRemaining(info -> {
			JsonObject syntaxJsonObject = generatedAnnotatedElement(info.getElementClass(), info.patterns);
			if (syntaxJsonObject != null)
				syntaxArray.add(syntaxJsonObject);
		});

		return syntaxArray;
	}

	@Override
	public void generate() {
		JsonObject jsonDocs = new JsonObject();

		jsonDocs.add("conditions", generateSyntaxElementArray(Skript.getConditions().iterator()));
		jsonDocs.add("effects", generateSyntaxElementArray(Skript.getEffects().iterator()));
		jsonDocs.add("expressions", generateSyntaxElementArray(Skript.getExpressions()));
		jsonDocs.add("events", generateStructureElementArray(Skript.getEvents().iterator()));
		Stream<StructureInfo<? extends Structure>> structuresExcludingEvents = Skript.getStructures().stream()
				.filter(structureInfo -> !(structureInfo instanceof SkriptEventInfo));
		jsonDocs.add("structures", generateStructureElementArray(structuresExcludingEvents.iterator()));

		// TODO: functions, classinfos

		Path jsonOutputPath = outputDir.toPath().resolve("docs.json");
		try {
			Files.writeString(jsonOutputPath, jsonDocs.toString());
		} catch (IOException exception) {
			//noinspection ThrowableNotThrown
			Skript.exception(exception, "An error occurred while trying to generate JSON documentation");
		}
	}

}
