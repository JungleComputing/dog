package ibis.deploy.android;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import android.util.Log;

public class AndroidAppender extends AppenderSkeleton {

    private String tag;

    public AndroidAppender(String tag) {
        super();
        this.tag = tag;
        Log.i(tag, "Android Appender Initialized.");
    }

    @Override
    protected void append(LoggingEvent event) {
        switch (event.getLevel().toInt()) {
        case Level.ALL_INT:
        case Level.TRACE_INT:
            // if (Log.isLoggable(tag, Log.VERBOSE)) {
            if (event.getThrowableInformation().getThrowable() != null) {
                Log.v(tag, event.getLocationInformation().fullInfo + ": "
                        + event.getRenderedMessage(), event
                        .getThrowableInformation().getThrowable());
            } else {
                Log.v(tag, event.getLocationInformation().fullInfo + ": "
                        + event.getRenderedMessage());
            }
            // }
            break;
        case Level.DEBUG_INT:
            // if (Log.isLoggable(tag, Log.DEBUG)) {
            if (event.getThrowableInformation() != null) {
                Log.d(tag, event.getLocationInformation().fullInfo + ": "
                        + event.getRenderedMessage(), event
                        .getThrowableInformation().getThrowable());
            } else {
                Log.d(tag, event.getLocationInformation().fullInfo + ": "
                        + event.getRenderedMessage());
            }
            // }
            break;
        case Level.ERROR_INT:
        case Level.FATAL_INT:
            // if (Log.isLoggable(tag, Log.ERROR)) {
            if (event.getThrowableInformation() != null) {
                Log.e(tag, event.getLocationInformation().fullInfo + ": "
                        + event.getRenderedMessage(), event
                        .getThrowableInformation().getThrowable());
            } else {
                Log.e(tag, event.getLocationInformation().fullInfo + ": "
                        + event.getRenderedMessage());
            }
            // }
            break;
        case Level.INFO_INT:
            // if (Log.isLoggable(tag, Log.INFO)) {
            if (event.getThrowableInformation() != null) {
                Log.i(tag, event.getLocationInformation().fullInfo + ": "
                        + event.getRenderedMessage(), event
                        .getThrowableInformation().getThrowable());
            } else {
                Log.i(tag, event.getLocationInformation().fullInfo + ": "
                        + event.getRenderedMessage());
            }
            // }
            break;
        case Level.WARN_INT:
            // if (Log.isLoggable(tag, Log.WARN)) {
            if (event.getThrowableInformation() != null) {
                Log.w(tag, event.getLocationInformation().fullInfo + ": "
                        + event.getRenderedMessage(), event
                        .getThrowableInformation().getThrowable());
            } else {
                Log.w(tag, event.getLocationInformation().fullInfo + ": "
                        + event.getRenderedMessage());
            }
            // }
            break;
        case Level.OFF_INT:
        }
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean requiresLayout() {
        // TODO Auto-generated method stub
        return false;
    }

}
