package com.bpurohit.aphrodite.ui.my_profile;

import static com.bpurohit.aphrodite.R.id.edtDialogUsername;
import static com.bpurohit.aphrodite.constant.AllConstant.STORAGE_REQUEST_CODE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bpurohit.aphrodite.R;
import com.bpurohit.aphrodite.constant.AllConstant;
import com.bpurohit.aphrodite.databinding.FragmentMyProfileBinding;
import com.bpurohit.aphrodite.permission.AppPermissions;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MyProfileFragment extends Fragment {

    private FragmentMyProfileBinding fragmentMyProfileBinding;
    private FirebaseAuth firebaseAuth;
    private AppPermissions appPermissions;
    private Uri imageUri;
    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentMyProfileBinding = FragmentMyProfileBinding.inflate(inflater, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        appPermissions = new AppPermissions();

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult activityResult) {
                        if (activityResult.getResultCode() == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                            CropImage.ActivityResult result =  CropImage.getActivityResult(activityResult.getData());

                            if (activityResult.getResultCode() == Activity.RESULT_OK){
                                imageUri = result.getUri();
                                uploadImage(imageUri);
                            }
                            else if (activityResult.getResultCode() == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                                Exception exception = result.getError();
                                Toast.makeText(getContext(), "" +exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        fragmentMyProfileBinding.imgCamera.setOnClickListener(camera->{
            if (appPermissions.isStorageOk(getContext())){
                pickImage();
            }
            else {
                ActivityCompat.requestPermissions((Activity) getContext(), new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_REQUEST_CODE);



            }
        });

        fragmentMyProfileBinding.txtUsername.setOnClickListener(username-> {
            usernameDialog();
        });

        return fragmentMyProfileBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentMyProfileBinding.txtEmail.setText(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail());
        fragmentMyProfileBinding.txtUsername.setText(firebaseAuth.getCurrentUser().getDisplayName());

        Glide.with(requireContext()).load(firebaseAuth.getCurrentUser()).into(fragmentMyProfileBinding.imgProfile);

        if (firebaseAuth.getCurrentUser().isEmailVerified()){
            fragmentMyProfileBinding.txtVerifyEmail.setVisibility(View.GONE);
        }
        else {
            fragmentMyProfileBinding.txtVerifyEmail.setVisibility(View.VISIBLE);
        }

        fragmentMyProfileBinding.txtVerifyEmail.setOnClickListener(verify-> firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Toast.makeText(getContext(), "Mail sent verify the email", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getContext(), "" +task.getException(), Toast.LENGTH_SHORT).show();
                Log.d("TAG", "onComplete: profile email " +task.getException());

            }
        }));

    }

    private void pickImage() {

        CropImage.activity()
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(getContext(), this);
    }

    private void uploadImage(Uri imageUri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child(firebaseAuth.getUid()+AllConstant.IMAGE_PATH).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> image = taskSnapshot.getStorage().getDownloadUrl();
                image.addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            String url = task.getResult().toString();

                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(Uri.parse(url))
                                    .build();
                            firebaseAuth.getCurrentUser().updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> profile) {
                                    if (profile.isSuccessful()) {
                                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("image", url);
                                        databaseReference.child(firebaseAuth.getUid()).updateChildren(map);
                                        Glide.with(requireContext()).load(url).into(fragmentMyProfileBinding.imgProfile);
                                        Toast.makeText(getContext(), "Image Updated", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Log.d("TAG", "Profile: "+profile.getException());
                                        Toast.makeText(getContext(), "Profile: "+profile.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                        else {
                            Toast.makeText(getContext(), "" +task.getException(), Toast.LENGTH_SHORT).show();
                            Log.d("TAG", "onComplete: image url "+task.getException());
                        }

                    }
                });
            }
        });
    }

    private void usernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.username_dialog_layout, null, false);
        builder.setView(view);
        TextInputEditText edtUsername = view.findViewById(edtDialogUsername);
        builder.setTitle("Edit Username");
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String username = edtUsername.getText().toString().trim();

                if (!username.isEmpty()){
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build();
                    firebaseAuth.getCurrentUser().updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                                Map<String, Object> map = new HashMap<>();
                                map.put("username", username);
                                databaseReference.child(firebaseAuth.getUid()).updateChildren(map);

                                fragmentMyProfileBinding.txtUsername.setText(username);
                                Toast.makeText(getContext(), "Username is updated", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Log.d("Tag", "onComplete: "+task.getException());
                                Toast.makeText(getContext(), ""+task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(getContext(), "Username is required", Toast.LENGTH_SHORT).show();
                }
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create().show();
    }

}
