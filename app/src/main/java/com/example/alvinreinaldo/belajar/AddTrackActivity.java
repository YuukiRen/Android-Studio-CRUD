package com.example.alvinreinaldo.belajar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddTrackActivity extends AppCompatActivity {
    TextView textViewArtist;
    EditText editTextName;
    SeekBar seekBarRating;
    ListView listViewTracks;
    Button buttonAddTrack;

    DatabaseReference databaseReference;

    List<Track> trackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_track);
        textViewArtist = (TextView) findViewById(R.id.textViewArtist);
        editTextName = (EditText) findViewById(R.id.editTextName);
        seekBarRating = (SeekBar) findViewById(R.id.seekBarRating);
        buttonAddTrack = (Button) findViewById(R.id.buttonAddTrack);
        listViewTracks = (ListView) findViewById(R.id.listViewTracks);

        trackList = new ArrayList<>();

        Intent intent = getIntent();
        String id = intent.getStringExtra(MainActivity.ARTIST_ID);
        String name = intent.getStringExtra(MainActivity.ARTIST_NAME);
        textViewArtist.setText(name);
        databaseReference = FirebaseDatabase.getInstance().getReference("tracks").child(id);
        buttonAddTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTrack();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                trackList.clear();
                for(DataSnapshot trackSnapshot : dataSnapshot.getChildren()){
                    Track track = trackSnapshot.getValue(Track.class);
                    trackList.add(track);

                }
                TrackList trackListAdapter = new TrackList(AddTrackActivity.this,trackList);
                listViewTracks.setAdapter(trackListAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        })
    }

    private void saveTrack(){
        String name = editTextName.getText().toString().trim();
        int rating = seekBarRating.getProgress();
        if(!TextUtils.isEmpty(name)){
            String id = databaseReference.push().getKey();
            Track track = new Track(id, name,rating);

            databaseReference.child(id).setValue(track);
            Toast.makeText(this,"Track Saved Succesfully",Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this,"track name should not be empty",Toast.LENGTH_LONG).show();
        }
    }
}