package railml.exceptions;

public class RailMlServiceImportException extends Exception
{

    /**
     * <p>
     * Create a new <code>RailMlServiceImportException</code> with no specified detail
     * message and cause.</p>
     */
    public RailMlServiceImportException()
    {
        super();
    }

    /**
     * <p>
     * Create a new <code>RailMlServiceImportException</code> with the specified detail
     * message.</p>
     *
     * @param message The detail message.
     */
    public RailMlServiceImportException( String message )
    {
        super( message );
    }

    /**
     * <p>
     * Create a new <code>RailMlServiceImportException</code> with the specified detail
     * message and cause.</p>
     *
     * @param message The detail message.
     * @param cause   The cause. A <code>null</code> value is permitted, and
     *                indicates that the cause is nonexistent or unknown.
     */
    public RailMlServiceImportException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * <p>
     * Create a new <code>RailMlServiceImportException</code> with the specified
     * cause.</p>
     *
     * @param cause The cause. A <code>null</code> value is permitted, and
     *              indicates that the cause is nonexistent or unknown.
     */
    public RailMlServiceImportException( Throwable cause )
    {
        super( cause );
    }

}
