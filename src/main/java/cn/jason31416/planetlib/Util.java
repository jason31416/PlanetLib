package cn.jason31416.planetlib;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.stream.Stream;

public class Util {
    public static void saveFolder(String name) {
        if(new File(PlanetLib.instance.getDataFolder(), name).isDirectory()) return;
        URI uri = null;
        try {
            uri = Util.class.getClassLoader().getResource(name).toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        try(FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
            try(Stream<Path> walk = Files.walk(fileSystem.getPath(name), 1)) {
                for (Iterator<Path> it = walk.iterator(); it.hasNext(); ) {
                    Path i = it.next();
                    if(!i.toString().equals(name)) savePluginResource(i.toString());
                }
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    public static void savePluginResource(@NotNull String resourcePath) {
        if (!resourcePath.isEmpty()) {
            resourcePath = resourcePath.replace('\\', '/');
            InputStream in = PlanetLib.instance.getResource(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found!");
            } else {
                File outFile = new File(PlanetLib.instance.getDataFolder(), resourcePath);
                int lastIndex = resourcePath.lastIndexOf(47);
                File outDir = new File(PlanetLib.instance.getDataFolder(), resourcePath.substring(0, Math.max(lastIndex, 0)));
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }
                try {
                    if (!outFile.exists()) {
                        OutputStream out = Files.newOutputStream(outFile.toPath());
                        byte[] buf = new byte[1024];

                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        out.close();
                        in.close();
                    }
                } catch (IOException var10) {
                    PlanetLib.instance.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, var10);
                }
            }
        } else {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
    }
}
