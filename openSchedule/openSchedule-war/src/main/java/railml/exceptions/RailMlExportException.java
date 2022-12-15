package railml.exceptions;

public class RailMlExportException extends Exception
{

    /**
     * <p>
     * Create a new <code>RailMlExportException</code> with no specified detail
     * message and cause.</p>
     */
    public RailMlExportException()
    {
        super();
    }

    /**
     * <p>
     * Create a new <code>RailMlExportException</code> with the specified detail
     * message.</p>
     *
     * @param message The detail message.
     */
    public RailMlExportException( String message )
    {
        super( message );
    }

    /**
     * <p>
     * Create a new <code>RailMlExportException</code> with the specified detail
     * message and cause.</p>
     *
     * @param message The detail message.
     * @param cause   The cause. A <code>null</code> value is permitted, and
     *                indicates that the cause is nonexistent or unknown.
     */
    public RailMlExportException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * <p>
     * Create a new <code>RailMlExportException</code> with the specified
     * cause.</p>
     *
     * @param cause The cause. A <code>null</code> value is permitted, and
     *              indicates that the cause is nonexistent or unknown.
     */
    public RailMlExportException( Throwable cause )
    {
        super( cause );
    }

}
