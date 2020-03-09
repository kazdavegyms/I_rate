package com.example.fire.irate.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.fire.irate.models.Comment;
import com.fire.fire.postandcommenttutorial.R;
import com.example.fire.irate.models.Post;
import com.example.fire.irate.utils.Constants;
import com.example.fire.irate.utils.FirebaseUtils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PostActivity2 extends AppCompatActivity implements View.OnClickListener {

    private static final String BUNDLE_COMMENT = "comment";
    private Post mPost;
    private EditText mCommentEditTextView;
    private Comment mComment;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        firebaseAuth = FirebaseAuth.getInstance();

        if (savedInstanceState != null) {
            mComment = (Comment) savedInstanceState.getSerializable(BUNDLE_COMMENT);
        }

        Intent intent = getIntent();
        mPost = (Post) intent.getSerializableExtra(Constants.EXTRA_POST);

        init();
        initPost();
        initCommentSection();
    }


    // loade the commets from the recyle iew in to comment sction


    private void initCommentSection() {
        RecyclerView commentRecyclerView = (RecyclerView) findViewById(R.id.comment_recyclerview);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(PostActivity2.this));

        FirebaseRecyclerAdapter<Comment, CommentHolder> commentAdapter = new FirebaseRecyclerAdapter<Comment, CommentHolder>(
                Comment.class,
                R.layout.row_comment,
                CommentHolder.class,
                FirebaseUtils.getCommentRef(mPost.getPostId())
        ) {
            @Override
            protected void populateViewHolder(final CommentHolder viewHolder, final Comment model, int position) {
                //user should be one who done the



                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                DatabaseReference image = ref.child("users").child(model.getUser().replace(".", ",")).child("photoUrl");
                final DatabaseReference unamee = ref.child("users").child(model.getUser().replace(".", ",")).child("user");


                image.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String urll = dataSnapshot.getValue(String.class);
                        final String url = String.valueOf(urll);


                        StorageReference storageReferencee = FirebaseStorage.getInstance()
                                .getReference(url);

                        Glide.with(PostActivity2.this)
                                .using(new FirebaseImageLoader())
                                .load(storageReferencee)
                                .into(viewHolder.commentOwnerDisplay);


                        unamee.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String urll = dataSnapshot.getValue(String.class);
                                final String unamenew = String.valueOf(urll);


                                viewHolder.setUsername(unamenew);
                                viewHolder.setComment(model.getComment());
                                viewHolder.setTime(DateUtils.getRelativeTimeSpanString(model.getTimeCreated()));



                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }


                        });



                        // Toast.makeText(UserProfile.this, url, Toast.LENGTH_SHORT).show();




                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }


                });


            }
        };

        commentRecyclerView.setAdapter(commentAdapter);
    }

    private void initPost() {

        final ImageView postOwnerDisplayImageView = (ImageView) findViewById(R.id.iv_post_owner_display);
        TextView postOwnerUsernameTextView = (TextView) findViewById(R.id.tv_post_username);
        TextView postTimeCreatedTextView = (TextView) findViewById(R.id.tv_time);
        final ImageView postDisplayImageView = (ImageView) findViewById(R.id.iv_post_display);
        LinearLayout postLikeLayout = (LinearLayout) findViewById(R.id.like_layout);
        LinearLayout postCommentLayout = (LinearLayout) findViewById(R.id.comment_layout);
        TextView postNumLikesTextView = (TextView) findViewById(R.id.tv_likes);
        TextView postNumCommentsTextView = (TextView) findViewById(R.id.tv_comments);
        TextView postTextTextView = (TextView) findViewById(R.id.tv_post_text);

        postOwnerUsernameTextView.setText(mPost.getUser().getUser());
        postTimeCreatedTextView.setText(DateUtils.getRelativeTimeSpanString(mPost.getTimeCreated()));
        postTextTextView.setText(mPost.getPostText());
        postNumLikesTextView.setText(String.valueOf(mPost.getNumLikes()));
        postNumCommentsTextView.setText(String.valueOf(mPost.getNumComments()));


         //fetching the url of the user for post profile
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference image = ref.child("users").child(mPost.getUser().getEmail().replace(".", ",")).child("photoUrl");


        image.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String urll = dataSnapshot.getValue(String.class);
                final String url = String.valueOf(urll);


                StorageReference storageReferencee = FirebaseStorage.getInstance()
                        .getReference(url);

                Glide.with(PostActivity2.this)
                        .using(new FirebaseImageLoader())
                        .load(storageReferencee)
                        .into(postOwnerDisplayImageView);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });



        if (mPost.getPostImageUrl() != null) {
            postDisplayImageView.setVisibility(View.VISIBLE);
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(mPost.getPostImageUrl());

            Glide.with(PostActivity2.this)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .into(postDisplayImageView);
        } else {
            postDisplayImageView.setImageBitmap(null);
            postDisplayImageView.setVisibility(View.GONE);
        }
    }

    private void init() {
        mCommentEditTextView = (EditText) findViewById(R.id.et_comment);
        findViewById(R.id.iv_send).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_send:
                sendComment();
        }
    }

    //to add the new comments

    private void sendComment() {
        final ProgressDialog progressDialog = new ProgressDialog(PostActivity2.this);
        progressDialog.setMessage("Sending comment..");
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(true);
        progressDialog.show();

//lode the comment details in to the to comment model

                mComment = new Comment();
                final String uid = FirebaseUtils.getUid();
                String strComment = mCommentEditTextView.getText().toString();

                mComment.setUser(firebaseAuth.getCurrentUser().getEmail());
                mComment.setCommentId(uid);
                mComment.setComment(strComment);
                mComment.setTimeCreated(System.currentTimeMillis());

                //sending the values to database

                FirebaseUtils.getUserRef(FirebaseUtils.getCurrentUser().getEmail().replace(".", ","))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //User user = dataSnapshot.getValue(User.class);
                                FirebaseUtils.getCommentRef(mPost.getPostId())
                                        .child(uid)
                                        .setValue(mComment);

                                //count of the comment

                                FirebaseUtils.getPostRef().child(mPost.getPostId())
                                        .child(Constants.NUM_COMMENTS_KEY)
                                        .runTransaction(new Transaction.Handler() {
                                            @Override
                                            public Transaction.Result doTransaction(MutableData mutableData) {
                                                long num = (long) mutableData.getValue();
                                                mutableData.setValue(num + 1);
                                                return Transaction.success(mutableData);
                                            }

                                            @Override
                                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                                progressDialog.dismiss();
                                                FirebaseUtils.addToMyRecord(Constants.COMMENTS_KEY, uid);
                                            }
                                        });
                            }




            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });




    }


    public static class CommentHolder extends RecyclerView.ViewHolder {
        ImageView commentOwnerDisplay;
        TextView usernameTextView;
        TextView timeTextView;
        TextView commentTextView;

        public CommentHolder(View itemView) {
            super(itemView);
            commentOwnerDisplay = (ImageView) itemView.findViewById(R.id.iv_comment_owner_display);
            usernameTextView = (TextView) itemView.findViewById(R.id.tv_username);
            timeTextView = (TextView) itemView.findViewById(R.id.tv_time);
            commentTextView = (TextView) itemView.findViewById(R.id.tv_comment);
        }

        public void setUsername(String username) {
            usernameTextView.setText(username);
        }

        public void setTime(CharSequence time) {
            timeTextView.setText(time);
        }

        public void setComment(String comment) {
            commentTextView.setText(comment);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(BUNDLE_COMMENT, mComment);
        super.onSaveInstanceState(outState);
    }
}
