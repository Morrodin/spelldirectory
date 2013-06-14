package com.zzoranor.spelldirectory.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import com.zzoranor.spelldirectory.R;
import com.zzoranor.spelldirectory.util.Constants;

/**
 * Dialog to choose the long click mode for the prepared spell list.
 *
 * @author morrodin
 */
public class PreparedSpellsLongclickModeDialogFragment extends DialogFragment {

    private PreparedLongclickModeCallback mCallback;
    private CharSequence[] items = {"Longclick Remove", "Longclick Use"};
    private int mCurrentMode;

    public PreparedSpellsLongclickModeDialogFragment(PreparedLongclickModeCallback callback, int longClickMode) {
        this.mCallback = callback;
        this.mCurrentMode = longClickMode;

        findCurrentLongClickMode();
    }

    private void findCurrentLongClickMode() {
        switch(mCurrentMode) {
            case Constants.LONG_CLICK_REMOVE_PREPARED:
                mCurrentMode = 0;
                break;
            case Constants.LONG_CLICK_USE_SPELL:
                mCurrentMode = 1;
                break;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.drawer_longclick).setSingleChoiceItems(items,
                mCurrentMode, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if (which == 0) {
                    mCallback.longclickRemoveSelected();
                } else if (which == 1) {
                    mCallback.longclickUseSelected();
                }
            }
        });

        return builder.create();
    }

    public interface PreparedLongclickModeCallback {

        public void longclickRemoveSelected();

        public void longclickUseSelected();
    }
}
