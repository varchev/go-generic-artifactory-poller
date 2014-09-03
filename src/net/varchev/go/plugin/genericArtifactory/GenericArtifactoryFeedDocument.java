package net.varchev.go.plugin.genericArtifactory;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.util.HttpRepoURL;
import maven.Version;
import net.varchev.go.plugin.genericArtifactory.config.GenericArtifactoryPackageConfig;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class GenericArtifactoryFeedDocument {

    private static Logger LOGGER = Logger.getLoggerFor(GenericArtifactoryFeedDocument.class);
    private final JSONObject feedObject;
    private String packageId;
    private Version lowerBoundVersion;
    private Version upperBoundVersion;
    private Version lastKnownVersion;
    private HashMap<Version, String> versionStringMap;
    private HashMap<Version, JSONObject> versionJSONObjectMap;
    private ArrayList<Version> versionsList;
    private GenericArtifactoryParams params;


    public GenericArtifactoryFeedDocument(JSONObject feedObject, GenericArtifactoryParams params) {
        this.feedObject = feedObject;
        if(params.getPollVersionFrom() != null) {
            this.lowerBoundVersion = new Version(params.getPollVersionFrom());
        }

        if(params.getPollVersionTo() != null) {
            this.upperBoundVersion = new Version(params.getPollVersionTo());
        }

        if(params.getLastKnownVersion() != null) {
            String versionString = params.getLastKnownVersion();
            this.lastKnownVersion = new Version(versionString);
        }

        this.packageId = params.getPackageId();
        this.params = params;
        populateVersionsList();
    }

    public GenericArtifactoryFeedDocument(String url, GenericArtifactoryParams params) {
        this(getJsonObject(url), params);
    }

    private static JSONObject getJsonObject(String url) {
        DefaultHttpClient client = HttpRepoURL.getHttpClient();
        HttpGet method = new HttpGet(url);
        method.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10 * 1000);
        try {
            HttpResponse response = client.execute(method);
            if(response.getStatusLine().getStatusCode() == 404) {
                throw new GenericArtifactoryException("No such package found");
            }
            else if(response.getStatusLine().getStatusCode() != 200){
                throw new RuntimeException(String.format("HTTP %s, %s",
                        response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
            }

            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);

            LOGGER.info(responseBody);

            JSONObject result = new JSONObject(responseBody);

            return result;
        } catch (GenericArtifactoryException ex) {
            throw ex;
        } catch (Exception ex) {
            String message = String.format("%s (%s) while getting package feed for : %s ", ex.getClass().getSimpleName(), ex.getMessage(), url);
            LOGGER.error(message);
            throw new RuntimeException(message, ex);
        } finally {
            method.releaseConnection();
            client.getConnectionManager().shutdown();
        }
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

    private Date getVersionModifiedDate(){

        String time = feedObject.getString("lastUpdated");

        return javax.xml.bind.DatatypeConverter.parseDateTime(time).getTime();
    }

    private Date getPublishedDate() {
        return getVersionModifiedDate();
    }

    private String getPackageVersion() {
        return getVersionString(getLatestVersion());
    }

    private Version getLatestVersion(){
        return versionsList.get(0);
    }

    private String getVersionString(Version version){
        return versionStringMap.get(version);
    }

    private void populateVersionsList(){

        JSONArray versions = feedObject.getJSONArray("children");

        versionsList = new ArrayList<Version>();

        versionStringMap = new HashMap<Version, String>() ;
        versionJSONObjectMap = new HashMap<Version, JSONObject>();
        for (int i = 0; i < versions.length(); i++) {
            JSONObject file = versions.getJSONObject(i);
            String fileName = file.getString("uri").substring(1);
            boolean isFolder = file.getBoolean("folder");
            if(!fileName.startsWith(packageId) || isFolder)
            {
                continue;
            }
            int lastDot = fileName.lastIndexOf(".");
            LOGGER.info("lastDot index : " + lastDot);
            LOGGER.info("fileName  : " + fileName);
            LOGGER.info("packageId length : " + packageId.length());
            String versionString  = fileName.substring(packageId.length() + 1, lastDot);
            LOGGER.info("versionString  : " + versionString);
            Version currentVersion = new Version(versionString);
            if(isWithinBounds(currentVersion)) {
                LOGGER.info("is withing bounds  : " + currentVersion);
                versionStringMap.put(currentVersion, versionString);
                versionsList.add(currentVersion);
                versionJSONObjectMap.put(currentVersion, file);
            }
        }
        Collections.sort(versionsList, Collections.reverseOrder());

    }

    private boolean isWithinBounds(Version currentVersion) {
        if(lowerBoundVersion != null && lowerBoundVersion.compareTo(currentVersion) > 0 ){
            return false;
        }
        if(upperBoundVersion != null && upperBoundVersion.compareTo(currentVersion) <= 0 ){
            return false;
        }
        if(lastKnownVersion != null &&  lastKnownVersion.compareTo(currentVersion) >=0  ){
            return false;
        }
        return true;
    }

    public PackageRevision getPackageRevision(boolean lastVersionKnown) {

        if(versionsList.isEmpty()){
            if(lastVersionKnown) return null;
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
