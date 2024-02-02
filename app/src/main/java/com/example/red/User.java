package com.example.red;

import android.net.Uri;

import com.google.firebase.database.PropertyName;

public class User {
    public String uid;
    public String name;
    public String foto;
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
    @PropertyName("name")
    public String getName() {
        return name;
    }
    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
    }
    @PropertyName("foto")
    public String getFoto() {
        return foto;
    }
    @PropertyName("foto")
    public void setFoto(String foto) {
        this.foto = foto;
    }

    public User() {}

    public User(String uid, String name, String foto) {
        this.uid = uid;
        this.name = name;
        this.foto = foto;
    }
}
