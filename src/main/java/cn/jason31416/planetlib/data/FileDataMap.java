package cn.jason31416.planetlib.data;

import cn.jason31416.planetlib.util.MapTree;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Map;

public class FileDataMap extends MapTree implements AutoCloseable {
    @Getter
    private final File file;
    @SneakyThrows
    public FileDataMap(File file){
        this.file = file;
        if(file.exists()){
            data = new Gson().fromJson(Files.readString(file.toPath()), new TypeToken<Map<String, Object>>(){}.getType());
        }
    }

    public void save() throws IOException {
        Files.writeString(file.toPath(), new Gson().toJson(data));
    }

    @Override
    public void close() throws Exception {
        save();
    }
}
