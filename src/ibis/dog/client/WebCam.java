package ibis.dog.client;

import ibis.media.imaging.Format;
import ibis.media.video.VideoDeviceDescription;
import ibis.media.video.VideoDeviceFactory;
import ibis.media.video.devices.VideoSource;

public class WebCam {

    public static final int TARGET_WIDTH = 800;
    public static final int TARGET_HEIGHT = 600;

    private static Format selectFormat(VideoDeviceDescription description)
            throws Exception {
        if (description.getFormats().length == 0) {
            throw new Exception("No formats found for device " + description);
        }

        // prefer a compressed image format
        for (Format format : description.getFormats()) {
            if (format.isCompressed()) {
                return format;
            }
        }

        // first format found
        return description.getFormats()[0];
    }

    private Client client;

    public WebCam(Client client) {
        this.client = client;

    }

    public VideoDeviceDescription[] availableDevices() throws Exception {
        return VideoDeviceFactory.availableDevices();
    }
    
    public VideoSource selectDevice(VideoDeviceDescription description)
            throws Exception {
        // first select format
        Format format = selectFormat(description);

        // FIXME: we don't select the resolution automatically, or even check if
        // it is valid

        VideoSource device = VideoDeviceFactory.openDevice(client,
                description.deviceNumber, TARGET_WIDTH, TARGET_HEIGHT, 0,
                format, 85);
        device.start();

        return device;

    }

}
