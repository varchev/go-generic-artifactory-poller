package net.varchev.go.plugin.genericArtifactory.apimpl;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.*;
import com.thoughtworks.go.plugin.api.config.*;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import net.varchev.go.plugin.genericArtifactory.config.GenericArtifactoryPackageConfig;
import net.varchev.go.plugin.genericArtifactory.config.GenericArtifactoryRepoConfig;
import com.tw.go.plugin.util.RepoUrl;

import java.util.Arrays;

import static com.thoughtworks.go.plugin.api.config.Property.*;

public class PluginConfig implements PackageMaterialConfiguration {

    private static Logger LOGGER = Logger.getLoggerFor(PluginConfig.class);

    public static final Property[] REPO_CONFIG_PROPERTIES = new Property[]{
            new PackageMaterialProperty(RepoUrl.REPO_URL).with(DISPLAY_NAME, "Artifactory server API root").with(DISPLAY_ORDER, 0),
            new PackageMaterialProperty(RepoUrl.USERNAME).with(REQUIRED, false).with(DISPLAY_NAME, "UserName").with(DISPLAY_ORDER, 1).with(PART_OF_IDENTITY, false),
            new PackageMaterialProperty(RepoUrl.PASSWORD).with(REQUIRED, false).with(SECURE, true).with(DISPLAY_NAME, "Password").with(DISPLAY_ORDER, 2).with(PART_OF_IDENTITY, false)
    };

    public static final Property[] PACKAGE_CONFIG_PROPERTIES = new Property[]{
            new PackageMaterialProperty(GenericArtifactoryPackageConfig.REPO_ID).with(DISPLAY_NAME, "Repository Id").with(DISPLAY_ORDER, 0),
            new PackageMaterialProperty(GenericArtifactoryPackageConfig.PACKAGE_PATH).with(DISPLAY_NAME, "Package Path").with(DISPLAY_ORDER, 1),
            new PackageMaterialProperty(GenericArtifactoryPackageConfig.PACKAGE_ID).with(DISPLAY_NAME, "Package Id").with(DISPLAY_ORDER, 2),
            new PackageMaterialProperty(GenericArtifactoryPackageConfig.POLL_VERSION_FROM).with(REQUIRED, false).with(DISPLAY_NAME, "Version to poll >=").with(DISPLAY_ORDER, 3).with(PART_OF_IDENTITY, false),
            new PackageMaterialProperty(GenericArtifactoryPackageConfig.POLL_VERSION_TO).with(REQUIRED, false).with(DISPLAY_NAME, "Version to poll <").with(DISPLAY_ORDER, 4).with(PART_OF_IDENTITY, false)
    };

    public RepositoryConfiguration getRepositoryConfiguration() {
        RepositoryConfiguration configurations = new RepositoryConfiguration();
        addPropertiesToConfig(configurations, REPO_CONFIG_PROPERTIES);
        return configurations;
    }

    private void addPropertiesToConfig(Configuration configurations, Property[] properties) {
        for (Property property : properties){
            configurations.add(property);
        }
    }

    public PackageConfiguration getPackageConfiguration() {
        PackageConfiguration configurations = new PackageConfiguration();
        addPropertiesToConfig(configurations, PACKAGE_CONFIG_PROPERTIES);
        return configurations;
    }

    public ValidationResult isRepositoryConfigurationValid(RepositoryConfiguration repoConfigs) {
        GenericArtifactoryRepoConfig genericArtifactoryRepoConfig = new GenericArtifactoryRepoConfig(repoConfigs);
        ValidationResult validationResult = new ValidationResult();
        if (genericArtifactoryRepoConfig.isRepoUrlMissing()) {
            String message = "Repository url not specified";
            LOGGER.error(message);
            validationResult.addError(new ValidationError(RepoUrl.REPO_URL, message));
            return validationResult;
        }
        RepoUrl repoUrl = genericArtifactoryRepoConfig.getRepoUrl();
        if (!repoUrl.isHttp()) {
            String message = "Only http/https urls are supported";
            LOGGER.error(message);
            validationResult.addError(new ValidationError(RepoUrl.REPO_URL, message));
        }
        repoUrl.validate(validationResult);
        detectInvalidKeys(repoConfigs, validationResult, GenericArtifactoryRepoConfig.getValidKeys());
        return validationResult;
    }

    private void detectInvalidKeys(Configuration config, ValidationResult errors, String[] validKeys) {
        for (Property property : config.list()) {
            boolean valid = false;
            for (String validKey : validKeys) {
                if (validKey.equals(property.getKey())) {
                    valid = true;
                    break;
                }
            }
            if (!valid)
                errors.addError(new ValidationError(String.format("Unsupported key: %s. Valid keys: %s", property.getKey(), Arrays.toString(validKeys))));
        }
    }

    public ValidationResult isPackageConfigurationValid(PackageConfiguration packageConfig, RepositoryConfiguration repoConfig) {
        GenericArtifactoryPackageConfig genericArtifactoryPackageConfig = new GenericArtifactoryPackageConfig(packageConfig);
        ValidationResult validationResult = genericArtifactoryPackageConfig.validatePackagePath();

        if(!validationResult.isSuccessful()){
            return validationResult;
        }

        validationResult = genericArtifactoryPackageConfig.validatePackageId();

        if(!validationResult.isSuccessful()){
            return validationResult;
        }

        detectInvalidKeys(packageConfig, validationResult, GenericArtifactoryPackageConfig.getValidKeys());

        return validationResult;
    }

}
