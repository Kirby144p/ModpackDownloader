/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package modpackDownloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Scanner;

import com.google.gson.Gson;

/**
 * The jar gets placed in the mods folder with the manifest file in it.
 */
public class App {
    private static final String MANIFEST_FILE = "manifest.json";

    public static void main(String[] args) {
        File file = new File(MANIFEST_FILE);
        if(!file.exists() || !file.isFile()) {
            System.out.printf(Locale.ROOT, "A file with the name %s does not exist.\n", MANIFEST_FILE);
            return;
        }

        //https://stackoverflow.com/questions/3402735/what-is-simplest-way-to-read-a-file-into-string
        Scanner scanner = null;
        try {
            scanner = new Scanner(file).useDelimiter("\\Z");
        } catch (FileNotFoundException e) {
            System.out.printf(Locale.ROOT, "A file with the name %s does not exist.\n", MANIFEST_FILE);
            e.printStackTrace();
            return;
        }

        if(scanner == null) return;

        String content = scanner.next();
        scanner.close();

        if(content == null || content.isBlank()) {
            System.out.println("Could not read data from file or file is blank.");
            return;
        }

        ManifestFile manifestFile = new Gson().fromJson(content, ManifestFile.class);

        if(manifestFile == null) {
            System.out.println("Could not deserialize json string.");
            return;
        }

        int totalDownloads = manifestFile.downloadAllMods();
        System.out.printf(Locale.ROOT, "Downloaded %d of %d mods%n", totalDownloads, manifestFile.getNumberOfMods());
        manifestFile.printRecommendedModLoader();
        manifestFile.printDoneMessage();
    }
}
