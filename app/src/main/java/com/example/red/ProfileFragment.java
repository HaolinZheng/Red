package com.example.red;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.FirebaseStorage;

import org.checkerframework.common.subtyping.qual.Bottom;

import java.util.UUID;

public class ProfileFragment extends Fragment {

    ImageView photoImageView;
    EditText displayNameTextView;
    TextView emailTextView;
    Button confirmarButtonView;
    public AppViewModel appViewModel;
    String mediaTipo, userPhoto, userName, mediaUri;
    FirebaseAuth mAuth;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @SuppressLint("SuspiciousIndentation")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        photoImageView = view.findViewById(R.id.photoImageView);
        displayNameTextView = view.findViewById(R.id.displayNameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        confirmarButtonView = view.findViewById(R.id.confirmarButtonView);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference user = FirebaseFirestore.getInstance().collection("users").document(firebaseUser.getUid());
        user.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User userUser = documentSnapshot.toObject(User.class);
                displayNameTextView.setText(userUser.getName());
                emailTextView.setText(firebaseUser.getEmail().toString());
                Glide.with(requireView()).load(userUser.getFoto()).circleCrop().into(photoImageView);
            }
        });

        photoImageView.setOnClickListener(v -> seleccionarImagen());
        appViewModel.mediaSeleccionado.observe(getViewLifecycleOwner(), media -> {
            this.mediaUri = media.uri.toString();
            this.mediaTipo = media.tipo;
            Glide.with(this).load(media.uri).into(photoImageView);
        });
        displayNameTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            botonGuardar();
            }
        });
    }
    private void botonGuardar(){
        confirmarButtonView.setEnabled(true);
        confirmarButtonView.setTextColor(Color.WHITE);
        confirmarButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardar();
            }
        });
    }
    private final ActivityResultLauncher<String> galeria =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                Glide.with(this).load(uri).circleCrop().into(photoImageView);
                this.mediaUri = uri.toString();
                botonGuardar();
            });
    private void seleccionarImagen() {
        mediaTipo = "image";
        galeria.launch("image/*");
    }
    private void guardar() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference user = FirebaseFirestore.getInstance().collection("users").document(firebaseUser.getUid());
        user.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User userUser = documentSnapshot.toObject(User.class);
                if(mediaUri != null && !mediaUri.equals(userUser.getFoto())){
                    pujaIguardarEnFirestore();
                }
                User user = new User(mAuth.getUid(), displayNameTextView.getText().toString(), userUser.getFoto());
                FirebaseFirestore.getInstance().collection("users").document(user.getUid()).set(user);
                confirmarButtonView.setTextColor(Color.RED);
                confirmarButtonView.setEnabled(false);
            }
        });
    }
    private void pujaIguardarEnFirestore() {
        FirebaseStorage.getInstance().getReference(mediaTipo + "/" +
                        UUID.randomUUID())
                .putFile(Uri.parse(mediaUri)) .continueWithTask(task ->
                        task.getResult().getStorage().getDownloadUrl())
                .addOnSuccessListener(url -> selectPhoto(url.toString()));
    }
    private void selectPhoto(String uri){
        mediaUri = uri;
    }
}