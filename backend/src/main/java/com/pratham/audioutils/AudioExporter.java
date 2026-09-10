package com.pratham.audioutils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class AudioExporter {

    public static void save(short[] audioData, String filename)
            throws IOException {

        File file = new File(filename);

        File parent = file.getParentFile();

        if (parent != null) {
            parent.mkdirs();
        }

        try (FileWriter writer = new FileWriter(file)) {

            for (short sample : audioData) {
                writer.write(sample + "\n");
            }
        }
    }
}
