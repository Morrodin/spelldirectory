package com.zzoranor.spelldirectory.fragments.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Kenton
 * Date: 6/16/13
 * Time: 12:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrepareWithMetamagicDialogFragment extends DialogFragment {

    private PrepareWithMetamagicCallback mCallback;
    private List<String> metamagicList;

    public PrepareWithMetamagicDialogFragment( PrepareWithMetamagicCallback callback,
                                              List<String> metamagicList) {
        this.mCallback = callback;
        this.metamagicList = metamagicList;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String[] arr = new String[metamagicList.size()];
        arr = metamagicList.toArray(arr);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Metamagic");
        builder.setItems(arr, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                mCallback.metamagicConfirmCallback(metamagicList.get(item));
            }
        });
        return  builder.create();
    }

    public interface PrepareWithMetamagicCallback {

        public void metamagicConfirmCallback(String item);

    }
}
