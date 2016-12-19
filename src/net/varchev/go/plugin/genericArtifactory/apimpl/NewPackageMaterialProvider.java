package net.varchev.go.plugin.genericArtifactory.apimpl;

import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.material.packagerepository.shim.ReplacementProvider;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

@Extension
public class NewPackageMaterialProvider implements GoPlugin {
    private ReplacementProvider replacementProvider;

    public NewPackageMaterialProvider() {
        replacementProvider = new ReplacementProvider(new GenericArtifactoryProvider());
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) throws UnhandledRequestTypeException {
        return replacementProvider.handle(goPluginApiRequest);
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return replacementProvider.pluginIdentifier();
    }

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
    }
}