package xyz.gangle.bleconnector.presentation.customviews;


import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import timber.log.Timber;

/**
 * Created by Tong Gang on 8/10/16.
 */

public class MacAddressTextWatcher implements TextWatcher {

    private EditText editText;
    private String match;


    public MacAddressTextWatcher(EditText editText, String match) {
        this.editText = editText;
        this.match = match;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
//        Timber.v("变化前:" + charSequence + ";" + start + ";" + count + ";" + after);
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
//        Timber.v("变化后:" + charSequence + ";" + start + ";" + before + ";" + count);
    }

    @Override
    public void afterTextChanged(Editable editable) {
        String value = editable.toString();

        if (value.matches(match))
            editText.setTextColor(Color.BLACK);
        else
            editText.setTextColor(Color.RED);

    }
}
