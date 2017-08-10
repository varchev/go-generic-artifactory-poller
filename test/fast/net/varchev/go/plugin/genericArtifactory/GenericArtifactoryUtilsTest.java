package net.varchev.go.plugin.genericArtifactory;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class GenericArtifactoryUtilsTest {
    private static final String REPO_URL = "https://artifactory.example.com/artifactory/api/storage/repo-id/Path/To/Artifact";
    private static final String ITEM_URL = "https://artifactory.example.com/artifactory/api/storage/repo-id/Path/To/Artifact/Artifact.1.8.26.1.zip";
    private static final String ITEM_UPDATED_DATE = "2016-09-13T12:29:51.341+03:00";
    private static final String REPO_UPDATED_DATE = "2014-08-27T10:40:47.567+03:00";
    private static final String BASE_TEST_FILE_PATH = "test" + File.separator + "fast" + File.separator;

    @Test
    public void shouldParseAndReturnLastUpdatedDate() throws Exception {
        GenericArtifactoryUtils utils = spy(new GenericArtifactoryUtils());
        String repoFeedFile = FileUtils.readFileToString(new File(BASE_TEST_FILE_PATH + "artifactory-good-feed.json"));
        doReturn(new JSONObject(repoFeedFile)).when(utils).getJsonObject(REPO_URL);
        String itemFeedFile = FileUtils.readFileToString(new File(BASE_TEST_FILE_PATH + "artifactory-item-good-feed.json"));
        doReturn(new JSONObject(itemFeedFile)).when(utils).getJsonObject(ITEM_URL);

        Date repoDate = utils.getLastUpdatedDate(REPO_URL);
        Date lastItemDate = utils.getLastUpdatedDate(ITEM_URL);

        assertThat(lastItemDate, is(DatatypeConverter.parseDateTime((ITEM_UPDATED_DATE)).getTime()));
        assertThat(repoDate, is(DatatypeConverter.parseDateTime(REPO_UPDATED_DATE).getTime()));


    }
}
