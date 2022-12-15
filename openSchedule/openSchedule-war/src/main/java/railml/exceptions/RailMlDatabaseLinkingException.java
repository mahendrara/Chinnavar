package railml.exceptions;

public class RailMlDatabaseLinkingException extends Exception
{

    /**
     * <p>
     * Create a new <code>RailMlDatabaseLinkingException</code> with no
     * specified detail message and cause.</p>
     */
    public RailMlDatabaseLinkingException()
    {
        super();
    }

    /**
     * <p>
     * Create a new <code>RailMlDatabaseLinkingException</code> with the
     * specified detail message.</p>
     *
     * @param message The detail message.
     */
    public RailMlDatabaseLinkingException( String message )
    {
        super( message );
    }

    /**
     * <p>
     * Create a new <code>RailMlDatabaseLinkingException</code> with the
     * specified detail message and cause.</p>
     *
     * @param message The detail message.
     * @param cause   The cause. A <code>null</code> value is permitted, and
     *                indicates that the cause is nonexistent or unknown.
     */
    public RailMlDatabaseLinkingException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * <p>
     * Create a new <code>RailMlDatabaseLinkingException</code> with the
     * specified cause.</p>
     *
     * @param cause The cause. A <code>null</code> value is permitted, and
     *              indicates that the cause is nonexistent or unknown.
     */
    public RailMlDatabaseLinkingException( Throwable cause )
    {
        super( cause );
    }
}
