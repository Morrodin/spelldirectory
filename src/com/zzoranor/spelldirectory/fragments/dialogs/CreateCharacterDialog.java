//package com.zzoranor.spelldirectory.fragments.dialogs;
//
//
//import android.app.Dialog;
//import android.os.Bundle;
//import android.support.v4.app.DialogFragment;
//import android.widget.EditText;
//import com.zzoranor.spelldirectory.R;
//
///**
// * DialogFragment for creating a new character.
// *
// * @author morrodin
// */
//public class CreateCharacterDialog extends DialogFragment {
//
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        Dialog dialog = new Dialog(getActivity());
//        dialog.setTitle("Create New Character");
//        dialog.setContentView(R.layout.create_dialog);
//        EditText etext = (EditText) dialog.findViewById(R.id.create_charname);
//
//        etext.setText("");
//        dialog.findViewById(R.id.create_ok).setOnClickListener(characterListFragment);
//        dialog.findViewById(R.id.create_cancel).setOnClickListener(characterListFragment);
//        dialog.setOnCancelListener(this);
//        dialog = dialog;
//    }
//}
