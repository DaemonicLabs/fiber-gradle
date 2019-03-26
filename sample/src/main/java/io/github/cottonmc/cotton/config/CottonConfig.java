package io.github.cottonmc.cotton.config;

import fiber.annotations.Comment;
import fiber.annotations.ConfigFile;

import java.util.ArrayList;
import java.util.List;

@ConfigFile(name="CottonConfig")
public class CottonConfig {

    public int number1 = 8;

    @Comment(value="A list of mod ids, in order of preference for resource loading.")
    public List<String> namespacePreferenceOrder = new ArrayList<>();

    public int[] javaIntArray = {1, 2, 3};
}
