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
import static org.mockito.Mockito.*;


public class GenericArtifactoryFeedDocumentTest {
    private static final String BASE_URL = "http://artifactory.example.com/artifactory";

    @Test
    public void shouldCreatePackageRevision() throws Exception {
        GenericArtifactoryUtils mockUtils = mock(GenericArtifactoryUtils.class);
        String itemUpdatedTime = "2016-09-13T12:29:51.341+03:00";
        when(mockUtils.getLastUpdatedDate(anyString())).thenReturn(javax.xml.bind.DatatypeConverter.parseDateTime((itemUpdatedTime)).getTime());
        GenericArtifactoryParams params = new GenericArtifactoryParams(RepoUrl.create(BASE_URL, null, null),
                "repo-id", "Path/To/Artifact", "Artifact", "1.7", null, null);
        String fileContent = FileUtils.readFileToString(new File("test" + File.separator + "fast" + File.separator + "artifactory-good-feed.json"));
        when(mockUtils.getJsonObject(params.getQuery())).thenReturn(new JSONObject(fileContent));
        PackageRevision result = new GenericArtifactoryFeedDocument(params.getQuery(), params, mockUtils).getPackageRevision(false);

        assertThat(result.getUser(), is("userName"));
        assertThat(result.getRevision(), is("1.8.26.1"));
        assertThat(result.getTimestamp(), is(javax.xml.bind.DatatypeConverter.parseDateTime((itemUpdatedTime)).getTime()));
        assertThat(result.getDataFor(GenericArtifactoryPackageConfig.PACKAGE_LOCATION), is("http://artifactory.example.com/artifactory/repo-id/Path/To/Artifact/Artifact.1.8.26.1.zip"));
        assertThat(result.getDataFor(GenericArtifactoryPackageConfig.PACKAGE_VERSION), is("1.8.26.1"));
    }
}
