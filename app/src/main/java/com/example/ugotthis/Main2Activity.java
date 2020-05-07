package com.example.ugotthis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;

public class Main2Activity extends AppCompatActivity implements SensorEventListener {

    TextView txt;
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 3000;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Main2Activity mShakeDetector;
    private OnShakeListener mListener;
    private long mShakeTimestamp;
    private int mShakeCount;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("Notebook");
    private NoteAdapter adapter;
    private FirebaseAuth mAuth;
    private ListenerRegistration listenerRegistration;
    boolean check;
    int passedposition;

    public void setOnShakeListener(OnShakeListener listener) {
        this.mListener = listener;
    }

    public interface OnShakeListener {
        public void onShake(int count);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (mListener != null) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            // gForce will be close to 1 when there is no movement.
            float gForce = (float)Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                final long now = System.currentTimeMillis();
                // ignore shake events too close to each other (500ms)
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    return;
                }

                // reset the shake count after 3 seconds of no shakes
                if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                    mShakeCount = 0;
                }

                mShakeTimestamp = now;
                mShakeCount++;

                mListener.onShake(mShakeCount);

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent.getExtras() == null) {
            // Do first time stuff here
        } else {
            // Do stuff with intent data here
            Bundle b = getIntent().getExtras();

            passedposition = b.getInt("passedpos");
            check = b.getBoolean("isTrue");

        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        txt = findViewById(R.id.txtView);
        FloatingActionButton fab = findViewById(R.id.fab);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentuser = currentUser.getEmail();
        String currentuserID = currentUser.getUid();
        txt.setText("Email: " + currentuser + "\n " + "User ID: " + currentuserID);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new Main2Activity();

        mShakeDetector.setOnShakeListener(new OnShakeListener() {
            @Override
            public void onShake(int count) {
                /*
                 * The following method, "handleShakeEvent(count):" is a stub //
                 * method you would use to setup whatever you want done once the
                 * device has been shook.
                 */

                handleShakeEvent(count);


            }

            private void handleShakeEvent(int count) {

                Intent intent = new Intent(Main2Activity.this, NewNoteActivity.class);
                    //adapter.deleteItem(1);
                    //adapter.deleteItem(2);
                startActivity(intent);
                Toast.makeText(Main2Activity.this, "You have shaken the device to add activity", Toast.LENGTH_SHORT).show();
            }


        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(Main2Activity.this, NewNoteActivity.class));

            }
        });

        setUpRecyclerView();


    }

    private void setUpRecyclerView() {
        Query query = notebookRef
                .orderBy("priority", Query.Direction.DESCENDING)
                .whereEqualTo("uid", mAuth.getInstance().getCurrentUser().getUid());
                //.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                   // @Override
                   // public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                  //  }
               // });

        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();
        adapter = new NoteAdapter(options);
        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                if (direction == ItemTouchHelper.RIGHT) {
                    Note note = adapter.getSnapshots().getSnapshot(viewHolder.getAdapterPosition()).toObject(Note.class);

                    String Title = note.getTitle();
                    String Desc = note.getDescription();
                    int Priority = note.getPriority();
                    String imageUrl = note.getImgurl();
                    String timestamp = note.getTimeStamp();


                    CollectionReference notebookRef = FirebaseFirestore.getInstance()
                            .collection("NotebookCompleted");
                    notebookRef.add(new Note(Title, Desc, Priority, mAuth.getUid(), imageUrl, timestamp));
                    adapter.deleteItem(viewHolder.getAdapterPosition());

                }
                if (direction == ItemTouchHelper.LEFT) {

                    adapter.deleteItem(viewHolder.getAdapterPosition());


                }





            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(Main2Activity.this, R.color.colorRed))
                        .addSwipeLeftActionIcon(R.drawable.ic_delete_sweep_black_24dp)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(Main2Activity.this, R.color.colorGreen))
                        .addSwipeRightActionIcon(R.drawable.ic_playlist_add_check_black_24dp)

                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final DocumentSnapshot documentSnapshot, final int position) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(Main2Activity.this);
                builder1.setMessage("Please select an option");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Send SMS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                Note note = documentSnapshot.toObject(Note.class);
                                String description = note.getDescription();
                                String title = note.getTitle();
                                int priority = note.getPriority();
                                String imgurl = note.getImgurl();
                                String uid = note.getUid();
                                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                                smsIntent.setType("vnd.android-dir/mms-sms");
                                smsIntent.putExtra("address", "12125551212");
                                smsIntent.putExtra("sms_body","Title: " + title + "\n Desc: " + description + "\n Priority: " + priority + "\n Image url: "+ imgurl);
                                startActivity(smsIntent);

                            }
                        });

                builder1.setNegativeButton(
                        "Delete Task",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();


                                adapter.deleteItem(position);


                            }
                        });
                builder1.setNeutralButton("Edit Task",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                Note note = documentSnapshot.toObject(Note.class);
                                String description = note.getDescription();
                                String title = note.getTitle();
                                int priority = note.getPriority();
                                String imgurl = note.getImgurl();
                                String uid = note.getUid();
                                String ts = note.getTimeStamp();
                                String noteid = documentSnapshot.getId();
                                note.setNoteid(noteid);
                                if(!description.isEmpty() && !title.isEmpty()) {
                                    Intent intent = new Intent(Main2Activity.this, Update.class);
                                    intent.putExtra("note", note);

                                    startActivity(intent);
                                }


                                   // adapter.deleteItem(position);


                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();

            }

        });

    }



    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }
    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all_tasks:
                mAuth.signOut();
                finish();
                Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.completed:
                Intent intent = new Intent(this, successActivity.class);
                startActivity(intent);

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
