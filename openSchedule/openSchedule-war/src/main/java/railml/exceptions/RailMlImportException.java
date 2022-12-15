package railml.exceptions;

public class RailMlImportException extends Exception
{

    /**
     * <p>
     * Create a new <code>RailMlImportException</code> with no specified detail
     * message and cause.</p>
     */
    public RailMlImportException()
    {
        super();
    }

    /**
     * <p>
     * Create a new <code>RailMlImportException</code> with the specified detail
     * message.</p>
     *
     * @param message The detail message.
     */
    public RailMlImportException( String message )
    {
        super( message );
    }

    /**
     * <p>
     * Create a new <code>RailMlImportException</code> with the specified detail
     * message and cause.</p>
     *
     * @param message The detail message.
     * @param cause   The cause. A <code>null</code> value is permitted, and
     *                indicates that the cause is nonexistent or unknown.
     */
    public RailMlImportException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * <p>
     * Create a new <code>RailMlImportException</code> with the specified
     * cause.</p>
     *
     * @param cause The cause. A <code>null</code> value is permitted, and
     *              indicates that the cause is nonexistent or unknown.
     */
    public RailMlImportException( Throwable cause )
    {
        super( cause );
    }

}
