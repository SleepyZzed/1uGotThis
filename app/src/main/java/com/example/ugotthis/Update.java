package com.example.ugotthis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class Update extends AppCompatActivity {
    EditText title, description;
    NumberPicker priority;
    ProgressBar progbar;
    Button Update, File;
    ImageView imgview;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask mUploadTask;
    private FirebaseAuth mAuth;
    private static final int PICK_IMAGE_REQUEST = 1;
    String id;
    String ts;
    String imgurl;
    private Uri uri;
    private Note note;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);


            note = (Note) getIntent().getSerializableExtra("note");
        db = FirebaseFirestore.getInstance();
        progbar = findViewById(R.id.progressBar4);
        description = findViewById(R.id.edit_text_description_update);
        title = findViewById(R.id.edit_text_title_update);
        priority = findViewById(R.id.number_picker_priority_update);
        priority.setMinValue(0);
        priority.setMaxValue(10);
        Update = findViewById(R.id.Save_update);
        File = findViewById(R.id.chooseimage1);
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        mAuth = FirebaseAuth.getInstance();
        id = note.getUid();
        description.setText(note.getDescription());
        title.setText(note.getTitle());
        priority.setValue(note.getPriority());
        imgview = findViewById(R.id.imageshow1);
        imgurl = note.getImgurl();

        File.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });
        Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                updateNote();
            }
        });

    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            uri = data.getData();
            Picasso.get().load(uri).into(imgview);

        }
    }
    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void updateNote()
    {
        if(title.getText().toString().trim().isEmpty() || description.getText().toString().trim().isEmpty())
        {
            Toast.makeText(Update.this, "Please fill in all details", Toast.LENGTH_SHORT).show();
            return;
        }
        if(uri == null)
        {
            Toast.makeText(Update.this, "Please Reupload or select new image", Toast.LENGTH_SHORT).show();
            return;
        }

        final String Titlestr = title.getText().toString().trim();
        final String DescStr = description.getText().toString().trim();
        final int priorittyint = priority.getValue();
        ts = note.getTimeStamp();



        //db.collection("Notebook").document(note.getNoteid()).delete();
        final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                +"." + getFileExtension(uri));

        mUploadTask = fileReference.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                imgurl = uri.toString();
                                Note n = new Note(
                                        Titlestr,
                                        DescStr,
                                        priorittyint,
                                        id,
                                        imgurl,
                                        ts
                                );
                                db.collection("Notebook").document(note.getNoteid())
                                        .set(n)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(Update.this, "Task updated", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                finish();
                            }
                        });
                        Handler handler  = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progbar.setProgress(0);
                            }

                        }, 500);
                        Toast.makeText(Update.this, "Upload Successful", Toast.LENGTH_LONG).show();
                        Upload upload = new Upload((taskSnapshot.getUploadSessionUri()).toString());
                        String imguri = mDatabaseRef.push().getKey();
                        mDatabaseRef.child(imguri).setValue(upload);



                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Update.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        progbar.setProgress((int) progress);
                        if(progress == 100)
                        {
                            Toast.makeText(Update.this, "Updated", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Update.this, Main2Activity.class);

                            finish();
                        }
                    }
                });



    }

}
