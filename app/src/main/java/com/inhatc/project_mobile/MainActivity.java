package com.inhatc.project_mobile;


import static android.content.ContentValues.TAG;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.Account;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private ImageView captchaImageView;

    private FirebaseDatabase mFirebase;

    private DatabaseReference mDatabase = null;
    private EditText edtUserID;
    private EditText edtUserPwd;
    private EditText edtUserCode;
    private Button btnLogin;
    private Button btnSignUpLoad;
    private ImageButton btnReset;
    private FirebaseAuth auth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //db연결
        mFirebase = FirebaseDatabase.getInstance();
        mDatabase = mFirebase.getReference();


        //captcah 이미지 뷰
        captchaImageView = findViewById(R.id.captchaImageView);
        GeneratorCaptcha.resetCaptcha();
        captchaImageView.setImageBitmap(GeneratorCaptcha.getCaptchaImage());


        edtUserID = findViewById(R.id.edtUserID);
        edtUserPwd = findViewById(R.id.edtUserPwd);
        edtUserCode = findViewById(R.id.edtCode);

        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(this);

        btnSignUpLoad = findViewById(R.id.btnSignUpLoad);
        btnSignUpLoad.setOnClickListener(this);



        //카카오 로그인 ->

        ImageButton kakao_login_button = (ImageButton)findViewById(R.id.kakao_login_button);
        kakao_login_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(MainActivity.this)){
                    Log.e(TAG,"login진입");
                    login();
                }
                else{
                    Log.e(TAG,"accountlogin진입");
                    accountLogin();
                }
            }
        });
        //<- 카카오 로그인
    }




    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v){
        if(v == btnLogin){

            if(edtIsEmpty(edtUserID)) return;
            if(edtIsEmpty(edtUserPwd)) return;
            if(edtIsEmpty(edtUserCode)) return;

            String userID   = edtUserID.getText().toString();
            String userPwd  = edtUserPwd.getText().toString();
            String userCode = edtUserCode.getText().toString();

            // Captcha 인증 확인
            if(!userCode.equals(GeneratorCaptcha.getCaptchaCode())){
                // 값 초기화
                edtUserCode.setText("");

                // 팝업 메시지
                Toast.makeText(this,"Captche인증에 실패하였습니다.",Toast.LENGTH_SHORT).show();

                // Captcha 초기화
                GeneratorCaptcha.resetCaptcha();
                captchaImageView.setImageBitmap(GeneratorCaptcha.getCaptchaImage());

                return;
            }

            //로그인 성공로직 구현
            loginDB(userID, userPwd);

            //액티비티 전환
            Toast.makeText(this,edtUserID.getText().toString()+"님 반갑습니다.",Toast.LENGTH_SHORT).show();
        }

        if(v == btnSignUpLoad) {
            //액티비티 전환
            Intent signUpLoad = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(signUpLoad);
            //회원가입
        }

        if(v == btnReset){
            edtUserCode.setText("");
            edtUserCode.requestFocus();
            GeneratorCaptcha.resetCaptcha();
            captchaImageView.setImageBitmap(GeneratorCaptcha.getCaptchaImage());
        }
    }
    public void loginDB(String email, String password){
        auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        String uid = user.getUid();

                        Intent mainAppLoad = new Intent(MainActivity.this, AppMainActivity.class);
                        mainAppLoad.putExtra("UID", uid);
                        startActivity(mainAppLoad);

                        Toast.makeText(getApplicationContext(), email+"님 환영홥니다.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        edtUserID.setText("");
                        edtUserPwd.setText("");
                        edtUserCode.setText("");
                        edtUserID.requestFocus();
                        Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public boolean edtIsEmpty(EditText userValue){
        if(userValue.getText().toString().isEmpty()){
            userValue.requestFocus();
            userValue.setHint("비어있음!");
            return true;
        }
        return false;
    }

    //카카오 로그인 구현
    public void login(){
        String TAG = "login()";
        UserApiClient.getInstance().loginWithKakaoTalk(MainActivity.this,(oAuthToken, error) -> {
            if (error != null) {
                Log.e(TAG, "Basic Login실패", error);
            } else if (oAuthToken != null) {
                Log.i(TAG, "로그인 성공(토큰) : " + oAuthToken.getAccessToken());
            }
            return null;
        });
    }

    public void accountLogin(){
        String TAG = "accountLogin()";
        Log.e(TAG, "accountLogin 진입");
        UserApiClient.getInstance().loginWithKakaoAccount(MainActivity.this,(token, error) -> {
            Log.e(TAG,"token:" +token);
            if (error != null) {
                Log.e(TAG, "accountLogin 실패", error);
            } else if (token != null) {
                Log.i(TAG, "로그인 성공(토큰) : " + token.getAccessToken());
                getUserInfo();
            }
            return null;
        });
    }
    public void getUserInfo(){
        String TAG = "getUserInfo()";
        Log.e(TAG, "getUserInfo 실행");
        UserApiClient.getInstance().me((user, meError) -> {
            Log.e(TAG, "토큰발행 실행");
            if (meError != null) {
                Log.e(TAG, "사용자 정보 요청 실패", meError);
            } else {
                System.out.println("로그인 완료");
                Log.i(TAG, user.toString());
                Log.i(TAG, "사용자 정보 요청 성공" +
                            "\nuid:" + user.getId() +
                            "\n이름: " + user.getProperties().get("nickname") +
                            "\n이메일: " + user.getKakaoAccount().getEmail());
                    User kakaouser = new User(user.getProperties().get("nickname"), user.getKakaoAccount().getEmail());
                    signupDB(kakaouser, user.getId().toString());
                    Account user1 = user.getKakaoAccount();
                    Log.i(TAG,"user1 : "+ user1);
                    Intent appMainLoad = new Intent(MainActivity.this, AppMainActivity.class);
                    appMainLoad.putExtra("UID", user.getId().toString());
                    startActivity(appMainLoad);
                    finish();
            }
            return null;
        });
    }
    public void signupDB(User user, String uid){
        /*
            Realtime DataBase구조
            users
                uid
                    email : value
                    name : value
         */
        mDatabase.child("users").child(uid).setValue(user);
    }

    public String getKeyHash(){
        try{
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            if(packageInfo == null) return null;
            for(Signature signature: packageInfo.signatures){
                try{
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    return android.util.Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                }catch (NoSuchAlgorithmException e){
                    Log.w("getKeyHash", "Unable to get MessageDigest. signature="+signature, e);
                }
            }
        }catch(PackageManager.NameNotFoundException e){
            Log.w("getPackageInfo", "Unable to getPackageInfo");
        }
        return null;
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    //카카오 로그인 구현 종료
}
