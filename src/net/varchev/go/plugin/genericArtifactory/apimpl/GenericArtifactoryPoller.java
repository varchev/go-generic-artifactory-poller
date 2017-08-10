package net.varchev.go.plugin.genericArtifactory.apimpl;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialPoller;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.Result;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import net.varchev.go.plugin.genericArtifactory.GenericArtifactoryFeedDocument;
import net.varchev.go.plugin.genericArtifactory.GenericArtifactoryParams;
import net.varchev.go.plugin.genericArtifactory.GenericArtifactoryUtils;
import net.varchev.go.plugin.genericArtifactory.config.GenericArtifactoryPackageConfig;
import net.varchev.go.plugin.genericArtifactory.config.GenericArtifactoryRepoConfig;
import com.tw.go.plugin.util.Credentials;
import com.tw.go.plugin.util.HttpRepoURL;
import com.tw.go.plugin.util.RepoUrl;
import org.json.JSONObject;

public class GenericArtifactoryPoller implements PackageMaterialPoller {
    private static Logger LOGGER = Logger.getLoggerFor(GenericArtifactoryPoller.class);

    private final GenericArtifactoryUtils utils;

    public GenericArtifactoryPoller() {
        utils = new GenericArtifactoryUtils();
    }

    public PackageRevision getLatestRevision(PackageConfiguration packageConfig, RepositoryConfiguration repoConfig) {
        LOGGER.info(String.format("getLatestRevision called with packageName %s, for repo: %s",
                packageConfig.get(GenericArtifactoryPackageConfig.PACKAGE_ID).getValue(), repoConfig.get(RepoUrl.REPO_URL).getValue()));

        validateConfig(repoConfig, packageConfig);

        GenericArtifactoryPackageConfig artifactoryPackageConfig = new GenericArtifactoryPackageConfig(packageConfig);
        GenericArtifactoryRepoConfig artifactoryRepoConfig = new GenericArtifactoryRepoConfig(repoConfig);

        GenericArtifactoryParams params = GenericArtifactoryParams.createFrom(
                artifactoryRepoConfig.getRepoUrl(),
                artifactoryPackageConfig,
                null);

        PackageRevision packageRevision = poll(params);

        LOGGER.info(String.format("getLatestRevision returning with %s, %s",
                packageRevision.getRevision(),
                packageRevision.getTimestamp()));

        return packageRevision;
    }

    public PackageRevision latestModificationSince(PackageConfiguration packageConfig, RepositoryConfiguration repoConfig, PackageRevision previouslyKnownRevision) {
        String message = String.format("latestModificationSince called with packageName %s, for repo: %s",
                packageConfig.get(GenericArtifactoryPackageConfig.PACKAGE_ID).getValue(),
                repoConfig.get(RepoUrl.REPO_URL).getValue());
        LOGGER.info(message);

        validateConfig(repoConfig, packageConfig);

        GenericArtifactoryPackageConfig genericArtifactoryPackageConfig = new GenericArtifactoryPackageConfig(packageConfig);

        GenericArtifactoryParams params = GenericArtifactoryParams.createFrom(
                new GenericArtifactoryRepoConfig(repoConfig).getRepoUrl(),
                genericArtifactoryPackageConfig,
                previouslyKnownRevision);

        PackageRevision updatedPackage = poll(params);

        if (updatedPackage == null) {
            LOGGER.info(String.format("no modification since %s", previouslyKnownRevision.getRevision()));
            return null;
        }

        LOGGER.info(String.format("latestModificationSince returning with %s, %s", updatedPackage.getRevision(), updatedPackage.getTimestamp()));
        if (updatedPackage.getTimestamp().getTime() < previouslyKnownRevision.getTimestamp().getTime())
        {
            message = String.format("Updated Package %s published earlier (%s) than previous (%s, %s)",
                    updatedPackage.getRevision(),
                    updatedPackage.getTimestamp(),
                    previouslyKnownRevision.getRevision(),
                    previouslyKnownRevision.getTimestamp());

            LOGGER.warn(message);
        }
        return updatedPackage;
    }

    @Override
    public Result checkConnectionToRepository(RepositoryConfiguration repoConfigs) {
        Result response = new Result();
        GenericArtifactoryRepoConfig genericArtifactoryRepoConfig = new GenericArtifactoryRepoConfig(repoConfigs);
        RepoUrl repoUrl = genericArtifactoryRepoConfig.getRepoUrl();
        if (repoUrl.isHttp()) {
            try {
                repoUrl.checkConnection(((HttpRepoURL) repoUrl).getUrlStrWithTrailingSlash());
            } catch (Exception e) {
                response.withErrorMessages(e.getMessage());
            }
        } else {
            repoUrl.checkConnection();
        }
        LOGGER.info(response.getMessagesForDisplay());
        return response;
    }

    @Override
    public Result checkConnectionToPackage(PackageConfiguration packageConfigs, RepositoryConfiguration repoConfigs) {
        Result response = checkConnectionToRepository(repoConfigs);
        if (!response.isSuccessful()) {
            LOGGER.info(response.getMessagesForDisplay());
            return response;
        }
        PackageRevision packageRevision = getLatestRevision(packageConfigs, repoConfigs);
        response.withSuccessMessages("Found " + packageRevision.getRevision());
        return response;
    }

    private void validateConfig(RepositoryConfiguration repoConfig, PackageConfiguration packageConfig) {
        ValidationResult errors = new PluginConfig().isRepositoryConfigurationValid(repoConfig);

        errors.addErrors(new PluginConfig().isPackageConfigurationValid(packageConfig, repoConfig).getErrors());

        if (!errors.isSuccessful()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ValidationError validationError : errors.getErrors()) {
                stringBuilder.append(validationError.getMessage()).append("; ");
            }
            String errorString = stringBuilder.toString();
            throw new RuntimeException(errorString.substring(0, errorString.length() - 2));
        }
    }

    public PackageRevision poll(GenericArtifactoryParams params) {
        String url = params.getQuery();
        LOGGER.info(String.format("params received: %s", params.toString()));

        GenericArtifactoryFeedDocument artifactoryFeedDocument = new GenericArtifactoryFeedDocument(url, params, utils);
        PackageRevision packageRevision = artifactoryFeedDocument.getPackageRevision(params.isLastVersionKnown());
        if(packageRevision != null && params.getRepoUrl().getCredentials().provided())
        {
            addUserInfoToLocation(packageRevision, params.getRepoUrl().getCredentials());
        }
        return packageRevision;
    }

    private void addUserInfoToLocation(PackageRevision packageRevision, Credentials credentials) {
        String location = packageRevision.getDataFor(GenericArtifactoryPackageConfig.PACKAGE_LOCATION);
        packageRevision.addData(GenericArtifactoryPackageConfig.PACKAGE_LOCATION, HttpRepoURL.getUrlWithCreds(location, credentials));
    }
}
