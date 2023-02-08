package com.schedule.model;


import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import com.schedule.base.model.Locale;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Locale.class)
public abstract class Locale_ {

	public static volatile SingularAttribute<Locale, String> localeCode;
	public static volatile SingularAttribute<Locale, Integer> lId;
	public static volatile ListAttribute<Locale, TextKey> textKeys;
	public static volatile SingularAttribute<Locale, String> description;

}

