package com.zzoranor.spelldirectory.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.zzoranor.spelldirectory.R;
import com.zzoranor.spelldirectory.util.Constants;

/**
 * Dialog to choose the long click mode for the primary spell list
 *
 * @author morrodin
 */
public class SpellsLongclickModeDialogFragment extends DialogFragment {

    private CharSequence[] items = {"Prepare Spell", "Add to Known Spells", "Remove Prepared Spell"};
    private SpellLongclickModeCallback mCallback;
    private int mCurrentMode;
    boolean mIsBrowsingKnownSpells;

    /**
     * Constructor
     *
     * @param callback
     *          Class which button press callbacks should be passed to
     */
    public SpellsLongclickModeDialogFragment(SpellLongclickModeCallback callback,
                                             int currentLongclickMode, boolean browsingKnownSpells) {
        this.mCallback = callback;
        this.mCurrentMode = currentLongclickMode;
        this.mIsBrowsingKnownSpells = browsingKnownSpells;

        setupItems();
        findCurrentLongclickMode();
    }

    /**
     * Choice names will be different depending on whether or not we are browsing known spells.
     * Detect for that here.
     */
    private void setupItems() {
        if (mIsBrowsingKnownSpells) {
            items = new CharSequence[]{"Prepare Spell", "Remove from Known Spells", "Remove Prepared Spell"};
        }
    }

    /**
     * Associates the correct menu item position with the current long click mode
     */
    private void findCurrentLongclickMode() {
        switch(mCurrentMode) {
            case Constants.LONG_CLICK_PREPARE:
                mCurrentMode = 0;
                break;
            case Constants.LONG_CLICK_KNOWN_SPELL:
                mCurrentMode = 1;
                break;
            case Constants.LONG_CLICK_REMOVE_PREPARED:
                mCurrentMode = 2;
                break;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.drawer_longclick).setSingleChoiceItems(items, mCurrentMode,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch(which) {
                    case 0:
                        mCallback.longclickPrepareSelected();
                        break;

                    case 1:
                        mCallback.longclickAddToKnownSelected();
                        break;

                    case 2:
                        mCallback.longclickRemoveSelected();
                        break;
                }
            }
        });

        return builder.create();
    }

    public interface SpellLongclickModeCallback {

        public void longclickPrepareSelected();

        public void longclickRemoveSelected();

        public void longclickAddToKnownSelected();

    }

}
