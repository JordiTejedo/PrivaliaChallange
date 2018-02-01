package com.example.jordi.privaliachallange.others;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jordi.privaliachallange.R;
import com.example.jordi.privaliachallange.models.Results;
import java.util.List;

/**
 * Created by Jordi on 31/01/2018.
 */

public class ListViewAdapter extends ArrayAdapter<Results> {
    private List<Results> results;
    Context context;

    private static class ViewHolder {
        TextView title, year, overview;
        ImageView poster;
    }

    public ListViewAdapter(List<Results> results, Context context)
    {
        super(context, R.layout.list_item, results);
        this.results = results;
        this.context = context;
    }

    public void updateData(List<Results> results) {
        this.results.addAll(results);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Results result = results.get(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View view;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            viewHolder.title = (TextView) convertView.findViewById(R.id.title_tv);
            viewHolder.year = (TextView) convertView.findViewById(R.id.year_tv);
            viewHolder.overview = (TextView) convertView.findViewById(R.id.overview_tv);
            viewHolder.poster = (ImageView) convertView.findViewById(R.id.poster_iv);

            view = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            view = convertView;
        }

        viewHolder.title.setText(result.getTitle());
        String year = "("+result.getRelease_date().split("-")[0]+")";
        viewHolder.year.setText(year);
        if (result.getOverview().isEmpty()) {
            viewHolder.overview.setText(context.getString(R.string.DEFAULT_OVERVIEW));
        } else {
            viewHolder.overview.setText(result.getOverview());
        }
        viewHolder.poster.setImageBitmap(result.getPoster_bitmap());
        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
