package io.seal.swarmwear;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class CountDownView extends TextView {

    @SuppressWarnings("unused")
    public CountDownView(Context context) {
        super(context);
    }

    @SuppressWarnings("unused")
    public CountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("unused")
    public CountDownView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void start(int totalSeconds) {
        setText(totalSeconds-- + "");
        long delayMillis = TimeUnit.SECONDS.toMillis(1);
        new Handler().postDelayed(new CountDownRunnable(totalSeconds, delayMillis), delayMillis);
    }

    private class CountDownRunnable implements Runnable {

        private final long mDelayMillis;
        private int mTotalSeconds;

        private CountDownRunnable(int totalSeconds, long delayMillis) {
            this.mTotalSeconds = totalSeconds;
            mDelayMillis = delayMillis;
        }

        @Override
        public void run() {
            setText(mTotalSeconds-- + "");
            if (mTotalSeconds > -1) {
                postDelayed(this, mDelayMillis);
            }
        }

    }
}