package com.abcew.stickerview.utils;

import android.location.Location;
import android.media.ExifInterface;
import android.os.Build;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by laputan on 2017/1/11.
 */

public class ExifUtils {

    private static final String TAG = "ExifUtils";

    private static final String DATE_FORMAT = "yyyy:MM:dd";
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final String DATETIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;

    private ExifUtils() {}

    /**
     * Get the exif rotation of the image.
     * @param filename
     * @return
     */
    public static int getAngle(final String filename) {

        try {
            final ExifInterface exif = new ExifInterface(filename);
            switch (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    return 0;
            }
        } catch (IOException e) {
            return 0;
        }
    }

    private static final String TAG_GPS_DATE_STAMP = "GPSDateStamp";

    public static void save(final String filename, @Nullable final Date datetime, final int orientation, @Nullable final Boolean flash, @Nullable final Location location) throws IOException {
        final ExifInterface exif = new ExifInterface(filename);

        if (datetime != null) {
            exif.setAttribute(ExifInterface.TAG_DATETIME, new SimpleDateFormat(DATETIME_FORMAT, Locale.ENGLISH).format(datetime));
        }
        exif.setAttribute(ExifInterface.TAG_MAKE, Build.MANUFACTURER);
        exif.setAttribute(ExifInterface.TAG_MODEL, Build.MODEL);
        exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation + "");

        if (flash != null) {
            exif.setAttribute(ExifInterface.TAG_FLASH, String.valueOf(flash ? 1 : 0));
        }

        if (location != null) {
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, formatExifGpsDMS(location.getLatitude()));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, formatExifGpsDMS(location.getLongitude()));

            exif.setAttribute(TAG_GPS_DATE_STAMP, new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH).format(datetime));
        }

        exif.saveAttributes();
    }

    private static String formatExifGpsDMS(final double d) {
        final double degrees = Math.floor(d);
        final double minutes = Math.floor((d - degrees) * 60D);
        final double seconds = (d - degrees - minutes / 60D) * 3600D * 1000D;

        final String _degrees = String.valueOf((int) degrees);
        final String _minutes = String.valueOf((int) minutes);
        final String _seconds = String.valueOf((int) seconds);

        return new StringBuilder()
                .append(_degrees).append("/1,")
                .append(_minutes).append("/1,")
                .append(_seconds.substring(0, Math.min(_seconds.length(), 4))).append("/1000")
                .toString();
    }
}
