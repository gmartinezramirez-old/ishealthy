package com.gmartinezramirez.healthyfood.ai;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface CustomVisionAPI {
    String BASE_URL = "https://southcentralus.api.cognitive.microsoft.com/customvision/v1.0/Prediction/e864c330-02e0-4293-a5fa-20b7fd9b5079/";
    String PREDICTION_KEY = "7b2789c3e6d3403a843088da78f8c236";
    String OCTET_STREAM = "application/octet-stream";
    String JSON = "application/json";

    @POST("image")
    Call<CustomVisionResponse> sendImage(
            @Header("Prediction-Key") String predictionKey,
            @Header("Content-Type") String contentType,
            @Body RequestBody bytes
    );

    @POST("url")
    Call<CustomVisionResponse> sendURL(
            @Header("Prediction-Key") String predictionKey,
            @Header("Content-Type") String contentType,
            @Body CustomVisionURLImage url
    );
}
