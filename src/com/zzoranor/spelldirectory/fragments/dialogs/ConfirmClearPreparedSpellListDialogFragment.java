package com.zzoranor.spelldirectory.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.zzoranor.spelldirectory.activity.CharacterList;

/**
 * Created with IntelliJ IDEA.
 * User: Kenton
 * Date: 6/16/13
 * Time: 4:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfirmClearPreparedSpellListDialogFragment extends DialogFragment {

    private ConfirmListClearCallback mCallback;

    public ConfirmClearPreparedSpellListDialogFragment(ConfirmListClearCallback callback)  {
        mCallback = callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Really clear the prepared list?")
                .setCancelable(false).setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int id) {

                        mCallback.listClearCallback();
                    }
                }).setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    public interface ConfirmListClearCallback {

        public void listClearCallback();

    }
}
