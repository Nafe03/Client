package dev.anarchy.waifuhax.api.util;

import lombok.Cleanup;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PathUtils {

    public static final String BASE_PATH = "./WaifuHax/";

    public static String join(String... paths) {
        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
            sb.append(path);
            sb.append(FileSystems.getDefault().getSeparator());
        }
        return sb.substring(0, sb.toString().length() - 1);
    }

    @SneakyThrows
    public static String readFileToString(String filePath) {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    @SneakyThrows
    public static void writeStringToFile(String join, String string) {
        Files.write(Paths.get(join), string.getBytes());
    }

    @SneakyThrows
    public static List<String> getAllLines(String path) {
        List<String> result = new ArrayList<>();
        @Cleanup
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line = reader.readLine();

        while (line != null) {
            result.add(line);
            line = reader.readLine();
        }
        return result;
    }


    public static void createDirectoryRecursive(String path) {

    }
}
