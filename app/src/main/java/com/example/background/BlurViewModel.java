/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;

import com.example.background.workers.BlurWorker;
import com.example.background.workers.CleanupWorker;
import com.example.background.workers.SaveImageToFileWorker;

public class BlurViewModel extends ViewModel {

    private Uri mImageUri;
    private WorkManager workManager;

    public BlurViewModel(@NonNull Application application) {
        super();
        mImageUri = getImageUri(application.getApplicationContext());
        workManager = WorkManager.getInstance(application);
    }

    public Data createInputDataForUri(){
        if (mImageUri != null){
            return new Data.Builder()
                    .putString(Constants.KEY_IMAGE_URI, mImageUri.toString())
                    .build();
        }
        return new Data.Builder().build();
    }

    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     * @param blurLevel The amount to blur the image
     */
    void applyBlur(int blurLevel) {

        OneTimeWorkRequest cleanupRequest =
                OneTimeWorkRequest.from(CleanupWorker.class);

        WorkContinuation workContinuation = workManager.beginWith(cleanupRequest);

        for (int level = 0; level < blurLevel; level++){
            OneTimeWorkRequest.Builder blurBuilder =
                    new OneTimeWorkRequest.Builder(BlurWorker.class);

            if (level == 0){
                blurBuilder.setInputData(createInputDataForUri());
            }

            workContinuation = workContinuation.then(blurBuilder.build());
        }

        OneTimeWorkRequest saveRequest =
                OneTimeWorkRequest.from(SaveImageToFileWorker.class);
        workContinuation.then(saveRequest)
                .enqueue();
    }

    private Uri uriOrNull(String uriString) {
        if (!TextUtils.isEmpty(uriString)) {
            return Uri.parse(uriString);
        }
        return null;
    }

    private Uri getImageUri(Context context) {
        Resources resources = context.getResources();

        Uri imageUri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(R.drawable.android_cupcake))
                .appendPath(resources.getResourceTypeName(R.drawable.android_cupcake))
                .appendPath(resources.getResourceEntryName(R.drawable.android_cupcake))
                .build();

        return imageUri;
    }

    /**
     * Getters
     */
    Uri getImageUri() {
        return mImageUri;
    }

}