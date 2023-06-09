package com.example.groceries;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Class that holds global instances of the firebase authentication and firebase realtime database
 */
public class GlobalVariables {
    public static FirebaseAuth mAuth;
    public static FirebaseDatabase database;
}
