/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.test.subspace;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.filesystem.FSSharkKB;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.subspace.knowledgeBase.StandardSubSpace;
import net.sharkfw.subspace.knowledgeBase.SubSpace;
import net.sharkfw.xml.jaxb.JAXBSerializer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jgrundma
 */
public class SubSpaceAlgebraTest extends AbstractSubSpaceTest
{

    private static final String TEST_DIRECTORY = System.getProperty("user.home") + File.separator + "__tmp_shark_test_kb";

//    private static class StandardSubSpaceFactory implements SubSpaceFactory<SyncKB>
//    {
//
//        private final SharkCS context;
//
//        public StandardSubSpaceFactory(final SharkCS context)
//        {
//            this.context = context;
//        }
//
//        @Override
//        public SubSpace createSubSpace(final SemanticTag description, final SyncKB base)
//        {
//            return new StandardSubSpace(base, context, description);
//        }
//    }
    private static final SimpleFileVisitor DELETE_VISITOR = new SimpleFileVisitor<Path>()
    {

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException
        {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException ex) throws IOException
        {
            // try to delete the file anyway, even if its attributes
            // could not be read, since delete-only access is theoretically possible
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException ex) throws IOException
        {
            if (ex == null)
            {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            } else
            {
                // directory iteration failed; propagate exception
                throw ex;
            }
        }
    };

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
    public static void tearDownClass() throws IOException
    {
        final File file = new File(TEST_DIRECTORY);
        if (file.exists() && file.isDirectory())
        {
            final Path path = file.toPath();
            Files.walkFileTree(path, DELETE_VISITOR);
        }
    }

    @Test
    public void assimilateAndExtractionTest() throws SharkKBException, IOException, JAXBException
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
        final SubSpace javaSS = new StandardSubSpace(javaCC, java);
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
        final SubSpace teapotSS = new StandardSubSpace(teaportCC, teapot);
        subspaces.add(teapotSS);

        final JAXBSerializer serializer = JAXBSerializer.INSTANCE;
        final String javaXml = serializer.serializeSubSpace(javaSS);
        System.out.println("JavaSS XML:");
        System.out.println(javaXml);
        
        final String teapotXml = serializer.serializeSubSpace(teapotSS);
        System.out.println("TeapotSS XML:");
        System.out.println(teapotXml);
        
        final String listXml = serializer.serializeSubSpaceList(subspaces);
        System.out.println("List XML:");
        System.out.println(listXml);
        

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
