package com.shaikds.togather.repository;

import java.util.ArrayList;
import java.util.List;

public class CategoriesRepo {
    List<String> categoryList = new ArrayList<>();
    OnCategoryInterface onCategoryInterface;

    public CategoriesRepo(OnCategoryInterface onProductInterface) {
        this.onCategoryInterface = onProductInterface;
        //TODO : add all the categories to firebase so the repo communicate firebase to get the list.
    }

    public void getAllCategories() {
        categoryList.clear();
        categoryList.add("הכל");
        categoryList.add("אוכל");
        categoryList.add("ביגוד והנעלה");
        categoryList.add("בריאות וטיפוח");
        categoryList.add("בידור ופנאי");
        categoryList.add("חבילות שי");
        categoryList.add("חיות");
        categoryList.add("כרטיסי מתנה");
        categoryList.add("מוצרי חשמל ואלקטרוניקה");
        categoryList.add("לבית");
        categoryList.add("לגינה");
        categoryList.add("לימודים");
        categoryList.add("לילד ולתינוק");
        categoryList.add("ספורט");
        categoryList.add("תוכנות ומשחקים");
        categoryList.add("שירות");
        categoryList.add("אחר");
        onCategoryInterface.Categories(categoryList);
    }

    public interface OnCategoryInterface {
        void Categories(List<String> categories);
    }
}

