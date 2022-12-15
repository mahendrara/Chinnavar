package railml.exceptions;

public class RailMlVersionInformationException extends Exception
{

    /**
     * <p>
     * Create a new <code>RailMlVersionInformationException</code> with no
     * specified detail message and cause.</p>
     */
    public RailMlVersionInformationException()
    {
        super();
    }

    /**
     * <p>
     * Create a new <code>RailMlVersionInformationException</code> with the
     * specified detail message.</p>
     *
     * @param message The detail message.
     */
    public RailMlVersionInformationException( String message )
    {
        super( message );
    }

    /**
     * <p>
     * Create a new <code>RailMlVersionInformationException</code> with the
     * specified detail message and cause.</p>
     *
     * @param message The detail message.
     * @param cause   The cause. A <code>null</code> value is permitted, and
     *                indicates that the cause is nonexistent or unknown.
     */
    public RailMlVersionInformationException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * <p>
     * Create a new <code>RailMlVersionInformationException</code> with the
     * specified cause.</p>
     *
     * @param cause The cause. A <code>null</code> value is permitted, and
     *              indicates that the cause is nonexistent or unknown.
     */
    public RailMlVersionInformationException( Throwable cause )
    {
        super( cause );
    }

}
