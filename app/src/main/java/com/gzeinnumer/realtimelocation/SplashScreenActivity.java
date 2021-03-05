package com.gzeinnumer.realtimelocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.gzeinnumer.eeda.helper.FGPermission;

import rebus.permissionutils.PermissionEnum;
import rebus.permissionutils.PermissionManager;

public class SplashScreenActivity extends AppCompatActivity {

    private final PermissionEnum[] permissions = new PermissionEnum[]{
            PermissionEnum.ACCESS_COARSE_LOCATION,
            PermissionEnum.ACCESS_FINE_LOCATION,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        FGPermission.checkPermissions(this, permissions);

        checkPermissions();
    }

    private void onSuccessCheckPermitions() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }, 4000);

    }

    private void checkPermissions() {
        boolean isAllGranted = FGPermission.getPermissionResult(this, permissions);

        if (isAllGranted) {
            onSuccessCheckPermitions();
        } else {
            Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.handleResult(this, requestCode, permissions, grantResults);

        checkPermissions();
    }
}