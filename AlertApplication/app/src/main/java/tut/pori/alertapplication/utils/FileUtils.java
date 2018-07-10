/*
 * Copyright 2017 Tampere University of Technology, Pori Department
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tut.pori.alertapplication.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
public final class FileUtils {
    private static final String CLASS_NAME = File.class.toString();
    private static final SimpleDateFormat PHOTO_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private static final String PREFIX_PHOTO = "ALERT_PHOTO_";
    private static final String PROVIDER_AUTHORITY = "com.example.android.fileprovider";
	private static final String SUFFIX_PHOTO = ".jpg";

    /**
     *
     */
    private FileUtils(){
        // nothinf needed
    }

    /**
     *
     * @param context the context user for resolving paths and URIs
     * @return file absolute path and Uri for the created file or null on failure
     */
    public static Pair<String, Uri> createFile(Context context) {
		File file = null;
        try {
            file = File.createTempFile(PREFIX_PHOTO+PHOTO_DATE_FORMAT.format(new Date())+'_', SUFFIX_PHOTO, context.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            Log.d(CLASS_NAME, "Saving to path " + file.getAbsolutePath());
        } catch (IOException ex) {
            Log.e(CLASS_NAME, "Failed to create file.", ex);
			return null;
        }
        return Pair.of(file.getAbsolutePath(), FileProvider.getUriForFile(context, PROVIDER_AUTHORITY, file));
    }

    /**
     *
     * @param filePath
     * @return true if the file was deleted
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        try {
            return file.delete();
        } catch (SecurityException ex){
            Log.w(CLASS_NAME, "Failed to delete file, path: "+filePath, ex);
            return false;
        }
    }

    /**
     *
     * @param path
     * @return the path as file object
     */
    public static File getFile(String path) {
        return new File(path);
    }
}
