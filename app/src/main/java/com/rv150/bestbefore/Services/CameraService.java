package com.rv150.bestbefore.Services;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.rv150.bestbefore.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivan on 06.02.17.
 */

public class CameraService {

    private static final String TEMP_IMAGE_NAME = "tempImage";
    private static boolean useWay2 = false;

    public static Intent getPickImageIntent(Context context) throws IOException {
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(context)));
        intentList = addIntentsToList(context, intentList, pickIntent);
        intentList = addIntentsToList(context, intentList, takePhotoIntent);

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                    context.getString(R.string.pick_image_intent_text));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }
        return chooserIntent;
    }

    private static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
    }

    public static File getTempFile(Context context) throws IOException {
        try {
            if (useWay2) {
                throw new RuntimeException("Go to way2");
            }
            File imageFile = new File(context.getExternalCacheDir(), TEMP_IMAGE_NAME);
            if (imageFile.getParentFile() != null) {
                imageFile.getParentFile().mkdirs();
            }
            return imageFile;
        }
        catch (Exception ex) {
            useWay2 = true;
            File outputDir = context.getCacheDir(); // context being the Activity pointer
            return File.createTempFile(TEMP_IMAGE_NAME, null, outputDir);
        }
    }
}
