package railml.exceptions;

public class RailMlServiceExportException extends Exception
{

    /**
     * <p>
     * Create a new <code>RailMlServiceExportException</code> with no specified
     * detail message and cause.</p>
     */
    public RailMlServiceExportException()
    {
        super();
    }

    /**
     * <p>
     * Create a new <code>RailMlServiceExportException</code> with the specified
     * detail message.</p>
     *
     * @param message The detail message.
     */
    public RailMlServiceExportException( String message )
    {
        super( message );
    }

    /**
     * <p>
     * Create a new <code>RailMlServiceExportException</code> with the specified
     * detail message and cause.</p>
     *
     * @param message The detail message.
     * @param cause   The cause. A <code>null</code> value is permitted, and
     *                indicates that the cause is nonexistent or unknown.
     */
    public RailMlServiceExportException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * <p>
     * Create a new <code>RailMlServiceExportException</code> with the specified
     * cause.</p>
     *
     * @param cause The cause. A <code>null</code> value is permitted, and
     *              indicates that the cause is nonexistent or unknown.
     */
    public RailMlServiceExportException( Throwable cause )
    {
        super( cause );
    }

}
