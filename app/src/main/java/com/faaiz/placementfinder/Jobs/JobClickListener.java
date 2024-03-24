package com.faaiz.placementfinder.Jobs;

import android.content.Context;

import androidx.cardview.widget.CardView;

import com.faaiz.placementfinder.JobPost;

public interface JobClickListener {
    void setOnJobClick(JobPost job, Context context);
    void setOnJobLongClick(JobPost job, CardView card, Context context);
}
