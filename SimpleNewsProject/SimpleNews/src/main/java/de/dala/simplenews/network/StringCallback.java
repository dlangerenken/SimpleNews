package de.dala.simplenews.network;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Daniel on 06.02.2015.
 */
public abstract class StringCallback implements Callback {

    @Override
    public abstract void onFailure(Call call, IOException e);
    public abstract void success(String result);

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        success(response.body().string());
    }

}
