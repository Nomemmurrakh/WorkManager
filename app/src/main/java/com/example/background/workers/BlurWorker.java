package com.example.background.workers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.background.Constants;
import com.example.background.R;

import java.io.FileNotFoundException;

public class BlurWorker extends Worker {

    private Context applicationContext;

    public BlurWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        applicationContext = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Result doWork() {

        WorkerUtils.makeStatusNotification("Blurring Image", applicationContext);
        WorkerUtils.sleep();
        String resourceUri = getInputData().getString(Constants.KEY_IMAGE_URI);

        Bitmap picture = null;
        try {

            if (TextUtils.isEmpty(resourceUri)){
                throw new IllegalArgumentException("Invalid image uri.");
            }

            picture = BitmapFactory.decodeStream(
                    applicationContext.getContentResolver().openInputStream(
                            Uri.parse(resourceUri)
                    )
            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap blurredPicture = WorkerUtils.blurBitmap(
                picture,
                applicationContext
        );

        try {
            Uri uri = WorkerUtils.writeBitmapToFile(
                    applicationContext,
                    blurredPicture
            );

            WorkerUtils.makeStatusNotification(
                    "Image blurred Successfully, " + uri.toString(),
                    applicationContext
            );

            Data output = new Data.Builder()
                    .putString(Constants.KEY_IMAGE_URI, uri.toString())
                    .build();

            return Result.success(output);
        } catch (FileNotFoundException e) {
            Log.d("TAG", "doWork: " + "Image Not Blurred Successfully.");
            return Result.failure();
        }
    }
}
