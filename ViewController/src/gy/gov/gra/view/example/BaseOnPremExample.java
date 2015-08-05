package gy.gov.gra.view.example;


import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

import gy.gov.gra.view.example.util.Config;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;

import java.io.ByteArrayInputStream;

import java.io.UnsupportedEncodingException;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

/**
 * This class contains only the logic that is specific to using the Public API
 * against an Alfresco repository running on-premise (4.2.d or later).
 *
 * @author jpotts
 */
public class BaseOnPremExample extends BasePublicAPIExample {

    /**
     * Change these to match your environment
     */
    //public static final String CMIS_URL = "/public/cmis/versions/1.0/atom";
    public static final String CMIS_URL = "alfresco/api/-default-/public/cmis/versions/1.1/atom";

    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    public static final JsonFactory JSON_FACTORY = new JacksonFactory();

    private HttpRequestFactory requestFactory;
    private Session cmisSession;
    private Folder rootFolder;
    private Folder newFolder;
    

    public String getAtomPubURL(HttpRequestFactory requestFactory) {
        String alfrescoAPIUrl = getAlfrescoAPIUrl();
        String atomPubURL = null;

        //try {
        //atomPubURL = alfrescoAPIUrl + getHomeNetwork() + CMIS_URL;
        atomPubURL = alfrescoAPIUrl + CMIS_URL;
        //		} catch (IOException ioe) {
        //			System.out.println("Warning: Couldn't determine home network, defaulting to -default-");
        //			atomPubURL = alfrescoAPIUrl + "-default-" + CMIS_URL;
        //		}

        return atomPubURL;
    }

    /**
     * Gets a CMIS Session by connecting to the local Alfresco server.
     *
     * @return Session
     */


    public void Session() {
        if (cmisSession == null) {
            // default factory implementation
            SessionFactory factory = SessionFactoryImpl.newInstance();
            Map<String, String> parameter = new HashMap<String, String>();

            // connection settings
            parameter.put(SessionParameter.ATOMPUB_URL, getAtomPubURL(getRequestFactory()));
            parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
            parameter.put(SessionParameter.AUTH_HTTP_BASIC, "true");
            parameter.put(SessionParameter.USER, getUsername());
            parameter.put(SessionParameter.PASSWORD, getPassword());
            parameter.put(SessionParameter.OBJECT_FACTORY_CLASS,
                          "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");

            List<Repository> repositories = factory.getRepositories(parameter);

            Repository repository = repositories.get(0);
            this.cmisSession = repository.createSession();

            System.out.println("Got a connection to repository: " + repository.getName() + ", with id: " +
                               repository.getId());


            //parameter.put(SessionParameter.REPOSITORY_ID, "-default-");
            //cmisSession = factory.createSession(parameter);

            //            cmisSession = repositories.get(0).createSession();
            //            for (Repository r : repositories) {
            //                System.out.println("Got a connection to repository: " + r.getName() + ", with id: " + r.getId());
            //            }

            System.out.println("Successfully connected to alfresco");
        }
        //return this.cmisSession;
    }

    public void folderList(ActionEvent actionEvent) {

        rootFolder = cmisSession.getRootFolder();
        ItemIterable<CmisObject> children = rootFolder.getChildren();

        System.out.println("Found the following objects in the root folder:-");
        for (CmisObject o : children) {
            System.out.println(o.getName() + " which is of type " + o.getType().getDisplayName());
        }
    }

    public void createFolderInRoot(ActionEvent actionEvent){     
        System.out.println("Creating 'ADGNewFolder' in the root folder");
        Map<String, String> newFolderProps = new HashMap<String, String>();
        newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        newFolderProps.put(PropertyIds.NAME, "ADGNewFolder");

        newFolder = rootFolder.createFolder(newFolderProps);

        // Did it work?
        ItemIterable<CmisObject> children = rootFolder.getChildren();
        System.out.println("Now finding the following objects in the root folder:-");
        for (CmisObject o : children) {
            System.out.println(o.getName());
        }        
        
    }

    public void CreateFileInARootFolder(ActionEvent actionEvent){
        final String textFileName = "test.txt";
        System.out.println("creating a simple text file, " + textFileName);

        String mimetype = "text/plain; charset=UTF-8";
        String content = "This is some test content.";
        String filename = textFileName;

        byte[] buf = null;
        try {
            buf = content.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        ByteArrayInputStream input = new ByteArrayInputStream(buf);
        ContentStream contentStream = cmisSession.getObjectFactory().createContentStream(filename, buf.length, mimetype, input);
        
        
        //now create the actial document
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        properties.put(PropertyIds.NAME, filename);

        Document doc = newFolder.createDocument(properties, contentStream, VersioningState.MAJOR);

        System.out.println("Document ID: " + doc.getId());
        
    }

    public void filesList(ActionEvent actionEvent) {

    }
    
    public void docList(ActionEvent actionEvent) {


    }


    /**
     * Uses basic authentication to create an HTTP request factory.
     *
     * @return HttpRequestFactory
     */
    public HttpRequestFactory getRequestFactory() {
        if (this.requestFactory == null) {
            this.requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                    request.setParser(new JsonObjectParser(new JacksonFactory()));
                    request.getHeaders().setBasicAuthentication(getUsername(), getPassword());
                }
            });
        }
        return this.requestFactory;
    }

    public String getAlfrescoAPIUrl() {
        //String host = Config.getConfig().getProperty("host");
        //return host + "/api/";
        return "http://hq-alfresco:8080/";


    }

    public String getUsername() {
        //return Config.getConfig().getProperty("username");
        return "admin";
    }

    public String getPassword() {
        //return Config.getConfig().getProperty("password");
        return "Alfresco@dmin";
    }

    public void setCmisSession(Session cmisSession) {
        this.cmisSession = cmisSession;
    }

    public Session getCmisSession() {
        return cmisSession;
    }

    public void test(ActionEvent actionEvent) {
        Session();
    }

    public void setRootFolder(Folder rootFolder) {
        this.rootFolder = rootFolder;
    }

    public Folder getRootFolder() {
        return rootFolder;
    }

    public void setNewFolder(Folder newFolder) {
        this.newFolder = newFolder;
    }

    public Folder getNewFolder() {
        return newFolder;
    }
}
