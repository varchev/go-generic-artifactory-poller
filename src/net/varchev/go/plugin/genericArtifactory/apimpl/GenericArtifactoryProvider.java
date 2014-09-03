package net.varchev.go.plugin.genericArtifactory.apimpl;

import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialProvider;

@Extension
public class GenericArtifactoryProvider implements PackageMaterialProvider {

    public PluginConfig getConfig() {
        return new PluginConfig();
    }

    public GenericArtifactoryPoller getPoller() {
        return new GenericArtifactoryPoller();
    }
}
