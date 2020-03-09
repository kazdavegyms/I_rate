package com.example.fire.irate.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.fire.irate.models.Post;
import com.example.fire.irate.ui.activities.PostActivity2;
import com.example.fire.irate.ui.dialogs.PostCreateDialog;
import com.example.fire.irate.utils.Constants;
import com.fire.fire.postandcommenttutorial.R;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private View mRootVIew;
    private FirebaseRecyclerAdapter<Post, PostHolder> mPostAdapter;
    private RecyclerView mPostRecyclerView;
    private FirebaseAuth firebaseAuth;



    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootVIew = inflater.inflate(R.layout.fragment_home, container, false);
        FloatingActionButton fab = (FloatingActionButton) mRootVIew.findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostCreateDialog dialog = new PostCreateDialog();
                dialog.show(getFragmentManager(), null);
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        init();
        return mRootVIew;
    }

    private void init() {
        mPostRecyclerView = (RecyclerView) mRootVIew.findViewById(R.id.recyclerview_post);
        mPostRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setupAdapter();
        mPostRecyclerView.setAdapter(mPostAdapter);
    }

    //set up the post valus to the clas adapter
    private void setupAdapter() {
        mPostAdapter = new FirebaseRecyclerAdapter<Post, PostHolder>(
                Post.class,
                R.layout.row_post,
                PostHolder.class,
                FirebaseUtils.getPostRef()
        ) {
            @Override
            protected void populateViewHolder(PostHolder viewHolder, final Post model, int position) {

                viewHolder.setNumCOmments(String.valueOf(model.getNumComments()));

                int likesvoteup = Integer.valueOf((int) model.getNumLikes());
                int dislikedownvote = Integer.valueOf((int)model.getNumdisLikes());
                int totalv = likesvoteup - dislikedownvote;
                String totalvots = Integer.toString(totalv);
                Toast.makeText(getActivity(),totalvots,Toast.LENGTH_SHORT).show();



                viewHolder.setNumLikes(totalvots);
               // viewHolder.setNumdisLikes(String.valueOf(model.getNumdisLikes()));
                viewHolder.setTIme(DateUtils.getRelativeTimeSpanString(model.getTimeCreated()));
                viewHolder.setUsername(model.getUser().getUser());
                viewHolder.setPostText(model.getPostText());


                if (model.getUser().getPhotoUrl() != null) {
                    StorageReference storageReferencee = FirebaseStorage.getInstance()
                            .getReference(model.getUser().getPhotoUrl());


                    Glide.with(getActivity())
                            .using(new FirebaseImageLoader())
                            .load(storageReferencee)
                            .into(viewHolder.postOwnerDisplayImageView);

                }else{
                    viewHolder.postOwnerDisplayImageView.setImageBitmap(null);
                    viewHolder.postOwnerDisplayImageView.setVisibility(View.GONE);
                }

                if (model.getPostImageUrl() != null) {
                    viewHolder.postDisplayImageVIew.setVisibility(View.VISIBLE);
                    StorageReference storageReference = FirebaseStorage.getInstance()
                            .getReference(model.getPostImageUrl());
                    Glide.with(getActivity())
                            .using(new FirebaseImageLoader())
                            .load(storageReference)
                            .into(viewHolder.postDisplayImageVIew);
                } else {
                    viewHolder.postDisplayImageVIew.setImageBitmap(null);
                    viewHolder.postDisplayImageVIew.setVisibility(View.GONE);
                }

                viewHolder.postLikeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onLikeClick(model.getPostId());
                    }
                });

                viewHolder.downvote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ondisLikeClick(model.getPostId());
                    }
                });



                viewHolder.postCommentLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                         Intent intent = new Intent(getContext(), PostActivity2.class);
                         intent.putExtra(Constants.EXTRA_POST, model);
                         startActivity(intent);
                    }
                });
            }
        };
    }


    // for the belove code i have explain he same thing  in onlikeClick methode,
    private void ondisLikeClick(final String postId) {


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference mostafa = ref.child("posts").child(postId).child("userText");
      //  final DatabaseReference checkliketrue = ref.child("posts").child(postId).child("userText");


       //this is to get the state true or false in the 'post dislike' table for current user and set the like and dis like according to that
        FirebaseUtils.getPostdisLikedRef(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {

                            Toast.makeText(getActivity(),"you already dislike this" ,Toast.LENGTH_SHORT).show();


                        } else {


                            FirebaseUtils.getPostLikedRef(postId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getValue() != null) {

                                     //if like is not null and disslike is null  remove like and add dislike

                                                mostafa.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        final String email = dataSnapshot.getValue(String.class);
                                                        Toast.makeText(getActivity(),email ,Toast.LENGTH_SHORT).show();

                                                        FirebaseUtils.getratingslike(email)
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        if (dataSnapshot.getValue() == null) {

                                                                            FirebaseUtils.getratingslike(email)
                                                                                    .child("rating")
                                                                                    .setValue(1);

                                                                        }else {

                                                                            FirebaseUtils.getratingslike(email)
                                                                                    .child("rating")
                                                                                    .runTransaction(new Transaction.Handler() {
                                                                                        @Override
                                                                                        public Transaction.Result doTransaction(MutableData mutableData) {
                                                                                            long num = (long) mutableData.getValue();
                                                                                            mutableData.setValue(num - 1);
                                                                                            return Transaction.success(mutableData);
                                                                                        }

                                                                                        @Override
                                                                                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                                                                            Toast.makeText(getActivity(), "done like", Toast.LENGTH_SHORT).show();
                                                                                        }

                                                                                    });
                                                                        }

                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });
                                                        //do what you want with the email
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                                //User liked
                                                FirebaseUtils.getPostRef()
                                                        .child(postId)
                                                        .child(Constants.NUM_LIKES_KEY)
                                                        .runTransaction(new Transaction.Handler() {
                                                            @Override
                                                            public Transaction.Result doTransaction(MutableData mutableData) {
                                                                long num = (long) mutableData.getValue();
                                                                mutableData.setValue(num - 1);
                                                                return Transaction.success(mutableData);
                                                            }

                                                            @Override
                                                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                                                FirebaseUtils.getPostLikedRef(postId)
                                                                        .setValue(null);
                                                            }
                                                        });
                                            } else {


                                                //if like is null and dislike is null add new dislike

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });




                            mostafa.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    final String email = dataSnapshot.getValue(String.class);
                                    Toast.makeText(getActivity(),email ,Toast.LENGTH_SHORT).show();

                                    FirebaseUtils.getratings(email)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.getValue() == null) {

                                                        FirebaseUtils.getratings(email)
                                                                .child("rating")
                                                                .setValue(1);

                                                    }else {

                                                        FirebaseUtils.getratings(email)
                                                                .child("rating")
                                                                .runTransaction(new Transaction.Handler() {
                                                                    @Override
                                                                    public Transaction.Result doTransaction(MutableData mutableData) {
                                                                        long num = (long) mutableData.getValue();
                                                                        mutableData.setValue(num + 1);
                                                                        return Transaction.success(mutableData);
                                                                    }

                                                                    @Override
                                                                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                                                      //  Toast.makeText(getActivity(), "done dis like", Toast.LENGTH_SHORT).show();
                                                                    }

                                                                });
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                    //do what you want with the email
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });



                            FirebaseUtils.getPostRef()
                                    .child(postId)
                                    .child(Constants.NUM_DISLIKES_KEY)
                                    .runTransaction(new Transaction.Handler() {
                                        @Override
                                        public Transaction.Result doTransaction(MutableData mutableData) {
                                            long num = (long) mutableData.getValue();
                                            mutableData.setValue(num + 1);
                                            return Transaction.success(mutableData);
                                        }

                                        @Override
                                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                            FirebaseUtils.getPostdisLikedRef(postId)
                                                    .setValue(true);


                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




    }

    private void onLikeClick(final String postId) {

        //reference to user text in post db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference mostafa = ref.child("posts").child(postId).child("userText");

        FirebaseUtils.getPostLikedRef(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {

                            Toast.makeText(getActivity(),"you already like this" ,Toast.LENGTH_SHORT).show();
                            //if you clik button twice, mean more than one time


                        } else {

                            //this is to get the state true or false in the 'post dislike' table for current user and set the like and dis like according to that

                            FirebaseUtils.getPostdisLikedRef(postId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getValue() != null) {

                                                //here we check wether you have alreadey press the dislike btn(means dislike is true)

                          //dislike remove from ratings
                            mostafa.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    final String email = dataSnapshot.getValue(String.class);
                                    Toast.makeText(getActivity(),email ,Toast.LENGTH_SHORT).show();

                                    FirebaseUtils.getratings(email)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    FirebaseUtils.getratings(email)
                                                            .child("rating")
                                                            .runTransaction(new Transaction.Handler() {
                                                                @Override
                                                                public Transaction.Result doTransaction(MutableData mutableData) {
                                                                    long num = (long) mutableData.getValue();
                                                                    mutableData.setValue(num - 1);
                                                                    return Transaction.success(mutableData);
                                                                }

                                                                @Override
                                                                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                                                  //  Toast.makeText(getActivity(),"done dis like",Toast.LENGTH_SHORT).show();
                                                                }

                                                            });

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                    //do what you want with the email
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                            //so dislike will be removed
                            FirebaseUtils.getPostRef()
                                    .child(postId)
                                    .child(Constants.NUM_DISLIKES_KEY)
                                    .runTransaction(new Transaction.Handler() {
                                        @Override
                                        public Transaction.Result doTransaction(MutableData mutableData) {
                                            long num = (long) mutableData.getValue();
                                            mutableData.setValue(num - 1);
                                            return Transaction.success(mutableData);
                                        }



                                        @Override
                                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                            FirebaseUtils.getPostdisLikedRef(postId)
                                                    .setValue(null);


                                        }
                                    });

                                            } else{


                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                //if the post like null, then add like to database
                            mostafa.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    final String email = dataSnapshot.getValue(String.class);
                                    Toast.makeText(getActivity(),email ,Toast.LENGTH_SHORT).show();

                                    FirebaseUtils.getratingslike(email)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.getValue() == null) {

                                                        FirebaseUtils.getratingslike(email)
                                                                .child("rating")
                                                                .setValue(1);

                                                    }else {

                                                        FirebaseUtils.getratingslike(email)
                                                                .child("rating")
                                                                .runTransaction(new Transaction.Handler() {
                                                                    @Override
                                                                    public Transaction.Result doTransaction(MutableData mutableData) {
                                                                        long num = (long) mutableData.getValue();
                                                                        mutableData.setValue(num + 1);
                                                                        return Transaction.success(mutableData);
                                                                    }

                                                                    @Override
                                                                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                                                        Toast.makeText(getActivity(), "done like", Toast.LENGTH_SHORT).show();
                                                                    }

                                                                });
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                    //do what you want with the email
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            FirebaseUtils.getPostRef()
                                    .child(postId)
                                    .child(Constants.NUM_LIKES_KEY)
                                    .runTransaction(new Transaction.Handler() {
                                        @Override
                                        public Transaction.Result doTransaction(MutableData mutableData) {
                                            long num = (long) mutableData.getValue();
                                            mutableData.setValue(num + 1);
                                            return Transaction.success(mutableData);
                                        }

                                        @Override
                                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                            FirebaseUtils.getPostLikedRef(postId)
                                                    .setValue(true);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public static class PostHolder extends RecyclerView.ViewHolder {
        ImageView postOwnerDisplayImageView;
        TextView postOwnerUsernameTextView;
        TextView postTimeCreatedTextView;
        ImageView postDisplayImageVIew;
        TextView postTextTextView;
        LinearLayout downvote;
        LinearLayout postLikeLayout;
        LinearLayout postCommentLayout;
        TextView postNumLikesTextView;
        TextView postNumCommentsTextView;


        public PostHolder(View itemView) {
            super(itemView);
            postOwnerDisplayImageView = (ImageView) itemView.findViewById(R.id.iv_post_owner_display);
            postOwnerUsernameTextView = (TextView) itemView.findViewById(R.id.tv_post_username);
            postTimeCreatedTextView = (TextView) itemView.findViewById(R.id.tv_time);
            postDisplayImageVIew = (ImageView) itemView.findViewById(R.id.iv_post_display);
            postLikeLayout = (LinearLayout) itemView.findViewById(R.id.like_layout);
            downvote = (LinearLayout) itemView.findViewById(R.id.downvote);
            postCommentLayout = (LinearLayout) itemView.findViewById(R.id.comment_layout);
            postNumLikesTextView = (TextView) itemView.findViewById(R.id.tv_likes);
            postNumCommentsTextView = (TextView) itemView.findViewById(R.id.tv_comments);
            postTextTextView = (TextView) itemView.findViewById(R.id.tv_post_text);
        }

        public void setUsername(String username) {
            postOwnerUsernameTextView.setText(username);
        }

        public void setTIme(CharSequence time) {
            postTimeCreatedTextView.setText(time);
        }

        public void setNumLikes(String numLikes) {
            postNumLikesTextView.setText(numLikes);
        }



        public void setNumCOmments(String numComments) {
            postNumCommentsTextView.setText(numComments);
        }

        public void setPostText(String text) {
            postTextTextView.setText(text);
        }

    }
}
