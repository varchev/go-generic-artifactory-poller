package net.varchev.go.plugin.genericArtifactory.apimpl;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialProperty;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import maven.Version;
import net.varchev.go.plugin.genericArtifactory.config.GenericArtifactoryPackageConfig;
import net.varchev.go.plugin.genericArtifactory.config.GenericArtifactoryRepoConfig;
import com.tw.go.plugin.util.RepoUrl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.thoughtworks.go.plugin.api.config.Property.*;
import static net.varchev.go.plugin.genericArtifactory.config.GenericArtifactoryPackageConfig.*;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class PluginConfigTest {
    private PluginConfig pluginConfig;

    @Before
    public void setUp() {
        pluginConfig = new PluginConfig();
    }

    @Test
    public void shouldGetRepositoryConfiguration() {
        RepositoryConfiguration configurations = pluginConfig.getRepositoryConfiguration();
        assertThat(configurations.get(RepoUrl.REPO_URL), is(notNullValue()));
        assertThat(configurations.get(RepoUrl.REPO_URL).getOption(SECURE), is(false));
        assertThat(configurations.get(RepoUrl.REPO_URL).getOption(REQUIRED), is(true));
        assertThat(configurations.get(RepoUrl.REPO_URL).getOption(DISPLAY_NAME), is("Artifactory server API root"));
        assertThat(configurations.get(RepoUrl.REPO_URL).getOption(DISPLAY_ORDER), is(0));
        assertThat(configurations.get(RepoUrl.USERNAME), is(notNullValue()));
        assertThat(configurations.get(RepoUrl.USERNAME).getOption(SECURE), is(false));
        assertThat(configurations.get(RepoUrl.USERNAME).getOption(REQUIRED), is(false));
        assertThat(configurations.get(RepoUrl.USERNAME).getOption(DISPLAY_NAME), is("UserName"));
        assertThat(configurations.get(RepoUrl.USERNAME).getOption(DISPLAY_ORDER), is(1));
        assertThat(configurations.get(RepoUrl.PASSWORD), is(notNullValue()));
        assertThat(configurations.get(RepoUrl.PASSWORD).getOption(SECURE), is(true));
        assertThat(configurations.get(RepoUrl.PASSWORD).getOption(REQUIRED), is(false));
        assertThat(configurations.get(RepoUrl.PASSWORD).getOption(DISPLAY_NAME), is("Password"));
        assertThat(configurations.get(RepoUrl.PASSWORD).getOption(DISPLAY_ORDER), is(2));
    }

    @Test
    public void shouldGetPackageConfiguration() {
        PackageConfiguration configurations = pluginConfig.getPackageConfiguration();
        assertNotNull(configurations.get(PACKAGE_PATH));
        assertThat(configurations.get(PACKAGE_PATH).getOption(DISPLAY_NAME), is("Package Path"));
        assertThat(configurations.get(PACKAGE_PATH).getOption(DISPLAY_ORDER), is(1));
        assertNotNull(configurations.get(PACKAGE_ID));
        assertThat(configurations.get(PACKAGE_ID).getOption(DISPLAY_NAME), is("Package Id"));
        assertThat(configurations.get(PACKAGE_ID).getOption(DISPLAY_ORDER), is(2));
        assertNotNull(configurations.get(POLL_VERSION_FROM));
        assertThat(configurations.get(POLL_VERSION_FROM).getOption(DISPLAY_NAME), is("Version to poll >="));
        assertThat(configurations.get(POLL_VERSION_FROM).getOption(DISPLAY_ORDER), is(3));
        assertThat(configurations.get(POLL_VERSION_FROM).getOption(REQUIRED), is(false));
        assertNotNull(configurations.get(POLL_VERSION_TO));
        assertThat(configurations.get(POLL_VERSION_TO).getOption(DISPLAY_NAME), is("Version to poll <"));
        assertThat(configurations.get(POLL_VERSION_TO).getOption(DISPLAY_ORDER), is(4));
        assertThat(configurations.get(POLL_VERSION_TO).getOption(REQUIRED), is(false));
    }

    @Test
    public void shouldValidateRepoUrl() {


        assertForRepositoryConfigurationErrors(new RepositoryConfiguration(), asList(new ValidationError(RepoUrl.REPO_URL, "Repository url not specified")), false);
        assertForRepositoryConfigurationErrors(repoConfigurations(RepoUrl.REPO_URL, null), asList(new ValidationError(RepoUrl.REPO_URL, "Repository url not specified")), false);
        assertForRepositoryConfigurationErrors(repoConfigurations(RepoUrl.REPO_URL, ""), asList(new ValidationError(RepoUrl.REPO_URL, "Repository url not specified")), false);
        assertForRepositoryConfigurationErrors(repoConfigurations(RepoUrl.REPO_URL, "incorrectUrl"), asList(new ValidationError(RepoUrl.REPO_URL, "Only http/https urls are supported")), false);
        assertForRepositoryConfigurationErrors(repoConfigurations(RepoUrl.REPO_URL, "http://correct.com/url"), new ArrayList<ValidationError>(), true);
    }

    @Test
    public void shouldRejectUnsupportedTagsInRepoConfig() {
        RepositoryConfiguration repoConfig = new RepositoryConfiguration();
        repoConfig.add(new PackageMaterialProperty(RepoUrl.REPO_URL, "http://artifactory.example.org/artifactory"));
        repoConfig.add(new PackageMaterialProperty("unsupported_key", "value"));
        assertForRepositoryConfigurationErrors(
                repoConfig,
                asList(new ValidationError("Unsupported key: unsupported_key. Valid keys: " + Arrays.toString(GenericArtifactoryRepoConfig.getValidKeys()))),
                false);

    }

    @Test
    public void shouldRejectUnsupportedTagsInPkgConfig() {
        PackageConfiguration pkgConfig = new PackageConfiguration();
        pkgConfig.add(new PackageMaterialProperty(PACKAGE_ID, "abc"));
        pkgConfig.add(new PackageMaterialProperty(PACKAGE_PATH, "Path/To/Artifact"));
        pkgConfig.add(new PackageMaterialProperty("unsupported_key", "value"));
        assertForPackageConfigurationErrors(
                pkgConfig,
                asList(new ValidationError("Unsupported key: unsupported_key. Valid keys: " + Arrays.toString(GenericArtifactoryPackageConfig.getValidKeys()))),
                false);
    }

    @Test
    public void shouldValidatePackageId() {
        PackageConfiguration packageConfiguration = new PackageConfiguration();
        assertForPackageConfigurationErrors(packageConfiguration, asList(new ValidationError(PACKAGE_PATH, "Package path not specified")), false);
        assertForPackageConfigurationErrors(configurations(PACKAGE_ID, null), asList(new ValidationError(PACKAGE_ID, "Package Id is null")), false);
        assertForPackageConfigurationErrors(configurations(PACKAGE_ID, ""), asList(new ValidationError(PACKAGE_ID, "Package Id is empty")), false);
        assertForPackageConfigurationErrors(configurations(PACKAGE_ID, "go-age?nt-*"), asList(new ValidationError(PACKAGE_ID, "Package Id [go-age?nt-*] is invalid")), false);
        assertForPackageConfigurationErrors(configurations(PACKAGE_ID, "go-agent"), new ArrayList<ValidationError>(), true);
    }

    private void assertForRepositoryConfigurationErrors(RepositoryConfiguration repositoryConfigurations, List<ValidationError> expectedErrors, boolean expectedValidationResult) {
        ValidationResult errors = pluginConfig.isRepositoryConfigurationValid(repositoryConfigurations);
        assertThat(errors.isSuccessful(), is(expectedValidationResult));
        assertThat(errors.getErrors().size(), is(expectedErrors.size()));
        assertThat(errors.getErrors().containsAll(expectedErrors), is(true));
    }

    private void assertForPackageConfigurationErrors(PackageConfiguration packageConfiguration, List<ValidationError> expectedErrors, boolean expectedValidationResult) {
        final RepositoryConfiguration repoConfig = new RepositoryConfiguration();
        repoConfig.add(new PackageMaterialProperty(RepoUrl.REPO_URL, "http://artifactory.example.org/artifactory"));
        ValidationResult errors = pluginConfig.isPackageConfigurationValid(packageConfiguration, repoConfig);

        assertThat(errors.isSuccessful(), is(expectedValidationResult));
        assertThat(errors.getErrors().size(), is(expectedErrors.size()));
        assertThat(errors.getErrors().containsAll(expectedErrors), is(true));
    }

    private PackageConfiguration configurations(String key, String value) {
        PackageConfiguration packageConfiguration = new PackageConfiguration();
        packageConfiguration.add(new PackageMaterialProperty(PACKAGE_PATH, "Path/To/Artifact"));
        packageConfiguration.add(new PackageMaterialProperty(REPO_ID, "repo-id"));
        packageConfiguration.add(new PackageMaterialProperty(key, value));
        return packageConfiguration;
    }

    private RepositoryConfiguration repoConfigurations(String key, String value) {
        RepositoryConfiguration packageConfiguration = new RepositoryConfiguration();
        packageConfiguration.add(new PackageMaterialProperty(key, value));
        return packageConfiguration;
    }
}