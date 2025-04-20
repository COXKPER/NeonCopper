package com.neon.copper;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

public class LogRotator {
    public static void hook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                Path src = Paths.get("logs/latest.txt");
                Path dst = Paths.get("logs/" + timestamp + ".txt.gz");

                try (
                    InputStream in = Files.newInputStream(src);
                    OutputStream out = new GZIPOutputStream(Files.newOutputStream(dst))
                ) {
                    in.transferTo(out);
                }

                Files.delete(src);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }
}
