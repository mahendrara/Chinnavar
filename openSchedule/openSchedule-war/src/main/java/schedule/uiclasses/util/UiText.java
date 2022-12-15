package schedule.uiclasses.util;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.inject.Inject;
import schedule.uiclasses.LanguageBean;

/**
 *
 * @author Pavel
 */
public class UiText implements Serializable
{

    private static final long serialVersionUID = 1L;

    @Inject
    protected LanguageBean languageBean;

    public String get( String key )
    {
        Locale locale = languageBean.getLocale();
        ResourceBundle bundle = ResourceBundle.getBundle( "Bundle", locale );

        return bundle.getString( key );
    }

}
