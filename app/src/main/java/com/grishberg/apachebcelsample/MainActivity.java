package com.grishberg.apachebcelsample;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.github.grishberg.consoleview.Logger;
import com.github.grishberg.consoleview.LoggerImpl;
import com.grishberg.apachebcelsample.files.FileWalker;

import java.io.File;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 123;

    private Logger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logger = new LoggerImpl();
        checkPermission();
    }

    private void checkPermission() {
        // Check whether this app has write external storage permission or not.
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // If do not grant write external storage permission.
        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            // Request user to grant write external storage permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
        } else {
            doWork();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            int grantResultsLength = grantResults.length;
            if (grantResultsLength > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doWork();
            } else {
                Toast.makeText(
                        getApplicationContext(),
                        "You denied write external storage permission.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * @return True if the external storage is available. False otherwise.
     */
    public static boolean isAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static String getSdCardPath() {
        return Environment.getExternalStorageDirectory().getPath() + "/";
    }

    private void doWork() {
        FileWalker fileWalker = new FileWalker();
        String rootDirName = getSdCardPath() + "Download/classes/barcodescanner";

        File rootFolder = new File(rootDirName);
        fileWalker.walk(rootFolder);
        List<File> files = fileWalker.getFiles();

        Parser parser = new Parser(logger);
        String classFileName = getSdCardPath() + "Download/classes/barcodescanner/main/MainScreenService.class";

        try {
            parser.parse(new File(classFileName), files);
        } catch (ParseErrorException e) {
            logger.d(TAG, e.getMessage());
        }
    }
}
