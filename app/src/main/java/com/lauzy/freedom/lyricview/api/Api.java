package com.lauzy.freedom.lyricview.api;

import com.lauzy.freedom.lyricview.model.AdminMessage;
import com.lauzy.freedom.lyricview.model.Comment;
import com.lauzy.freedom.lyricview.model.DatumAlbum;
import com.lauzy.freedom.lyricview.model.DatumComment;
import com.lauzy.freedom.lyricview.model.DatumSinger;
import com.lauzy.freedom.lyricview.model.PaginateComment;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface Api {
    @GET("files/{singer}/{album}/{id}")
    Call<ResponseBody> getLrc(@Path("singer") String singer,
                              @Path("album") String album,
                              @Path("id") String id);

    @GET("safir/singers")
    Call<DatumSinger> getSinger(@Query("page") int page);

    @GET("safir/album/{id}")
    Call<DatumAlbum> getAlbum(@Path("id") int id, @Query("page") int page);

    @GET("safir/comment/{id}")
    Call<DatumComment> getComment(@Path("id") int id, @Query("page") int page);

    @POST("safir/comment/{id}")
    Call<ResponseBody> postComment(@Path("id") int id, @Body Comment comment);

    @GET("safir/album-rate/{id}")
    Call<ResponseBody> getScore(@Path("id") int id);

    @GET("safir/admin-message")
    Call<AdminMessage> getAdminMessage();
}









