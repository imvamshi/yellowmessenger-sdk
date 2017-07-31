package com.yellowmessenger.sdk.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import com.yellowmessenger.sdk.ChatActivity;
import com.yellowmessenger.sdk.R;


public class RecordDialogFragment extends DialogFragment {

    ProgressDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.MyDialog);
        int dialogWidth = LinearLayout.LayoutParams.MATCH_PARENT;
        int dialogHeight = LinearLayout.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);


        View rootView = inflater.inflate(R.layout.record_fragment, container,
                false);

        rootView.findViewById(R.id.stopButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ChatActivity)getActivity()).stopRecording();
            }
        });


        return rootView;
    }



    @SuppressWarnings("deprecation")
    @Override
    public void onStart() {
        super.onStart();

        // change dialog width
        if (getDialog() != null) {

            int fullWidth = getDialog().getWindow().getAttributes().width;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                fullWidth = size.x;
            } else {
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                fullWidth = display.getWidth();
            }

            final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                    .getDisplayMetrics());

            int w = fullWidth - padding;
            int h = getDialog().getWindow().getAttributes().height;

            getDialog().getWindow().setLayout(w, h);
        }
    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        return show(transaction, tag, false);
    }


    public int show(FragmentTransaction transaction, String tag, boolean allowStateLoss) {
        transaction.add(this, tag);
        return allowStateLoss ? transaction.commitAllowingStateLoss() : transaction.commit();
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
