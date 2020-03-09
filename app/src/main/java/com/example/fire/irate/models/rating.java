package com.example.fire.irate.models;

import java.io.Serializable;

/**
 * Created by brad on 2017/02/05.
 */

public class rating implements Serializable {

    private long numLikes;


    public rating() {
    }

    public rating( long numLikes) {


        this.numLikes = numLikes;

    }



    public long getNumLikes() {
        return numLikes;
    }

    public void setNumLikes(long numLikes) {
        this.numLikes = numLikes;
    }


}
