package net.sharkfw.test.descriptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchema;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchemaException;
import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.system.L;
import static net.sharkfw.test.descriptor.AbstractDescriptorTest.JAVA_NAME;
import net.sharkfw.test.util.Dummy;
import net.sharkfw.test.util.DummyDataFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * A class to test the {@link DescriptorSchema} class.
 *
 * @author Nitros Razril (pseudonym)
 */
public class DescriptorSchemaTest extends AbstractDescriptorTest
{

    /**
     * ID of a {@link ContextSpaceDescriptor} that is empty.
     */
    private final static String FIRST_EMPTY_ID = "FIRST_EMPTY";
    /**
     * ID of another {@link ContextSpaceDescriptor} that is empty.
     */
    private final static String SECOND_EMPTY_ID = "SECOND_EMPTY";
    /**
     * Returned if an index was not found.
     */
    private static final int NOT_FOUND = -1;

    /**
     * Save a {@link ContextSpaceDescriptor}. It should be in the
     * {@link DescriptorSchema} now.
     */
    @Test
    public void saveAndLoadTest() throws SharkKBException, JAXBException, DescriptorSchemaException
    {
        final SharkKB sharkKB = new InMemoSharkKB();
        final DescriptorSchema schema = new DescriptorSchema(sharkKB);
        final Set<ContextSpaceDescriptor> descriptors = new HashSet<>();
        Set<ContextSpaceDescriptor> savedDescriptors;

        final ContextSpaceDescriptor javaDescriptor = DummyDataFactory.createSimpleDescriptor(JAVA_NAME, JAVA_SI);
        descriptors.add(javaDescriptor);
        schema.saveDescriptor(javaDescriptor);
        final ContextSpaceDescriptor teapotDescriptor = DummyDataFactory.createSimpleDescriptor(TEAPOT_NAME, TEAPOT_SI);
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

    /**
     * Tests if
     * {@link DescriptorSchema#setParent(ContextSpaceDescriptor, ContextSpaceDescriptor)}
     * throws the described exceptions
     */
    @Test
    public void errorTest() throws SharkKBException, DescriptorSchemaException
    {
        final SharkKB sharkKB = new InMemoSharkKB();
        final DescriptorSchema schema = new DescriptorSchema(sharkKB);
        final Set<ContextSpaceDescriptor> descriptors = new HashSet<>();

        final ContextSpaceDescriptor javaDescriptor = DummyDataFactory.createSimpleDescriptor(JAVA_NAME, JAVA_SI);
        descriptors.add(javaDescriptor);
        final ContextSpaceDescriptor teapotDescriptor = DummyDataFactory.createSimpleDescriptor(TEAPOT_NAME, TEAPOT_SI);
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

    /**
     * Tests if relationships are correctly set.
     */
    @Test
    public void relationshipTest() throws DescriptorSchemaException, SharkKBException
    {
        final SharkKB sharkKB = new InMemoSharkKB();
        final DescriptorSchema schema = new DescriptorSchema(sharkKB);
        final Set<ContextSpaceDescriptor> descriptors = new HashSet<>();

        final ContextSpaceDescriptor javaDescriptor = DummyDataFactory.createSimpleDescriptor(JAVA_NAME, JAVA_SI);
        descriptors.add(javaDescriptor);
        final ContextSpaceDescriptor teapotDescriptor = DummyDataFactory.createSimpleDescriptor(TEAPOT_NAME, TEAPOT_SI);
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

    /**
     * Tests the {@link DescriptorSchema} methods that begin with contains.
     */
    @Test
    public void containsTest() throws SharkKBException, DescriptorSchemaException
    {
        final SharkKB sharkKB = new InMemoSharkKB();
        final DescriptorSchema schema = new DescriptorSchema(sharkKB);

        final ContextSpaceDescriptor javaDescriptor = DummyDataFactory.createSimpleDescriptor(JAVA_NAME, JAVA_SI);
        schema.saveDescriptor(javaDescriptor);
        final ContextSpaceDescriptor javaNotIdeticalDescriptor = new ContextSpaceDescriptor(JAVA_SI);
        final ContextSpaceDescriptor teapotDescriptor = DummyDataFactory.createSimpleDescriptor(TEAPOT_NAME, TEAPOT_SI);

        Assert.assertTrue(schema.contains(javaDescriptor));
        Assert.assertTrue(schema.contains(javaNotIdeticalDescriptor));
        Assert.assertFalse(schema.contains(teapotDescriptor));
        Assert.assertTrue(schema.containsIdentical(javaDescriptor));
        Assert.assertFalse(schema.containsIdentical(teapotDescriptor));
        Assert.assertFalse(schema.containsIdentical(javaNotIdeticalDescriptor));
    }

    /**
     * Tests if you can properly get tree and subtree. Uses the following tree, 
     * read from top to bottom:<br/><br/>
     *
     * <table border="1">
     * <tr><td align="center" colspan="2">Deutschland</td></tr>
     * <tr><td align="center" colspan="2">First Empty Descriptor</td></tr>
     * <tr><td align="center" colspan="2">Java</td></tr>
     * <tr>
     * <td align="center">Second Empty Descriptor</td>
     * <td align="center">Teapot</td>
     * </tr>
     * <tr>
     * <td align="center">C</td>
     * <td align="center"></td>
     * </tr>
     * </table>
     *
     */
    @Test
    public void treeTest() throws SharkKBException, DescriptorSchemaException
    {

        final SharkKB sharkKB = new Dummy(ALICE_NAME, ALICE_SI).getKnowledgeBase();
        final DescriptorSchema schema = new DescriptorSchema(sharkKB);

        final STSet javaSet = new InMemoSTSet();
        javaSet.createSemanticTag(JAVA_NAME, JAVA_SI);

        final STSet teapotSet = new InMemoSTSet();
        teapotSet.createSemanticTag(TEAPOT_NAME, TEAPOT_SI);

        final STSet cSet = new InMemoSTSet();
        cSet.createSemanticTag(C_NAME, C_SI);

        final STSet deutschlandSet = new InMemoSTSet();
        deutschlandSet.createSemanticTag(DEUTSCHLAND_NAME, DEUTSCHLAND_SI);

        final SharkCS javaContext = new InMemoSharkKB().createInterest(
                javaSet,
                null,
                null,
                null,
                null,
                null,
                SharkCS.DIRECTION_INOUT
        );
        final ContextSpaceDescriptor javaDescriptor = new ContextSpaceDescriptor(javaContext, JAVA_SI);
        schema.saveDescriptor(javaDescriptor);

        final SharkCS teapotContext = new InMemoSharkKB().createInterest(
                teapotSet,
                null,
                null,
                null,
                null,
                null,
                SharkCS.DIRECTION_INOUT
        );
        final ContextSpaceDescriptor teapotDescriptor = new ContextSpaceDescriptor(teapotContext, TEAPOT_SI);
        schema.saveDescriptor(teapotDescriptor);

        final SharkCS cContext = new InMemoSharkKB().createInterest(
                cSet,
                null,
                null,
                null,
                null,
                null,
                SharkCS.DIRECTION_INOUT
        );
        final ContextSpaceDescriptor cDescriptor = new ContextSpaceDescriptor(cContext, C_SI);
        schema.saveDescriptor(cDescriptor);

        final SharkCS deuschlandContext = new InMemoSharkKB().createInterest(
                deutschlandSet,
                null,
                null,
                null,
                null,
                null,
                SharkCS.DIRECTION_INOUT
        );

        final ContextSpaceDescriptor deutschlandDescriptor = new ContextSpaceDescriptor(deuschlandContext, DEUTSCHLAND_SI);
        schema.saveDescriptor(deutschlandDescriptor);
        final ContextSpaceDescriptor firstEmptyDescriptor = new ContextSpaceDescriptor(FIRST_EMPTY_ID);
        schema.saveDescriptor(firstEmptyDescriptor);
        final ContextSpaceDescriptor secondEmptyDescriptor = new ContextSpaceDescriptor(SECOND_EMPTY_ID);
        schema.saveDescriptor(secondEmptyDescriptor);

        schema.addChild(deutschlandDescriptor, firstEmptyDescriptor);
        schema.setParent(javaDescriptor, firstEmptyDescriptor);
        schema.addChild(javaDescriptor, teapotDescriptor);
        schema.addChild(javaDescriptor, secondEmptyDescriptor);
        schema.setParent(cDescriptor, secondEmptyDescriptor);

        final Set<ContextSpaceDescriptor> tree = schema.getTree(teapotDescriptor);
        Assert.assertTrue(tree.contains(teapotDescriptor));
        Assert.assertTrue(tree.contains(javaDescriptor));
        Assert.assertTrue(tree.contains(cDescriptor));
        Assert.assertTrue(tree.contains(secondEmptyDescriptor));
        Assert.assertTrue(tree.contains(firstEmptyDescriptor));
        Assert.assertTrue(tree.contains(deutschlandDescriptor));

        final Set<ContextSpaceDescriptor> subtree = schema.getSubtree(javaDescriptor);
        Assert.assertTrue(subtree.contains(teapotDescriptor));
        Assert.assertTrue(subtree.contains(javaDescriptor));
        Assert.assertTrue(subtree.contains(cDescriptor));
        Assert.assertTrue(subtree.contains(secondEmptyDescriptor));
        Assert.assertFalse(subtree.contains(firstEmptyDescriptor));
        Assert.assertFalse(subtree.contains(deutschlandDescriptor));
    }

    /**
     * Test if the override methods properly override a
     * {@link ContextSpaceDescriptor}
     */
    @Test
    public void overrideTest() throws SharkKBException, DescriptorSchemaException
    {
        final SharkKB sharkKB = new Dummy(ALICE_NAME, ALICE_SI).getKnowledgeBase();
        final DescriptorSchema schema = new DescriptorSchema(sharkKB);

        final ContextSpaceDescriptor firstDescriptor = DummyDataFactory.createSimpleDescriptor(JAVA_NAME, JAVA_SI);
        schema.saveDescriptor(firstDescriptor);
        final ContextSpaceDescriptor secondDescriptor = DummyDataFactory.createSimpleDescriptor(JAVA_NAME, JAVA_SI);
        secondDescriptor.getContext().getTopics().createSemanticTag(C_NAME, C_SI);

        Assert.assertTrue(schema.containsIdentical(firstDescriptor));
        Assert.assertFalse(schema.containsIdentical(secondDescriptor));
        Assert.assertTrue(schema.contains(firstDescriptor));
        Assert.assertTrue(schema.contains(secondDescriptor));

        schema.overrideDescriptor(secondDescriptor);

        Assert.assertFalse(schema.containsIdentical(firstDescriptor));
        Assert.assertTrue(schema.containsIdentical(secondDescriptor));
        Assert.assertTrue(schema.contains(firstDescriptor));
        Assert.assertTrue(schema.contains(secondDescriptor));
    }
}
