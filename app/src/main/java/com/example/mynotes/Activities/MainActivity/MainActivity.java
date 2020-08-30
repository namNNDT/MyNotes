package com.example.mynotes.Activities.MainActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mynotes.Adapter.NoteAdapter;
import com.example.mynotes.Entities.Note;
import com.example.mynotes.Listener.NotesListener;
import com.example.mynotes.R;
import com.example.mynotes.database.NotesDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesListener {
    private List<Note> noteList;
    private NoteAdapter noteAdapter;
    private int noteClickedPosition = -1;
    private RecyclerView recyclerView;
    public static final int REQUEST_CODE_ADD = 1;
    public static final int REQUEST_CODE_UPDATE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;
    private static final int REQUEST_CODE_SELECT_IMAGE = 4;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 5;
    private AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.NoteRecyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        ImageView imageaddMain = findViewById(R.id.imageAddNoteMain);
        imageaddMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this,CreateNotesActivity.class),REQUEST_CODE_ADD);
            }
        });
        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList,this);
        recyclerView.setAdapter(noteAdapter);
        GetNotes(REQUEST_CODE_SHOW_NOTES,false);
        EditText inpuSearch = findViewById(R.id.inputSearch);
        inpuSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(noteList.size()!=0){
                    noteAdapter.Search(s.toString());
                }
            }
        });
        findViewById(R.id.imageAddnote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this,CreateNotesActivity.class),REQUEST_CODE_ADD);
            }
        });
        findViewById(R.id.imageAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION
                    );
                } else {
                    selectImage();
                }
            }
        });
        findViewById(R.id.imageAddWeblink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUrlDialog();
            }
        });
    }
    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else{
                Toast.makeText(this, "Permission Denied!!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private String getPathFromUri(Uri uri){
        String filePath;
        Cursor cursor = getContentResolver()
                .query(uri , null ,null,null,null);
        if(cursor == null){
            filePath = uri.getPath();
        }else{
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }
    private void GetNotes(final int requestCode,final boolean isNoteDeleted){
        class GetNoteTask extends AsyncTask<Void,Void, List<Note>>{

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase
                        .getNotesDatabase(getApplicationContext())
                        .noteDao()
                        .getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
           if(requestCode == REQUEST_CODE_SHOW_NOTES){
               noteList.addAll(notes);
                noteAdapter.notifyDataSetChanged();
           }else if (requestCode == REQUEST_CODE_ADD) {
               noteList.add(0, notes.get(0));
               noteAdapter.notifyItemInserted(0);
               recyclerView.smoothScrollToPosition(0);
           }else if (requestCode == REQUEST_CODE_UPDATE){
               noteList.remove(noteClickedPosition);
               if (isNoteDeleted){
                   noteAdapter.notifyItemRemoved(noteClickedPosition);
               }else {
                   noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                   noteAdapter.notifyItemChanged(noteClickedPosition);
               }
           }
           }
        }new GetNoteTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD && resultCode == RESULT_OK){
            GetNotes(REQUEST_CODE_ADD,false);
        }else if(requestCode == REQUEST_CODE_UPDATE && resultCode == RESULT_OK){
            if(data!=null){
                GetNotes(REQUEST_CODE_UPDATE,data.getBooleanExtra("isNoteDelete",false));
            }else if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
                if(data!=null){
                    Uri selectedImageUri = data.getData();
                    if (selectedImageUri!=null){
                        try {
                            String selectedImagePath = getPathFromUri(selectedImageUri);
                            Intent intent = new Intent(MainActivity.this,CreateNotesActivity.class);
                            intent.putExtra("isFromQuickAction",true);
                            intent.putExtra("quickActionType","image");
                             intent.putExtra("imagePath",selectedImagePath);
                             startActivityForResult(intent,REQUEST_CODE_ADD);
                        }catch (Exception e){
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onNoteClicked(Note note, int position) {
    noteClickedPosition = position;
    Intent intent = new Intent(getApplicationContext(),CreateNotesActivity.class);
    intent.putExtra("isViewOrUpdate",true);
    intent.putExtra("note",note);
    startActivityForResult(intent,REQUEST_CODE_UPDATE);
    }
    private void showAddUrlDialog(){
        if(alertDialog == null){
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(false);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,(ViewGroup)findViewById(R.id.layoutUrlContainer)
            );
            builder.setView(view);
            alertDialog = builder.create();
            if(alertDialog.getWindow() == null){
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final EditText inputUrl = view.findViewById(R.id.inputUrl);
            inputUrl.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(inputUrl.getText().toString().trim().isEmpty()){
                        Toast.makeText(MainActivity.this, "Enter Url", Toast.LENGTH_SHORT).show();
                    }else if (!Patterns.WEB_URL.matcher(inputUrl.getText().toString()).matches()){
                        Toast.makeText(MainActivity.this, "Enter Valid Url", Toast.LENGTH_SHORT).show();
                    }else {
                        Intent intent = new Intent(getApplicationContext(),CreateNotesActivity.class);
                        intent.putExtra("isFromQuickAction",true);
                        intent.putExtra("QuickActionType","URL");
                        intent.putExtra("URL",inputUrl.getText().toString());
                        startActivityForResult(intent,REQUEST_CODE_ADD);
                        alertDialog.dismiss();
                    }
                }
            });
            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
        }
        alertDialog.show();
    }
}
