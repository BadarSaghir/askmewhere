package org.blind.help.object.detection.distance;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class DistanceFinder {
    //create array list of DistanceModel objects person, car, microwave, cell phone, apple,cat,mug, platter, watch, AND remote
    public  static ArrayList<String> _class = new ArrayList<String>(){
        {

            add("person");
            add("car");
            add("microwave oven");
            add("mobile phone");
            add("apple");
            add("cat");
            add("mug");
            add("bowl");
            add("toothbrush");
            add("remote control");
            //new
            add("toilet");
            add("spoon");
            add("chair");
            add("bottle");
            add("suitcase");
            add("toaster");
            add("fork");
            add("table");
            add("laptop");
            add("sink");

        }
    };
    @NonNull
    public static String getDistanceInInches(String objectName, float widthInPixels){
        if(DistanceFinder._class.contains(objectName)) {
            // result upto 2 floating point precision
            switch (objectName){
                case "person":// average 15 inches
                    return String.format("%.02f",new DistanceModel("person", 147.606f, 15.0f, 227.5f).calcuteDistanceFromCameraInInches(widthInPixels)/10 )+ " inches";
                case "car":
                    return String.format("%.02f",new DistanceModel("car", 147.606f, 69.6f, 1049.5f).calcuteDistanceFromCameraInInches(widthInPixels)/10 )+ " inches";
                case "microwave oven":
                    return String.format("%.02f",new DistanceModel("microwave", 147.606f, 30.0f, 452.5f).calcuteDistanceFromCameraInInches(widthInPixels)/10) + " inches";
                case "mobile phone":// working pretty good.
                    return String.format("%.02f",new DistanceModel("Mobile phone", 147.606f, 5.5f, 84.0f).calcuteDistanceFromCameraInInches(widthInPixels)/10 )+ " inches";
                case "apple":
                    return String.format("%.02f",new DistanceModel("apple", 147.606f, 3.5f, 52.5f).calcuteDistanceFromCameraInInches(widthInPixels)/10 )+ " inches";
                case "cat":
                    return String.format("%.02f",new DistanceModel("cat", 147.606f, 2.75591f, 42.5f).calcuteDistanceFromCameraInInches(widthInPixels)/10) + " inches";
                case "mug":
                    return String.format("%.02f",new DistanceModel("mug", 147.606f, 2.99213f, 44.5f).calcuteDistanceFromCameraInInches(widthInPixels)/10) + " inches";
                case "bowl":
                    return String.format("%.02f",new DistanceModel("bowl", 147.606f, 9.75f, 147.5f).calcuteDistanceFromCameraInInches(widthInPixels)/10) + " inches";
                case "toothbrush":
                    return String.format("%.02f",new DistanceModel("platter", 147.606f, 9.75f, 147.5f).calcuteDistanceFromCameraInInches(widthInPixels)/10) + " inches";
                case "sink":
                    return String.format("%.02f",new DistanceModel("watch", 147.606f, 1.65354f, 25.5f).calcuteDistanceFromCameraInInches(widthInPixels)/10) + " inches";
                case "remote control":
                    return String.format("%.02f",new DistanceModel("remote", 387.606f, 1.43f, 21.5f).calcuteDistanceFromCameraInInches(widthInPixels)/10) + " inches";
                    //changes needed
                case "spoon":
                    return String.format("%.02f",new DistanceModel("spoon", 387.606f, 1.43f, 21.5f).calcuteDistanceFromCameraInInches(widthInPixels)/10) + " inches";

                case "chair":
                    return String.format("%.02f",new DistanceModel("chair", 387.606f, 1.43f, 21.5f).calcuteDistanceFromCameraInInches(widthInPixels)/10) + " inches";

                case "toilet":
                    return String.format("%.02f",new DistanceModel("toilet", 387.606f, 1.43f, 21.5f).calcuteDistanceFromCameraInInches(widthInPixels)/10) + " inches";

                case "bottle":
                    return String.format("%.02f",new DistanceModel("bottle", 387.606f, 1.43f, 21.5f).calcuteDistanceFromCameraInInches(widthInPixels)/10) + " inches";

                case "suitcase":
                    return String.format("%.02f",new DistanceModel("suitcase", 387.606f, 1.43f, 21.5f).calcuteDistanceFromCameraInInches(widthInPixels)/10) + " inches";

                case "fork":
                    return String.format("%.02f",new DistanceModel("fork", 387.606f, 1.43f, 21.5f).calcuteDistanceFromCameraInInches(widthInPixels)/10) + " inches";

                case "toaster":
                    return String.format("%.02f",new DistanceModel("toaster", 387.606f, 1.43f, 21.5f).calcuteDistanceFromCameraInInches(widthInPixels)/10) + " inches";

                case "table":
                    return String.format("%.02f",new DistanceModel("table", 387.606f, 1.43f, 21.5f).calcuteDistanceFromCameraInInches(widthInPixels)/10) + " inches";

                case "laptop":
                    return String.format("%.02f",new DistanceModel("laptop", 387.606f, 1.43f, 21.5f).calcuteDistanceFromCameraInInches(widthInPixels)/10) + " inches";


            }

        }
        return " ";
    }

    @NonNull
    public static String getDistanceInInchesUsingFocal(String objectName, float widthInPixels){
        if(DistanceFinder._class.contains(objectName)) {
            // result upto 2 floating point precision
            switch (objectName){
                case "person":// average 15 inches
                    return String.format("%.02f",new DistanceModel("person", 147.606f, 15.0f, 227.5f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels) )+ " inches";
                case "car":
                    return String.format("%.02f",new DistanceModel("car", 147.606f, 69.6f, 1049.5f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels))+ " inches";
                case "microwave oven":
                    return String.format("%.02f",new DistanceModel("microwave", 147.606f, 30.0f, 452.5f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels)) + " inches";
                case "mobile phone":// working pretty good.
                    return String.format("%.02f",new DistanceModel("Mobile phone", 147.606f, 5.5f, 84.0f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels) )+ " inches";
                case "apple":
                    return String.format("%.02f",new DistanceModel("apple", 147.606f, 3.5f, 52.5f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels))+ " inches";
                case "cat":
                    return String.format("%.02f",new DistanceModel("cat", 147.606f, 2.75591f, 42.5f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels)) + " inches";
                case "mug":
                    return String.format("%.02f",new DistanceModel("mug", 147.606f, 2.99213f, 44.5f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels)) + " inches";
                case "bowl":
                    return String.format("%.02f",new DistanceModel("bowl", 147.606f, 9.75f, 147.5f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels)) + " inches";
                case "platter":
                    return String.format("%.02f",new DistanceModel("platter", 147.606f, 9.75f, 147.5f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels)) + " inches";
                case "watch":
                    return String.format("%.02f",new DistanceModel("watch", 147.606f, 1.65354f, 25.5f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels)) + " inches";
                case "remote control":
                    return String.format("%.02f",new DistanceModel("remote", 387.606f, 1.43f, 21.5f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels)) + " inches";
                //changes needed
                case "spoon":
                    return String.format("%.02f",new DistanceModel("spoon", 387.606f, 1.43f, 21.5f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels)) + " inches";

                case "chair":
                    return String.format("%.02f",new DistanceModel("chair", 387.606f, 1.43f, 21.5f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels)) + " inches";

                case "toilet":
                    return String.format("%.02f",new DistanceModel("toilet", 387.606f, 1.43f, 21.5f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels)) + " inches";

                case "bottle":
                    return String.format("%.02f",new DistanceModel("bottle", 387.606f, 1.43f, 21.5f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels)) + " inches";

                case "suitcase":
                    return String.format("%.02f",new DistanceModel("suitcase", 387.606f, 1.43f, 21.5f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels)) + " inches";

                case "fork":
                    return String.format("%.02f",new DistanceModel("fork", 387.606f, 1.43f, 21.5f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels)) + " inches";

                case "toaster":
                    return String.format("%.02f",new DistanceModel("toaster", 387.606f, 1.43f, 21.5f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels)) + " inches";

                case "table":
                    return String.format("%.02f",new DistanceModel("table", 387.606f, 1.43f, 21.5f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels)) + " inches";

                case "laptop":
                    return String.format("%.02f",new DistanceModel("laptop", 387.606f, 18.1f, 21.5f).calcuteDistanceFromCameraInInchesUsingFocal(widthInPixels)) + " inches";


            }

        }
        return " ";
    }



}
