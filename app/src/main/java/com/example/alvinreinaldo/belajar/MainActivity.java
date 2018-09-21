package com.example.alvinreinaldo.belajar;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String ARTIST_NAME = "artistname";
    public static final String ARTIST_ID = "artistid";
    //view object
    EditText editTextName;
    Button buttonAdd;
    Spinner spinnerGenres;
    ListView listViewArtist;

    //our database reference object
    DatabaseReference databaseArtists;

    //a list to store all the artist from firebase database
    List<Artist> artistList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getting the reference of artists node
        databaseArtists = FirebaseDatabase.getInstance().getReference("artists");

        editTextName = (EditText) findViewById(R.id.editTextName);
        buttonAdd = (Button) findViewById(R.id.buttonAddArtist);
        spinnerGenres = (Spinner) findViewById(R.id.spinnerGenres);

        listViewArtist = (ListView) findViewById(R.id.listViewArtist);

        //list to store artists
        artistList = new ArrayList<>();
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //calling the method addArtist()
                //the method is defined below
                //this method is actually performing the write operation
                addArtist();
            }
        });
        listViewArtist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Artist artist = artistList.get(i);

                Intent intent = new Intent(getApplicationContext(),AddTrackActivity.class);

                intent.putExtra(ARTIST_NAME,artist.getArtistName());
                intent.putExtra(ARTIST_ID,artist.getArtistId());
                startActivity(intent);
            }
        });
        listViewArtist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = artistList.get(position);

                showUpdateDialog(artist.getArtistId(),artist.getArtistName(),artist.getArtistGenre());

                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseArtists.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                artistList.clear();
                for(DataSnapshot artistSnapshot : dataSnapshot.getChildren()){
                    Artist artist = artistSnapshot.getValue(Artist.class);
                    artistList.add(artist);
                }
                ArtistList adapter = new ArtistList(MainActivity.this,artistList);
                listViewArtist.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private int getIndex(Spinner spinner, String myString){

        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).equals(myString)){
                index = i;
            }
        }
        return index;
    }
    private void showUpdateDialog(final String artistId, String artistName,String artistGenre){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog,null);
        dialogBuilder.setView(dialogView);
        final TextView editTextName = (TextView) dialogView.findViewById(R.id.editTextNameUpdate);
//        final EditText textViewName = (EditText) dialogView.findViewById(R.id.textViewNameUpdate);
        final Button buttonUpdate = (Button) dialogView.findViewById(R.id.buttonUpdate);
        final Spinner spinnerGenres = (Spinner) dialogView.findViewById(R.id.spinnerGenresUpdate);
        final Button buttonDelete = (Button) dialogView.findViewById(R.id.buttonDelete);
        spinnerGenres.setSelection(getIndex(spinnerGenres,artistGenre));
        dialogBuilder.setTitle("Updating Artist "+artistName+" "+artistId);

        final AlertDialog alertDialog =dialogBuilder.create();
        alertDialog.show();
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString().trim();
                String genre = spinnerGenres.getSelectedItem().toString();
                if(TextUtils.isEmpty(name)){
                    editTextName.setError("Name required");
                    return;
                }
                updateArtist(artistId,name,genre);
                alertDialog.dismiss();
            }
        });
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteArtist(artistId);
                alertDialog.dismiss();
            }
        });
    }

    private boolean updateArtist(String id, String name,String genre){
        DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("artists").child(id);
        Artist artist = new Artist(id,name,genre);
        databaseReference2.setValue(artist);
        Toast.makeText(this,"Artist Updated Successfully",Toast.LENGTH_LONG).show();
        return true;
    }

    private void deleteArtist(String id){
        DatabaseReference drArtist = FirebaseDatabase.getInstance().getReference("artists").child(id);
        DatabaseReference drTracks = FirebaseDatabase.getInstance().getReference("tracks").child(id);

        drArtist.removeValue();
        drTracks.removeValue();
        Toast.makeText(this,"Artist Deleted",Toast.LENGTH_LONG).show();
    }

    /*
     * This method is saving a new artist to the
     * Firebase Realtime Database
     * */
    private void addArtist(){
        //getting the values to save
        String name = editTextName.getText().toString().trim();
        String genre = spinnerGenres.getSelectedItem().toString();
        //checking if the value is provided
        if(TextUtils.isEmpty(name)){
            //if the value is not given displaying a toast
            Toast.makeText(this,"You Should Enter a name",Toast.LENGTH_LONG).show();
        }
        else{
            //getting a unique id using push().getKey() method
            //it will create a unique id and we will use it as the Primary Key for our Artist
            String id = databaseArtists.push().getKey();
            //creating an Artist Object
            Artist artist = new Artist(id,name,genre);
            //Saving the Artist
            databaseArtists.child(id).setValue(artist);
            //setting edittext to blank again
            editTextName.setText("");
            //displaying a success toast
            Toast.makeText(this,"Artist added",Toast.LENGTH_LONG).show();
        }
    }
}
