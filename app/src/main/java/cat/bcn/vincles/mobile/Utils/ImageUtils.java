package cat.bcn.vincles.mobile.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.CallSuper;
import android.support.media.ExifInterface;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import cat.bcn.vincles.mobile.R;

import static android.content.Context.MODE_PRIVATE;

public class ImageUtils {

    private final static int DESIREDWIDTH = 2048;

    public static Uri getImageUri(Context context, Bitmap image) {

        Calendar c = Calendar.getInstance();

        ContextWrapper wrapper = new ContextWrapper(context);
        File file = wrapper.getDir("Images",MODE_PRIVATE);
        file = new File(file, generateUniqueFileName()+".jpg");
        try{
            OutputStream stream = null;
            stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG,100,stream);
            stream.flush();
            stream.close();
        }catch (IOException e) // Catch the exception
        {
            e.printStackTrace();
        }
        String absolutaPath = file.getAbsolutePath();
        Uri savedImageURI = Uri.parse(file.getAbsolutePath());
        return savedImageURI;
    }

    public static String generateUniqueFileName() {
        return System.currentTimeMillis() + "_"
                + UUID.randomUUID().toString().substring(0, 7);
    }

    public static Uri saveFile(InputStream data) {
        ContextWrapper wrapper = new ContextWrapper(MyApplication.getAppContext());
        File file = wrapper.getDir("Images",MODE_PRIVATE);
        file = new File(file, generateUniqueFileName() +".jpg");
        try {
            FileUtils.copyInputStreamToFile(data, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(file);
    }

    public static Uri saveFile(Context context, Bitmap image) {

        Calendar c = Calendar.getInstance();

        ContextWrapper wrapper = new ContextWrapper(context);
        File file = wrapper.getDir("Images",MODE_PRIVATE);
        file = new File(file, generateUniqueFileName()+".jpg");
        try{
            OutputStream stream = null;
            stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG,100,stream);
            stream.flush();
            stream.close();
        }catch (IOException e) // Catch the exception
        {
            e.printStackTrace();
        }
        String absolutaPath = file.getAbsolutePath();
        Uri savedImageURI = Uri.parse(file.getAbsolutePath());
        return savedImageURI;
    }

    public static String getRealPathFromURI(Uri contentURI, Activity context) {
        String[] projection = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = context.managedQuery(contentURI, projection, null,
                null, null);
        if (cursor == null)
            return null;
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        if (cursor.moveToFirst()) {
            String s = cursor.getString(column_index);
            return s;
        }
        return null;
    }

    public static String getMimeType(String url)
    {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static String getImage64(String imagePath){
        Log.d("rgph", "getImage64 start");
        InputStream inputStream = null;//You can get an inputStream using any IO API
        try {
            inputStream = new FileInputStream(imagePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] bytes;
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        bytes = output.toByteArray();
        Log.d("rgph", "getImage64 end, byyes length:"+bytes.length);
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static Bitmap getCorrectlyOrientedImage(Context context, String path) throws IOException {

        InputStream is = context.getContentResolver().openInputStream(Uri.parse(path));
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, Uri.parse(path));

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(Uri.parse(path));

        srcBitmap = BitmapFactory.decodeStream(is);

        is.close();

        /*
         * if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation.
         */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        return srcBitmap;
    }

    public static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels, int heigh, int width) {


        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, heigh);
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static void drawCutoutBackground(View view) {
        int width = view.getWidth();
        int height = view.getHeight();
        float radius = height / 2 + view.getResources().getDimension(R.dimen.main_avatar_radius_margin);

        Paint transparentPaint = new Paint();
        transparentPaint.setColor(0xFFFFFF);
        transparentPaint.setAlpha(0);
        transparentPaint.setAntiAlias(true);
        transparentPaint.setColor(Color.TRANSPARENT);
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(view.getResources().getColor(R.color.darkGray));
        backgroundPaint.setAntiAlias(true);

        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bitmap = Bitmap.createBitmap(width, height, conf); // this creates a MUTABLE bitmap

        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(0,0,width,height, backgroundPaint);
        canvas.drawRect(0,0, radius, height, transparentPaint);
        canvas.drawCircle(radius, height/2, radius, transparentPaint);

        BitmapDrawable drawable = new BitmapDrawable(view.getResources(), bitmap);
        view.setBackground(drawable);
    }

    public static File createAvatarFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, generateUniqueFileName() + ".jpg");

        Log.d("newimg","create image, name:"+image.getAbsolutePath()+" currTime:"+System.currentTimeMillis());

        return image;
    }

    public static File createAudioFile(Context context) throws IOException {
        Long timeStamp = System.currentTimeMillis();
        String audioFileName = timeStamp.toString();
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(storageDir, audioFileName + ".aac");
    }

    public static String decodeFile(String path) throws IOException, IllegalArgumentException  {
        String strMyImagePath = null;
        Bitmap scaledBitmap = null;
        Bitmap adjustedBitmap = null;
        ExifInterface exif = new ExifInterface(path);
        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int rotationInDegrees = exifToDegrees(rotation);
        Matrix matrix = new Matrix();
        if (rotation != 0f) {matrix.preRotate(rotationInDegrees);}
        try {
            // Part 1: Decode image
            Bitmap unscaledBitmap = ScalingUtils.decodeFile(path, DESIREDWIDTH, DESIREDWIDTH, ScalingUtils.ScalingLogic.FIT);

            if (!(unscaledBitmap.getWidth() <= DESIREDWIDTH && unscaledBitmap.getHeight() <= DESIREDWIDTH)) {
                // Part 2: Scale image
                scaledBitmap = ScalingUtils.createScaledBitmap(unscaledBitmap, DESIREDWIDTH, DESIREDWIDTH, ScalingUtils.ScalingLogic.FIT);

            } else {
                unscaledBitmap.recycle();
                return path;
            }

            adjustedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            scaledBitmap.recycle();
            // Store to tmp file


            File f = new File(path);

            strMyImagePath = f.getAbsolutePath();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                adjustedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {

                e.printStackTrace();
            } catch (Exception e) {

                e.printStackTrace();
            }

            adjustedBitmap.recycle();
        } catch (Throwable e) {
        }

        if (strMyImagePath == null) {
            return path;
        }
        return strMyImagePath;

    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    /**
     * Retrieve video frame image from given video path
     *
     * @param p_videoPath
     *            the video file path
     *
     * @return Bitmap - the bitmap is video frame image
     *
     * @throws Throwable
     */
    @SuppressLint("NewApi")
    public static Bitmap retriveVideoFrameFromVideo(String p_videoPath)
            throws Throwable
    {
        Bitmap m_bitmap = null;
        MediaMetadataRetriever m_mediaMetadataRetriever = null;
        try
        {
            m_mediaMetadataRetriever = new MediaMetadataRetriever();
            m_mediaMetadataRetriever.setDataSource(p_videoPath);
            m_bitmap = m_mediaMetadataRetriever.getFrameAtTime();
        }
        catch (Exception m_e)
        {
            throw new Throwable(
                    "Exception in retriveVideoFrameFromVideo(String p_videoPath)"
                            + m_e.getMessage());
        }
        finally
        {
            if (m_mediaMetadataRetriever != null)
            {
                m_mediaMetadataRetriever.release();
            }
        }
        return m_bitmap;
    }

    public static void saveMediaExternalMemory(Context context, String fileName,
                                               String internalFilePath) {
        if (isExternalStorageWritable()) {
            File externalDir = getPublicAlbumStorageDir();
            try {
                storeFileExternalMemory(new File(internalFilePath), externalDir, fileName);
            } catch (IOException e) {
                Log.d("SaveFileExternalMemory","storeFileExternalMemory error: "+e);
            }
        }
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static File getPublicAlbumStorageDir() {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                "VINCLES" + File.separator);
        /*File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "VINCLES");
*/
        /*File file = new File(Environment.getExternalStoragePublicDirectory(
                null), "VINCLES");*/
        if (!file.mkdirs()) {
            Log.e("image_utils", "Directory not created, path:"+file.getPath());
        }
        return file;
    }

    private static File storeFileExternalMemory(File src, File dst, String dstFilename) throws IOException {

        //if folder does not exist
        if (!dst.exists()) {
            if (!dst.mkdir()) {
                Log.e("image_utils", "storeFileExternalMemory cannit mkdir");
                return null;
            }
        }

        File expFile = new File(dst.getPath() + File.separator + dstFilename);
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(expFile).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            if (inChannel != null) {
                inChannel.transferTo(0, inChannel.size(), outChannel);
            }
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }

        return expFile;
    }

    public static void deleteFileIfExists(String path) {
        File myFile = new File(path);
        if(myFile.exists())
            myFile.delete();
    }

    public static void safeCopyFileToExternalStorage(String internalPath, String fileName) {
        String[] pathParts = internalPath.split("\\.");
        if (pathParts.length <=0) {
            Log.w("ImageUtils", "Error copying file due to missing extension. Path:"
                    + internalPath);
            return;
        }
        String extension = pathParts[pathParts.length-1];
        String externalPath = ImageUtils.getPublicAlbumStorageDir().getPath();
        String externalFilename = fileName + "." + extension;

        ImageUtils.deleteFileIfExists(externalPath + File.separator + externalFilename);
        ImageUtils.saveMediaExternalMemory(MyApplication.getAppContext(), externalFilename,
                internalPath);
    }

}
