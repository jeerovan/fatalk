package com.kaarss.fatalk;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

public class FileUtils {
    private FileUtils() {} //private constructor to enforce Singleton pattern

    /** TAG for log messages. */
    static final String TAG = "FileUtils";
    private static final boolean DEBUG = false; // Set to true to enable logging

    private static final String MIME_TYPE_AUDIO = "audio";
    private static final String MIME_TYPE_IMAGE = "image";
    private static final String MIME_TYPE_VIDEO = "video";
    private static final String MIME_TYPE_GIF = "gif";
    public static final String MIME_TYPE_CONTACT = "contact";
    private static final String MIME_TYPE_APPLICATION = "application";

    static String getMimeType(Context context, Uri uri){
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    static int getMessageType(String mime){
        int messageType;
        if(mime.contains(MIME_TYPE_GIF)){
            messageType = Keys.chatMessageTypeGif;
        } else if(mime.contains(MIME_TYPE_IMAGE)){
            messageType = Keys.chatMessageTypePicture;
        } else if(mime.contains(MIME_TYPE_VIDEO)){
            messageType = Keys.chatMessageTypeVideo;
        } else if(mime.contains(MIME_TYPE_AUDIO)){
            messageType = Keys.chatMessageTypeAudio;
        }  else if(mime.contains(MIME_TYPE_APPLICATION)) {
            messageType = Keys.chatMessageTypeDocument;
        } else {
            messageType = -1;
        }
        return messageType;
    }

    private static String getMimeText(int mimeInt){
        String mime;
        if(mimeInt == Keys.chatMessageTypePicture || mimeInt == Keys.chatMessageTypeGif){
            mime = "image/*";
        } else if(mimeInt == Keys.chatMessageTypeAudio){
            mime = "audio/*";
        } else if(mimeInt == Keys.chatMessageTypeVideo){
            mime = "video/*";
        } else if(mimeInt == Keys.chatMessageTypeDocument){
            mime = "application/*";
        } else {
            mime = "*/*";
        }
        return mime;
    }

    public static String getPublicDirectory(int mime){
        String directory;
        if(mime == 1){
            directory = Environment.DIRECTORY_MUSIC;
        } else if(mime == 2){
            directory = Environment.DIRECTORY_PICTURES;
        } else if(mime == 3){
            directory = Environment.DIRECTORY_MOVIES;
        } else {
            directory = Environment.DIRECTORY_DOWNLOADS;
        }
        return directory;
    }

    public static File getFile(int mime,String name){
        File filePath = Environment.getExternalStoragePublicDirectory(getPublicDirectory(mime));
        if (!filePath.mkdirs()) {
            AppLog.e(TAG, "Directory Not Created.");
        }
        return new File(filePath+File.separator+name);
    }
    public static File getMediaFile(String messageId){
        File directory = App.getDirectory();
        return new File(directory,messageId);
    }
    public static void saveMediaPreview(Bitmap bitmapImage, String filename){
        //Log.i(TAG,"Saving Downloaded Image For : "+ filename);
        File directory = App.getDirectory();
        // Create imageDir
        File mypath = new File(directory,filename);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static String getFileSize(long fileSize) {
        String hrSize = "";
        double m = fileSize / (1024.0 * 1024.0);
        DecimalFormat dec = new DecimalFormat("0.0");
        if (m > 1) {
            hrSize = dec.format(m).concat(" MB");
        } else {
            hrSize = dec.format(fileSize / 1024.0).concat(" KB");
        }
        return hrSize;
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
    static void openFile(File file, int mime) {
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        newIntent.setDataAndType(FileProvider.getUriForFile(App.applicationContext,BuildConfig
                .APPLICATION_ID+".provider",file), getMimeText(mime));
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            App.applicationContext.startActivity(newIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(App.applicationContext, "No handler for this type of file.", Toast.LENGTH_LONG)
                    .show();
        }
    }
    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
    static Bitmap decodeSampledBitmapFromUri(Context context,Uri uri, int reqWidth, int reqHeight) {
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap1 = BitmapFactory.decodeStream(inputStream,null,options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        try {
            inputStream = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(inputStream, null, options);
    }
    //-------- Thumbnails For Video/Gif ----
    public static Bitmap createVideoThumbnail(String filePath, int width, int height) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(-1);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }

        if (bitmap == null) return null;
        bitmap = ThumbnailUtils.extractThumbnail(bitmap,width,height);
        return bitmap;
    }
}
