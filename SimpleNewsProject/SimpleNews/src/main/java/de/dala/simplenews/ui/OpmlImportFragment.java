package de.dala.simplenews.ui;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.rometools.rome.feed.WireFeed;
import com.rometools.rome.feed.opml.Opml;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedInput;
import com.rometools.rome.io.XmlReader;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.utilities.BaseNavigation;
import de.dala.simplenews.utilities.OpmlConverter;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by Daniel on 01.08.2014.
 */
public class  OpmlImportFragment extends BaseFragment implements BaseNavigation {

    private OnFeedsLoaded parent;
    private Button importButton;
    private ProgressBar importProgres;
    private ImportAsyncTask task;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public OpmlImportFragment() {
    }

    @Override
    public void onPause() {
        super.onPause();
        if (task != null){
            task.cancel(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (task != null){
            task.cancel(true);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment newsFragment = getParentFragment();
        if (newsFragment != null && newsFragment instanceof OnFeedsLoaded){
            this.parent = (OnFeedsLoaded) newsFragment;
        }else{
            throw new ClassCastException("ParentFragment is not of type OnFeedsLoaded");
        }
    }

    public static OpmlImportFragment newInstance() {
        OpmlImportFragment fragment = new OpmlImportFragment();
        Bundle b = new Bundle();
        fragment.setArguments(b);
        return fragment;
    }
    private String readTxt()
    {
        InputStream raw = getResources().openRawResource(R.raw.opml_sample);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try
        {
            int i = raw.read();
            while (i != -1)
            {
                byteArrayOutputStream.write(i);
                i = raw.read();
            }
            raw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toString();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.opml_import_view, container, false);

        final EditText opmlContentEditText = (EditText) rootView.findViewById(R.id.opmlContentEditText);
        // opmlContentEditText.setText(readTxt());

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
                if (opmlContentEditText.getText() != null && opmlContentEditText.getText().toString() != ""){
                    //content added
                    task = new ImportAsyncTask();
                    task.execute(opmlContentEditText.getText().toString());
                }
            }
        });


        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle("Import Opml");
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
            if (params == null ||params.length == 0 ||params[0] == null || params[0].equals("")){
                return null;
            }
            String enteredText = params[0];
            boolean loaded;

            List<Feed> feeds = new ArrayList<Feed>();
            WireFeedInput input = new WireFeedInput();
            try {
                WireFeed feed = input.build(new XmlReader(new ByteArrayInputStream(enteredText.getBytes())));
                if (feed != null && feed instanceof Opml) {
                    feeds = OpmlConverter.convertOpmlListToFeedList((Opml)feed);
                }
                loaded = feeds != null && feeds.size() > 0;
            } catch (FeedException e) {
                e.printStackTrace();
                loaded = false;
            } catch (IOException e) {
                e.printStackTrace();
                loaded = false;
            } catch (ClassCastException e){
                e.printStackTrace();
                loaded = false;
            } catch (IllegalArgumentException e){
                e.printStackTrace();
                loaded = false;
            }

            if (!loaded) {
                // try to load by url
                try {
                    if (!enteredText.startsWith("http://")){
                        enteredText = "http://" + enteredText;
                    }
                    WireFeed feed = input.build(new XmlReader(new URL(enteredText)));
                    if (feed != null && feed instanceof Opml) {
                        feeds = OpmlConverter.convertOpmlListToFeedList((Opml)feed);
                    }
                    loaded = feeds != null && feeds.size() > 0;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    loaded = false;
                } catch (FeedException e) {
                    e.printStackTrace();
                    loaded = false;
                } catch (IOException e) {
                    e.printStackTrace();
                    loaded = false;
                } catch (IllegalArgumentException e){
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
            if (s == null){
                Crouton.makeText(getActivity(), getActivity().getString(R.string.not_valid_url_nor_opml_file), Style.ALERT).show();
            }
            enableProgressView(false);
        }
    }


    private void enableProgressView(boolean b) {
        importButton.setVisibility(b ? View.INVISIBLE : View.VISIBLE);
        importProgres.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public String getTitle() {
        return "OpmlImportFragment";
    }

    @Override
    public int getNavigationDrawerId() {
        return NavigationDrawerFragment.IMPORT;
    }

    public interface OnFeedsLoaded {
        void assignFeeds(List<Feed> feeds);
    }
}
