package schedule.uiclasses;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import schedule.entities.TrainType;
import schedule.entities.SpeedProfile;
import schedule.entities.SpeedProfile_;
import schedule.sessions.AbstractPredicate;

/**
 *
 * @author Pavel
 */
public class SpeedProfileFilter extends AbstractPredicate<SpeedProfile>
{

    private TrainType traintype = null;

    public void setTrainType( TrainType traintype )
    {
        this.traintype = traintype;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addPredicate( CriteriaBuilder qb, CriteriaQuery cq, Root<SpeedProfile> p, boolean useOrdering )
    {
        List<Predicate> predicates = new ArrayList<>();

        if( traintype != null )
        {
            Path<TrainType> path = p.get( SpeedProfile_.traintype );
            predicates.add( qb.equal( path, traintype ) );
        }

        cq.where( qb.and( predicates.toArray( new Predicate[ predicates.size() ] ) ) );
    }

}
