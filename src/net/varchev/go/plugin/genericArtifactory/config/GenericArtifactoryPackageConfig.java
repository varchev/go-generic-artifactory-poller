package net.varchev.go.plugin.genericArtifactory.config;

import com.thoughtworks.go.plugin.api.config.Property;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class GenericArtifactoryPackageConfig {

    private static Logger LOGGER = Logger.getLoggerFor(GenericArtifactoryPackageConfig.class);

    public static final String REPO_ID = "REPO_ID";
    public static final String PACKAGE_PATH = "PACKAGE_PATH";
    public static final String PACKAGE_ID = "PACKAGE_ID";
    public static final String POLL_VERSION_FROM = "POLL_VERSION_FROM";
    public static final String POLL_VERSION_TO = "POLL_VERSION_TO";
    public static final String PACKAGE_LOCATION = "LOCATION";
    public static final String PACKAGE_VERSION = "VERSION";
    private final PackageConfiguration packageConfigs;
    private final Property repoIdConfig;
    private final Property packagePathConfig;
    private final Property packageIdConfig;

    public GenericArtifactoryPackageConfig(PackageConfiguration packageConfigs) {
        this.packageConfigs = packageConfigs;
        this.repoIdConfig = packageConfigs.get(REPO_ID);
        this.packagePathConfig = packageConfigs.get(PACKAGE_PATH);
        this.packageIdConfig = packageConfigs.get(PACKAGE_ID);
    }

    public boolean isPackagePathMissing() {
        return packagePathConfig == null;
    }

    public boolean isPackageNameMissing() {
        return packageIdConfig == null;
    }

    public String getRepoId() {
        return repoIdConfig.getValue();
    }

    public String getPackagePath() {
        return packagePathConfig.getValue();
    }

    public String getPackageId() {
        return packageIdConfig.getValue();
    }

    public ValidationResult validatePackagePath(){
        ValidationResult validationResult = new ValidationResult();
        if (isPackagePathMissing()) {
            String message = "Package path not specified";
            LOGGER.info(message);
            validationResult.addError(new ValidationError(GenericArtifactoryPackageConfig.PACKAGE_PATH, message));
            return validationResult;
        }

        String packagePath = getPackagePath();
        if (packagePath == null) {
            String message = "Package path is null";
            LOGGER.info(message);
            validationResult.addError(new ValidationError(GenericArtifactoryPackageConfig.PACKAGE_PATH, message));
        }
        if (packagePath != null && isBlank(packagePath.trim())) {
            String message = "Package path is empty";
            LOGGER.info(message);
            validationResult.addError(new ValidationError(GenericArtifactoryPackageConfig.PACKAGE_PATH, message));
        }
        if (packagePath != null && (packagePath.contains("*") || packagePath.contains("?"))) {
            String message = String.format("Package path [%s] is invalid", packagePath);
            LOGGER.info(message);
            validationResult.addError(new ValidationError(GenericArtifactoryPackageConfig.PACKAGE_PATH, message));
        }
        return validationResult;
    }

    public ValidationResult validatePackageId(){
        ValidationResult validationResult = new ValidationResult();
        if (isPackageNameMissing()) {
            String message = "Package Id not specified";
            LOGGER.info(message);
            validationResult.addError(new ValidationError(GenericArtifactoryPackageConfig.PACKAGE_ID, message));
            return validationResult;
        }

        String packageName = getPackageId();
        if (packageName == null) {
            String message = "Package Id is null";
            LOGGER.info(message);
            validationResult.addError(new ValidationError(GenericArtifactoryPackageConfig.PACKAGE_ID, message));
        }

        if (packageName != null && isBlank(packageName.trim())) {
            String message = "Package Id is empty";
            LOGGER.info(message);
            validationResult.addError(new ValidationError(GenericArtifactoryPackageConfig.PACKAGE_ID, message));
        }

        if (packageName != null && (packageName.contains("*") || packageName.contains("?"))) {
            String message = String.format("Package Id [%s] is invalid", packageName);
            LOGGER.info(message);
            validationResult.addError(new ValidationError(GenericArtifactoryPackageConfig.PACKAGE_ID, message));
        }
        return validationResult;
    }

    public static String[] getValidKeys() {
        return new String[]{REPO_ID, PACKAGE_PATH, PACKAGE_ID, POLL_VERSION_FROM, POLL_VERSION_TO};
    }

    public String getPollVersionFrom() {
        Property from = packageConfigs.get(POLL_VERSION_FROM);
        return (from == null) ? null : from.getValue();
    }

    public String getPollVersionTo() {
        Property to = packageConfigs.get(POLL_VERSION_TO);
        return (to == null) ? null : to.getValue();
    }

    public boolean hasBounds() {
        return getPollVersionFrom() != null || getPollVersionTo() != null;
    }
}
