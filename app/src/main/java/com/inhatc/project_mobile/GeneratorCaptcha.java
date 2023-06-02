package com.inhatc.project_mobile;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

public class GeneratorCaptcha {

    private static final int WIDTH = 290; // Captcha 이미지의 너비
    private static final int HEIGHT = 100; // Captcha 이미지의 높이
    private static final int CODE_LENGTH = 6; // Captcha 코드 길이
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"; // 사용될 문자

    private static String captchaCode;
    private static Bitmap captchaImage;

    public static String getCaptchaCode(){
        return captchaCode;
    }
    public static Bitmap getCaptchaImage(){
        return captchaImage;
    }

    public static void resetCaptcha(){
        captchaCode = generateCaptchaCode(CODE_LENGTH);
        captchaImage = generateCaptchaImage(captchaCode);

    }

    private static String generateCaptchaCode(int length) {
        Random random = new Random();
        StringBuilder captchaCode = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            captchaCode.append(CHARACTERS.charAt(randomIndex));
        }
        return captchaCode.toString();
    }

    private static Bitmap generateCaptchaImage(String captchaCode) {
        Bitmap image = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        Paint paint = new Paint();

        // 배경 색상 설정
        canvas.drawColor(Color.WHITE);

        // 텍스트 색상 및 폰트 설정
        paint.setColor(Color.BLACK);
        paint.setTextSize(50);
        paint.setAntiAlias(true);

        // Captcha 코드 그리기
        canvas.drawText(captchaCode, 60, 65, paint);

        // 노이즈 추가
        Random random = new Random();
        for (int i = 0; i < 220; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            int width = 4 + random.nextInt(4);
            int height = 4 + random.nextInt(4);
            paint.setColor(getRandomColor());
            canvas.drawRect(x, y, x + width, y + height, paint);
        }

        return image;
    }

    private static int getRandomColor() {
        Random random = new Random();
        return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }
}

