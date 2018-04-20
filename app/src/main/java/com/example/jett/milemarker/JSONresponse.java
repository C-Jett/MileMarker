package com.example.jett.milemarker;

import java.util.List;

/**
 * List for snapped coordinates along the road.
 * Saves longitude and latitude data as string for GSON.
 */
public class JSONresponse {
    List<snappedPoints> snappedPoints;
    class snappedPoints {
        Location location;
        Integer originalIndex;
        String placeId;

        @Override
        public String toString(){
            return this.location.toString();
        }
    }
    class Location {
        Float latitude;
        Float longitude;

        @Override
        public String toString() {
            return this.latitude + ", " + this.longitude;
        }
    }
}

/**
 * Similar to above. Used by Google to determine correct name and distances
 * that has been travelled on the polyline.
 */
class DistanceResponse{
    List<String> destination_addresses;
    List<String> origin_addresses;
    List<row> rows;
    String status;

    class row {
        List<element> elements;
    }

    class element {
        distance distance;
        duration duration;
    }

    class distance {
        String text;
        Integer value;
    }

    class duration {
        String text;
        Integer value;
    }
}
