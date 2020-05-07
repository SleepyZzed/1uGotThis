package com.example.ugotthis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class NewNoteActivity extends AppCompatActivity {


    private EditText editTextTitle, editTextDescription;
    private NumberPicker numberPickerPriority;
    private FirebaseAuth mAuth;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Button btnChoose, btnUpload;
    private ImageView imgview;
    private ProgressBar progbar;
    private Uri uri;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask mUploadTask;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        editTextDescription = findViewById(R.id.edit_text_description);
        editTextTitle = findViewById(R.id.edit_text_title);
        numberPickerPriority = findViewById(R.id.number_picker_priority);
        mAuth = FirebaseAuth.getInstance();
        numberPickerPriority.setMinValue(1);
        numberPickerPriority.setMaxValue(10);
        btnChoose = findViewById(R.id.chooseimage);
        imgview = findViewById(R.id.imageshow);
        progbar = findViewById(R.id.progressBar3);
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");



        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });


    }



    private void openFileChooser()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
        && data != null && data.getData() != null){
            uri = data.getData();

            Picasso.get().load(uri).into(imgview);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.notemenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                if(mUploadTask != null && mUploadTask.isInProgress())
                {
                    Toast.makeText(this, "Upload in progress", Toast.LENGTH_SHORT).show();
                }


                else {
                    saveNote();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }
    private String getFileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }


    private void saveNote(){

        final String title = editTextTitle.getText().toString();
        final String description = editTextDescription.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'-'HH:mm:ss");
        final String ts = sdf.format(new Date());
        final int priority = numberPickerPriority.getValue();

        if(title.trim().isEmpty() || description.trim().isEmpty() || uri == null)
        {
            Toast.makeText(this, "Please insert a task and description", Toast.LENGTH_SHORT).show();
            return;
        }

        final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                +"." + getFileExtension(uri));

        mUploadTask = fileReference.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                String imageUrl = uri.toString();
                                CollectionReference notebookRef = FirebaseFirestore.getInstance()
                                        .collection("Notebook");
                                notebookRef.add(new Note(title, description, priority, mAuth.getUid(), imageUrl, ts));
                            }
                        });
                        Handler handler  = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progbar.setProgress(0);
                            }

                        }, 500);
                        Toast.makeText(NewNoteActivity.this, "Upload Successful", Toast.LENGTH_LONG).show();
                        Upload upload = new Upload((taskSnapshot.getUploadSessionUri()).toString());
                        String imguri = mDatabaseRef.push().getKey();
                        mDatabaseRef.child(imguri).setValue(upload);



                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewNoteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progbar.setProgress((int) progress);
                if(progress == 100)
                {
                    Toast.makeText(NewNoteActivity.this, "Task added", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(NewNoteActivity.this, Main2Activity.class);

                    finish();
                }
            }
        });




       /* CollectionReference notebookRef = FirebaseFirestore.getInstance()
                .collection("Notebook");
        notebookRef.add(new Note(title, description, priority, mAuth.getUid()));
        Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show();
        finish();*/
    }
}
