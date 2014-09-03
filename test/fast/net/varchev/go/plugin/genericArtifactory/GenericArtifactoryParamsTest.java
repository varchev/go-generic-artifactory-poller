package net.varchev.go.plugin.genericArtifactory;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.util.RepoUrl;
import org.junit.Test;

import static net.varchev.go.plugin.genericArtifactory.config.GenericArtifactoryPackageConfig.PACKAGE_VERSION;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GenericArtifactoryParamsTest {
    @Test
    public void shouldHandleUpperBound(){
        GenericArtifactoryParams params = new GenericArtifactoryParams(RepoUrl.create("http://artifactory.example.com/artifactory", null, null),
                "repo-id", "Path/To/Artifact", "Artifact", null, "1.2", null);
        assertThat(params.getQuery(),
                is("http://artifactory.example.com/artifactory/api/storage/repo-id/Path/To/Artifact"));
    }
    @Test
    public void shouldIgnoreLowerBoundDuringUpdate(){
        PackageRevision known = new PackageRevision("1.1.2",null,"abc");
        known.addData(PACKAGE_VERSION,"1.1.2");
        GenericArtifactoryParams params = new GenericArtifactoryParams(RepoUrl.create("http://artifactory.example.com/artifactory", null, null),
                "repo-id", "Path/To/Artifact", "Artifact", "1.0", null, null);
        assertThat(params.getQuery(),
                is("http://artifactory.example.com/artifactory/api/storage/repo-id/Path/To/Artifact"));
    }
}
