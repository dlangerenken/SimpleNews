package de.dala.simplenews.ui;

import android.app.Activity;
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

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.network.NetworkCommunication;
import de.dala.simplenews.parser.OpmlReader;
import de.dala.simplenews.utilities.BaseNavigation;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by Daniel on 01.08.2014.
 */
public class  OpmlImportFragment extends Fragment implements BaseNavigation {

    private OnFeedsLoaded parent;
    private Button importButton;
    private ProgressBar importProgres;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public OpmlImportFragment() {
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
                    importString(opmlContentEditText.getText().toString());
                }
            }
        });


        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle("Import Opml");
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return rootView;
    }

    private void importString(String enteredText) {
        enableProgressView(true);
        try {
            List<Feed> feeds = OpmlReader.importFile(new StringReader(enteredText));
            if (parent != null && feeds.size() > 0) {
                parent.assignFeeds(feeds);
                enableProgressView(false);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        NetworkCommunication.loadOpmlFeeds(enteredText, new Response.Listener<String>() {
            @Override
            public void onResponse(String responseString) {
                try {
                    List<Feed> feeds = OpmlReader.importFile(new StringReader(responseString));
                    if (parent != null && feeds.size() > 0) {
                        parent.assignFeeds(feeds);
                    }else{
                        Crouton.makeText(getActivity(), getActivity().getString(R.string.not_valid_url_nor_opml_file), Style.ALERT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
                enableProgressView(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Crouton.makeText(getActivity(), getActivity().getString(R.string.not_valid_url_nor_opml_file), Style.ALERT).show();
                enableProgressView(false);
            }
        });
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
