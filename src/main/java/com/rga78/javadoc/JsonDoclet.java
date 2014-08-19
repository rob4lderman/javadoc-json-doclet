
package com.rga78.javadoc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.rga78.coll.Cawls;
import com.rga78.coll.MapBuilder;
import com.rga78.json.JSONTrace;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.AnnotationTypeElementDoc;
import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.MemberDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ParameterizedType;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.javadoc.ThrowsTag;
import com.sun.javadoc.Type;
import com.sun.javadoc.TypeVariable;
import com.sun.javadoc.WildcardType;

/**
 * Prints javadoc in JSON format.
 * 
 * The structure of the JSON objects closely matches that of the
 * PackageDoc, ClassDoc, MethodDoc, etc. APIs in the com.sun.javadoc library.
 * 
 *
 * Explanation of types:
 *
 * An AnnotationDesc represents an annotation applied to a class/method/parm/whatever.
 * It contains an AnnotationType and a list of ElementValuePairs.
 * 
 * An AnnotationType represents an annotation class (type). It inherits from ClassDoc.
 * It contains a list of AnnotationTypeElementDocs.
 * 
 * An AnnotationTypeElementDoc represents a single element/field within an annotation.
 * It inherits from MethodDoc. It contains an AnnotationValue defaultValue.
 * 
 * An AnnotationValue is just a wrapper around an Object value (usually a String, could be an Object).
 * 
 * An AnnotationDesc.ElementValuePair is a pairing of AnnotationTypeElementDoc and AnnotationValue.
 * It describes an applied annotation element value.
 * 
 * TypeVariable is a template type variable name, e.g. for java.util.List<E>, the TypeVariable is "E".
 * A TypeVariable has "bounds", which is its "extends" clause.
 *
 * TypeArgument is a value for a TypeVariable. E.g, for java.util.List<String>, the TypeArgument is "String".
 * TypeArguments are contained in ParameterizedTypes.
 *
 * A ParameterizedType is a type that contains a TypeArgument. E.g java.util.List<String> is a ParameterizedType.
 * The Type is "java.util.List", the TypeArgument is "String".
 *
 * A WildcardType is a TypeArgument that contains the wildcard '?'.  Often it's accompanied with an 'extends' or
 * 'super' clause.
 *
 *
 */
public class JsonDoclet {

    /**
     * {@inheritDoc} is searched for and replaced in any methodDoc commentText, 
     * @return, @param, or @throws comment in which it is found -- so long as 
     * the overridden class/method is available to this javadoc process.
     */
    public static final String InheritDocTag = "{@inheritDoc}";

    /**
     * Doclet entry point. Javadoc calls this method, passing in the
     * doc metadata.
     */
    public static boolean start(RootDoc root) {
        return new JsonDoclet(root).go();
    }

    /**
     * NOTE: Without this method present and returning LanguageVersion.JAVA_1_5,
     *       Javadoc will not process generics because it assumes LanguageVersion.JAVA_1_1
     * @return language version (hard coded to LanguageVersion.JAVA_1_5)
     */
    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }

    /**
     * The rootDoc, passed in to the custom doclet by the javadoc framework.
     */
    protected RootDoc rootDoc;
    
    /**
     * Set of packages.  This gets populated as we process classDocs.  Then
     * at the end we process all the packageDocs.
     */
    private Set<PackageDoc> packageDocs = new HashSet<PackageDoc>();

    /**
     * CTOR.
     */
    public JsonDoclet(RootDoc rootDoc) {
        this.rootDoc = rootDoc;
    }
    
    
    /**
     * 
     */
    protected boolean go() {

        // TODO: OPTIMIZATION: remove all null/empty entries from the map, to minimize storage use.
        //       process Maps and Lists recursively.
        
        for (ClassDoc classDoc : rootDoc.classes()) {
            for ( Object obj : processClass(classDoc)) {
                System.out.println( JSONTrace.prettyPrint( (Map) obj ) );
            }
        }
        
        for ( Object obj : processPackages( getPackageDocs() )) {
            System.out.println( JSONTrace.prettyPrint( (Map) obj ) );
        }
        
        return true;
    }
    
    /**
     * Process the given classDoc along with all its methods, constructors, fields, enumConstants, etc.
     * 
     * @return a list of javadoc models.
     */
    protected JSONArray processClass(ClassDoc classDoc) {
        JSONArray retMe = new JSONArray();
        
        retMe.add( classDoc.isAnnotationType() ? processAnnotationTypeDoc( (AnnotationTypeDoc) classDoc ) : processClassDoc(classDoc) );

        retMe.addAll( processMethodDocs( classDoc.methods() ) );

        retMe.addAll( processConstructorDocs( classDoc.constructors() ) );

        retMe.addAll( processFieldDocs( classDoc.fields() ) );

        retMe.addAll( processFieldDocs( classDoc.enumConstants() ) );
        
        return retMe;
    }
    
    /**
     * Process the given set of packageDocs.  
     * 
     * @return a list of package models.
     */
    protected JSONArray processPackages( Collection<PackageDoc> packageDocs ) {
        
        JSONArray retMe = new JSONArray();

        for (PackageDoc packageDoc : packageDocs) {
            retMe.add( processPackageDoc(packageDoc) );
        }

        return retMe;

    }
    
    /**
     * @return the set of packageDocs that were accumulated while processing the classDocs.
     */
    protected Set<PackageDoc> getPackageDocs() {
        packageDocs.remove(null); // Remove null in case it was added
        return packageDocs;
    }

    /**
     * @return full JSON objects for the given ClassDoc[]
     */
    protected JSONArray processClassDocs(ClassDoc[] classDocs) {
        JSONArray retMe = new JSONArray();
        for (ClassDoc classDoc : classDocs) {
            retMe.add( processClassDoc(classDoc) );
        }
        return retMe;
    }

    /**
     * @return the full JSON for the given ClassDoc
     */
    protected JSONObject processClassDoc(ClassDoc classDoc) {

        JSONObject classJson = processProgramElementDoc(classDoc);
        classJson.putAll( processType(classDoc) );  // ??? maybe to get parameterized type info?

        classJson.put("superclass", processClassDocStub(classDoc.superclass()));
        classJson.put("superclassType", processType(classDoc.superclassType()));
        classJson.put("allSuperclassTypes", getAllUniqueSuperclassTypes(classDoc));

        classJson.put("interfaces", processClassDocStubs(classDoc.interfaces()));
        classJson.put("interfaceTypes", processTypes(classDoc.interfaceTypes()));
        classJson.put("allInterfaceTypes", getAllUniqueInterfaceTypes(classDoc));
        classJson.put("typeParameters", processTypeVariables( classDoc.typeParameters() ) );
        classJson.put("typeParamTags", processParamTags( classDoc.typeParamTags() ) );
        
        classJson.put("methods", processMethodDocStubs(classDoc.methods()));
        classJson.put("allInheritedMethods", getAllInheritedMethods( classDoc ));
        classJson.put("constructors", processConstructorDocStubs( classDoc.constructors() ));
        classJson.put("fields", processFieldDocStubs(classDoc.fields()));
        classJson.put("enumConstants", processFieldDocStubs(classDoc.enumConstants()));
        classJson.put("innerClasses", processClassDocStubs(classDoc.innerClasses()));

        return classJson;
    }

    /**
     * @return JSON stubs for the given ClassDoc[].
     */
    protected JSONArray processClassDocStubs(ClassDoc[] classDocs) {
        JSONArray retMe = new JSONArray();
        for (ClassDoc classDoc : classDocs) {
            retMe.add( processClassDocStub(classDoc) );
        }
        
        return retMe;
    }
    
    /**
     * @return a JSON stub for the given ClassDoc.
     */
    protected JSONObject processClassDocStub(ClassDoc classDoc) {
        return processProgramElementDocStub(classDoc);
    }
    
    /**
     * @return the unique set of all superclass types extended by the given classDoc
     *         (note: interfaces may extend more than one interface).
     */
    protected List<Map> getAllUniqueSuperclassTypes(ClassDoc classDoc) {
        return Cawls.uniqueForField( getAllSuperclassTypes(classDoc), "qualifiedTypeName" );
    }
    
    /**
     * @return a list of all superclass types, from most derived to oldest grand parent
     */
    protected JSONArray getAllSuperclassTypes(ClassDoc classDoc) {
        
        JSONArray retMe = new JSONArray();
        
        for (Type superclassType : getSuperclassTypes(classDoc)) {
            retMe.add( processType( superclassType ) );
            retMe.addAll( getAllSuperclassTypes( superclassType.asClassDoc() ) );
        }
        
        return retMe;
    }
    
    /**
     * @return the unique set of all interface types implemented by the given classDoc
     *         including interfaces implemented by superclasses.
     */
    protected List<Map> getAllUniqueInterfaceTypes(ClassDoc classDoc) {
        return Cawls.uniqueForField( getAllInterfaceTypes(classDoc), "qualifiedTypeName" );
    }
    
    /**
     * @return a list of all superclass types
     */
    protected JSONArray getAllInterfaceTypes(ClassDoc classDoc) {
        
        JSONArray retMe = new JSONArray();
        
        if (classDoc != null) {
            retMe.addAll( processTypes( classDoc.interfaceTypes() ) );
            retMe.addAll( getAllInterfaceTypes( classDoc.superclass() ) );
        }
        
        return retMe;
    }
    
    /**
     * @return a list of all interfaces implemented by the given classDoc and
     *         all superclasses of the classDoc.
     */
    protected List<ClassDoc> getAllInterfaces(ClassDoc classDoc) {
        List<ClassDoc> retMe = new ArrayList<ClassDoc>();
        
        if (classDoc != null) {
            retMe.addAll( Arrays.asList(classDoc.interfaces()) );
            retMe.addAll( getAllInterfaces( classDoc.superclass() ) );
        }
        
        return retMe;
    }

    /**
     * @return the full JSON for the given PackageDoc
     */
    protected JSONObject processPackageDoc(PackageDoc packageDoc) {

        if (packageDoc == null) {
            return null;
        }

        JSONObject retMe = processDoc(packageDoc);

        retMe.put("annotations", processAnnotationDescs(packageDoc.annotations()));
        retMe.put("annotationTypes", processAnnotationTypeDocStubs(packageDoc.annotationTypes()));
        retMe.put("enums", processClassDocStubs(packageDoc.enums()));
        retMe.put("errors", processClassDocStubs(packageDoc.errors()));
        retMe.put("exceptions", processClassDocStubs(packageDoc.exceptions()));
        retMe.put("interfaces", processClassDocStubs(packageDoc.interfaces()));
        retMe.put("ordinaryClasses", processClassDocStubs(packageDoc.ordinaryClasses()));

        return retMe;
    }
    
    /**
     * @return a JSON stub for the given PackageDoc.
     */
    protected JSONObject processPackageDocStub(PackageDoc packageDoc) {
        return processDocStub(packageDoc);
    }

    /**
     * @return full JSON objects for the given ConstructorDoc[]
     */
    protected JSONArray processConstructorDocs(ConstructorDoc[] constructorDocs) {
        JSONArray retMe = new JSONArray();
        for (ConstructorDoc constructorDoc: constructorDocs) {
            retMe.add( processConstructorDoc( constructorDoc ) );
        }
        return retMe;
    }

    /**
     * @return JSON stubs for the given ConstructorDoc[].
     */
    protected JSONArray processConstructorDocStubs(ConstructorDoc[] constructorDocs) {
        JSONArray retMe = new JSONArray();
        for (ConstructorDoc constructorDoc: constructorDocs) {
            retMe.add( processConstructorDocStub( constructorDoc ) );
        }
        return retMe;
    }

    /**
     * @return the full JSON for the given ConstructorDoc
     */
    protected JSONObject processConstructorDoc(ConstructorDoc constructorDoc) {
        return processExecutableMemberDoc(constructorDoc);
    }

    /**
     * @return a JSON stub for the given ConstructorDoc
     */
    protected JSONObject processConstructorDocStub(ConstructorDoc constructorDoc) {
        return processExecutableMemberDocStub(constructorDoc);
    }
    
    /**
     * @return classDoc.superclass for classes, classDoc.interfaces[0] for interfaces
     */
    protected ClassDoc getSuperclass(ClassDoc classDoc) {
        if (classDoc.isInterface()){
            return (Cawls.isEmpty(classDoc.interfaces())) ? null : classDoc.interfaces()[0] ;
        } else {
            return classDoc.superclass();
        }
    }
    
    /**
     * @return classDoc.superclass for classes, classDoc.interfaces[0] for interfaces
     */
    protected List<Type> getSuperclassTypes(ClassDoc classDoc) {
        if (classDoc == null) {
            return new ArrayList<Type>();
        } else if (classDoc.isInterface()){
            return Arrays.asList( classDoc.interfaceTypes() );
        } else if (classDoc.superclassType() != null) {
            return Arrays.asList( classDoc.superclassType() );
        } else {
            return new ArrayList<Type>();
        }
    }
    
//    /**
//     * @return [ { "superclassType": {}, "inheritedMethods": [ {}, {}, ... ] },
//     *           { "superclassType": {}, "inheritedMethods": [ {}, {}, ... ] } ]
//     */
//    protected JSONArray getAllInheritedMethods(ClassDoc classDoc) {
//        JSONArray retMe = new JSONArray();
//        
//        // Keep track of methods we've already inherited so as not to 
//        // inherit them again from another superclass.
//        List<MethodDoc> alreadyInherited = new ArrayList<MethodDoc>();
//        
//        // TODO: use getSuperclassTypes to handle interfaces with more than 1 superclass.
//        // for ( Type superclassType : getSuperclassTypes(classDoc) ) {
//            
//            for ( ClassDoc superclassDoc = getSuperclass(classDoc);
//                    superclassDoc != null;
//                    superclassDoc = getSuperclass(superclassDoc) ) {
//
//                List<MethodDoc> inheritedMethods = new ArrayList<MethodDoc>();
//
//                for (MethodDoc supermethodDoc : superclassDoc.methods()) {
//                    if ( !isMethodOverridden( supermethodDoc, classDoc.methods(), alreadyInherited ) ) {
//                        inheritedMethods.add( supermethodDoc );
//                    }
//                }
//
//                if (inheritedMethods.size() > 0) {
//                    retMe.add( processInheritedMethods( superclassDoc, inheritedMethods ) );
//
//                    // Keep track of inheritedMethods so as not to inherit them again from
//                    // another superclass.
//                    alreadyInherited.addAll( inheritedMethods );
//                }
//            }
//        // }
//        
//        return retMe;
//    }
    
    /**
     * @return [ { "superclassType": {}, "inheritedMethods": [ {}, {}, ... ] },
     *           { "superclassType": {}, "inheritedMethods": [ {}, {}, ... ] } ]
     */
    protected List<Map> getAllInheritedMethods(ClassDoc classDoc) {
        return Cawls.uniqueForField( getAllInheritedMethodsHelper(classDoc, classDoc, new ArrayList<MethodDoc>()), "superclassType");
    }
    
    /**
     * 
     * @param childClassDoc - The class for which we are determining the inherited methods 
     * @param parentClassDoc - will search for inherited methods from the *parent*
     *                         of this class. So the first pass thru this recursive
     *                         algorithm should pass in the base/child class for this
     *                         argument.
     * @param alreadyInherited - keeps track of methods we've already inherited so
     *                           as not to inherit them again from another superclass.
     *                           
     * @return [ { "superclassType": {}, "inheritedMethods": [ {}, {}, ... ] },
     *           { "superclassType": {}, "inheritedMethods": [ {}, {}, ... ] } ]
     */
    protected List<Map> getAllInheritedMethodsHelper(ClassDoc childClassDoc,
                                                     ClassDoc parentClassDoc, 
                                                     List<MethodDoc> alreadyInherited) {
        List<Map> retMe = new ArrayList<Map>();
        
        // use getSuperclassTypes to handle interfaces with more than 1 superclass.
        for ( Type superclassType : getSuperclassTypes(parentClassDoc) ) {
            
            ClassDoc superclassDoc = superclassType.asClassDoc();
            
            // Collect all methods inherited from this superclass,
            // ignoring methods that we've alreadyInherited from a
            // previous superclass.
            List<MethodDoc> inheritedMethods = new ArrayList<MethodDoc>();

            for (MethodDoc supermethodDoc : superclassDoc.methods()) {
                if ( !isMethodOverridden( supermethodDoc, childClassDoc.methods(), alreadyInherited ) ) {
                    inheritedMethods.add( supermethodDoc );
                }
            }

            if (inheritedMethods.size() > 0) {
                // Create an entry for the inherited methods from this superclass
                retMe.add( processInheritedMethods( superclassDoc, inheritedMethods ) );

                // Keep track of inheritedMethods so as not to inherit them again from
                // another superclass.
                alreadyInherited.addAll( inheritedMethods );
            }
            
            // Recurse to search parents of this superclass
            retMe.addAll( getAllInheritedMethodsHelper( childClassDoc, superclassDoc, alreadyInherited ) );
        }
        
        return retMe;
    }
    
    /**
     * @return true if the given supermethodDoc is overridden by one of the given methodDocs.
     */
    protected boolean isMethodOverridden( MethodDoc supermethodDoc, 
                                          MethodDoc[] methodDocs, 
                                          List<MethodDoc> alreadyInherited) {
        for (MethodDoc methodDoc : methodDocs) {
            if (methodDoc.overrides(supermethodDoc)) {
                return true;
            }
        }
        
        for (MethodDoc methodDoc : alreadyInherited) {
            if (methodDoc.overrides(supermethodDoc)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * @return the method from the givne supermethodDocs list that is overridden by the given methodDoc.
     */      
    protected MethodDoc getOverriddenMethod(MethodDoc methodDoc, MethodDoc[] supermethodDocs) {
        if (supermethodDocs == null) {
            return null;
        }
        
        for (MethodDoc supermethodDoc : supermethodDocs) {
            if (methodDoc.overrides(supermethodDoc)) {
                return supermethodDoc;
            }
        }
        
        return null;
    }
    
    /**
     * @return { "superclassType": {}, "inheritedMethods": [ {}, {}, ... ] }
     */
    protected JSONObject processInheritedMethods(ClassDoc superclassDoc, List<MethodDoc> inheritedMethods) {
        JSONObject retMe = new JSONObject();
        retMe.put("superclassType", processType(superclassDoc));
        retMe.put("inheritedMethods", processMethodDocStubs( inheritedMethods ) );
        return retMe;
    }

    /**
     * @return JSON stubs for the given MethodDoc[].
     */
    protected JSONArray processMethodDocStubs(MethodDoc[] methodDocs) {
        JSONArray retMe = new JSONArray();
        for (MethodDoc methodDoc: methodDocs) {
            retMe.add( processMethodDocStub( methodDoc ) );
        }
        return retMe;
    }
    
    /**
     * @return JSON stubs for the given MethodDoc[].
     */
    protected JSONArray processMethodDocStubs(Collection<MethodDoc> methodDocs) {
        JSONArray retMe = new JSONArray();
        for (MethodDoc methodDoc: methodDocs) {
            retMe.add( processMethodDocStub( methodDoc ) );
        }
        return retMe;
    }

    /**
     * @return full JSON objects for the given MethodDoc[]
     */
    protected JSONArray processMethodDocs(MethodDoc[] methodDocs) {
        JSONArray retMe = new JSONArray();
        for (MethodDoc methodDoc: methodDocs) {
            retMe.add( processMethodDoc( methodDoc ) );
        }
        return retMe;
    }

    /**
     * @return a JSON stub for the given MethodDoc.
     */
    protected JSONObject processMethodDocStub(MethodDoc methodDoc) {
        
        if (methodDoc == null) {
            return null;
        }

        JSONObject retMe = processExecutableMemberDocStub(methodDoc);

        retMe.put("returnType", processTypeStub(methodDoc.returnType()));

        return retMe;
    }

    /**
     * @return the full JSON for the given MethodDoc
     */
    protected JSONObject processMethodDoc(MethodDoc methodDoc) {
        
        if (methodDoc == null) {
            return null;
        }
        
        JSONObject retMe = processExecutableMemberDoc(methodDoc);

        retMe.put("returnType", processType(methodDoc.returnType()));
        retMe.put("overriddenMethod", processMethodDocStub(methodDoc.overriddenMethod() ) );
        retMe.put("overriddenType", processTypeStub(methodDoc.overriddenType() ) );
        MethodDoc specifiedByMethodDoc = getSpecifiedByMethod(methodDoc);
        retMe.put("specifiedByMethod", processMethodDocStub( specifiedByMethodDoc ) );
        
        if (methodDoc.overriddenMethod() != null || specifiedByMethodDoc != null) {
            inheritDoc(retMe, methodDoc, specifiedByMethodDoc);
        }
        
        return retMe;
    }
    
    /**
     * Process any and all commentText/@return/@param/@throws tags that are
     * either missing or contain "{@inheritDoc}" by walking up the inheritance
     * tree looking for doc to inherit.
     * 
     * @return retMe
     */
    protected JSONObject inheritDoc(JSONObject retMe, MethodDoc methodDoc, MethodDoc specifiedByMethodDoc) {

        retMe.put("commentText", getInheritedCommentText(methodDoc, specifiedByMethodDoc));
 
        if ( !methodDoc.returnType().typeName().equals("void")) {
            inheritReturnTag(retMe, methodDoc, specifiedByMethodDoc);
        }

        inheritParamTags(retMe, methodDoc, specifiedByMethodDoc);

        inheritThrowsTags(retMe, methodDoc, specifiedByMethodDoc);

        return retMe;
    }

    /**
     * @return retMe
     */
    protected JSONObject inheritReturnTag(JSONObject retMe, MethodDoc methodDoc, MethodDoc specifiedByMethodDoc) {

        String returnTagText = getInheritedReturnTagText(methodDoc, specifiedByMethodDoc);

        if (! StringUtils.isEmpty( returnTagText ) ) {

            // TODO: Cawls.replaceFirst would be a nice method to have....
            JSONObject returnTag = (JSONObject) Cawls.findFirst( (List<Map>)retMe.get("tags"), new MapBuilder().append("name","@return") );

            if (returnTag != null) {
                returnTag.put("text", returnTagText);   // updates the list in place.
            } else {
                // Add a new tag to the list.
                ((JSONArray)retMe.get("tags")).add( new MapBuilder().append("name", "@return")
                                                                    .append("kind", "@return")
                                                                    .append("text", returnTagText ) );
            }
        }

        return retMe;
    }

    /**
     * Either the paramTag exists or it doesn't.  If it doesn't, inherit.
     * If it does, and contains {@inheritDoc}, resolve inherited doc.
     *
     * @return retMe
     */
    protected JSONObject inheritParamTags(JSONObject retMe, MethodDoc methodDoc, MethodDoc specifiedByMethodDoc) {

        // First things first - compile a list of ParamTags.  If any are missing,
        // inherit from the parent class.
        List<ParamTag> paramTags = getInheritedParamTags(methodDoc, specifiedByMethodDoc);

        List<Map> paramTagModels = new ArrayList<Map>();

        for ( ParamTag paramTag : paramTags ) {

            Map paramTagModel = processParamTag(paramTag);

            paramTagModel.put("parameterComment",  getInheritedParamTagComment(methodDoc, paramTag.parameterName(), specifiedByMethodDoc) );

            paramTagModels.add(paramTagModel);
        }
        
        retMe.put("paramTags", paramTagModels);

        return retMe;
    }

    /**
     * @return retMe
     */
    protected JSONObject inheritThrowsTags(JSONObject retMe, MethodDoc methodDoc, MethodDoc specifiedByMethodDoc) {
        // TODO
        return retMe;
    }
    
    /**
     * @return if currentText is empty, return inheritedText; otherwise replace all
     *         instances of {@inheritDoc} in currentText with inheritedText
     */
    protected String resolveInheritDoc(String currentText, String inheritedText) {
        if (StringUtils.isEmpty(currentText)) {
            return inheritedText;

        } else if ( !StringUtils.isEmpty(inheritedText) ) {
            // Replace any occurence of {@inheritDoc} with the inheritedText.
            return currentText.replace( JsonDoclet.InheritDocTag, inheritedText );
        }
        
        return currentText;
    }
 
    /**
     * Resolve inherited comment text by scanning up the methodDoc's inheritance chain,
     * resolving any {@inheritDoc} encountered along the way.
     *
     * This method returns as soon as it finds a non-empty commentText with all {@inheritDoc}
     * tags resolved.
     * 
     * @return the comment text for the given methodDoc, all inheritance resolved.
     */
    protected String getInheritedCommentText(MethodDoc methodDoc, MethodDoc specifiedByMethodDoc) {

        String retMe = null;
        
        for ( ;
             methodDoc != null && (StringUtils.isEmpty(retMe) || retMe.contains(JsonDoclet.InheritDocTag) );
             methodDoc = methodDoc.overriddenMethod() ) {

            retMe = resolveInheritDoc(retMe, methodDoc.commentText() );
        }
        
        // Inherit from the interface
        retMe = resolveInheritDoc(retMe, (specifiedByMethodDoc != null) ? specifiedByMethodDoc.commentText() : null);

        return retMe;
    }

    /**
     * Resolve inherited @return tag text by scanning up the methodDoc's inheritance chain,
     * resolving any {@inheritDoc} encountered along the way.
     *
     * This method returns as soon as it finds a non-empty @return tag text with all {@inheritDoc}
     * tags resolved.
     *
     * Note: the logic of this method is exactly the same as getInheritedCommentText.
     *       The only difference is the value we're retrieving from the methodDoc (@return tag
     *       text vs commentText).
     * 
     * @return the @return tag text for the given methodDoc, all inheritance resolved.
     */
    protected String getInheritedReturnTagText(MethodDoc methodDoc, MethodDoc specifiedByMethodDoc) {

        String retMe = null;
        
        for ( ;
             methodDoc != null && (StringUtils.isEmpty(retMe) || retMe.contains(JsonDoclet.InheritDocTag) );
             methodDoc = methodDoc.overriddenMethod() ) {
            
            retMe = resolveInheritDoc(retMe, getReturnTagText( methodDoc ) );
        }
        
        // Inherit from the interface
        retMe = resolveInheritDoc(retMe, getReturnTagText( specifiedByMethodDoc) );
        
        return retMe;
    }

    /**
     * @return the @return tag text for the given methodDoc, or null if not found.
     */
    protected String getReturnTagText(MethodDoc methodDoc) {
        if (methodDoc == null) {
            return null;
        }

        for (Tag tag : Cawls.safeIterable(methodDoc.tags())) {
            if (tag.name().equals("@return")) {
                return tag.text();
            }
        }
        
        return null;
    }

    /**
     * Resolve inherited @param tag text by scanning up the methodDoc's inheritance chain,
     * resolving any {@inheritDoc} encountered along the way.
     *
     * This method returns as soon as it finds a non-empty @param tag text with all {@inheritDoc}
     * tags resolved.
     *
     * Note: the logic of this method is exactly the same as getInheritedCommentText.
     *       The only difference is the value we're retrieving from the methodDoc (@param tag
     *       text vs commentText).
     * 
     * @return the @param tag text for the given methodDoc, all inheritance resolved.
     */
    protected String getInheritedParamTagComment(MethodDoc methodDoc, String parameterName, MethodDoc specifiedByMethodDoc) {

        String retMe = null;
        
        for ( ;
             methodDoc != null && (StringUtils.isEmpty(retMe) || retMe.contains(JsonDoclet.InheritDocTag) );
             methodDoc = methodDoc.overriddenMethod() ) {

            retMe = resolveInheritDoc(retMe, getParamTagComment( methodDoc, parameterName ));
        }
        
        // Inherit from the interface
        retMe = resolveInheritDoc(retMe, getParamTagComment( specifiedByMethodDoc, parameterName) );
        
        return retMe;
    }

    /**
     * @return the @param tag comment for the given methodDoc and parameter, or null if not found.
     */
    protected String getParamTagComment(MethodDoc methodDoc, String parameterName) {
        
        if (methodDoc == null) {
            return null;
        }

        for (ParamTag paramTag : Cawls.safeIterable(methodDoc.paramTags())) {
            if (paramTag.parameterName().equals( parameterName )) {
                return paramTag.parameterComment();
            }
        }
        
        return null;
    }

    /**
     * @return the @param tag for the given parameterName
     */
    protected ParamTag getParamTag(ParamTag[] paramTags, String parameterName) {

        for (ParamTag paramTag : Cawls.safeIterable(paramTags)) {
            if (paramTag.parameterName().equals( parameterName )) {
                return paramTag;
            }
        }
        
        return null;
    }

    /**
     * Resolve inherited @param tags.
     *
     * This method compiles a list of param tags for each of the given methodDoc's parameters.
     * If a paramTag is missing from the given methodDoc, it is searched for in the method's
     * inheritance chain.
     * 
     * @return a list of @param tags for the given methodDoc, some of which may be inherited.
     */
    protected List<ParamTag> getInheritedParamTags(MethodDoc methodDoc, MethodDoc specifiedByMethodDoc) {

        List<ParamTag> retMe = new ArrayList<ParamTag>();

        for ( Parameter parameter : methodDoc.parameters() ) {
            
            ParamTag paramTag = getInheritedParamTag( methodDoc, parameter.name(), specifiedByMethodDoc);
            if (paramTag != null) {
                retMe.add( paramTag );
            } 
        }
        
        return retMe;
    }

    /**
     * @return the first non-null ParamTag with the given parameterName in the inheritance tree
     *         for the given methodDoc.
     */
    protected ParamTag getInheritedParamTag(MethodDoc methodDoc, String parameterName, MethodDoc specifiedByMethodDoc)  {

        for ( ;
             methodDoc != null;
             methodDoc = methodDoc.overriddenMethod() ) {

            ParamTag retMe = getParamTag( methodDoc.paramTags(), parameterName );
            if (retMe != null) {
                return retMe;
            }
        }
        
        // Couldn't find it in the superclass hierarchy. Check the interface method
        return (specifiedByMethodDoc != null) ? getParamTag( specifiedByMethodDoc.paramTags(), parameterName ) : null;
    }
    
    /**
     * 
     */
    protected MethodDoc getSpecifiedByMethod(MethodDoc methodDoc) {
        
        List<ClassDoc> allInterfaces = getAllInterfaces(methodDoc.containingClass());
        
        for (ClassDoc intf : allInterfaces) {
            MethodDoc specifiedByMethod = getSpecifiedByMethod(methodDoc, intf);
            if (specifiedByMethod != null) {
                return specifiedByMethod;
            }
        }
        
        return null;
    }
    
    /**
     * @return the method from the given intf (or its superclasses) that is overridden
     *         (or implemented by) the given methodDoc.
     */
    protected MethodDoc getSpecifiedByMethod(MethodDoc methodDoc, ClassDoc intf) {
        
        MethodDoc retMe = null;
        
        if (intf != null) {
            retMe = getOverriddenMethod( methodDoc, intf.methods() ); 
            
            if (retMe == null) {
                // Try the super interface.
                retMe = getSpecifiedByMethod( methodDoc, intf.superclass() );
            }
        }
        
        return retMe;
    }
 
    /**
     * @return full JSON objects for the given FieldDoc[]
     */
    protected JSONArray processFieldDocs(FieldDoc[] fieldDocs) {
        JSONArray retMe = new JSONArray();
        for (FieldDoc fieldDoc: fieldDocs) {
            retMe.add( processFieldDoc( fieldDoc ) );
        }
        return retMe;
    }

    /**
     * @return JSON stubs for the given FieldDoc[].
     */
    protected JSONArray processFieldDocStubs(FieldDoc[] fieldDocs) {
        JSONArray retMe = new JSONArray();
        for (FieldDoc fieldDoc: fieldDocs) {
            retMe.add( processFieldDocStub( fieldDoc ) );
        }
        return retMe;
    }

    /**
     * @return the full JSON for the given FieldDoc
     */
    protected JSONObject processFieldDoc(FieldDoc fieldDoc) {
        
        if (fieldDoc == null) {
            return null;
        }
        
        JSONObject retMe = processMemberDoc(fieldDoc);

        retMe.put("type", processType(fieldDoc.type()));
        retMe.put("constantValueExpression", fieldDoc.constantValueExpression());
        retMe.put("constantValue", ((fieldDoc.constantValue() != null) ? fieldDoc.constantValue().toString() : null));
        retMe.put("serialFieldTags", processTags(fieldDoc.serialFieldTags()));
        
        return retMe;
    }

    /**
     * @return a JSON stub for the given FieldDoc
     */
    protected JSONObject processFieldDocStub(FieldDoc fieldDoc) {
        
        if (fieldDoc == null) {
            return null;
        }
        
        JSONObject retMe = processMemberDocStub(fieldDoc);

        retMe.put("type", processTypeStub(fieldDoc.type()));
        retMe.put("constantValueExpression", fieldDoc.constantValueExpression());
        
        return retMe;
    }
    
    /**
     * @return the full JSON for the given MemberDoc
     */
    protected JSONObject processMemberDoc(MemberDoc memberDoc) {

        if (memberDoc == null) {
            return null;
        }

        JSONObject retMe = processProgramElementDoc(memberDoc);

        retMe.put("isSynthetic", memberDoc.isSynthetic());

        return retMe;
    }

    /**
     * @return a JSON stub for the given MemberDoc
     */
    protected JSONObject processMemberDocStub(MemberDoc memberDoc) {
        return processProgramElementDocStub(memberDoc);
    }

    /**
     * @return the full JSON for the given ExecutableMemberDoc
     */
    protected JSONObject processExecutableMemberDoc(ExecutableMemberDoc emDoc) {
        
        JSONObject retMe = processMemberDoc(emDoc);
        
        retMe.put("flatSignature", emDoc.flatSignature());
        retMe.put("signature", emDoc.signature());
        retMe.put("parameters", processParameters(emDoc.parameters()));
        
        retMe.put("paramTags", processParamTags(emDoc.paramTags()));     
        retMe.put("thrownExceptions", processClassDocStubs(emDoc.thrownExceptions()));
        retMe.put("thrownExceptionTypes", processTypes(emDoc.thrownExceptionTypes()));
        retMe.put("typeParameters", processTypeVariables(emDoc.typeParameters()));
        retMe.put("typeParamTags", processParamTags(emDoc.typeParamTags()));
        retMe.put("throwsTags", processThrowsTags(emDoc.throwsTags()));
        
        return retMe;
    }

    /**
     * @return a JSON stub for the given ExecutableMemberDoc
     */
    protected JSONObject processExecutableMemberDocStub(ExecutableMemberDoc emDoc) {
        
        JSONObject retMe = processMemberDocStub(emDoc);
        
        retMe.put("parameters", processParameterStubs(emDoc.parameters()));
        retMe.put("flatSignature", emDoc.flatSignature());
        retMe.put("thrownExceptionTypes", processTypes(emDoc.thrownExceptionTypes()));
        retMe.put("typeParameters", processTypeVariables(emDoc.typeParameters()));
        
        return retMe;
    }

    /**
     * @return full JSON objects for the given Parameter[]
     */
    protected JSONArray processParameters(Parameter[] parameters) {
        JSONArray retMe = new JSONArray();
        for (Parameter parameter: parameters) {
            retMe.add( processParameter( parameter ) );
        }
        return retMe;
    }

    /**
     * @return JSON stubs for the given Parameter[].
     */
    protected JSONArray processParameterStubs(Parameter[] parameters) {
        JSONArray retMe = new JSONArray();
        for (Parameter parameter: parameters) {
            retMe.add( processParameterStub( parameter ) );
        }
        return retMe;
    }

    /**
     * @return the full JSON for the given Parameter
     */
    protected JSONObject processParameter(Parameter parameter) {
        if (parameter == null) {
            return null;
        }
        
        JSONObject retMe = new JSONObject();
        retMe.put("name", parameter.name());
        retMe.put("toString", parameter.toString());
        retMe.put("type", processType(parameter.type()));
        retMe.put("typeName", parameter.typeName());
        retMe.put("annotations", processAnnotationDescs(parameter.annotations()));
        
        return retMe;
    }

    /**
     * @return a JSON stub for the given Parameter
     */
    protected JSONObject processParameterStub(Parameter parameter) {
        if (parameter == null) {
            return null;
        }
        
        JSONObject retMe = new JSONObject();

        retMe.put("type", processTypeStub(parameter.type()));
        retMe.put("name", parameter.name());

        return retMe;
    }
    
    /**
     * @return the full JSON for the given ProgramElementDoc
     */
    protected JSONObject processProgramElementDoc(ProgramElementDoc programElementDoc) {

        if (programElementDoc == null) {
            return null;
        }

        JSONObject retMe = processDoc(programElementDoc);
        
        retMe.put("containingPackage", processPackageDocStub(programElementDoc.containingPackage()) );
        
        packageDocs.add(programElementDoc.containingPackage());
        
        retMe.put("containingClass", processClassDocStub(programElementDoc.containingClass()) );
        retMe.put("qualifiedName", programElementDoc.qualifiedName());
        
        retMe.put("modifiers", programElementDoc.modifiers());
        retMe.put("modifierSpecifier", programElementDoc.modifierSpecifier());
        retMe.put("annotations", processAnnotationDescs(programElementDoc.annotations()));
        
        return retMe;
    }

    /**
     * @return a JSON stub for the given ProgramElementDoc
     */
    protected JSONObject processProgramElementDocStub(ProgramElementDoc peDoc) {
        if (peDoc == null) {
            return null;
        }
        
        JSONObject retMe = processDocStub(peDoc);
        
        retMe.put("qualifiedName", peDoc.qualifiedName());
        retMe.put("modifiers", peDoc.modifiers());
        
        return retMe;
    }
    
    /**
     * @return full JSON objects for the given AnnotationDesc[]
     */
    protected JSONArray processAnnotationDescs(AnnotationDesc[] annotations) {
        JSONArray retMe = new JSONArray();
        
        for (AnnotationDesc annotation : annotations) {
            retMe.add( processAnnotationDesc(annotation) );
        }
        
        return retMe;
    }
    
    /**
     *  
     * 
     * @return the full JSON for the given AnnotationDesc
     */
    protected JSONObject processAnnotationDesc(AnnotationDesc annotation) {
        if (annotation == null) {
            return null;
        }
        
        JSONObject retMe = new JSONObject();
        
        retMe.put("annotationType", processAnnotationTypeDocStub( annotation.annotationType()));
        retMe.put("elementValues", processAnnotationElementValues( annotation.elementValues() ));
        
        return retMe;
    }
    
    
    /**
     * @return the full JSON for the given annotation type
     */
    protected JSONObject processAnnotationTypeDoc(AnnotationTypeDoc annoTypeDoc) {
        JSONObject retMe = processClassDoc(annoTypeDoc);
        
        retMe.put( "elements", processAnnotationTypeElementDocs( annoTypeDoc.elements() ) );
        
        return retMe;
    }
    
    /**
     * @return JSON stubs for the given AnnotationTypeDoc[].
     *
     *
     */
    protected JSONArray processAnnotationTypeDocStubs(AnnotationTypeDoc[] annotationTypeDocs) {
        JSONArray retMe = new JSONArray();
        for (AnnotationTypeDoc annotationTypeDoc : annotationTypeDocs) {
            retMe.add( processAnnotationTypeDocStub(annotationTypeDoc) );
        }
        return retMe;
    }

    /**
     * @return a JSON stub for the given AnnotationTypeDoc.
     */
    protected JSONObject processAnnotationTypeDocStub(AnnotationTypeDoc annotationTypeDoc) {
        return processClassDocStub(annotationTypeDoc);
    }
    
    /**
     * @return the full JSON for the given annotation type
     */
    protected JSONArray processAnnotationTypeElementDocs(AnnotationTypeElementDoc[] annoTypeElementDocs) {
        
        JSONArray retMe = new JSONArray();
        
        for (AnnotationTypeElementDoc annoTypeElementDoc : annoTypeElementDocs) {
            retMe.add( processAnnotationTypeElementDoc( annoTypeElementDoc ) );
        }
        
        return retMe;
    }
    
    /**
     * @return the full JSON for the given annotation type
     */
    protected JSONObject processAnnotationTypeElementDoc(AnnotationTypeElementDoc annoTypeElementDoc) {
        if (annoTypeElementDoc == null) {
            return null;
        }
        
        JSONObject retMe = processMethodDoc( annoTypeElementDoc );
        
        // retMe.put("name", annoTypeElementDoc.name());
        // retMe.put("qualifiedName", annoTypeElementDoc.qualifiedName());
        // retMe.put("returnType", processTypeStub(annoTypeElementDoc.returnType()));
        retMe.put("defaultValue", processAnnotationValue( annoTypeElementDoc.defaultValue() ) );
        
        return retMe;
    }
    
    /**
     * @return the full JSON for the given annotation type
     */
    protected JSONObject processAnnotationTypeElementDocStub(AnnotationTypeElementDoc annoTypeElementDoc) {
        if (annoTypeElementDoc == null) {
            return null;
        }
        
        JSONObject retMe = processMemberDocStub( annoTypeElementDoc );
        
        retMe.put("returnType", processTypeStub(annoTypeElementDoc.returnType()));
        
        // JSONObject retMe = new JSONObject();
        // retMe.put("name", annoTypeElementDoc.name())
        return retMe;
    }
    
    /**
     * @return the full JSON for the given annotation type
     */
    protected JSONObject processAnnotationValue(AnnotationValue annoValue) {
        
        if (annoValue == null) {
            return null;
        }
        
        JSONObject retMe = new JSONObject();
        retMe.put( "toString", annoValue.toString() );
        
        return retMe;
    }
    
    /**
     * @return full JSON objects for the given AnnotationDesc.ElementValuePair[]
     */
    protected JSONArray processAnnotationElementValues( AnnotationDesc.ElementValuePair[] elementValues) {
        
        JSONArray retMe = new JSONArray();
        
        for (AnnotationDesc.ElementValuePair elementValue : elementValues) {
            retMe.add( processAnnotationElementValue( elementValue ));
        }
        
        return retMe;
    }
    
    /**
     * @return the full JSON for the given AnnotationDesc.ElementValuePair
     */
    protected JSONObject processAnnotationElementValue(AnnotationDesc.ElementValuePair elementValue) {
        if (elementValue == null) {
            return null;
        }
        
        JSONObject retMe = new JSONObject();
        
        retMe.put( "element", processAnnotationTypeElementDocStub( elementValue.element() )); 
        retMe.put("value", processAnnotationValue( elementValue.value() ) );
        
        return retMe;
    }
    
    
    /**
     * @return full JSON objects for the given TypeVariable[]
     */
    protected JSONArray processTypeVariables( TypeVariable[] typeVariables) {
        
        JSONArray retMe = new JSONArray();
        
        for (TypeVariable typeVariable : typeVariables) {
            retMe.add( processTypeVariable(typeVariable) );
        }
        
        return retMe;
    }
    
    /**
     * 
     * @return the full JSON for the given TypeVariable
     */
    protected JSONObject processTypeVariable(TypeVariable typeVariable) {
        if (typeVariable == null) {
            return null;
        }
        
        JSONObject retMe = processType(typeVariable);
        
        retMe.put("bounds", processTypes(typeVariable.bounds()));
        
        return retMe;
    }
    
    
    /**
     * @return full JSON objects for the given Type[]
     */
    protected JSONArray processTypes(Type[] types) {
        
        JSONArray retMe = new JSONArray();
        
        for (Type type : types) {
            retMe.add( processType(type) );
        }
        
        return retMe;
    }
    
    /**
     * @return the full JSON for the given Type
     */
    protected JSONObject processType(Type type) {
        
        if (type == null) {
            return null;
        }
        
        JSONObject retMe = new JSONObject();
        
        retMe.put("qualifiedTypeName", type.qualifiedTypeName());
        retMe.put("simpleTypeName", type.simpleTypeName());
        retMe.put("typeName", type.typeName());
        retMe.put("toString", type.toString());
        retMe.put("dimension", type.dimension());

        retMe.put("parameterizedType", processParameterizedType( type.asParameterizedType() ) );
        retMe.put("wildcardType", processWildcardType( type.asWildcardType() ) );

        // TODO: add metaType=type/parameterizedType/wildcardType
        
        return retMe;
    }

    /**
     * @return a JSON stub for the given Type
     */
    protected JSONObject processTypeStub(Type type) {

        return processType(type);
        
        // -rx- if (type == null) {
        // -rx-     return null;
        // -rx- }
        // -rx- 
        // -rx- JSONObject retMe = new JSONObject();
        // -rx- 
        // -rx- retMe.put("qualifiedTypeName", type.qualifiedTypeName());
        // -rx- retMe.put("typeName", type.typeName());
        // -rx- retMe.put("dimension", type.dimension());
        // -rx- retMe.put("toString", type.toString());
        // -rx- 
        // -rx- return retMe;
    }
    
    /**
     * @return the full JSON for the given Type
     */
    protected JSONObject processParameterizedType(ParameterizedType parameterizedType) {
        
        if (parameterizedType == null) {
            return null;
        }
        
        JSONObject retMe = new JSONObject();
        
        retMe.put("typeArguments", processTypes( parameterizedType.typeArguments() ) );

        return retMe;
    }

    /**
     * @return the full JSON for the given Type
     */
    protected JSONObject processWildcardType(WildcardType wildcardType) {
        
        if (wildcardType == null) {
            return null;
        }
        
        JSONObject retMe = new JSONObject();
        
        retMe.put("extendsBounds", processTypes( wildcardType.extendsBounds() ) );
        retMe.put("superBounds", processTypes( wildcardType.superBounds() ) );

        return retMe;
    }

    /**
     * The Doc element is a supertype to the others (ClassDoc, PackageDoc, etc).
     * 
     * @return JSON-mapping of the doc's javadoc meta data.
     */
    protected JSONObject processDoc(Doc doc) {
        if (doc == null) {
            return null;
        }

        JSONObject docJson = new JSONObject();
        
        docJson.put("name", doc.name());
        
        // commentText includes in-line tags but not block tags.
        docJson.put("commentText", doc.commentText());
        
        // rawCommentText includes everything - the entire javadoc comment, unprocessed.
        docJson.put("rawCommentText", doc.getRawCommentText());

        // tags includes only block tags, no inline tags.
        docJson.put("tags", processTags( doc.tags() ) );
        
        // Like commentText, includes raw text + inline tags; does NOT include block tags
        // raw text wrapped in Tag with kind="Text" and name="Text"
        docJson.put("inlineTags", processTags( doc.inlineTags() ) );
        
        docJson.put("seeTags", processTags( doc.seeTags() ) );
        
        docJson.put("firstSentenceTags", processTags( doc.firstSentenceTags() ));
        
        docJson.put("metaType", determineMetaType(doc));
        
        return docJson;
    }

    /**
     * @return a JSON stub for the given Doc.
     */
    protected JSONObject processDocStub(Doc doc) {
        
        if (doc == null) {
            return null;
        }
        
        JSONObject retMe = new JSONObject();
        
        retMe.put("name", doc.name());
        retMe.put("metaType", determineMetaType(doc));
        retMe.put("firstSentenceTags", processTags( doc.firstSentenceTags() ));
        
        return retMe;
    }
    
    /**
     * 
     * @return The type that the given Doc represents, e.g. "class", "enum", "method", etc..
     */
    protected String determineMetaType(Doc doc) {
        
        if (doc.isEnum()) {
            return "enum";
        } else if (doc.isAnnotationType()) {
            return "annotationType";
        } else if (doc.isClass()) {
            return "class";
        } else if (doc.isInterface()) {
            return "interface";
        } else if (doc.isConstructor()) {
            return "constructor";
        } else if (doc.isMethod()) {
            return "method";
        } else if (doc.isField()) {
            return "field";
        } else if (doc.isEnumConstant()) {
            return "enumConstant";
        } else if (doc.isAnnotationTypeElement()) {
            return "annotationTypeElement";
        } else if (doc instanceof PackageDoc) {
            return "package";
        } else {
            return "unknown";
        }
    }

    /**
     * @return full JSON objects for the given Tag[]
     */
    protected JSONArray processTags(Tag[] tags) {

        JSONArray tagsJson = new JSONArray();

        for (Tag tag : tags) {
            tagsJson.add( processTag(tag) );
        }

        return tagsJson;
    }

    /**
     * @return the full JSON for the given Tag  
     *
     * TODO: what is a Tag?
     */
    protected JSONObject processTag(Tag tag) {
        JSONObject tagJson = new JSONObject();

        tagJson.put("name", tag.name());
        tagJson.put("kind", tag.kind());
        tagJson.put("text", tag.text());

        return tagJson;
    }
    
    /**
     * @return full JSON objects for the given ParamTag[]
     */
    protected JSONArray processParamTags(ParamTag[] paramTags) {
        JSONArray retMe = new JSONArray();
        
        for (ParamTag paramTag : paramTags) {
            retMe.add(processParamTag(paramTag));
        }
        
        return retMe;
    }
    
    /**
     * @return the full JSON for the given ParamTag
     */
    protected JSONObject processParamTag(ParamTag paramTag) {
        if (paramTag == null) {
            return null;
        }
        
        JSONObject paramJson = processTag(paramTag);
        paramJson.put("parameterComment", paramTag.parameterComment());
        paramJson.put("parameterName", paramTag.parameterName());
        
        return paramJson;
    }
    
    /**
     * @return full JSON objects for the given ThrowsTag[]
     */
    protected JSONArray processThrowsTags(ThrowsTag[] throwsTags) {
        JSONArray retMe = new JSONArray();
        
        for (ThrowsTag throwsTag : throwsTags) {
            retMe.add(processThrowsTag(throwsTag));
        }
        
        return retMe;
    }
    
    /**
     * @return the full JSON for the given ThrowsTag
     */
    protected JSONObject processThrowsTag(ThrowsTag throwsTag) {
        if (throwsTag == null) {
            return null;
        }
        
        JSONObject retMe = processTag(throwsTag);
        retMe.put("exceptionComment", throwsTag.exceptionComment());
        retMe.put("exceptionName", throwsTag.exceptionName());
        retMe.put("exceptionType", processTypeStub(throwsTag.exceptionType()));
        
        return retMe;
    }

}


