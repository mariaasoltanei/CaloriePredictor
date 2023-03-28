package ro.android.thesis.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import ro.android.thesis.R;

public class LoadingDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.loading_screen, null);
        builder.setView(dialogView);

        AlertDialog ad = builder.create();

        if (ad.getWindow() != null) {
            ad.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ad.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return ad;
    }
    public void startLoadingDialogFragment(){
        this.setCancelable(false);
        this.show(getChildFragmentManager(), "loading_screen");
    }
}
