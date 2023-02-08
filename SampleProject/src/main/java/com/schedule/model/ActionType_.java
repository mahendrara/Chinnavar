package com.schedule.model;


import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import com.schedule.base.model.ActionType;
import com.schedule.base.model.Locale;
import com.schedule.base.model.MainActionType;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ActionType.class)
public abstract class ActionType_ {

	public static volatile SingularAttribute<ActionType, MainActionType> mainActionType;
	public static volatile SingularAttribute<ActionType, Integer> timingMode;
	public static volatile SingularAttribute<ActionType, Integer> defaultPlanSecs;
	public static volatile ListAttribute<ActionType, TTObjectType> ttObjectTypes;
	public static volatile MapAttribute<ActionType, Locale, TextKey> textKeys;
	public static volatile SingularAttribute<ActionType, Integer> actionSubtype;
	public static volatile SingularAttribute<ActionType, String> description;
	public static volatile SingularAttribute<ActionType, Integer> atypeId;
	public static volatile SingularAttribute<ActionType, Integer> userType;
	public static volatile SingularAttribute<ActionType, Boolean> used;
	public static volatile SingularAttribute<ActionType, String> userName;
	public static volatile SingularAttribute<ActionType, Integer> defaultMinSecs;

}


