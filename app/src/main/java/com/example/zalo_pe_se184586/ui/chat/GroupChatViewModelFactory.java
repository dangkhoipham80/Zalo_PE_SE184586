package com.example.zalo_pe_se184586.ui.chat;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.savedstate.SavedStateRegistryOwner;

/**
 * Factory for creating GroupChatViewModel with SavedStateHandle support
 * This ensures the ViewModel can survive configuration changes and process death
 */
public class GroupChatViewModelFactory extends AbstractSavedStateViewModelFactory {

    private final Application application;

    public GroupChatViewModelFactory(@NonNull SavedStateRegistryOwner owner, @NonNull Application application) {
        super(owner, null);
        this.application = application;
    }

    @NonNull
    @Override
    protected <T extends ViewModel> T create(@NonNull String key, @NonNull Class<T> modelClass, @NonNull SavedStateHandle handle) {
        if (modelClass.isAssignableFrom(GroupChatViewModel.class)) {
            return (T) new GroupChatViewModel(application, handle);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}