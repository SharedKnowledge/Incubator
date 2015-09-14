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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;
import net.sharkfw.descriptor.base.DescriptorEngine;
import net.sharkfw.descriptor.base.SpaceDescriptor;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.filesystem.FSSharkKB;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import static net.sharkfw.test.subspace.AbstractDescriptorTest.JAVA_NAME;
import net.sharkfw.test.util.DescriptorDummyFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public class DescriptorEngineTest extends AbstractDescriptorTest
{
    private static final int NOT_FOUND = -1;

    private static final String TEST_DIRECTORY = System.getProperty("user.home") + File.separator + "__tmp_shark_test_kb";

    private static final SimpleFileVisitor<Path> DELETE_VISITOR = new SimpleFileVisitor<Path>()
    {
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException
        {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(final Path file, final IOException ex) throws IOException
        {
            // try to delete the file anyway, even if its attributes
            // could not be read, since delete-only access is theoretically possible
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException ex) throws IOException
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
    public void saveAndLoadTest() throws SharkKBException, JAXBException
    {
        final SharkKB sharkKB = new InMemoSharkKB();
        final DescriptorEngine engine = new DescriptorEngine(sharkKB);
        final Set<SpaceDescriptor> descriptors = new HashSet<>();
        Set<SpaceDescriptor> savedDescriptors;

        final SpaceDescriptor javaDescriptor = DescriptorDummyFactory.createSimpleDescriptor(JAVA_NAME, JAVA_SI);
        descriptors.add(javaDescriptor);
        final SpaceDescriptor teapotDescriptors = DescriptorDummyFactory.createSimpleDescriptor(TEAPOT_NAME, TEAPOT_SI);
        teapotDescriptors.setParent(javaDescriptor);
        descriptors.add(teapotDescriptors);
        engine.saveDescriptor(descriptors);

        savedDescriptors = engine.getDescriptors();
        Assert.assertEquals(descriptors.size(), savedDescriptors.size());
        final List<SpaceDescriptor> savedDescriptorsAsList = new ArrayList<>(savedDescriptors);
        for (SpaceDescriptor descriptor : descriptors)
        {
            final boolean contains = savedDescriptors.contains(descriptor);
            Assert.assertTrue(contains);
            final int index = savedDescriptorsAsList.indexOf(descriptor);
            if (index != NOT_FOUND)
            {
                SpaceDescriptor savedDescriptor = savedDescriptorsAsList.get(index);
                Assert.assertEquals(descriptor, savedDescriptor);
                Assert.assertEquals(descriptor.getId(), savedDescriptor.getId());
                Assert.assertEquals(descriptor.getParent(), savedDescriptor.getParent());
                final SharkCS descriptorContext = descriptor.getContext();
                final SharkCS deserializedDescriptorContext = savedDescriptor.getContext();
                final boolean identical = SharkCSAlgebra.identical(descriptorContext, deserializedDescriptorContext);
                Assert.assertTrue(identical);
            } else
            {
                Assert.fail("Descriptor not found in original List.");
            }
        }

        final int size = descriptors.size();
        descriptors.remove(teapotDescriptors);
        engine.saveDescriptor(descriptors);
        savedDescriptors = engine.getDescriptors();
        Assert.assertEquals(size - 1, savedDescriptors.size());

        descriptors.clear();
        engine.saveDescriptor(descriptors);
        savedDescriptors = engine.getDescriptors();
        Assert.assertEquals(0, savedDescriptors.size());
        Assert.assertTrue(savedDescriptors.isEmpty());

        System.out.println(descriptors.size());

    }

}
