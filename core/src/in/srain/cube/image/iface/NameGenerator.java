package in.srain.cube.image.iface;

import in.srain.cube.image.ImageTask;

public interface NameGenerator {
    public String getRemoteUrl(ImageTask imageTask);
    public String generateIdentityUrl(ImageTask imageTask);
}
