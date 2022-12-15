package railml.exceptions;

/**
 *
 * @author Pavel
 */
public class RailMlUnsupportedFileFormatException extends Exception
{

    /**
     * <p>
     * Create a new <code>RailMlUnsupportedFileFormatException</code> with no
     * specified detail message and cause.</p>
     */
    public RailMlUnsupportedFileFormatException()
    {
        super( "Unsupported version of the file." );
    }

    /**
     * <p>
     * Create a new <code>RailMlUnsupportedFileFormatException</code> with the
     * specified detail message.</p>
     *
     * @param message The detail message.
     */
    public RailMlUnsupportedFileFormatException( String message )
    {
        super( message );
    }

    /**
     * <p>
     * Create a new <code>RailMlUnsupportedFileFormatException</code> with the
     * specified detail message and cause.</p>
     *
     * @param message The detail message.
     * @param cause   The cause. A <code>null</code> value is permitted, and
     *                indicates that the cause is nonexistent or unknown.
     */
    public RailMlUnsupportedFileFormatException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * <p>
     * Create a new <code>RailMlUnsupportedFileFormatException</code> with the
     * specified cause.</p>
     *
     * @param cause The cause. A <code>null</code> value is permitted, and
     *              indicates that the cause is nonexistent or unknown.
     */
    public RailMlUnsupportedFileFormatException( Throwable cause )
    {
        super( cause );
    }
}
