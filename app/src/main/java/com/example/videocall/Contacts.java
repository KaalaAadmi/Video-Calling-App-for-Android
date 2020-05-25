package com.example.videocall;

public class Contacts {
    String Name, Image,Status, UID;

    public Contacts() {
    }

    public Contacts(String name, String image, String status, String UID) {
        Name = name;
        Image = image;
        Status = status;
        this.UID = UID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }
}
