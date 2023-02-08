package com.schedule.model;


import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import com.schedule.base.model.Locale;
import com.schedule.base.model.TTPropValues;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(TTObject.class)
public abstract class TTObject_ {

	public static volatile SingularAttribute<TTObject, Integer> extPhysicalId2;
	public static volatile SingularAttribute<TTObject, String> scheduleName;
	public static volatile ListAttribute<TTObject, TTObject> childObjects;
	public static volatile SingularAttribute<TTObject, TTObject> parentObject;
	public static volatile SingularAttribute<TTObject, Character> objectClass;
	public static volatile MapAttribute<TTObject, Locale, TextKey> textKeys;
	public static volatile SingularAttribute<TTObject, String> description;
	public static volatile SingularAttribute<TTObject, String> userName;
	public static volatile SingularAttribute<TTObject, TTObjectState> ttObjectState;
	public static volatile CollectionAttribute<TTObject, TTPropValues> ttpropvaluesCollection;
	public static volatile SingularAttribute<TTObject, TTObjectType> ttObjectType;
	public static volatile SingularAttribute<TTObject, Integer> extPhysicalId;
	public static volatile SingularAttribute<TTObject, Integer> extLogicalId;
	public static volatile SingularAttribute<TTObject, String> externalName;
	public static volatile SingularAttribute<TTObject, Integer> utcDiff;
	public static volatile SingularAttribute<TTObject, Integer> ttObjId;

}


