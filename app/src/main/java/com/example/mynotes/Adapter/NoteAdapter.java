package com.example.mynotes.Adapter;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynotes.Entities.Note;
import com.example.mynotes.Listener.NotesListener;
import com.example.mynotes.R;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder>  {
   private List<Note> notes;
   private NotesListener notesListener;
   private Timer timer;
   private List<Note> notesSource;

    public NoteAdapter(List<Note> notes,NotesListener notesListener) {
        this.notes = notes;
        this.notesListener = notesListener;
        notesSource = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_container_notes,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, final int position) {
        holder.setNote(notes.get(position));
        holder.layoutnote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notesListener.onNoteClicked(notes.get(position),position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }



    static class NoteViewHolder extends RecyclerView.ViewHolder {
    TextView textTitle,textSubtitle,textDatatime;
    LinearLayout layoutnote;
    RoundedImageView imageNote;
        NoteViewHolder(@NonNull View itemview){
            super(itemview);
            textTitle = itemview.findViewById(R.id.textTitle);
            textSubtitle = itemview.findViewById(R.id.textSubTitle);
            textDatatime = itemview.findViewById(R.id.textDatetime);
            layoutnote = itemview.findViewById(R.id.layoutNote);
            imageNote = itemview.findViewById(R.id.imageNote);
        }
        void setNote(Note note){
            textTitle.setText(note.getTitle());
            if(note.getSubtitle().trim().isEmpty()){
                textSubtitle.setVisibility(View.GONE);
            }else {
                textSubtitle.setText(note.getSubtitle());
            }
        textDatatime.setText(note.getDataTime());
            GradientDrawable gradientDrawable = (GradientDrawable) layoutnote.getBackground();
            if(note.getColor() == null){
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }else{
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            }
            if (note.getImagePath() != null){
                imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
            }else{
                imageNote.setVisibility(View.GONE);
            }
        }
    }
    public void Search(final String searchKeyWords){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyWords.trim().isEmpty()){
                     notes = notesSource;
                }else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : notesSource){
                        if (note.getTitle().toLowerCase().contains(searchKeyWords.toLowerCase())
                        ||note.getSubtitle().toLowerCase().contains(searchKeyWords.toLowerCase())
                        ||note.getNoteText().toLowerCase().contains(searchKeyWords.toLowerCase())){
                       temp.add(note);
                        }
                    }
                    notes = temp;
                }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
            }
        },10000);
    }
    public void CancelTimer(){
        if (timer!=null){
            timer.cancel();
        }
    }
}
