package io.seal.swarmwear.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.foursquare.android.nativeoauth.FoursquareOAuth;
import com.foursquare.android.nativeoauth.model.AccessTokenResponse;
import com.foursquare.android.nativeoauth.model.AuthCodeResponse;
import io.seal.swarmwear.BuildConfig;
import io.seal.swarmwear.BusProvider;
import io.seal.swarmwear.EventManager;
import io.seal.swarmwear.R;
import io.seal.swarmwear.event.FoursquareAccessTokenAvailableEvent;
import io.seal.swarmwear.networking.Foursquare;

public class ConnectFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ConnectFragment";

    private static final int CONNECT_REQUEST_CODE = 1001;
    private static final int TOKEN_EXCHANGE_REQUEST_CODE = 1002;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connect, container, false);
        view.findViewById(R.id.btnConnect).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        Intent intent = FoursquareOAuth.getConnectIntent(getActivity(), BuildConfig.FOURSQUARE_CLIENT_ID);
        startActivityForResult(intent, CONNECT_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case CONNECT_REQUEST_CODE:

                AuthCodeResponse codeResponse = FoursquareOAuth.getAuthCodeFromResult(resultCode, data);
                String code = codeResponse.getCode();

                if (!TextUtils.isEmpty(code)) {
                    Intent intent = FoursquareOAuth.getTokenExchangeIntent(getActivity(), BuildConfig.FOURSQUARE_CLIENT_ID,
                            BuildConfig.FOURSQUARE_CLIENT_SECRET, code);
                    startActivityForResult(intent, TOKEN_EXCHANGE_REQUEST_CODE);

                } else {
                    EventManager.trackAndLogEvent(TAG, "connect request failed");
                }
                break;

            case TOKEN_EXCHANGE_REQUEST_CODE:

                AccessTokenResponse tokenResponse = FoursquareOAuth.getTokenFromResult(resultCode, data);
                String accessToken = tokenResponse.getAccessToken();

                if (!TextUtils.isEmpty(accessToken)) {
                    Foursquare.saveAccessToken(getActivity(), accessToken);
                    BusProvider.getInstance().post(new FoursquareAccessTokenAvailableEvent());
                } else {
                    EventManager.trackAndLogEvent(TAG, "token exchange failed");
                }

                break;
        }
    }
}
