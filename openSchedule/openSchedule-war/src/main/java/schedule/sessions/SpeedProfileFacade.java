package schedule.sessions;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import schedule.entities.SpeedProfile;
import schedule.entities.TrainType;
import schedule.uiclasses.SpeedProfileFilter;

/**
 *
 * @author Pavel
 */
@Stateless
public class SpeedProfileFacade extends AbstractFacade<SpeedProfile>
{

    @PersistenceContext(unitName = "openSchedule-warPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager()
    {
        return em;
    }

    public SpeedProfileFacade()
    {
        super( SpeedProfile.class );
    }

    public void removeAllRelatedSpeedProfiles( TrainType trainType )
    {
        List<SpeedProfile> speedProfiles = this.findAllByTrainType( trainType );
        
        for( SpeedProfile speedProfile : speedProfiles )
        {
            remove( speedProfile );
        }
    }

    public List<SpeedProfile> findAllByTrainType( TrainType trainType )
    {
        SpeedProfileFilter filter = new SpeedProfileFilter();

        filter.setTrainType( trainType );

        return findAll( filter );
    }

}
