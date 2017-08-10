package net.varchev.go.plugin.genericArtifactory;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import maven.Version;
import net.varchev.go.plugin.genericArtifactory.config.GenericArtifactoryPackageConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class GenericArtifactoryFeedDocument {

    private static Logger LOGGER = Logger.getLoggerFor(GenericArtifactoryFeedDocument.class);
    private final JSONObject feedObject;
    private final GenericArtifactoryUtils utils;
    private String packageId;
    private Version lowerBoundVersion;
    private Version upperBoundVersion;
    private Version lastKnownVersion;
    private HashMap<Version, String> versionStringMap;
    private HashMap<Version, JSONObject> versionJSONObjectMap;
    private ArrayList<Version> versionsList;
    private GenericArtifactoryParams params;

    public GenericArtifactoryFeedDocument(String url, GenericArtifactoryParams params, GenericArtifactoryUtils utils) {
        if(utils == null) {
            throw new InvalidParameterException(String.format("%s utils should not be null.", GenericArtifactoryUtils.class.getName()));
        }
        if(params == null) {
            throw new InvalidParameterException(String.format("%s params should not be null.", GenericArtifactoryParams.class.getName()));
        }
        if(url == null || url.trim().length() == 0) {
            throw new InvalidParameterException("URL should not null or empty");
        }

        this.utils = utils;
        feedObject = utils.getJsonObject(url);

        if (params.getPollVersionFrom() != null) {
            this.lowerBoundVersion = new Version(params.getPollVersionFrom());
        }

        if (params.getPollVersionTo() != null) {
            this.upperBoundVersion = new Version(params.getPollVersionTo());
        }

        if (params.getLastKnownVersion() != null) {
            String versionString = params.getLastKnownVersion();
            this.lastKnownVersion = new Version(versionString);
        }

        this.packageId = params.getPackageId();
        this.params = params;
        populateVersionsList();
    }

    private String getPackageLocation() {
        Version latestVersion = getLatestVersion();

        JSONObject versionDetails = getVersionDetails(latestVersion);
        String repoUrl = (params.getRepoUrl()).getUrlStr();
        String repoId = feedObject.getString("repo");
        String packagePath = feedObject.getString("path");
        String packageUri = versionDetails.getString("uri");
        String result = repoUrl.concat("/" + repoId).concat(packagePath).concat(packageUri);
        LOGGER.info("package location : " + result);
        return result;
    }

    private String getAuthor() {
        return feedObject.getString("modifiedBy");
    }

    private JSONObject getVersionDetails(Version version) {
        return versionJSONObjectMap.get(version);
    }

    private Date getPublishedDate() {
        StringBuilder lastItem = new StringBuilder();
        String packageUri = getVersionDetails(getLatestVersion()).getString("uri");
        lastItem.append(params.getQuery()).append("/").append(packageUri);
        return utils.getLastUpdatedDate(lastItem.toString());
    }

    private String getPackageVersion() {
        return getVersionString(getLatestVersion());
    }

    private Version getLatestVersion() {
        return versionsList.get(0);
    }

    private String getVersionString(Version version) {
        return versionStringMap.get(version);
    }

    private void populateVersionsList() {

        JSONArray versions = feedObject.getJSONArray("children");

        versionsList = new ArrayList<Version>();

        versionStringMap = new HashMap<Version, String>();
        versionJSONObjectMap = new HashMap<Version, JSONObject>();
        for (int i = 0; i < versions.length(); i++) {
            JSONObject file = versions.getJSONObject(i);
            String fileName = file.getString("uri").substring(1);
            boolean isFolder = file.getBoolean("folder");
            if (!fileName.startsWith(packageId) || isFolder) {
                continue;
            }
            int lastDot = fileName.lastIndexOf(".");
            LOGGER.info("lastDot index : " + lastDot);
            LOGGER.info("fileName  : " + fileName);
            LOGGER.info("packageId length : " + packageId.length());
            String versionString = fileName.substring(packageId.length() + 1, lastDot);
            LOGGER.info("versionString  : " + versionString);
            Version currentVersion = new Version(versionString);
            if (isWithinBounds(currentVersion)) {
                LOGGER.info("is withing bounds  : " + currentVersion);
                versionStringMap.put(currentVersion, versionString);
                versionsList.add(currentVersion);
                versionJSONObjectMap.put(currentVersion, file);
            }
        }
        Collections.sort(versionsList, Collections.reverseOrder());

    }

    private boolean isWithinBounds(Version currentVersion) {
        if (lowerBoundVersion != null && lowerBoundVersion.compareTo(currentVersion) > 0) {
            return false;
        }
        if (upperBoundVersion != null && upperBoundVersion.compareTo(currentVersion) <= 0) {
            return false;
        }
        if (lastKnownVersion != null && lastKnownVersion.compareTo(currentVersion) >= 0) {
            return false;
        }
        return true;
    }

    public PackageRevision getPackageRevision(boolean lastVersionKnown) {

        if (versionsList.isEmpty()) {
            if (lastVersionKnown) return null;
            else throw new GenericArtifactoryException("No such package found");
        }
        PackageRevision result = new PackageRevision(getPackageLabel(), getPublishedDate(), getAuthor());
        result.addData(GenericArtifactoryPackageConfig.PACKAGE_LOCATION, getPackageLocation());
        result.addData(GenericArtifactoryPackageConfig.PACKAGE_VERSION, getPackageVersion());
        return result;
    }

    private String getPackageLabel() {
        return getPackageVersion();
    }
}
