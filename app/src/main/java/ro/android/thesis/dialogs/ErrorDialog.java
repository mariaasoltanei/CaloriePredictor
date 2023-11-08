package ro.android.thesis.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import ro.android.thesis.R;

public class ErrorDialog extends DialogFragment {
    String errorDescription;

    public ErrorDialog(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Button btnCancelDialog;
        TextView tvErrorDescription;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.error_dialog, null);
        btnCancelDialog = dialogView.findViewById(R.id.btnCancelDialog);
        tvErrorDescription = dialogView.findViewById(R.id.tvErrorDescription);
        builder.setView(dialogView);

        AlertDialog ad = builder.create();

        if (ad.getWindow() != null) {
            ad.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ad.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        tvErrorDescription.setText(errorDescription);
        btnCancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.dismiss();
            }
        });
        return ad;
    }
    public void setErrorText(){

    }
}
