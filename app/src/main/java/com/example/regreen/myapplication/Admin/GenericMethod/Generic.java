package com.example.regreen.myapplication.Admin.GenericMethod;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class Generic {
    private final Context context;

    public Generic(Context context) {
        this.context = context;
    }

    // Generic interface for data retrieval callback
    public interface DataCallback<T> {
        void onDataRetrieved(T data);
    }

    // Generic method to add or update an item
    public <T> void addOrUpdateItem(String pathName, boolean isUpdate, String currentName, String newName, T item) {
        DatabaseReference idReference = FirebaseDatabase.getInstance().getReference("metadata/lastID");
        DatabaseReference itemsReference = FirebaseDatabase.getInstance().getReference(pathName);

        if (isUpdate) {
            updateExistingItem(itemsReference, currentName, newName, item);
        } else {
            addNewItem(idReference, itemsReference, newName, item);
        }
    }

    // Helper method to add a new item with ID auto-increment
    private <T> void addNewItem(DatabaseReference idReference, DatabaseReference itemsReference, String name, T item) {
        idReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                Long currentID = currentData.getValue(Long.class);
                if (currentID == null) {
                    currentID = 0L; // Initialize if it doesn't exist
                }
                currentData.setValue(currentID + 1);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    itemsReference.child(name).setValue(item)
                            .addOnSuccessListener(aVoid -> Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(context, "Error adding item", Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(context, "Error creating ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Helper method to update an existing item
    private <T> void updateExistingItem(DatabaseReference itemsReference, String oldName, String newName, T item) {
        DatabaseReference oldItemRef = itemsReference.child(oldName);

        oldItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    itemsReference.child(newName).setValue(item).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (!oldName.equals(newName)) {
                                oldItemRef.removeValue().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Update cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Generic method to read an item of any type
    public <T> void readItemData(String pathName, String name, Class<T> itemType, DataCallback<T> callback) {
        DatabaseReference itemsReference = FirebaseDatabase.getInstance().getReference(pathName);

        itemsReference.child(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    T item = snapshot.getValue(itemType);
                    if (item != null) {
                        callback.onDataRetrieved(item);
                    } else {
                        Toast.makeText(context, "Failed to parse item data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Item not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error reading data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void delete(String itemId, OnOperationListener listener) {
        DatabaseReference itemsReference = FirebaseDatabase.getInstance().getReference("News"); // Adjust "News" if needed

        itemsReference.child(itemId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listener.onSuccess("Deleted successfully");
            } else {
                listener.onFailure("Failed to delete item");
            }
        }).addOnFailureListener(e -> listener.onFailure("Failed to delete item: " + e.getMessage()));
    }

    // Interface for operation callbacks
    public interface OnOperationListener {
        void onSuccess(String message);
        void onFailure(String message);
    }

    // Generic method to update an item in Firebase
    public void update(String itemId, Map<String, Object> updates, OnOperationListener listener) {
        DatabaseReference itemsReference = FirebaseDatabase.getInstance().getReference("News"); // Adjust "News" if needed

        itemsReference.child(itemId).updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listener.onSuccess("Updated successfully");
            } else {
                listener.onFailure("Failed to update item");
            }
        }).addOnFailureListener(e -> listener.onFailure("Failed to update item: " + e.getMessage()));
    }
}
