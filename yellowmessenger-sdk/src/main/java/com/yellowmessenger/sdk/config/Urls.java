package com.yellowmessenger.sdk.config;

public class Urls {
    public static final String API_HOST = "https://www.yellowmessenger.com";

    public static final String PUT_USER_PREFERENCE = "/api/putUserPreference";
    public static final String CONNECT_SOCIAL = "/api/connectSocial";
    public static final String SOCIAL_ACCOUNT = "/api/socialAccount";
    public static final String GET_USER_PREFERENCE = "/api/fetchUserPreference";
    public static final String GET_CITIES = "/api/listCities";
    public static final String SIGN_UP_URL = "/acc/";
    public static final String FACEBOOK_SIGN_UP_URL = "/acc/facebook_login";
    public static final String GOOGLE_SIGN_UP_URL = "/acc/google_login";
    public static final String MOBILE_SIGN_UP_URL = "/acc/mobile_login";
    public static final String ACC_DETAILS_URL = "/api/userAccDetails";
    public static final String SETTINGS_URL = "/api/listSettings";
    public static final String USER_DETAILS_URL = "/api/userDetails";
    public static final String LOGIN_URL = "/api/login";
    public static final String LOGOUT_URL = "/api/logout";
    public static final String CATALOG_URL = "/api/catalog";
    public static final String CASHBACK_URL = "/cashback";
    public static final String HOME_IMAGE_URL = "https://s3-us-west-2.amazonaws.com/yellowmessenger/home/";


    public static final String BUSINESS_ENQUIRY_URL = "/api/businessEnquiry";

    public static final String TEXT_SEARCH_URL = "/api/textSearch";
    public static final String SEARCH_URL = "/api/search";
    public static final String DEALS_URL = "/api/dealsV2";
    public static final String DEAL_SOURCE_URL = "/api/dealSource";


    public static final String DETAILS_URL = "/api/details";
    public static final String API_KEY = "AIzaSyDLCODKWWhVsFzYim7eSf-Q5Xpl10S-S4A";

    public static final String PHOTO_URL = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=100&key="+API_KEY+"&photoreference=";

    public static final String BG_PHOTO_URL = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=720&key="+API_KEY+"&photoreference=";

    public static final String THUMBNAIL_PHOTO_URL = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=100&key="+API_KEY+"&photoreference=";
    public static final String BRANDS_URL = "/api/brands";
    public static final String FAQS_URL = "/api/faqs";
    public static final String DEALS_V2_URL = "/api/dealsV2";

    //Application ID: 56f90c335311d2db527b21dd
    // Distribution id: 55db14886ae151de6f77d674

    //https://maps.googleapis.com/maps/api/place/photo?maxwidth=800&key=AIzaSyDLCODKWWhVsFzYim7eSf-Q5Xpl10S-S4A&photoreference=CnRnAAAA9kZ2aIUxJ1G9nz5oZ1vs09ZKM2I8LMjLv_RNn4cMjw9Zo2snQAtFV8EOtEXe7fi5lXe3pTcYZH44p2qh1sNKWR9Bjx8DDRiTPxOqxIGJDD_bHEfXmJf_ToiU1QUD5BKsm3BvP_1gdzu23s8TzAX4_RIQlfkGazAAABRZ80eqyTqT0xoUNiEMSgYAFLnXIPINS7VIyZFibEY
}


//db.userPreference.insert( {"_id" : NumberLong(965), "preference" : "BRANDS_INDIA","username":"current","value":"{brands:[{\"name\":\"Cafe Coffee Day\",\"placeId\":\"ChIJLbvFWncWrjsRK3OoAW6k2us\",\"username\":\"ChIJLbvFWncWrjsRK3OoAW6k2us\",\"category\":\"CAFE\"},{\"name\":\"Starbucks\",\"placeId\":\"ChIJZxVOpOnO5zsRC8MAF5NePGY\",\"username\":\"ChIJZxVOpOnO5zsRC8MAF5NePGY\",\"category\":\"CAFE\"},{\"name\":\"Costa Coffeee\",\"placeId\":\"ChIJ472NIPa25zsRGUbRjKJbjGg\",\"username\":\"ChIJ472NIPa25zsRGUbRjKJbjGg\",\"category\":\"CAFE\"},{\"name\":\"Airtel\",\"placeId\":\"ChIJI65kHV0ZDTkRPAs36-yOw94\",\"username\":\"ChIJI65kHV0ZDTkRPAs36-yOw94\",\"category\":\"TELECOM\"},{\"name\":\"Vodafone\",\"placeId\":\"ChIJC5uyvOnO5zsRsie_Y8NeU14\",\"username\":\"ChIJC5uyvOnO5zsRsie_Y8NeU14\",\"category\":\"TELECOM\"}]"}})