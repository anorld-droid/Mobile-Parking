package com.karanja.Api;

import android.util.Base64;

import androidx.annotation.NonNull;


import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import com.karanja.BuildConfig;

public class AccessTokenInterceptor implements Interceptor {
    public AccessTokenInterceptor() {

    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        Request request = chain.request().newBuilder()
                .addHeader("Authorization", "Basic cFJZcjZ6anEwaThMMXp6d1FETUxwWkIzeVBDa2hNc2M6UmYyMkJmWm9nMHFRR2xWOQ==")
                .addHeader("Content-Type", "application/json")
                .build();
        return chain.proceed(request);
    }
}