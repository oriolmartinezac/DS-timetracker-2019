package com.example.joans.timetracker;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
    ALL CODED BY TEAM
**/
      
public class IntervalAdapter extends RecyclerView.Adapter<IntervalAdapter.ViewHolder> {

    private List<DadesInterval> listIntervals;
    private SimpleDateFormat formatTimeInterval =
            new SimpleDateFormat("dd-MM-yyyy hh:mm", Locale.US);

    public IntervalAdapter() {
        this.listIntervals = new ArrayList<>();
        //HH: hours MM: Minutes SS: seconds
        TimeZone timezone = TimeZone.getTimeZone("GMT+0");
        formatTimeInterval.setTimeZone(timezone); //set time zone to GMT+0
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.interval_adapter_view, parent, false);
        return new IntervalAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        DadesInterval infoInterval = listIntervals.get(position);

        viewHolder.startTime.setText(formatTimeInterval.format(infoInterval.getDataInicial()));
        viewHolder.endTime.setText(formatTimeInterval.format(infoInterval.getDataFinal()));
        viewHolder.timeElapsed.setText(infoInterval.toString());


    }

    @Override
    public int getItemCount() {
        return listIntervals == null ? 0:  listIntervals.size();
    }

    public void add(ArrayList<DadesInterval> intervals){
        this.listIntervals = intervals;
    }

    public void clear() {
        listIntervals.clear();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView startTime;
        TextView endTime;
        TextView timeElapsed;

        public ViewHolder(View itemView) {
            super(itemView);
            startTime = (TextView) itemView.findViewById(R.id.startTime);
            endTime = (TextView) itemView.findViewById(R.id.endTime);
            timeElapsed = (TextView) itemView.findViewById(R.id.timeElapsed);
        }
    }
}
