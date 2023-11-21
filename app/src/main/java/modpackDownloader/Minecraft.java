package modpackDownloader;

import java.util.List;

public record Minecraft(String version, List<ModLoader> modLoaders) {}