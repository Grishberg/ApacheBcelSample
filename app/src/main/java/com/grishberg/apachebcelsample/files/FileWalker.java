package com.grishberg.apachebcelsample.files;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileWalker {
    private static final String TAG = FileWalker.class.getSimpleName();

    private ArrayList<File> files = new ArrayList<>();

    public void walk(File root) {
        walkAllFilesInside(root);
    }

    public List<File> getFiles() {
        return files;
    }

    private void walkAllFilesInside(File root) {

        File[] list = root.listFiles();

        for (File f : list) {
            if (f.isDirectory()) {
                Log.d(TAG, "Dir: " + f.getAbsoluteFile());
                walkAllFilesInside(f);
            } else {
                Log.d(TAG, "File: " + f.getAbsoluteFile());
                files.add(f);
            }
        }
    }
}
