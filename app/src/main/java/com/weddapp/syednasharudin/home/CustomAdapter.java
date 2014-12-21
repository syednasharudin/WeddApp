package com.weddapp.syednasharudin.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.weddapp.syednasharudin.dbase.Event;

import java.util.List;

/**
 * Created by syednasharudin on 12/21/14.
 */
public class CustomAdapter extends ArrayAdapter<Event> {

    String currentDate = "";

    public CustomAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public CustomAdapter(Context context, int resource, List<Event> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {

            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.fragment_event_list_data, null);

        }

        Event event = getItem(position);

        if (event != null) {

            TextView tvEventCode = (TextView) v.findViewById(R.id.tv_event_code);
            TextView tvEventName = (TextView) v.findViewById(R.id.tv_event_name);
            TextView tvBrideGroomName = (TextView) v.findViewById(R.id.tv_bride_groom_name);

            tvEventCode.setText(event.getEventCode());
            tvEventName.setText(event.getEventName());
            tvBrideGroomName.setText(event.getGroomName()+" & "+event.getBrideName());

        }

        return v;

    }
}
