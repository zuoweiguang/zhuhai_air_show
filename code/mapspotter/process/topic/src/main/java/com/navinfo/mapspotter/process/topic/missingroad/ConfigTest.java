package com.navinfo.mapspotter.process.topic.missingroad;

import java.io.*;

/**
 * Created by cuiliang on 2016/6/22.
 */
public class ConfigTest {
    public static void main(String[] args) throws IOException {
        String fileName = "chengdu_tecent_tile.txt";
        File file = new File(ConfigTest.class.getResource("/" + fileName).getFile());
        FileInputStream fis = new FileInputStream(file);

        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }

    }
}
