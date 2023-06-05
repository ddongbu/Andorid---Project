package com.inhatc.project_mobile.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.inhatc.project_mobile.GeneratorCaptcha;
import com.inhatc.project_mobile.R;
import com.inhatc.project_mobile.User;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView signCaptchaImageView;
    private ImageButton btnSignUpReset;
    private Button btnSignUp;
    private EditText edtSignUpName;
    private EditText edtSignUpEmail;
    private EditText edtSignUpPwd;
    private EditText edtSignUpCode;
    private FirebaseAuth auth;
    private FirebaseDatabase mFirebase;
    private DatabaseReference mDatabase = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signCaptchaImageView = findViewById(R.id.signCaptchaImageView);
        GeneratorCaptcha.resetCaptcha();
        signCaptchaImageView.setImageBitmap(GeneratorCaptcha.getCaptchaImage());

        btnSignUpReset = findViewById(R.id.btnSignUpReset);
        btnSignUpReset.setOnClickListener(this);

        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(this);

        edtSignUpPwd = findViewById(R.id.edtSignUpPwd);
        edtSignUpEmail = findViewById(R.id.edtSignUpEmail);
        edtSignUpCode = findViewById(R.id.edtSignUpCode);
        edtSignUpName = findViewById(R.id.edtSignUpName);

        mFirebase = FirebaseDatabase.getInstance();
        mDatabase = mFirebase.getReference();
    }


    public void onClick(View v){
        if(v == btnSignUp){
            //빈값 처리
            if(edtIsEmpty(edtSignUpName)) return;
            if(edtIsEmpty(edtSignUpEmail)) return;
            if(edtIsEmpty(edtSignUpPwd)) return;
            if(edtIsEmpty(edtSignUpCode)) return;

            String name = edtSignUpName.getText().toString();
            String password = edtSignUpPwd.getText().toString();
            String email = edtSignUpEmail.getText().toString();
            String code = edtSignUpCode.getText().toString();

            // 비밀번호가 6글자 미만일 경우
            if (password.length() < 6) {
                Toast.makeText(getApplicationContext(), "비밀번호는 6글자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

//            //캡챠 코드인증
            if (!code.equals(GeneratorCaptcha.getCaptchaCode())){
                GeneratorCaptcha.resetCaptcha();
                signCaptchaImageView.setImageBitmap(GeneratorCaptcha.getCaptchaImage());
                Toast.makeText(getApplicationContext(), "Captcha 코드 에러!", Toast.LENGTH_SHORT).show();
                return;
            }

            signUpWithEmailAndPassword(name, email, password);
        }
        if(v == btnSignUpReset){
            edtSignUpCode.setText("");
            edtSignUpCode.requestFocus();
            GeneratorCaptcha.resetCaptcha();
            signCaptchaImageView.setImageBitmap(GeneratorCaptcha.getCaptchaImage());
        }
    }

    public void signUpWithEmailAndPassword(String name, String email, String password){
        auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 회원가입 성공

                            // 해당 회원의 uid발급
                            final String uid = task.getResult().getUser().getUid();

                            // RealTime Database 회원가입 삽입요청
                            User user = new User(name, email);
                            signupDB(user, uid);


                            Toast.makeText(getApplicationContext(), email+"님 환영홥니다.", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            // 회원가입 실패
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                // 이미 동일한 이메일로 가입된 계정이 있음
                                Toast.makeText(getApplicationContext(), "ERROR: 이미 가입된 이메일입니다.", Toast.LENGTH_LONG).show();
                            }else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                                // 이메일 형식이 유효하지 않은 경우
                                Toast.makeText(getApplicationContext(), "ERROR: 올바른 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show();
                            }else {
                                // 기타 회원가입 실패 사유 처리
                                Toast.makeText(getApplicationContext(), "ERROR: 관리자에게 문의하세요.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
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
    public boolean edtIsEmpty(EditText userValue){
        if(userValue.getText().toString().isEmpty()){
            userValue.requestFocus();
            userValue.setHint("비어있음!");
            return true;
        }
        return false;
    }
}