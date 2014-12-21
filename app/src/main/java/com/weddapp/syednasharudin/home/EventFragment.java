package com.weddapp.syednasharudin.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.weddapp.syednasharudin.dbase.Event;
import com.weddapp.syednasharudin.dbase.User;
import com.weddapp.syednasharudin.dbase.UserDao;
import com.weddapp.syednasharudin.home.R;

import com.weddapp.syednasharudin.home.dummy.DummyContent;
import com.weddapp.syednasharudin.json.JSONParser;
import com.weddapp.syednasharudin.utilities.CheckNetwork;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class EventFragment extends Fragment implements AbsListView.OnItemClickListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SECTION_NUMBER = "section_number";

    // TODO: Rename and change types of parameters
    private int mParam1;

    private OnFragmentInteractionListener mListener;

    private AbsListView mListView;
    private ListAdapter mAdapter;
    private ProgressBar progressEventList;
    private FloatingActionButton btnAddEvent;

    // JSON
    private JSONParser jsonParser;
    private JSONObject jsonObject;
    private JSONArray jsonEvents = null;
    private String serverUrl;
    private String eventCode;

    // Data (Bean)
    private User user;
    private List<Event> events;

    // Check Connection
    private CheckNetwork checkNetwork;

    // TODO: Rename and change types of parameters
    public static EventFragment newInstance(int positionNumber) {
        EventFragment fragment = new EventFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, positionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_SECTION_NUMBER);
        }

        jsonParser = new JSONParser();
        serverUrl = getResources().getString(R.string.server_url);
        checkNetwork = new CheckNetwork();
        // TODO: Change Adapter to display your content
        //mAdapter = new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
        //        android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS);

        UserDao userDao = new UserDao(getActivity());
        userDao.open();
        user = userDao.getUser(0);
        userDao.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(R.id.lv_event);
        //((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
        btnAddEvent = (FloatingActionButton) view.findViewById(R.id.btn_add_event);
        progressEventList = (ProgressBar) view.findViewById(R.id.progress_event);

                // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        btnAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkNetwork.isNetworkConnected(getActivity())) {
                    DialogFragment newFragment = AddEventFragment
                            .newInstance(R.string.event_dialog_title);
                    newFragment.setTargetFragment(EventFragment.this, 0);
                    newFragment.show(getFragmentManager(), "AddEvent");
                }
            }
        });

        if(checkNetwork.isNetworkConnected(getActivity()))
            new RetrievesEvents().execute();

        return view;
    }

    public void positiveAddEventFragmentClick(String eventCode){
        Log.d("EventCode", eventCode);
        new AddWeddingEvent().execute();
        this.eventCode = eventCode;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

    class RetrievesEvents extends AsyncTask<String, String, String> {

        //private User user;
        private boolean error;
        private String message;

        protected void onPreExecute(){
            super.onPreExecute();
            progressEventList.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("", user.getEmail()));

            jsonObject = jsonParser.makeHttpRequestWithAuthorization(serverUrl+"events",
                    user.getApiKey(), "GET", params);

            Log.d("Create Response", jsonObject.toString());

            if(jsonObject != null) {

                try {

                    error = jsonObject.getBoolean("error");

                    if (error)
                        message = jsonObject.getString("message");
                    else {
                        jsonEvents = jsonObject.getJSONArray("events");
                        events = new ArrayList<Event>(jsonEvents.length());

                        for (int i = 0; i < jsonEvents.length(); i++) {
                            JSONObject jsonEvent = jsonEvents.getJSONObject(i);

                            Event event = new Event();
                            event.setEventId(i);
                            event.setEventCode(jsonEvent.getString("event_code"));
                            event.setEventName(jsonEvent.getString("event_name"));
                            event.setGroomName(jsonEvent.getString("groom"));
                            event.setBrideName(jsonEvent.getString("bride"));

                            events.add(event);
                            //Log.d("Event", jsonEvent.getString("groom")+" & "+jsonEvent.getString("bride"));
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onPostExecute(String file_url){
            progressEventList.setVisibility(View.GONE);

            if(error){
                new AlertDialog.Builder(getActivity())
                        .setTitle("WeddApp Events")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        })
                        .show();
            }else{
                CustomAdapter adapter = new CustomAdapter(getActivity(),
                        R.layout.fragment_event_list_data,
                        events);

                mListView.setAdapter(adapter);
            }
        }
    }

    class AddWeddingEvent extends AsyncTask<String, String, String> {

        //private User user;
        private boolean error;
        private String message;

        protected void onPreExecute(){
            super.onPreExecute();
            progressEventList.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("user_id", ""+user.getServerId()));
            params.add(new BasicNameValuePair("email", user.getEmail()));
            params.add(new BasicNameValuePair("event_code", eventCode));

            jsonObject = jsonParser.makeHttpRequestWithAuthorization(serverUrl+"add_event",
                    user.getApiKey(), "POST", params);

            Log.d("Create Response", jsonObject.toString());

            if(jsonObject != null) {

                try {

                    error = jsonObject.getBoolean("error");

                    if (error)
                        message = jsonObject.getString("message");
                    else {
                        jsonEvents = jsonObject.getJSONArray("events");

                        if(jsonEvents != null) {
                            events = new ArrayList<Event>(jsonEvents.length());

                            for (int i = 0; i < jsonEvents.length(); i++) {
                                JSONObject jsonEvent = jsonEvents.getJSONObject(i);

                                Event event = new Event();
                                event.setEventId(i);
                                event.setEventCode(jsonEvent.getString("event_code"));
                                event.setEventName(jsonEvent.getString("event_name"));
                                event.setGroomName(jsonEvent.getString("groom"));
                                event.setBrideName(jsonEvent.getString("bride"));

                                events.add(event);
                                //Log.d("Event", jsonEvent.getString("groom")+" & "+jsonEvent.getString("bride"));
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onPostExecute(String file_url){
            progressEventList.setVisibility(View.GONE);

            if(error){
                new AlertDialog.Builder(getActivity())
                        .setTitle("WeddApp Events")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        })
                        .show();
            }else{

                if(jsonEvents != null) {
                    CustomAdapter adapter = new CustomAdapter(getActivity(),
                            R.layout.fragment_event_list_data,
                            events);

                    mListView.setAdapter(adapter);
                }
            }
        }

    }

}
