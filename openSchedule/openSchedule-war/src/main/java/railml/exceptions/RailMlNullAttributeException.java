package railml.exceptions;

public class RailMlNullAttributeException extends Exception
{

    /**
     * <p>
     * Create a new <code>RailMlNullAttributeException</code> with no specified
     * detail message and cause.</p>
     */
    public RailMlNullAttributeException()
    {
        super();
    }

    /**
     * <p>
     * Create a new <code>RailMlNullAttributeException</code> with the specified
     * detail message.</p>
     *
     * @param message The detail message.
     */
    public RailMlNullAttributeException( String message )
    {
        super( message );
    }

    /**
     * <p>
     * Create a new <code>RailMlNullAttributeException</code> with the specified
     * detail message and cause.</p>
     *
     * @param message The detail message.
     * @param cause   The cause. A <code>null</code> value is permitted, and
     *                indicates that the cause is nonexistent or unknown.
     */
    public RailMlNullAttributeException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * <p>
     * Create a new <code>RailMlNullAttributeException</code> with the specified
     * cause.</p>
     *
     * @param cause The cause. A <code>null</code> value is permitted, and
     *              indicates that the cause is nonexistent or unknown.
     */
    public RailMlNullAttributeException( Throwable cause )
    {
        super( cause );
    }

}
