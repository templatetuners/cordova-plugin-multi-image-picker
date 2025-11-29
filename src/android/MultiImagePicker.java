package com.justapplications.multiimagepicker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.content.ClipData;
import android.provider.MediaStore;
import android.content.Context;
import android.database.Cursor;
import android.provider.OpenableColumns;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class MultiImagePicker extends CordovaPlugin {

    private static final int REQUEST_CODE = 10123;
    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, org.json.JSONArray args, CallbackContext callbackContext) {
        if ("pick".equals(action)) {
            this.callbackContext = callbackContext;
            openPicker();
            return true;
        }
        return false;
    }

    private void openPicker() {
        cordova.setActivityResultCallback(this);

        Intent intent;

        if (Build.VERSION.SDK_INT >= 33) {
            // Android 13+ Photo Picker oficial
            intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            intent.setType("image/*");
            intent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 20);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }

        Intent chooser = Intent.createChooser(intent, "Select images");
        cordova.getActivity().startActivityForResult(chooser, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE) {
            return;
        }

        if (callbackContext == null) {
            return;
        }

        if (resultCode == Activity.RESULT_OK && data != null) {
            try {
                JSONArray result = new JSONArray();

                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri uri = clipData.getItemAt(i).getUri();
                        if (uri != null) {
                            String fileUrl = copyUriToTempFile(uri);
                            if (fileUrl != null) {
                                result.put(fileUrl); // ex: file:///data/user/0/.../cache/mip_xxx.jpg
                            }
                        }
                    }
                } else {
                    Uri uri = data.getData();
                    if (uri != null) {
                        String fileUrl = copyUriToTempFile(uri);
                        if (fileUrl != null) {
                            result.put(fileUrl);
                        }
                    }
                }

                callbackContext.success(result);
            } catch (Exception e) {
                callbackContext.error("Failed to get images: " + e.getMessage());
            }
        } else {
            callbackContext.error("No images selected");
        }

        callbackContext = null;
    }

    /**
     * Copy content:// into app cache and returns file:///... 
     */
    private String copyUriToTempFile(Uri uri) throws IOException {
        Context context = this.cordova.getActivity().getApplicationContext();

        // try get original name
        String fileName = "mip_" + System.currentTimeMillis();
        String extension = ".jpg";

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    String displayName = cursor.getString(nameIndex);
                    if (displayName != null && !displayName.isEmpty()) {
                        fileName = displayName;
                    }
                }
            }
        } catch (Exception ignore) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // if no extension we add .jpg
        if (!fileName.contains(".")) {
            fileName = fileName + extension;
        }

        File cacheDir = context.getCacheDir();
        File outFile = new File(cacheDir, "mip_" + System.currentTimeMillis() + "_" + fileName);

        InputStream in = null;
        OutputStream out = null;
        try {
            in = context.getContentResolver().openInputStream(uri);
            out = new FileOutputStream(outFile);

            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } finally {
            if (in != null) try { in.close(); } catch (IOException ignored) {}
            if (out != null) try { out.close(); } catch (IOException ignored) {}
        }

        return "file://" + outFile.getAbsolutePath();
    }
}
