package net.sharkfw.descriptor.knowledgeBase;

/**
 * An {@link Exception} that signals an error in the {@link DescriptorShema}
 * class. 
 *
 * @author Nitros Razril (pseudonym)
 */
@SuppressWarnings("serial")
public class DescriptorSchemaException extends Exception
{

    /**
     * Constructs an instance of <code>DescriptorShemaException</code> with the
     * specified detail message.
     *
     * @param message The detail message.
     */
    public DescriptorSchemaException(final String message)
    {
        super(message);
    }

    /**
     * Constructs an instance of <code>DescriptorShemaException</code> with a
     * specified {@link Throwable} as cause.
     *
     * @param cause Cause of this Exception.
     */
    public DescriptorSchemaException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructs an instance of <code>DescriptorShemaException</code> with the
     * specified detail message and {@link Throwable} as cause.
     *
     * @param message The detail message.
     * @param cause Cause of this Exception.
     */
    public DescriptorSchemaException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

}
