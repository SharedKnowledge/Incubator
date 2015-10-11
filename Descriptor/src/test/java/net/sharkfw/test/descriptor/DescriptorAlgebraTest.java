package net.sharkfw.test.descriptor;

import java.util.Collections;
import java.util.List;
import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.descriptor.knowledgeBase.DescriptorAlgebra;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchema;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchemaException;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.test.util.Dummy;
import org.junit.Assert;
import org.junit.Test;

/**
 * This class tests the {@link DescriptorAlgebra} class.
 *
 * @author Nitros Razril (pseudonym)
 */
public class DescriptorAlgebraTest extends AbstractDescriptorTest
{

    /**
     * A sting to ad as test information.
     */
    private final static String TEST_INFORMATION = "Hello World!";
    /**
     * ID of a {@link ContextSpaceDescriptor} for Alice.
     */
    private final static String ALICE_ID = "ALICE";
    /**
     * ID of a {@link ContextSpaceDescriptor} that is empty.
     */
    private final static String FIRST_EMPTY_ID = "FIRST_EMPTY";
    /**
     * ID of another {@link ContextSpaceDescriptor} that is empty.
     */
    private final static String SECOND_EMPTY_ID = "SECOND_EMPTY";

    /**
     * Tests normal, tree and subtree extraction. The following tree is used.
     * Read from top to bottom. <br/><br/>
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
    public void extractionTest() throws SharkKBException, DescriptorSchemaException
    {
        final Dummy alice = new Dummy(ALICE_NAME, ALICE_SI);
        final Dummy bob = new Dummy(BOB_NAME, BOB_SI);
        final SharkKB aliceKB = alice.getKnowledgeBase();
        final DescriptorSchema aliceSchema = new DescriptorSchema(aliceKB);
        final STSet javaSet = new InMemoSTSet();
        final SemanticTag javaTopic = javaSet.createSemanticTag(JAVA_NAME, JAVA_SI);
        final ContextCoordinates aliceJavaCC = aliceKB.createContextCoordinates(
                javaTopic,
                alice.getPeer(),
                alice.getPeer(),
                bob.getPeer(),
                null,
                null,
                SharkCS.DIRECTION_INOUT
        );
        final ContextPoint javaCP = aliceKB.createContextPoint(aliceJavaCC);
        final Information javaInformation = javaCP.addInformation(TEST_INFORMATION);

        final SharkCS aliceOriginatorContext = new InMemoSharkKB().createInterest(
                null,
                alice.getPeer(),
                null,
                null,
                null,
                null,
                SharkCS.DIRECTION_INOUT
        );
        final ContextSpaceDescriptor aliceOriginatorDescriptor = new ContextSpaceDescriptor(aliceOriginatorContext, ALICE_ID);
        aliceSchema.saveDescriptor(aliceOriginatorDescriptor);

        final Knowledge aliceOriginatorKnowledge = DescriptorAlgebra.extract(aliceSchema, aliceOriginatorDescriptor);
        Assert.assertEquals(1, aliceOriginatorKnowledge.getNumberOfContextPoints());
        final ContextPoint aliceOriginatorCP = aliceOriginatorKnowledge.getCP(0);
        Assert.assertEquals(aliceOriginatorCP, javaCP);
        Assert.assertEquals(1, aliceOriginatorCP.getNumberInformation());
        final Information aliceOriginatorInformation = aliceOriginatorCP.getInformation().next();
        Assert.assertEquals(TEST_INFORMATION, aliceOriginatorInformation.getContentAsString());
        Assert.assertEquals(javaInformation.getContentAsString(), aliceOriginatorInformation.getContentAsString());
        Assert.assertEquals(javaInformation.creationTime(), aliceOriginatorInformation.creationTime());

        final STSet teapotSet = new InMemoSTSet();
        final SemanticTag teapotTopic = teapotSet.createSemanticTag(TEAPOT_NAME, TEAPOT_SI);
        final ContextCoordinates teapotCC = aliceKB.createContextCoordinates(
                teapotTopic,
                alice.getPeer(),
                alice.getPeer(),
                bob.getPeer(),
                null,
                null,
                SharkCS.DIRECTION_INOUT
        );
        final ContextPoint teapotCP = aliceKB.createContextPoint(teapotCC);
        final Information teapotInformation = teapotCP.addInformation(TEST_INFORMATION);

        final STSet cSet = new InMemoSTSet();
        final SemanticTag cTopic = cSet.createSemanticTag(C_NAME, C_SI);
        final ContextCoordinates cCC = aliceKB.createContextCoordinates(
                cTopic,
                alice.getPeer(),
                alice.getPeer(),
                bob.getPeer(),
                null,
                null,
                SharkCS.DIRECTION_INOUT
        );
        final ContextPoint cCP = aliceKB.createContextPoint(cCC);
        final Information cInformation = cCP.addInformation(TEST_INFORMATION);

        final STSet deutschlandSet = new InMemoSTSet();
        final SemanticTag deutschlandTopic = deutschlandSet.createSemanticTag(DEUTSCHLAND_NAME, DEUTSCHLAND_SI);
        final ContextCoordinates deutschlandCC = aliceKB.createContextCoordinates(
                deutschlandTopic,
                alice.getPeer(),
                alice.getPeer(),
                bob.getPeer(),
                null,
                null,
                SharkCS.DIRECTION_INOUT
        );
        final ContextPoint deutschlandCP = aliceKB.createContextPoint(deutschlandCC);
        final Information deutschlandInformation = deutschlandCP.addInformation(TEST_INFORMATION);

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
        aliceSchema.saveDescriptor(javaDescriptor);

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
        aliceSchema.saveDescriptor(teapotDescriptor);

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
        aliceSchema.saveDescriptor(cDescriptor);

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
        aliceSchema.saveDescriptor(deutschlandDescriptor);

        final ContextSpaceDescriptor firstEmptyDescriptor = new ContextSpaceDescriptor(FIRST_EMPTY_ID);
        aliceSchema.saveDescriptor(firstEmptyDescriptor);
        final ContextSpaceDescriptor secondEmptyDescriptor = new ContextSpaceDescriptor(SECOND_EMPTY_ID);
        aliceSchema.saveDescriptor(secondEmptyDescriptor);

        aliceSchema.addChild(deutschlandDescriptor, firstEmptyDescriptor);
        aliceSchema.setParent(javaDescriptor, firstEmptyDescriptor);
        aliceSchema.addChild(javaDescriptor, teapotDescriptor);
        aliceSchema.addChild(javaDescriptor, secondEmptyDescriptor);
        aliceSchema.setParent(cDescriptor, secondEmptyDescriptor);

        final Knowledge javaSubtreeKnowledge = DescriptorAlgebra.extractWithSubtree(aliceSchema, javaDescriptor);
        Assert.assertEquals(3, javaSubtreeKnowledge.getNumberOfContextPoints());
        final List<ContextPoint> javaSubtreeCPs = Collections.list(javaSubtreeKnowledge.contextPoints());
        Assert.assertTrue(javaSubtreeCPs.contains(javaCP));
        Assert.assertTrue(javaSubtreeCPs.contains(teapotCP));
        Assert.assertTrue(javaSubtreeCPs.contains(cCP));
        Assert.assertFalse(javaSubtreeCPs.contains(deutschlandCP));

        for (ContextPoint javaSubtreeCP : javaSubtreeCPs)
        {
            Assert.assertEquals(1, javaSubtreeCP.getNumberInformation());
            final Information information = javaSubtreeCP.getInformation().next();
            Assert.assertEquals(TEST_INFORMATION, information.getContentAsString());
            boolean machtTime = (information.creationTime() == javaInformation.creationTime());
            machtTime |= (information.creationTime() == teapotInformation.creationTime());
            machtTime |= (information.creationTime() == cInformation.creationTime());
            machtTime |= (information.creationTime() != deutschlandInformation.creationTime());
            Assert.assertTrue(machtTime);
        }

        final Knowledge wholeKnowledge = DescriptorAlgebra.extractWholeTree(aliceSchema, teapotDescriptor);
        Assert.assertEquals(4, wholeKnowledge.getNumberOfContextPoints());
        final List<ContextPoint> wholCPs = Collections.list(wholeKnowledge.contextPoints());
        Assert.assertTrue(wholCPs.contains(javaCP));
        Assert.assertTrue(wholCPs.contains(teapotCP));
        Assert.assertTrue(wholCPs.contains(cCP));
        Assert.assertTrue(wholCPs.contains(deutschlandCP));

        for (ContextPoint wholCP : wholCPs)
        {
            Assert.assertEquals(1, wholCP.getNumberInformation());
            final Information information = wholCP.getInformation().next();
            Assert.assertEquals(TEST_INFORMATION, information.getContentAsString());
            boolean machtTime = (information.creationTime() == javaInformation.creationTime());
            machtTime |= (information.creationTime() == teapotInformation.creationTime());
            machtTime |= (information.creationTime() == cInformation.creationTime());
            machtTime |= (information.creationTime() == deutschlandInformation.creationTime());
            Assert.assertTrue(machtTime);
        }
    }
}
