package com.example.administrator.videonews.ui.likes;

import android.content.Context;
import android.util.AttributeSet;

import com.example.administrator.videonews.UserManager;
import com.example.administrator.videonews.bombapi.BombConst;
import com.example.administrator.videonews.bombapi.entity.NewsEntity;
import com.example.administrator.videonews.bombapi.other.InQuery;
import com.example.administrator.videonews.bombapi.result.QueryResult;
import com.example.administrator.videonews.ui.base.BaseResourceView;

import retrofit2.Call;

/**
 * 我的收藏类表视图
 */

public class LikesListView extends BaseResourceView<NewsEntity,LikesItemView> {
    public LikesListView(Context context) {
        super(context);
    }

    public LikesListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LikesListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //请求数据（创建一个请求）
    @Override
    protected Call<QueryResult<NewsEntity>> queryData(int limit, int skip) {
        String userId = UserManager.getInstance().getObjectId();
        //由于服务器原因造成的参数（可以直接使用）
        InQuery where = new InQuery(BombConst.FIELD_LIKES,BombConst.TABLE_USER,userId);
        return newsApi.getLikedList(limit,skip,where);
    }

    //每次请求多少条数据
    @Override
    protected int getLimit() {
        return 15;
    }

    //实例化ItemView
    @Override
    protected LikesItemView createItemView() {
        LikesItemView likesItemView = new LikesItemView(getContext());
        return likesItemView;
    }

    //退出登录时，清空收藏列表
    public void clear(){
        adapter.clear();
    }
}
