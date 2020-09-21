package com.example.joans.timetracker;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
ALL CODED BY TEAM
**/

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private SimpleDateFormat formatTimeElapsed =
            new SimpleDateFormat("HH:mm:ss", Locale.US);

    private List<DadesActivitat> listItems;
    private Context context;
    private PosActivityListener posActivityListener;


    //public ListAdapter(ArrayList<DadesActivitat> items, Context context) {
    public ListAdapter() {
        this.listItems = new ArrayList<>();
        //HH: hours MM: Minutes SS: seconds
        TimeZone timezone = TimeZone.getTimeZone("GMT+0");
        formatTimeElapsed.setTimeZone(timezone); //set time zone to GMT+0
    }

    public void clear() {
        listItems.clear();
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_adapter_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        DadesActivitat infoActivities = listItems.get(position);

        viewHolder.textViewName.setText(infoActivities.getNom());

        viewHolder.textViewTimeElapsed.setText(infoActivities.toString());

        if(infoActivities.isTasca()) {
            viewHolder.imageButtonActivity.setImageDrawable(ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.ic_task));
            viewHolder.buttonPlayStop.setVisibility(View.VISIBLE);
            if(infoActivities.isCronometreEngegat()) {
                viewHolder.buttonPlayStop.setImageDrawable(ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.ic_pause));
            } else {
                viewHolder.buttonPlayStop.setImageDrawable(ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.ic_play));
            }
        } else {
            viewHolder.imageButtonActivity.setImageDrawable(ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.ic_project));
            viewHolder.buttonPlayStop.setVisibility(View.INVISIBLE);
        }

        viewHolder.buttonPlayStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                posActivityListener.playStopClicked(position);
            }
        });

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                posActivityListener.activityClicked(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listItems == null ? 0:  listItems.size();
    }

    public void add(ArrayList<DadesActivitat> items){
        this.listItems = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewName;
        public TextView textViewTimeElapsed;
        public ImageButton imageButtonActivity;
        public ImageButton buttonPlayStop;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewName = (TextView) itemView.findViewById(R.id.infoName);
            textViewTimeElapsed = (TextView) itemView.findViewById(R.id.timeElapsedCount);
            textViewName = (TextView) itemView.findViewById(R.id.infoName);
            buttonPlayStop = (ImageButton) itemView.findViewById(R.id.playStopButton);
            imageButtonActivity = (ImageButton) itemView.findViewById(R.id.activity);
        }
    }

    public void setPosActivityListener(PosActivityListener posActivityListener) {
        this.posActivityListener = posActivityListener;
    }

    public interface PosActivityListener {
        void activityClicked(int position);
        void playStopClicked(int position);
    }
}
