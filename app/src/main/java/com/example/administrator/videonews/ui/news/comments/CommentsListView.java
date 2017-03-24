package com.example.administrator.videonews.ui.news.comments;

import android.content.Context;
import android.util.AttributeSet;

import com.example.administrator.videonews.bombapi.BombConst;
import com.example.administrator.videonews.bombapi.entity.CommentsEntity;
import com.example.administrator.videonews.bombapi.other.InQuery;
import com.example.administrator.videonews.bombapi.result.QueryResult;
import com.example.administrator.videonews.ui.base.BaseResourceView;

import retrofit2.Call;

public class CommentsListView extends BaseResourceView<CommentsEntity,CommentsItemView> {
    public CommentsListView(Context context) {
        super(context);
    }

    public CommentsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CommentsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public String newsId;

    public void setNewsId(String newsId){
        this.newsId = newsId;
    }

    @Override
    protected Call<QueryResult<CommentsEntity>> queryData(int limit, int skip) {
        InQuery where = new InQuery(BombConst.FIELD_NEWS,BombConst.TABLE_NEWS,newsId);
        return newsApi.getComments(limit,skip,where);
    }

    @Override
    protected int getLimit() {
        return 20;
    }

    @Override
    protected CommentsItemView createItemView() {
        return new CommentsItemView(getContext());
    }
}
