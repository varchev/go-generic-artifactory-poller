package net.varchev.go.plugin.genericArtifactory;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.util.HttpRepoURL;
import com.tw.go.plugin.util.RepoUrl;
import net.varchev.go.plugin.genericArtifactory.config.GenericArtifactoryPackageConfig;

public class GenericArtifactoryParams {
    private final String repoId;
    private final String packagePath;
    private final String packageId;
    private final RepoUrl repoUrl;
    private String pollVersionFrom;
    private String pollVersionTo;
    private PackageRevision lastKnownVersion = null;


    public GenericArtifactoryParams(RepoUrl repoUrl, String repoId, String packagePath, String packageId, String pollVersionFrom, String pollVersionTo , PackageRevision previouslyKnownRevision) {
        this.repoUrl = repoUrl;
        this.repoId = repoId;
        this.packagePath = packagePath;
        this.packageId = packageId;

        if (pollVersionFrom != null && !pollVersionFrom.trim().isEmpty()) this.pollVersionFrom = pollVersionFrom;
        if (pollVersionTo != null && !pollVersionTo.trim().isEmpty()) this.pollVersionTo = pollVersionTo;
        this.lastKnownVersion = previouslyKnownRevision;
    }

    public static GenericArtifactoryParams createFrom (RepoUrl repoUrl, GenericArtifactoryPackageConfig packageConfig, PackageRevision previouslyKnownRevision){
        return new GenericArtifactoryParams(repoUrl,
                packageConfig.getRepoId(),
                packageConfig.getPackagePath(),
                packageConfig.getPackageId(),
                packageConfig.getPollVersionFrom(),
                packageConfig.getPollVersionTo(),
                previouslyKnownRevision);
    }
    @Override
    public String toString() {
        return String.format("repoUrl:%s, repoId:%s, packagePath:%s, packageId:%s, pollVersionFrom:%s, pollVersionTo:%s, lastKnownRevision:%s", repoUrl.getUrlStr(), repoId, packagePath, packageId, pollVersionFrom, pollVersionTo, lastKnownVersion);
    }

    public String getPollVersionFrom() {

        return pollVersionFrom;
    }
    public String getPollVersionTo() {

        return pollVersionTo;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public String getPackageId() {
        return packageId;
    }
    public RepoUrl getRepoUrl() {
        return repoUrl;
    }

    public boolean isLastVersionKnown() {
        return lastKnownVersion != null;
    }

    public String getLastKnownVersion() {
        if (lastKnownVersion == null) return null;
        return lastKnownVersion.getDataFor(GenericArtifactoryPackageConfig.PACKAGE_VERSION);
    }

    public String getQuery() {
        StringBuilder query = new StringBuilder();
        query.append(((HttpRepoURL) repoUrl).getUrlWithBasicAuth());
        query.append("/api/storage/");
        query.append(repoId + "/");
        query.append(getPackagePath());

        return query.toString();
    }


}
