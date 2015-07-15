/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.test.subspace;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.xml.internal.ws.util.pipe.StandalonePipeAssembler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.SubScene;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElements;
import net.sharkfw.kep.format.XMLSerializer;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.filesystem.FSSharkKB;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.subspace.knowledgeBase.StandardSubSpace;
import net.sharkfw.subspace.knowledgeBase.SubSpace;
import net.sharkfw.subspace.knowledgeBase.SubSpaceAlgebra;
import net.sharkfw.subspace.knowledgeBase.SubSpaceFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jgrundma
 */
public class SubSpaceAlgebraTest extends AbstractSubSpaceTest
{

    private static final String TEST_DIRECTORY = System.getProperty("user.home") + "/__tmp_shark_test_kb";

    private static class StandardSubSpaceFactory implements SubSpaceFactory<SyncKB>
    {

        private final SharkCS context;

        public StandardSubSpaceFactory(final SharkCS context)
        {
            this.context = context;
        }

        @Override
        public SubSpace createSubSpace(final SemanticTag description, final SyncKB base)
        {
            return new StandardSubSpace(base, context, description);
        }

    }

    @BeforeClass
    public static void setUpClass()
    {
        final File file = new File(TEST_DIRECTORY);
        if (file.exists() && file.isDirectory())
        {
            throw new IllegalStateException("Cannot create knowledge base on filesystem. File '" + TEST_DIRECTORY + "' already exitsts.");
        }
        final boolean success = file.mkdirs();
        if (!success)
        {
            throw new IllegalStateException("Cannot create knowledge base on filesystem. Method File#mkdirs() failed.");
        }
    }

    @AfterClass
    public static void tearDownClass()
    {
        System.out.println("Hello word 1");
        final File file = new File(TEST_DIRECTORY);
        if (file.exists() && file.isDirectory())
        {
            file.delete();
        }
         System.out.println("Hello word 2");
    }

    @Test
    public void assimilateAndExtractionTest() throws SharkKBException, JsonProcessingException, IOException
    {
        final List<SubSpace> subspaces = new ArrayList<>();
        final FSSharkKB knowledgeBase = new FSSharkKB(TEST_DIRECTORY);
        final SyncKB syncKnowledgeBase = new SyncKB(knowledgeBase);
        final SemanticTag java = SEMANTIC_TAG_FACTORY.createSemanticTag(JAVA_NAME, JAVA_SI);
        final ContextCoordinates javaCC = CONTEX_FACTORY.createContextCoordinates(
                java,
                null,
                null,
                null,
                null,
                null,
                SharkCS.DIRECTION_NOTHING
        );
        final SubSpace javaSS = new StandardSubSpace(syncKnowledgeBase, javaCC, java);
        subspaces.add(javaSS);
        final SemanticTag teapot = SEMANTIC_TAG_FACTORY.createSemanticTag(TEAPOT_NAME, TEAPOT_SI);
        final ContextCoordinates teaportCC = CONTEX_FACTORY.createContextCoordinates(
                teapot,
                null,
                null,
                null,
                null,
                null,
                SharkCS.DIRECTION_NOTHING
        );
        final SubSpace teapotSS = new StandardSubSpace(syncKnowledgeBase, teaportCC, teapot);
        subspaces.add(teapotSS);

        List<String> stuff = new ArrayList();
        XMLSerializer serializer = new XMLSerializer();
        stuff.add(serializer.serializeSharkCS(javaSS.getContext()));
        stuff.add(serializer.serializeSharkCS(teapotSS.getContext()));

        ObjectMapper mapper = new ObjectMapper();
        String string = mapper.writeValueAsString(stuff);
        List<String> stringlist = mapper.readValue(string, mapper.getTypeFactory().constructCollectionType(List.class, String.class));
        System.out.println(string);
        System.out.println(stringlist);
        

        //assimilateSubTest(syncKnowledgeBase, subspaces);
        //extractionSubTest(subspaces);
    }

//    private void assimilateSubTest(final SyncKB syncKnowledgeBase, final List<SubSpace> subspaces) throws SharkKBException
//    {
//        SubSpaceAlgebra<SyncKB> algebra = new SubSpaceAlgebra<>();
//        for (final SubSpace subSpace : subspaces)
//        {
//            algebra.assimilateSubSpace(subSpace, syncKnowledgeBase);
//        }
//        StandardSubSpaceFactory factory = new StandardSubSpaceFactory(null);
//        
//    }
//
//    private void extractionSubTest(final List<SubSpace> subspaces) throws SharkKBException
//    {
//        final FSSharkKB knowledgeBase = new FSSharkKB(TEST_DIRECTORY);
//        final SyncKB syncKnowledgeBase = new SyncKB(knowledgeBase);
//        final STSet subspaceDescriptions = SubSpaceAlgebra.extractSubspaceDescriptions(syncKnowledgeBase);
//        final String[] descriptionSI = description.getSI();
//        final SemanticTag subspaceDescription = subspaceDescriptions.getSemanticTag(descriptionSI);
//        Assert.assertNotNull(subspaceDescription);
//        final boolean identical = subspaceDescription.identical(description);
//        Assert.assertTrue(identical);
//    }
}
