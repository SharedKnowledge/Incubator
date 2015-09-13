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
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.filesystem.FSSharkKB;
import net.sharkfw.subspace.knowledgeBase.SubSpace;
import net.sharkfw.subspace.knowledgeBase.SubSpaceFactory;
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

    private static final String TEST_DIRECTORY = System.getProperty("user.home") + File.separator + "__tmp_shark_test_kb";

    private static final SimpleFileVisitor<Path> DELETE_VISITOR = new SimpleFileVisitor<Path>()
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

}
