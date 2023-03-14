//package ro.android.thesis;
//
//public class RequestThread extends Thread {
//
//    private String username;
//    private String password;
//
//    public RequestThread(String username, String password) {
//        this.username = username;
//        this.password = password;
//    }
//
//    @Override
//    public void run() {
//        try {
//            // Build request
//            RequestBody requestBody = new FormBody.Builder()
//                    .add("username", username)
//                    .add("password", password)
//                    .build();
//            Request request = new Request.Builder()
//                    .url("http://your-api-url.com/login")
//                    .post(requestBody)
//                    .build();
//
//            // Make request and get response
//            Response response = client.newCall(request).execute();
//
//            // Parse response and update UI
//            final String responseBody = response.body().string();
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    // Update UI with response body
//                    Toast.makeText(getActivity(), responseBody, Toast.LENGTH_SHORT).show();
//                }
//            });
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
//}