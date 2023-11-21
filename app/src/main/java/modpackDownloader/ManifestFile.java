package modpackDownloader;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Locale;

public class ManifestFile {
    private String manifestType = "minecraftModpack";
    private String version;
    private List<ManifestModEntry> files;
    private int manifestVersion;
    private String name;
    private String overrides = "overrides";
    private String author;
    private Minecraft minecraft;

    private class ManifestModEntry {
        private long projectID;
        private long fileID;
        private String downloadUrl;
        private boolean required;

        public boolean isValid() {
            if(projectID < 0) return false;
            if(fileID < 0) return false;
            if(downloadUrl == null || downloadUrl.isBlank()) return false;

            return true;
        }
    }

    public int getNumberOfMods() {
        if(files == null) return 0;

        return files.size();
    }

    public void printRecommendedModLoader() {
        for(ModLoader modLoader : minecraft.modLoaders()) {
            if(!modLoader.primary()) continue;
            
            System.out.printf(Locale.ROOT, "Recommended mod loader: %s%n", modLoader.id());
        }
    }

    public void printDoneMessage() {
        System.out.println("Finished downloading modpack.");

        if(name != null && !name.isBlank()
            && author != null && !author.isBlank()
            && version != null && !version.isBlank()) {
            System.out.printf(Locale.ROOT, "Enjoy playing %s version %s by %s!%n", name, version, author);
        }
    }

    public int downloadAllMods() {
        System.out.println("Downloading mods...");
        int totalDownloads = 0;
        int requiredMods = 0;
        boolean successful;
        for(ManifestModEntry file : files) {
            successful = false;

            if(file.required) {
                requiredMods++;
            }

            if(!file.isValid()) {
                System.out.printf(Locale.ROOT, "Mod entry %d is invalid.%n", files.indexOf(file));
                continue;
            }

            try {
                downloadMod(file);
                totalDownloads++;
                successful = true;
            } catch(MalformedURLException e) {
                System.out.printf(Locale.ROOT, "%s: Malformed URL.%n", file.downloadUrl);
                e.printStackTrace();
            } catch(FileNotFoundException e) {
                System.out.printf(Locale.ROOT, """
                    %s: Could not save downloaded file:
                    Check whether a file or folder with that name already exists.%n
                    """, file.downloadUrl);
                e.printStackTrace();
            } catch(SecurityException e) {
                System.out.printf(Locale.ROOT, """
                    %s: Could not save downloaded file:
                    Access denied.%n
                    """, file.downloadUrl);
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }

            if(!successful && file.required) {
                System.out.printf(Locale.ROOT, "%s: Failed to download required mod.%n", file.downloadUrl);
            }
        }

        if(requiredMods > totalDownloads) {
            System.out.printf(Locale.ROOT, """
                Not all required mods were downloaded successfully:
                %d of %d mods downloaded, %d required
                """, totalDownloads, getNumberOfMods(), requiredMods);
        }

        return totalDownloads;
    }

    private void downloadMod(ManifestModEntry mod) throws MalformedURLException, FileNotFoundException, SecurityException, IOException {
        if(mod == null || !mod.isValid()) {
            throw new IllegalArgumentException("Mod must not be null or invalid.");
        }

        URL website = new URL(mod.downloadUrl); //MalformedURLException

        //https://stackoverflow.com/questions/4050087/how-to-obtain-the-last-path-segment-of-a-uri
        String path = website.getPath();
        String fileName = path.substring(path.lastIndexOf('/') + 1);

        if(fileName == null || fileName.isBlank()) {
            System.out.printf(Locale.ROOT, """
                %s: Could not extract file name from URL.
                Using file ID %d instead.
                """, mod.downloadUrl, mod.fileID);
            fileName = Long.toString(mod.fileID);
        }

        //https://stackoverflow.com/questions/921262/how-can-i-download-and-save-a-file-from-the-internet-using-java
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        System.out.printf(Locale.ROOT, "\t- %s%n", fileName);
        FileOutputStream fos = new FileOutputStream(fileName); //FileNotFoundException, SecurityException
        fos.getChannel().transferFrom(rbc, 0, Integer.MAX_VALUE);
        fos.close();
    }
}