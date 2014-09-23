package io.seal.swarmwear.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import io.seal.swarmwear.Utils;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

public class ConsoleActivity extends BaseActivity {

    private static final String TAG = "ConsoleActivity";

    private TextView mConsoleTextView;
    private ScrollView mScrollView;
    private Tailer mTailer;
    private MenuItem mAutoScrollItem;
    Runnable mScrollDownRunnable = new Runnable() {
        @Override
        public void run() {
            if (mAutoScrollItem == null || mAutoScrollItem.isChecked()) {
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScrollView = new ScrollView(this);
        mScrollView.setSmoothScrollingEnabled(false);

        mConsoleTextView = new TextView(this);
        mConsoleTextView.setTextSize(10);

        mScrollView.addView(mConsoleTextView);

        setContentView(mScrollView);

    }

    @Override
    protected void onResume() {
        super.onResume();

        mTailer = new Tailer(Utils.getLogFile(), new LogTailerListener());
        Thread thread = new Thread(mTailer);
        thread.start();
    }

    @Override
    protected void onPause() {
        mTailer.stop();
        mTailer = null;
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mScrollView.removeCallbacks(mScrollDownRunnable);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mAutoScrollItem = menu.add("Auto Scroll").setCheckable(true);
        mAutoScrollItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mAutoScrollItem.setChecked(!mAutoScrollItem.isChecked());
                return true;
            }
        });
        mAutoScrollItem.setChecked(true);
        return true;
    }

    private class LogTailerListener extends TailerListenerAdapter {

        private final StringBuilder mStringBuilder;
        private Handler mHandler;

        private LogTailerListener() {
            mHandler = new Handler(Looper.getMainLooper());
            mStringBuilder = new StringBuilder();
        }

        @Override
        public void handle(Exception ex) {
            super.handle(ex);
            Log.e(TAG, "", ex);
        }

        public void handle(final String line) {
            mStringBuilder.append(line).append("\n");
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, 200);
        }

        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                mConsoleTextView.append(mStringBuilder.toString());
                mStringBuilder.setLength(0);
                mScrollView.post(mScrollDownRunnable);
            }
        };

    }
}
