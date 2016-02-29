package com.km.fusionbook.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.km.fusionbook.R;
import com.km.fusionbook.interfaces.StringCallback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class for image processing
 */
public class ImageUtils {

    /**
     * Transforms an image to be circular
     * @param source The image to process
     * @return The same image with a circle shape
     */
    public static Bitmap getCircularBitmapImage(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;
        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);
        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);
        squaredBitmap.recycle();
        return bitmap;
    }

    /**
     * Uploads an image to Cloudinary
     * @param context The current context
     * @param inputStream The stream for the image to upload
     * @param pictureId The unique ID for the saved image
     * @param callback A callback to execute some logic when upload is done
     */
    public static void upload(Context context, final InputStream inputStream, final String pictureId, final StringCallback callback) {

        final Cloudinary cloudinary = new Cloudinary(context.getString(R.string.cloudinary_url));

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Map map = cloudinary.uploader().upload(inputStream, ObjectUtils.asMap("public_id", pictureId));
                    String url = null;
                    try {
                        url = (String) map.get("url");
                    } catch (Exception e) {
                        // Returned map is null or empty
                        // Do nothing
                    }
                    if (callback != null) {
                        callback.done(url, null);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.done(null, e);
                    }
                }
            }
        };

        new Thread(runnable).start();
    }

    /**
     * Deletes a specific image from Cloudinary
     * @param context The current context
     * @param pictureId The unique ID of the image to delete
     */
    public static void delete(Context context, final String pictureId) {

        final Cloudinary cloudinary = new Cloudinary(context.getString(R.string.cloudinary_url));

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    cloudinary.uploader().destroy(pictureId, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(runnable).start();
    }

    /**
     * Creates a file to save an image
     * @return The created file
     * @throws IOException If unable to create file on the filesystem
     */
    public static File createImageFile() throws IOException {
        // Create the file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        // Create the actual file
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }
}
