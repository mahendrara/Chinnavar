package railml.exceptions;

public class RailMlNullElementException extends Exception
{

    /**
     * <p>
     * Create a new <code>RailMlNullElementException</code> with no specified
     * detail message and cause.</p>
     */
    public RailMlNullElementException()
    {
        super();
    }

    /**
     * <p>
     * Create a new <code>RailMlNullElementException</code> with the specified
     * detail message.</p>
     *
     * @param message The detail message.
     */
    public RailMlNullElementException( String message )
    {
        super( message );
    }

    /**
     * <p>
     * Create a new <code>RailMlNullElementException</code> with the specified
     * detail message and cause.</p>
     *
     * @param message The detail message.
     * @param cause   The cause. A <code>null</code> value is permitted, and
     *                indicates that the cause is nonexistent or unknown.
     */
    public RailMlNullElementException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * <p>
     * Create a new <code>RailMlNullElementException</code> with the specified
     * cause.</p>
     *
     * @param cause The cause. A <code>null</code> value is permitted, and
     *              indicates that the cause is nonexistent or unknown.
     */
    public RailMlNullElementException( Throwable cause )
    {
        super( cause );
    }

}
