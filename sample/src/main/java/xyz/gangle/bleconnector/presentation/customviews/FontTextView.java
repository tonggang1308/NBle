package xyz.gangle.bleconnector.presentation.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import xyz.gangle.bleconnector.R;


/**
 * 可以通过
 * <pre>
 *     xmlns:app="http://schemas.android.com/apk/res-auto"
 *     然后
 *     app:customTypeFace="fonts/ds_digit.ttf"
 *     这样的形式使用自定义字体
 * </pre>
 */
public class FontTextView extends TextView {

    protected String fontPath;

    public FontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomFont, defStyle, 0);
        fontPath = a.getString(R.styleable.CustomFont_customTypeFace);
        if (fontPath != null) {
            try {
                super.setTypeface(Typeface.createFromAsset(context.getAssets(), fontPath));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        a.recycle();
    }

    public FontTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public FontTextView(Context context) {
        super(context);
    }


}
