package io.seal.swarmwear.service;

import android.content.Intent;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

public abstract class BaseSpiceRequestService<T> extends BaseSpiceManagerService implements RequestListener<T> {

    protected abstract SpiceRequest<T> onCreateRequest(Intent intent);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getSpiceManager().execute(onCreateRequest(intent), this);
        return super.onStartCommand(intent, flags, startId);
    }

}
