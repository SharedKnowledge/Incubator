package net.sharkfw.test.descriptor;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchema;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchemaException;
import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.system.L;
import static net.sharkfw.test.descriptor.AbstractDescriptorTest.JAVA_NAME;
import net.sharkfw.test.util.DescriptorDummyFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public class DescriptorSchemaTest extends AbstractDescriptorTest
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
    public void saveAndLoadTest() throws SharkKBException, JAXBException, DescriptorSchemaException
    {
        final SharkKB sharkKB = new InMemoSharkKB();
        final DescriptorSchema schema = new DescriptorSchema(sharkKB);
        final Set<ContextSpaceDescriptor> descriptors = new HashSet<>();
        Set<ContextSpaceDescriptor> savedDescriptors;

        final ContextSpaceDescriptor javaDescriptor = DescriptorDummyFactory.createSimpleDescriptor(JAVA_NAME, JAVA_SI);
        descriptors.add(javaDescriptor);
        schema.saveDescriptor(javaDescriptor);
        final ContextSpaceDescriptor teapotDescriptor = DescriptorDummyFactory.createSimpleDescriptor(TEAPOT_NAME, TEAPOT_SI);
        descriptors.add(teapotDescriptor);

        schema.saveDescriptors(descriptors);
        schema.setParent(teapotDescriptor, javaDescriptor);

        savedDescriptors = schema.getDescriptors();
        Assert.assertEquals(descriptors.size(), savedDescriptors.size());
        schema.saveDescriptor(javaDescriptor);
        Assert.assertEquals(descriptors.size(), savedDescriptors.size());

        final List<ContextSpaceDescriptor> savedDescriptorsAsList = new ArrayList<>(savedDescriptors);
        for (ContextSpaceDescriptor descriptor : descriptors)
        {
            final boolean contains = savedDescriptors.contains(descriptor);
            Assert.assertTrue(contains);
            final int index = savedDescriptorsAsList.indexOf(descriptor);
            if (index != NOT_FOUND)
            {
                ContextSpaceDescriptor savedDescriptor = savedDescriptorsAsList.get(index);
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
        schema.removeDescriptor(teapotDescriptor);
        savedDescriptors = schema.getDescriptors();
        Assert.assertEquals(size - 1, savedDescriptors.size());

        schema.clearDescriptors();
        savedDescriptors = schema.getDescriptors();
        Assert.assertEquals(0, savedDescriptors.size());
        Assert.assertTrue(savedDescriptors.isEmpty());
    }

    @Test
    public void errorTest() throws SharkKBException, DescriptorSchemaException
    {
        final SharkKB sharkKB = new InMemoSharkKB();
        final DescriptorSchema schema = new DescriptorSchema(sharkKB);
        final Set<ContextSpaceDescriptor> descriptors = new HashSet<>();

        final ContextSpaceDescriptor javaDescriptor = DescriptorDummyFactory.createSimpleDescriptor(JAVA_NAME, JAVA_SI);
        descriptors.add(javaDescriptor);
        final ContextSpaceDescriptor teapotDescriptor = DescriptorDummyFactory.createSimpleDescriptor(TEAPOT_NAME, TEAPOT_SI);
        descriptors.add(teapotDescriptor);
        // Adding Parent here  must throw an IllegalArgumentException because teapotDescriptors is not in Schema
        boolean notInSchemaError = false;
        try
        {
            schema.setParent(javaDescriptor, teapotDescriptor);
        } catch (DescriptorSchemaException e)
        {
            L.d(e.getMessage());
            notInSchemaError = true;
        }
        Assert.assertTrue(notInSchemaError);

        schema.saveDescriptors(descriptors);
        schema.setParent(javaDescriptor, teapotDescriptor);

        // Adding Parent here  must throw an IllegalArgumentException because it would create a loop
        boolean loopError = false;
        try
        {
            schema.setParent(teapotDescriptor, javaDescriptor);
        } catch (DescriptorSchemaException e)
        {
            L.d(e.getMessage());
            loopError = true;
        }
        Assert.assertTrue(loopError);

        schema.removeDescriptor(javaDescriptor);
        // Clearing Parent here  must throw an IllegalArgumentException because javaDescriptor is not in Schema
        boolean clearError = false;
        try
        {
            schema.clearParent(javaDescriptor);
        } catch (DescriptorSchemaException e)
        {
            L.d(e.getMessage());
            clearError = true;
        }
        Assert.assertTrue(clearError);
    }

    @Test
    public void relationshipTest() throws DescriptorSchemaException, SharkKBException
    {
        final SharkKB sharkKB = new InMemoSharkKB();
        final DescriptorSchema schema = new DescriptorSchema(sharkKB);
        final Set<ContextSpaceDescriptor> descriptors = new HashSet<>();

        final ContextSpaceDescriptor javaDescriptor = DescriptorDummyFactory.createSimpleDescriptor(JAVA_NAME, JAVA_SI);
        descriptors.add(javaDescriptor);
        final ContextSpaceDescriptor teapotDescriptor = DescriptorDummyFactory.createSimpleDescriptor(TEAPOT_NAME, TEAPOT_SI);
        descriptors.add(teapotDescriptor);
        schema.saveDescriptors(descriptors);

        schema.setParent(javaDescriptor, teapotDescriptor);
        final ContextSpaceDescriptor javaDescriptorWithParent = schema.getDescriptor(javaDescriptor.getId());
        Assert.assertEquals(javaDescriptorWithParent.getParent(), teapotDescriptor.getId());
        final ContextSpaceDescriptor teapotDescriptorAsParent = schema.getParent(javaDescriptor);
        Assert.assertEquals(teapotDescriptor, teapotDescriptorAsParent);
        final Set<ContextSpaceDescriptor> teapotChildren = schema.getChildren(teapotDescriptor);
        Assert.assertTrue(teapotChildren.contains(javaDescriptor));

        schema.clearParent(javaDescriptor);
        final ContextSpaceDescriptor javaDescriptorCleared = schema.getDescriptor(javaDescriptor.getId());
        Assert.assertFalse(javaDescriptorCleared.hasParent());
        final Set<ContextSpaceDescriptor> emptyChildren = schema.getChildren(teapotDescriptor);
        Assert.assertTrue(emptyChildren.isEmpty());

        schema.addChild(javaDescriptor, teapotDescriptor);
        final ContextSpaceDescriptor teapotDescriptorWithParent = schema.getDescriptor(teapotDescriptor.getId());
        Assert.assertEquals(teapotDescriptorWithParent.getParent(), javaDescriptor.getId());

        final ContextSpaceDescriptor javaDescriptorAsParent = schema.getParent(teapotDescriptor);
        Assert.assertEquals(javaDescriptor, javaDescriptorAsParent);
        final Set<ContextSpaceDescriptor> javaChildren = schema.getChildren(javaDescriptor);
        Assert.assertTrue(javaChildren.contains(teapotDescriptor));
    }

    @Test
    public void containsTest() throws SharkKBException, DescriptorSchemaException
    {
        final SharkKB sharkKB = new InMemoSharkKB();
        final DescriptorSchema schema = new DescriptorSchema(sharkKB);

        final ContextSpaceDescriptor javaDescriptor = DescriptorDummyFactory.createSimpleDescriptor(JAVA_NAME, JAVA_SI);
        schema.saveDescriptor(javaDescriptor);
        final ContextSpaceDescriptor javaNotIdeticalDescriptor = new ContextSpaceDescriptor(JAVA_SI);
        final ContextSpaceDescriptor teapotDescriptor = DescriptorDummyFactory.createSimpleDescriptor(TEAPOT_NAME, TEAPOT_SI);

        Assert.assertTrue(schema.contains(javaDescriptor));
        Assert.assertTrue(schema.contains(javaNotIdeticalDescriptor));
        Assert.assertFalse(schema.contains(teapotDescriptor));
        Assert.assertTrue(schema.containsIdentical(javaDescriptor));
        Assert.assertFalse(schema.containsIdentical(teapotDescriptor));
        Assert.assertFalse(schema.containsIdentical(javaNotIdeticalDescriptor));
    }

}
