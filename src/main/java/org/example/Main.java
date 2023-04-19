package org.example;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.leshan.core.model.ObjectLoader;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.util.SecurityUtil;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.californium.LeshanServerBuilder;
import org.eclipse.leshan.server.model.LwM2mModelProvider;
import org.eclipse.leshan.server.model.StaticModelProvider;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;
import org.eclipse.leshan.server.registration.RegistrationUpdate;
import org.eclipse.leshan.server.security.EditableSecurityStore;
import org.eclipse.leshan.server.security.FileSecurityStore;
import org.eclipse.leshan.server.security.SecurityStore;
import org.example.servlet.ClientServlet;
import org.example.servlet.EventServlet;
import org.example.servlet.ObjectSpecServlet;
import org.example.servlet.SecurityServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collection;
import java.util.List;



public class Main {
    public static void main(String[] args) throws Exception {

        final Logger LOG = LoggerFactory.getLogger(Main.class);

        String webAdress="localhost";
        PublicKey publicKey = null;
        PrivateKey privateKey = null;

        try {
            InputStream privk = Main.class.getResourceAsStream("/credentials/cprik.der");
            privateKey = SecurityUtil.privateKey.decode(privk);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        try {
            InputStream pubk = Main.class.getResourceAsStream("/credentials/cpubk.der");
            publicKey = SecurityUtil.publicKey.decode(pubk);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }


        //Adding core objects(Security,Device ...)
        List<ObjectModel> models = ObjectLoader.loadDefault();
        String[] modelPaths = new String[] { "8.xml","9.xml"};
        models.addAll(ObjectLoader.loadDdfResources("/models/", modelPaths));


        LeshanServerBuilder builder = new LeshanServerBuilder();
        LwM2mModelProvider modelProvider = new StaticModelProvider(models);
        builder.setObjectModelProvider(modelProvider);

        EditableSecurityStore securityStore;
        securityStore = new FileSecurityStore();

        builder.setSecurityStore(securityStore);

        LeshanServer lwserver = builder.build();


        createAndStartWebServer(webAdress,lwserver,publicKey,privateKey,securityStore,LOG,builder);

        lwserver.start();

        clientRegistration(lwserver);

    }

    private static void createAndStartWebServer(String webAddress ,LeshanServer lwServer,PublicKey publicKey,PrivateKey privateKey,EditableSecurityStore securityStore,Logger LOG,LeshanServerBuilder builder) throws Exception {



        InetSocketAddress jettyAddr;
        if (webAddress == null) {
            jettyAddr = new InetSocketAddress(8081);
        } else {
            jettyAddr = new InetSocketAddress(webAddress, 8081);
        }
        Server webServer = new Server(jettyAddr);
        WebAppContext root = new WebAppContext();
        root.setContextPath("/");
        root.setResourceBase(Main.class.getClassLoader().getResource("webapp").toExternalForm());
        root.setParentLoaderPriority(true);
        webServer.setHandler(root);

        // Create Servlet
        EventServlet eventServlet = new EventServlet(lwServer, lwServer.getSecuredAddress().getPort());
        ServletHolder eventServletHolder = new ServletHolder(eventServlet);
        root.addServlet(eventServletHolder, "/event/*");

        ServletHolder clientServletHolder = new ServletHolder(new ClientServlet(lwServer));
        root.addServlet(clientServletHolder, "/api/clients/*");

        ServletHolder securityServletHolder;

            securityServletHolder = new ServletHolder(new SecurityServlet(securityStore, publicKey));

        root.addServlet(securityServletHolder, "/api/security/*");

        ServletHolder objectSpecServletHolder = new ServletHolder(
                new ObjectSpecServlet(lwServer.getModelProvider(), lwServer.getRegistrationService()));
        root.addServlet(objectSpecServletHolder, "/api/objectspecs/*");

        webServer.start();

    }

    private static void clientRegistration(final LeshanServer server) {
        server.getRegistrationService().addListener(new RegistrationListener() {

            public void registered(Registration registration, Registration previousReg,
                                   Collection<Observation> previousObsersations) {
                System.out.println("new device: " + registration.getEndpoint());
                try {
                    ReadResponse response = server.send(registration, new ReadRequest(3,0,13));
                    if (response.isSuccess()) {
                        System.out.println("Device time:" + ((LwM2mResource)response.getContent()).getValue());
                    }else {
                        System.out.println("Failed to read:" + response.getCode() + " " + response.getErrorMessage());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            public void updated(RegistrationUpdate update, Registration updatedReg, Registration previousReg) {
                System.out.println("device is still here: " + updatedReg.getEndpoint());
            }

            public void unregistered(Registration registration, Collection<Observation> observations, boolean expired,
                                     Registration newReg) {
                System.out.println("device left: " + registration.getEndpoint());
            }
        });
    }
}