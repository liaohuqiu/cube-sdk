package in.srain.cube.image.impl;

import in.srain.cube.image.ImageLoadRequest;
import in.srain.cube.image.iface.NameGenerator;

public class DefaultNameGenerator implements NameGenerator {

    private static DefaultNameGenerator sInstance;

    public static synchronized DefaultNameGenerator getInstance() {
        if (sInstance == null) {
            sInstance = new DefaultNameGenerator();
        }
        return sInstance;
    }

    @Override
    public String generateIdentityUrlFor(ImageLoadRequest request) {
        return request.getUrl();
    }
}
