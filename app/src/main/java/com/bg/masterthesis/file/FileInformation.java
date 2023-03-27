package com.bg.masterthesis.file;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.bg.masterthesis.Constants;

import java.io.File;

public class FileInformation {
    static long fileSizeBytes;
    static String fileSizeUnit;

    @SuppressLint("Range")
    public static String getFileName(Uri uri, Context context)
    {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if(fileName != null) {
            int cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }
        return fileName;
    }

    @SuppressLint("Range")
    public static double getFileSize(Uri uri, Context context)
    {
        File file = new File(uri.getPath());
        double fileSize = 0;
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                fileSize = cursor.getDouble(cursor.getColumnIndex(OpenableColumns.SIZE));
            }
        }
        if(fileSize == 0)
            fileSize = file.length();
        fileSizeBytes = (long) fileSize;
        if(fileSize > Constants.size1Kb) {
            fileSize /= Constants.size1Kb;
            if(fileSize > Constants.size1Kb) {
                fileSize /= Constants.size1Kb;
            }
        }
        return fileSize;
    }

    public static long getFileSizeBytes()
    {
        return fileSizeBytes;
    }

    public static String getFileSizeUnit(double fileSize)
    {
        fileSizeUnit = Constants.fileSizeUnitBytes;
        if(fileSize > Constants.size1Kb) {
            fileSize /= Constants.size1Kb;
            fileSizeUnit = Constants.fileSizeUnitKB;
            if(fileSize > Constants.size1Kb) {
                fileSizeUnit = Constants.fileSizeUnitMB;
            }
        }

        return fileSizeUnit;
    }
}
