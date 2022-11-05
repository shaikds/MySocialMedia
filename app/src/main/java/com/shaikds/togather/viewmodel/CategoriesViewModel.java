package com.shaikds.togather.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.shaikds.togather.repository.CategoriesRepo;
import java.util.List;

public class CategoriesViewModel extends AndroidViewModel implements CategoriesRepo.OnCategoryInterface {
    MutableLiveData<List<String>> ldCategoriesName = new MutableLiveData<>();
    CategoriesRepo categoriesRepo = new CategoriesRepo(this);
    private static String TAG = "CategoriesViewModel";

    public CategoriesViewModel(@NonNull Application application) {
        super(application);
        categoriesRepo.getAllCategories();


    }

    public MutableLiveData<List<String>> gettAllCategoriesName() { return ldCategoriesName;}

    @Override
    public void Categories(List<String> categories) {
        ldCategoriesName.setValue(categories);
        Log.d(TAG, "Categories: Categories Interface");
    }
}
