package com.lauzy.freedom.lyricview.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lauzy.freedom.lyricview.R;
import com.lauzy.freedom.lyricview.Utils.MyDividerItemDecoration;
import com.lauzy.freedom.lyricview.adapter.CommentAdapter;
import com.lauzy.freedom.lyricview.api.Api;
import com.lauzy.freedom.lyricview.api.ServiceGenerator;
import com.lauzy.freedom.lyricview.model.Comment;
import com.lauzy.freedom.lyricview.model.DatumComment;
import com.lauzy.freedom.lyricview.model.PaginateComment;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentsFragment extends Fragment implements CommentAdapter.CommentAdapterListener {
    private static final int PAGE_START = 1;
    private View view;
    private Context mContext;
    private int mId;
    private RecyclerView recyclerView;
    private List<Comment> mMusicList;
    private CommentAdapter mAdapter;
    private int currentPage = PAGE_START;
    private boolean loading = true;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;


    public CommentsFragment() {
    }

    public CommentsFragment(Context context, int id) {
        mContext = context;
        mId = id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_album, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        mMusicList = new ArrayList<>();

        mAdapter = new CommentAdapter(mContext, mMusicList, this);

        LinearLayoutManager mLayoutManager =
                new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(mContext,
                DividerItemDecoration.VERTICAL, 36));
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            ++currentPage;
                            loadNextPage();
                        }
                    }
                }
            }
        });
        loadFirstPage();
        return view;
    }
    private void loadNextPage() {
        Api service = null;
        try {
            service = ServiceGenerator.createService(Api.class);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        service.getComment(mId,currentPage).enqueue(new Callback<DatumComment>() {
            @Override
            public void onResponse(Call<DatumComment> call,
                                   Response<DatumComment> response) {
                try {
                    DatumComment results = response.body();
                    if (results != null) {
                        mAdapter.addAll(results.getData());
                    } else loading = false;
                } catch (Exception ignored) {
                    Log.e(">> Page-Last: ", ignored.getMessage() + " ");
                }
            }

            @Override
            public void onFailure(Call<DatumComment> call, Throwable t) {
            }
        });
    }

    private void loadFirstPage() {
        Api service = null;
        try {
            service = ServiceGenerator.createService(Api.class);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Call<DatumComment> call = service.getComment(mId,currentPage);
        call.enqueue(new Callback<DatumComment>() {
            @Override
            public void onResponse(Call<DatumComment> call, Response<DatumComment> response) {
                try {
                    DatumComment results = response.body();
                    if (results != null) {
                        mAdapter.addAll(results.getData());
                    } else loading = false;
                } catch (Exception ignored) {
                    Log.e(">> Page-Firs-Error: ", ignored.getMessage() + " ");
                }
            }

            @Override
            public void onFailure(Call<DatumComment> call, Throwable t) {
                Log.e(">> Load Error: ", t.getMessage() + " ");
            }
        });
    }
    @Override
    public void onContactSelected(Comment contact) {
        // Replay to comment
    }
}
