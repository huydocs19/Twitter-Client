package com.codepath.apps.restclienttemplate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;


public class SaveDraftFragment extends DialogFragment {

    private RelativeLayout rlDeleteDraft;
    private RelativeLayout rlSaveDraft;
    private TextView tvTweetReply;
    private Button btnCancel;

    public SaveDraftFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static SaveDraftFragment newInstance(String tweetContent) {
        SaveDraftFragment frag = new SaveDraftFragment();
        Bundle args = new Bundle();
        args.putString("tweetContent", tweetContent);
        frag.setArguments(args);
        return frag;
    }


    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_save_draft, parent, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view

        rlDeleteDraft = view.findViewById(R.id.rlDeleteDraft);
        rlSaveDraft = view.findViewById(R.id.rlSaveDraft);
        btnCancel = view.findViewById(R.id.btnCancel);

        rlDeleteDraft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref =
                        PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor edit = pref.edit();
                edit.putString("savedComposeContent", "");
                edit.commit();
                getActivity().finish();
            }
        });
        rlSaveDraft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref =
                        PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor edit = pref.edit();
                String savedContent = getArguments().getString("tweetContent", "");
                edit.putString("savedComposeContent", savedContent);
                edit.commit();
                getActivity().finish();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


    }
}