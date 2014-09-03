package net.varchev.go.plugin.genericArtifactory.apimpl;

import com.thoughtworks.go.plugin.api.config.Property;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialProperty;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import net.varchev.go.plugin.genericArtifactory.GenericArtifactoryParams;
import net.varchev.go.plugin.genericArtifactory.config.GenericArtifactoryPackageConfig;
import com.tw.go.plugin.util.RepoUrl;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Date;

import static org.mockito.Mockito.*;

public class GenericArtifactoryPollerTest {
    @Test
    public void PollerShouldExcuteCorrectCmd(){
        GenericArtifactoryPoller poller = new GenericArtifactoryPoller();

        GenericArtifactoryPoller spy = spy(poller);

        RepositoryConfiguration repoCfgs = mock(RepositoryConfiguration.class);
        PackageConfiguration pkgCfgs = mock(PackageConfiguration.class);

        String repoUrlStr = "http://google.com";//something valid to satisfy connection check
        when(repoCfgs.get(RepoUrl.REPO_URL)).thenReturn(new PackageMaterialProperty(RepoUrl.REPO_URL, repoUrlStr));

        String user = "user";
        when(repoCfgs.get(RepoUrl.USERNAME)).thenReturn(new PackageMaterialProperty(RepoUrl.USERNAME, user));

        String password = "passwrod";
        when(repoCfgs.get(RepoUrl.PASSWORD)).thenReturn(new PackageMaterialProperty(RepoUrl.PASSWORD, password));

        String repoId = "test";
        String packagePath = "Path/To/Artifact";
        String packageId = "ArtifactId";

        Property propertyRepoID = new PackageMaterialProperty(GenericArtifactoryPackageConfig.REPO_ID, repoId);
        when(pkgCfgs.get(GenericArtifactoryPackageConfig.REPO_ID)).thenReturn(propertyRepoID);

        Property propertyPath = new PackageMaterialProperty(GenericArtifactoryPackageConfig.PACKAGE_PATH, packagePath);
        when(pkgCfgs.get(GenericArtifactoryPackageConfig.PACKAGE_PATH)).thenReturn(propertyPath);

        Property property = new PackageMaterialProperty(GenericArtifactoryPackageConfig.PACKAGE_ID, packageId);
        when(pkgCfgs.get(GenericArtifactoryPackageConfig.PACKAGE_ID)).thenReturn(property);

        PackageRevision dummyResult = new PackageRevision("1.0", new Date(),"user");
        RepoUrl repoUrl = RepoUrl.create(repoUrlStr, user, password);
        final GenericArtifactoryParams params = new GenericArtifactoryParams(repoUrl, repoId, packagePath, packageId, null, null, null);

        Matcher<GenericArtifactoryParams> paramsMatcher = new BaseMatcher<GenericArtifactoryParams>() {
            GenericArtifactoryParams expected = params;
            @Override
            public boolean matches(Object item) {
                GenericArtifactoryParams genericArtifactoryParams = (GenericArtifactoryParams) item;
                return expected.getPackageId().equals(genericArtifactoryParams.getPackageId()) &&
                        expected.getRepoUrl().equals(genericArtifactoryParams.getRepoUrl());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(expected.getPackageId());
            }
        };

        doReturn(dummyResult).when(spy).poll(argThat(paramsMatcher));
        //actual test
        spy.getLatestRevision(pkgCfgs, repoCfgs);
        verify(spy).poll(argThat(paramsMatcher));
    }

}
