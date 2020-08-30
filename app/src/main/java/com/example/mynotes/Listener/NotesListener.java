package com.example.mynotes.Listener;

import com.example.mynotes.Entities.Note;

public interface NotesListener {
    void onNoteClicked(Note note,int position);
}
