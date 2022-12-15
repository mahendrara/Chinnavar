package railml.exceptions;

public class RailMlConversionException extends Exception
{

    /**
     * <p>
     * Create a new <code>RailMlConversionException</code> with no specified
     * detail message and cause.</p>
     */
    public RailMlConversionException()
    {
        super();
    }

    /**
     * <p>
     * Create a new <code>RailMlConversionException</code> with the specified
     * detail message.</p>
     *
     * @param message The detail message.
     */
    public RailMlConversionException( String message )
    {
        super( message );
    }

    /**
     * <p>
     * Create a new <code>RailMlConversionException</code> with the specified
     * detail message and cause.</p>
     *
     * @param message The detail message.
     * @param cause   The cause. A <code>null</code> value is permitted, and
     *                indicates that the cause is nonexistent or unknown.
     */
    public RailMlConversionException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * <p>
     * Create a new <code>RailMlConversionException</code> with the specified
     * cause.</p>
     *
     * @param cause The cause. A <code>null</code> value is permitted, and
     *              indicates that the cause is nonexistent or unknown.
     */
    public RailMlConversionException( Throwable cause )
    {
        super( cause );
    }
}
