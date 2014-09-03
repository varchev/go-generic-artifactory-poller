package net.varchev.go.plugin.genericArtifactory.apimpl;


import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialPoller;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class GenericArtifactoryProviderTest {
    @Test
    public void shouldGetRepositoryConfig() {
        GenericArtifactoryProvider repositoryMaterial = new GenericArtifactoryProvider();
        PackageMaterialConfiguration repositoryConfiguration = repositoryMaterial.getConfig();
        assertThat(repositoryConfiguration, is(notNullValue()));
        assertThat(repositoryConfiguration, instanceOf(PluginConfig.class));
    }

    @Test
    public void shouldGetRepositoryPoller() {
        GenericArtifactoryProvider repositoryMaterial = new GenericArtifactoryProvider();
        PackageMaterialPoller poller = repositoryMaterial.getPoller();
        assertThat(poller, is(notNullValue()));
        assertThat(poller, instanceOf(GenericArtifactoryPoller.class));
    }
}
