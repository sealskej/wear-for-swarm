package io.seal.swarmwear.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import io.seal.swarmwear.R;
import io.seal.swarmwear.lib.Properties;
import io.seal.swarmwear.service.DoCheckinService;

public class CheckinConfirmationFragment extends DialogFragment {

    public static DialogFragment newInstance(String venueId) {

        Bundle arguments = new Bundle();
        arguments.putString(Properties.Keys.VENUE_ID, venueId);
        CheckinConfirmationFragment fragment = new CheckinConfirmationFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String venueId = getArguments().getString(Properties.Keys.VENUE_ID);

        if (TextUtils.isEmpty(venueId)) {
            throw new IllegalArgumentException("argument[venueId] must NOT be null!");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.do_you_want_make_checking)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DoCheckinService.start(getActivity(), venueId);
                    }
                })
                .setNegativeButton(android.R.string.no, null);

        return builder.create();
    }
}
