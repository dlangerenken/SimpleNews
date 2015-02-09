package de.dala.simplenews.network;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by Daniel on 06.02.2015.
 */
public abstract class StringCallback implements Callback{

    @Override
    public abstract void onFailure(Request request, IOException e) ;

    public abstract void success(String result);

    @Override
    public void onResponse(Response response) throws IOException {
        success(response.body().string());
    }

}
