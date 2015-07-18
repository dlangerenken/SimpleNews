package de.dala.simplenews.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.rometools.rome.feed.WireFeed;
import com.rometools.rome.feed.opml.Opml;
import com.rometools.rome.io.WireFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.utilities.OpmlConverter;
import de.dala.simplenews.utilities.Utilities;

public class OpmlImportFragment extends BaseFragment {

    private OnFeedsLoaded parent;
    private Button importButton;
    private ProgressBar importProgres;
    private ImportAsyncTask task;

    public OpmlImportFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (task != null) {
            task.cancel(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (task != null) {
            task.cancel(true);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            parent = (OnFeedsLoaded) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    public static OpmlImportFragment newInstance() {
        OpmlImportFragment fragment = new OpmlImportFragment();
        Bundle b = new Bundle();
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.opml_import_view, container, false);

        final EditText opmlContentEditText = (EditText) rootView.findViewById(R.id.opmlContentEditText);

        importButton = (Button) rootView.findViewById(R.id.button);
        importProgres = (ProgressBar) rootView.findViewById(R.id.import_progress);

        opmlContentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                importButton.setEnabled(s.length() > 0);
            }
        });

        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (opmlContentEditText.getText() != null && !Utilities.equals(opmlContentEditText.getText().toString(), "")) {
                    //content added
                    task = new ImportAsyncTask();
                    task.execute(opmlContentEditText.getText().toString());
                }
            }
        });

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
        return rootView;
    }

    private class ImportAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            enableProgressView(true);
        }

        @Override
        protected String doInBackground(String[] params) {
            if (params == null || params.length == 0 || params[0] == null || params[0].equals("")) {
                return null;
            }
            String enteredText = params[0];
            boolean loaded;

            List<Feed> feeds = new ArrayList<>();
            WireFeedInput input = new WireFeedInput();
            try {
                WireFeed feed = input.build(new XmlReader(new ByteArrayInputStream(enteredText.getBytes())));
                if (feed != null && feed instanceof Opml) {
                    feeds = OpmlConverter.convertOpmlListToFeedList((Opml) feed);
                }
                loaded = feeds != null && feeds.size() > 0;
            } catch (Exception e) {
                e.printStackTrace();
                loaded = false;
            }

            if (!loaded) {
                // try to load by url
                try {
                    if (!enteredText.startsWith("http://")) {
                        enteredText = "http://" + enteredText;
                    }
                    WireFeed feed = input.build(new XmlReader(new URL(enteredText), getActivity()));
                    if (feed != null && feed instanceof Opml) {
                        feeds = OpmlConverter.convertOpmlListToFeedList((Opml) feed);
                    }
                    loaded = feeds != null && feeds.size() > 0;
                } catch (Exception e) {
                    e.printStackTrace();
                    loaded = false;
                }
            }

            if (loaded) {
                if (parent != null) {
                    parent.assignFeeds(feeds);
                    return "success";
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) {
                Toast.makeText(getActivity(), getActivity().getString(R.string.not_valid_url_nor_opml_file), Toast.LENGTH_SHORT).show();
            }
            enableProgressView(false);
        }
    }


    private void enableProgressView(boolean progress) {
        importButton.setVisibility(progress ? View.INVISIBLE : View.VISIBLE);
        importProgres.setVisibility(progress ? View.VISIBLE : View.INVISIBLE);
    }

    public interface OnFeedsLoaded {
        void assignFeeds(List<Feed> feeds);
    }
}
