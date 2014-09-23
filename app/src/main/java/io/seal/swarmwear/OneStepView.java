package io.seal.swarmwear;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OneStepView extends LinearLayout {

    @SuppressWarnings("unused")
    public OneStepView(Context context) {
        super(context);
        init(null);
    }

    @SuppressWarnings("unused")
    public OneStepView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    @SuppressWarnings("unused")
    public OneStepView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        inflate(getContext(), R.layout.view_one_step, this);

        setGravity(Gravity.CENTER_VERTICAL);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.OneStepView);

            int number = a.getInt(R.styleable.OneStepView_number, 1);
            String text = a.getString(R.styleable.OneStepView_text);
            Drawable image = a.getDrawable(R.styleable.OneStepView_image);
            boolean rightDirection = a.getBoolean(R.styleable.OneStepView_rightDirection, true);

            a.recycle();

            if (!rightDirection) {
                reverseChildren();
            }

            ((TextView) findViewById(R.id.txtNumber)).setText(number + "");
            ((TextView) findViewById(R.id.txtDescription)).setText(text);
            ((ImageView) findViewById(R.id.image)).setImageDrawable(image);

        }

    }

    private void reverseChildren() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(0);
            removeViewAt(0);
            int index = getChildCount() - 1 - i + 1;
            addView(view, index);
        }
    }

}
