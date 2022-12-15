package railml.exceptions;

public class RailMlBufferException extends Exception
{

    /**
     * <p>
     * Create a new <code>RailMlBufferException</code> with no specified detail
     * message and cause.</p>
     */
    public RailMlBufferException()
    {
        super();
    }

    /**
     * <p>
     * Create a new <code>RailMlBufferException</code> with the specified detail
     * message.</p>
     *
     * @param message The detail message.
     */
    public RailMlBufferException( String message )
    {
        super( message );
    }

    /**
     * <p>
     * Create a new <code>RailMlBufferException</code> with the specified detail
     * message and cause.</p>
     *
     * @param message The detail message.
     * @param cause   The cause. A <code>null</code> value is permitted, and
     *                indicates that the cause is nonexistent or unknown.
     */
    public RailMlBufferException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * <p>
     * Create a new <code>RailMlBufferException</code> with the specified
     * cause.</p>
     *
     * @param cause The cause. A <code>null</code> value is permitted, and
     *              indicates that the cause is nonexistent or unknown.
     */
    public RailMlBufferException( Throwable cause )
    {
        super( cause );
    }

}
