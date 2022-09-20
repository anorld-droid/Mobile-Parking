package com.karanja.Api;

import com.karanja.Api.Responses.BaseDataResponse;
import com.karanja.Api.Responses.BaseResponse;
import com.karanja.Api.Responses.LoginReg.UserResponse;
import com.karanja.Api.Responses.LoginReg.VerificationResponse;
import com.karanja.Api.Responses.Otp.OTPResponse;
import com.karanja.Api.Responses.Park.ActiveAndInactiveParkingSpaceAllResponse;
import com.karanja.Api.Responses.Park.PageParkingSpaceAllResponse;
import com.karanja.Api.Responses.Park.ParkingSpaceAllResponse;
import com.karanja.Api.Responses.Park.ParkingSpaceDataResponse;
import com.karanja.Api.Responses.Park.ParkingSpaceHistoryResponse;
import com.karanja.Api.Responses.Park.SingleParkingSpaceResponse;
import com.karanja.Model.Booking.BookingSchedule;
import com.karanja.Model.FacebookUser;
import com.karanja.Model.NewUser;
import com.karanja.Model.Park.NewParkingSpace;
import com.karanja.Model.Park.ParkingSpace;
import com.karanja.Model.Park.UserPackedSpace;
import com.karanja.Model.PhoneOtp;
import com.karanja.Model.User;
import com.karanja.Model.UserProfile;
import com.karanja.Model.Vehicle;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ParkingApi {


    //Auth
    //  Registration APIs
    // End  User
    @Headers({"Accept:application/json","Content-Type:application/json"})
    @POST("auth/phone-registration-status")
    Call<VerificationResponse> verifyPhoneNo(@Query("phone") String phone);

    @Headers({"Accept:application/json","Content-Type:application/json"})
    @POST("auth/request-otp")
    Call<OTPResponse> sendOTP(@Query("phone") String phone);

    @Headers({"Accept:application/json","Content-Type:application/json"})
    @POST("auth/verify-otp")
    Call<BaseResponse> verifyOTP(@Body PhoneOtp phoneOtp);

    @Headers({"Accept:application/json","Content-Type:application/json"})
    @POST("auth/register/user")
    Call <BaseDataResponse<UserResponse>> registerUser(@Body NewUser newUser);


    /*Admin and Partners.... Empty for now*/


    //Login
    //End User
    @Headers({"Accept:application/json","Content-Type:application/json"})
    @POST("auth/login/user")
    Call<BaseDataResponse<UserResponse>> loginPhoneNoUser(@Query("phone") String phoneNo, @Query("password") String password);

    @Headers({"Accept:application/json","Content-Type:application/json"})
    @POST("auth/login/facebook")
    Call<Void>  registerFacebookUser (@Body FacebookUser facebookUser);

    /*Admin and Partners.... Empty for now*/


    //Profile Management
    @Headers({"Accept:application/json"})
    @GET("user")
    Call<BaseDataResponse<User>> getProfileDetails(@Header("Authorization") String token);

    //API is Faulty
    @Headers({"Accept:application/json","Content-Type:application/json"})
    @PUT("user")
    Call<BaseDataResponse<UserProfile>> editUserProfile(@Header("Authorization") String token, @Body UserProfile userProfile);

    //More methods



    //Parking Space Management

    //Add New Parking Space
    @Headers({"Accept:application/json","Content-Type:application/json"})
    @POST("park")
    Call<ParkingSpaceDataResponse<ParkingSpace>> addParkingSpace(@Header("Authorization") String token, @Body NewParkingSpace newParkingSpace);

    @Headers({"Accept:application/json"})
    @GET("park")
    Call<ParkingSpaceAllResponse> getAllParkingSpace(@Header("Authorization") String token);

    @Headers({"Accept:application/json"})
    @GET("park/{id}")
    Call<SingleParkingSpaceResponse> getParkingSpace(@Path("id") int id, @Header("Authorization") String token);

    @Headers({"Accept:application/json"})
    @GET("park/active")
    Call<ActiveAndInactiveParkingSpaceAllResponse> getAllActiveParkingSpaces(@Header("Authorization") String token);

    @Headers({"Accept:application/json"})
    @GET("park/inactive")
    Call<ActiveAndInactiveParkingSpaceAllResponse> getAllInactiveParkingSpaces(@Header("Authorization") String token);

    @Headers({"Accept:application/json"})
    @GET("park/all")
    Call<PageParkingSpaceAllResponse> getAllParkingSpacesByPage(@Header("Authorization") String token);

    //TODO Response is incorrect but API works
    @Headers({"Accept:application/json","Content-Type:application/json"})
    @PATCH("park/{id}")
    Call<Vehicle> updateParkingSpaceDetails(@Header("Authorization") String token, @Path("id") int id, @Query("phone") String phone, @Query("status") String status);



    //ParkingSpace Booking

    @Headers({"Accept:application/json"})
    @GET("park/history/{user_id}")
    Call<ParkingSpaceHistoryResponse> getParkingSpaceBookedHistory(@Header("Authorization") String token, @Path("user_id") int user_id);

    @Headers({"Accept:application/json"})
    @POST("park/book/{car_park_id}")
    Call<ParkingSpaceDataResponse<UserPackedSpace>> scheduleParkingSpace(@Header("Authorization") String token, @Path("car_park_id") int booking_id, @Body BookingSchedule bookingSchedule);

    @Headers({"Accept:application/json"})
    @POST("park/book/{booking_id}")
    Call<ParkingSpaceDataResponse<UserPackedSpace>> editParkingSpaceSchedule(@Header("Authorization") String token, @Path("booking_id") int booking_id, @Query("check_out") String check_out);



    //Vehicles
    @Headers({"Accept:application/json"})
    @GET("vehicles")
    Call<BaseDataResponse<List<Vehicle>>> getAllVehicles(@Header("Authorization") String token);

    @Headers({"Accept:application/json","Content-Type:application/json"})
    @POST("vehicles")
    Call<BaseDataResponse<Vehicle>> addNewVehicle(@Header("Authorization") String token, @Query("plate_number") String plate_number, @Query("make_model") String make_model, @Query("main_ride") boolean main_ride);

    @Headers({"Accept:application/json","Content-Type:application/json"})
    @PUT("vehicles/{id}")
    Call<BaseDataResponse<Vehicle>> editVehicle(@Header("Authorization") String token, @Path("id") int id, @Query("plate_number") String plate_number, @Query("make_model") String make_model);


    @Headers({"Accept: ","Content-Type:text/plain"})
    @DELETE("vehicles/{id}")
    Call<Void> deleteVehicle(@Header("Authorization") String token, @Path("id") int id);


    //Space Booking

    //TODO Response is incorrect
    @Headers({"Accept:application/json"})
    @GET("vehicles")
    Call<List<Vehicle>> bookASpace(@Header("Authorization") String token);

}
