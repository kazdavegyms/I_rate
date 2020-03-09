package com.example.fire.irate.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.fire.irate.models.User;
import com.example.fire.irate.utils.Constants;
import com.fire.fire.postandcommenttutorial.R;
import com.example.fire.irate.utils.FirebaseUtils;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class UserProfile extends AppCompatActivity {


    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final int RC_PHOTO_PICKER = 1;
    private ImageView mDisplayImageView;
    private TextView mNameTextView;
    private TextView mEmailTextView;
    private ValueEventListener mUserValueEventListener;
    private DatabaseReference mUserRef;
    private FirebaseAuth firebaseAuth;
    private Uri mSelectedUri;
    private User muser;
    private Button uploadebtn;
    private ProgressDialog mProgressDialog;
    private RatingBar rBar;
    private static String dislk, lk;
    private int dislkk, lkk;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mDisplayImageView = (ImageView) findViewById(R.id.imageView_display);
        mNameTextView = (TextView) findViewById(R.id.textview_name);
        uploadebtn = (Button) findViewById(R.id.upbtn);
        mEmailTextView = (TextView) findViewById(R.id.textView_email);
        rBar = (RatingBar) findViewById(R.id.RATINGinitialvalueratingID);

        firebaseAuth = FirebaseAuth.getInstance();

        mNameTextView.setText(firebaseAuth.getCurrentUser().getDisplayName());
        mEmailTextView.setText(firebaseAuth.getCurrentUser().getEmail());


        mDisplayImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        uploadebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploade();

            }
        });


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reff = FirebaseDatabase.getInstance().getReference();

        // references to both total likes and total dislike user ever get from thr ranking database

        final DatabaseReference dislike = ref.child("rating").child(firebaseAuth.getCurrentUser().getEmail().replace(".", ",")).child("totdislike").child("rating");
        DatabaseReference likes = reff.child("rating").child(firebaseAuth.getCurrentUser().getEmail().replace(".", ",")).child("totlike").child("rating");
        DatabaseReference image = reff.child("users").child(firebaseAuth.getCurrentUser().getEmail().replace(".", ",")).child("photoUrl");



        image.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String urll = dataSnapshot.getValue(String.class);
                final String url = String.valueOf(urll);

                Toast.makeText(UserProfile.this, url, Toast.LENGTH_SHORT).show();


                if(url.equals("none")){

                    Toast.makeText(UserProfile.this, "please select a profile picture and update", Toast.LENGTH_SHORT).show();

                }else{
                    try {
                        StorageReference storageReferencee = FirebaseStorage.getInstance()
                                .getReference(url);

                        Glide.with(UserProfile.this)
                                .using(new FirebaseImageLoader())
                                .load(storageReferencee)
                                .into(mDisplayImageView);
                        //Toast.makeText(UserProfile.this, "please select a profile picture and update", Toast.LENGTH_SHORT).show();


                    }catch (Exception e){
                        Toast.makeText(UserProfile.this, "not sucess", Toast.LENGTH_SHORT).show();

                    }

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });



        //this is to fetch the likes user ever got

        likes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Long email = dataSnapshot.getValue(Long.class);
                final String nn = String.valueOf(email);

                // this is to fetch the dislikes user ever got
                dislike.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final Long email = dataSnapshot.getValue(Long.class);

                        String kk = String.valueOf(email);

                     //   Toast.makeText(UserProfile.this,nn, Toast.LENGTH_SHORT).show();
                      //  Toast.makeText(UserProfile.this,kk, Toast.LENGTH_SHORT).show();


                        // this is the simple algorithem to check the rating

                        try {

                            float number1 = Float.parseFloat(kk);
                            float number2 = Float.parseFloat(nn);

                            float sum = number1 + number2;
                            float rate = (number2 / sum) * 5;
                            rBar.setRating(rate);

                            String numberAsString = Float.toString(rate);

                            Toast.makeText(UserProfile.this,"your Rating is " + numberAsString, Toast.LENGTH_SHORT).show();

                        }catch (Exception e){


                        }



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        Toast.makeText(UserProfile.this,"error", Toast.LENGTH_SHORT).show();

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });
      //  rBar.setRating((float) 5);


    }



    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
    }

    // this is to add the imaes to image view from your local storage
    private void addimage() {


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference imgurl = ref.child("users").child(firebaseAuth.getCurrentUser().getEmail().replace(".", ","));
        String url = common.url;


        Map<String, Object> updates = new HashMap<String, Object>();

        updates.put("photoUrl", url);

//etc

        imgurl.updateChildren(updates);


    }
    //this is to uploade the selected image to database
    private void uploade() {

        final ProgressDialog progressDialog = new ProgressDialog(UserProfile.this);
        progressDialog.setMessage("Sending comment..");
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
//        mProgressDialog.setMessage("Sending post...");
        //    mProgressDialog.setCancelable(false);
        //  mProgressDialog.setIndeterminate(true);
        //  mProgressDialog.show();

        if (mSelectedUri != null) {

            FirebaseUtils.getImageSRef()
                    .child(mSelectedUri.getLastPathSegment())
                    .putFile(mSelectedUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            String url = Constants.POST_IMAGES + "/" + mSelectedUri.getLastPathSegment();
                            common.url = url;
                            addimage();

                            progressDialog.dismiss();



                        }
                    });


        } else {

            Toast.makeText(UserProfile.this, "not sucess", Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        // mProgressDialog.setMessage("Sending post...");
        // mProgressDialog.setCancelable(false);
        // mProgressDialog.setIndeterminate(true);
        //  mProgressDialog.show();

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER) {
            if (resultCode == RESULT_OK) {
                mSelectedUri = data.getData();
                mDisplayImageView.setImageURI(mSelectedUri);


            }

        }
    }



}

