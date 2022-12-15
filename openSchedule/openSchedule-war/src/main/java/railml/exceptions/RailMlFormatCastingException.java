package railml.exceptions;

public class RailMlFormatCastingException extends Exception
{

    /**
     * <p>
     * Create a new <code>RailMlFormatCastingException</code> with no specified
     * detail message and cause.</p>
     */
    public RailMlFormatCastingException()
    {
        super();
    }

    /**
     * <p>
     * Create a new <code>RailMlFormatCastingException</code> with the specified
     * detail message.</p>
     *
     * @param message The detail message.
     */
    public RailMlFormatCastingException( String message )
    {
        super( message );
    }

    /**
     * <p>
     * Create a new <code>RailMlFormatCastingException</code> with the specified
     * detail message and cause.</p>
     *
     * @param message The detail message.
     * @param cause   The cause. A <code>null</code> value is permitted, and
     *                indicates that the cause is nonexistent or unknown.
     */
    public RailMlFormatCastingException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * <p>
     * Create a new <code>RailMlFormatCastingException</code> with the specified
     * cause.</p>
     *
     * @param cause The cause. A <code>null</code> value is permitted, and
     *              indicates that the cause is nonexistent or unknown.
     */
    public RailMlFormatCastingException( Throwable cause )
    {
        super( cause );
    }
}
