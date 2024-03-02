package com.example.goeat.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.goeat.LoginActivity;
import com.example.goeat.R;
import com.example.goeat.User;
import com.example.goeat.auth.Auth;
import com.example.goeat.auth.Validator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.apache.commons.lang3.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ProfileFragment extends Fragment {

    Button logout;
    Button changepassword;
    Auth mAuth;
    TextView username;
    TextView email;
    TextView gender;
    TextView date;
    User u;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        mAuth = Auth.getInstance();
        u = mAuth.getCurrentUser();
        username = v.findViewById(R.id.username);
        username.append(u.getUsername());
        email = v.findViewById(R.id.email);
        email.append(u.getEmail());
        gender = v.findViewById(R.id.gender);
        String gen=(u.getGender().equals("Male"))?"Nam":"Nữ";
        gender.append(gen);
        date = v.findViewById(R.id.date);
        String dateString = new SimpleDateFormat("dd/MM/yyyy").format(new Date(u.getDateCreated()));
        date.append(dateString);

        changepassword = v.findViewById(R.id.changepassword);
        changepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPasswordResetEmail();
            }
        });
        logout = v.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent=new Intent(getActivity(), LoginActivity.class);
                getActivity().finish();
                startActivity(intent);
            }
        });
        return v;
    }
    private void sendPasswordResetEmail() {
        mAuth.sendPasswordResetEmail(u.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Chúng tôi đã gửi đến email của bạn một hướng dẫn, vui lòng thực hiện đễ thay đổi", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Thất bại", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}