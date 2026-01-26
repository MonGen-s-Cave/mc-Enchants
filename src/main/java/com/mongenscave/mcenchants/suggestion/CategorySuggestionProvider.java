package com.mongenscave.mcenchants.suggestion;

import com.mongenscave.mcenchants.McEnchants;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ExecutionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CategorySuggestionProvider<A extends CommandActor> implements SuggestionProvider<A> {
    @Override
    public @NotNull Collection<String> getSuggestions(@NotNull ExecutionContext<A> context) {
        List<String> suggestions = new ArrayList<>();

        try {
            Section categoriesSection = McEnchants.getInstance().getCategory().getSection("categories");

            if (categoriesSection != null) {
                suggestions.addAll(categoriesSection.getRoutesAsStrings(false));
            }
        } catch (Exception ignored) {}

        return suggestions;
    }
}