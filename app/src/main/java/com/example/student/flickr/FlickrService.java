package com.example.student.flickr;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by student on 2016.
 */
public interface FlickrService {

    @GET("/services/rest/")
    Call<Result> search(
            @Query("text") String text,
            @Query("method") String method,
            @Query("format") String format,
            @Query("api_key") String key,
            @Query("nojsoncallback") int flag,
            @Query("page") int page
    );

// https://api.flickr.com/services/rest/
// ?method=flickr.photos.search
// &api_key=dcceac9e627ee62a18f24c610f9f6e38
// &tags=Black+and+White
// &format=json
// &nojsoncallback=1
// &api_sig=cd70c67fd237a4f7f5b2d90d2019ee9a

}
