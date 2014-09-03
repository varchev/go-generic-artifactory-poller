package net.varchev.go.plugin.genericArtifactory;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.util.RepoUrl;
import net.varchev.go.plugin.genericArtifactory.config.GenericArtifactoryPackageConfig;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class GenericArtifactoryFeedDocumentTest {

    @Test
    public void shouldCreatePackageRevision() throws Exception {
        String fileContent = FileUtils.readFileToString(new File("test" + File.separator + "fast" + File.separator + "artifactory-good-feed.json"));
        JSONObject doc = new JSONObject(fileContent);

        GenericArtifactoryParams params = new GenericArtifactoryParams(RepoUrl.create("http://artifactory.example.com/artifactory", null, null),
                "repo-id", "Path/To/Artifact", "Artifact", "1.7", null, null);
        PackageRevision result = new GenericArtifactoryFeedDocument(doc,params).getPackageRevision(false);
        assertThat(result.getUser(), is("userName"));
        assertThat(result.getRevision(), is("1.8.26.1"));

        assertThat(result.getTimestamp(), is(javax.xml.bind.DatatypeConverter.parseDateTime(("2014-08-27T10:40:47.567+03:00")).getTime()));
        assertThat(result.getDataFor(GenericArtifactoryPackageConfig.PACKAGE_LOCATION), is("http://artifactory.example.com/artifactory/repo-id/Path/To/Artifact/Artifact.1.8.26.1.zip"));
        assertThat(result.getDataFor(GenericArtifactoryPackageConfig.PACKAGE_VERSION), is("1.8.26.1"));
    }
}
