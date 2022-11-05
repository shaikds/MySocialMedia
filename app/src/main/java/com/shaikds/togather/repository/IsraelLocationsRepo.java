package com.shaikds.togather.repository;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

public class IsraelLocationsRepo {
    List<String> stringList = new ArrayList<>();

    public IsraelLocationsRepo() {
    }

    public List<String> readFromFile(Context context) {
        try {

            InputStream inputStream = context.getAssets().open("locations.txt");
            //InputStream inputStream = context.openFileInput("israel_locations.txt");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringList.add(receiveString);
                }
                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            Log.e("location repo", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("location repo", "Can not read file: " + e.toString());
        }


        return stringList;

    }

    public String[] castToString(List<String> stringList) {
        String[] locationList = new String[stringList.size()];
        for (int i = 0; i < stringList.size(); i++) {
            locationList[i] = stringList.get(i);

        }

        return locationList;


    }

    public List<String> readFromFile() {

        stringList.clear();
        try {
            File file = new File("locations");
            Scanner readFile = new Scanner(file);
            StringTokenizer token = null;

            while (readFile.hasNextLine()) {
                token = new StringTokenizer(readFile.nextLine(), ",");
                String s = token.nextToken();
                stringList.add(s);
            }
        } catch (FileNotFoundException e) {
            Log.e("location repo", "File not found: " + e.toString());
        }
        return stringList;
    }
}
