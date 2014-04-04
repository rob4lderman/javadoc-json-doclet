
package com.surfapi.javadoc;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationTypeDoc;
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
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;
import com.sun.javadoc.TypeVariable;
import com.surfapi.json.JSONTrace;

/**
 * Prints javadoc in JSON format.
 * 
 * The structure of the JSON objects closely matches that of the
 * PackageDoc, ClassDoc, MethodDoc, etc. APIs in the com.sun.javadoc library.
 * 
 */
public class MyDoclet {

    /**
     * Doclet entry point. Javadoc calls this method, passing in the
     * doc metadata.
     */
    public static boolean start(RootDoc root) {

        // System.out.println( JSONTrace.prettyPrint( new MyDoclet(root).collect() ) );
        
        for ( Object obj :  new MyDoclet(root).collect() ) {
            System.out.println( JSONTrace.prettyPrint( (Map) obj ) );
        }

        return true;
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
    private RootDoc rootDoc;
    
    /**
     * Set of packages.  This gets populated as we process classDocs.  Then
     * at the end we process all the packageDocs.
     */
    private Set<PackageDoc> packageDocs = new HashSet<PackageDoc>();

    /**
     * CTOR.
     */
    public MyDoclet(RootDoc rootDoc) {
        this.rootDoc = rootDoc;
    }
    
    /**
     * @return a JSONArray containing all javadoc processed by this doclet.
     */
    protected JSONArray collect() {
        
        JSONArray retMe = new JSONArray();

        for (ClassDoc classDoc : rootDoc.classes()) {
            retMe.add( processClassDoc(classDoc) );
            
            retMe.addAll( processMethodDocs( classDoc.methods() ) );
            
            retMe.addAll( processConstructorDocs( classDoc.constructors() ) );
            
            retMe.addAll( processFieldDocs( classDoc.fields() ) );
            
            // TODO: anything else?
        }
        
        // Process the packageDocs.  packageDocs was populated while we processed 
        // all the classDocs above.
        packageDocs.remove(null); // Remove null in case it was added
        for (PackageDoc packageDoc : packageDocs) {
            retMe.add( processPackageDoc(packageDoc) );
        }
        
        return retMe;
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
        classJson.putAll( processType(classDoc) );

        classJson.put("superclass", processClassDocStub(classDoc.superclass()));
        classJson.put("superclassType", processType(classDoc.superclassType()));

        classJson.put("interfaces", processClassDocStubs(classDoc.interfaces()));
        classJson.put("interfaceTypes", processTypes(classDoc.interfaceTypes()));
        classJson.put("typeParameters", processTypeVariables( classDoc.typeParameters() ) );
        classJson.put("typeParamTags", processParamTags( classDoc.typeParamTags() ) );
        
        classJson.put("methods", processMethodDocStubs(classDoc.methods()));
        classJson.put("constructors", processConstructorDocStubs( classDoc.constructors() ));
        classJson.put("fields", processFieldDocStubs(classDoc.fields()));
        classJson.put("enumConstants", processFieldDocStubs(classDoc.enumConstants()));    // TODO: only enums.
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

        // TODO: add override info? 
        retMe.put("returnType", processType(methodDoc.returnType()));
        
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
        
        retMe.put("paramTags", processParamTags(emDoc.paramTags()));     // TODO: already included with tags()
        retMe.put("thrownExceptions", processClassDocStubs(emDoc.thrownExceptions()));
        retMe.put("thrownExceptionTypes", processTypes(emDoc.thrownExceptionTypes()));
        retMe.put("typeParameters", processTypeVariables(emDoc.typeParameters()));
        retMe.put("typeParamTags", processParamTags(emDoc.typeParamTags()));
        
        return retMe;
    }

    /**
     * @return a JSON stub for the given ExecutableMemberDoc
     */
    protected JSONObject processExecutableMemberDocStub(ExecutableMemberDoc emDoc) {
        
        JSONObject retMe = processMemberDocStub(emDoc);
        
        retMe.put("parameters", processParameterStubs(emDoc.parameters()));
        
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

        retMe.put("toString", parameter.toString());

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
        
        retMe.put( "element", elementValue.element().name());
        retMe.put("value", elementValue.value().toString());
        
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
        
        return retMe;
    }

    /**
     * @return a JSON stub for the given Type
     */
    protected JSONObject processTypeStub(Type type) {
        
        if (type == null) {
            return null;
        }
        
        JSONObject retMe = new JSONObject();
        
        retMe.put("typeNameAndDimension", type.typeName() + type.dimension());
        retMe.put("toString", type.toString());
        
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
        
        // TODO: should i add this to the stub?
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
     * TODO: what is a paramTag.  What are the different types of tags?
     *
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
     * TODO: this is unused. A valiant attempt at using FP ... but alas, java is just too clunky en ce moment.
     */
    protected <T> JSONArray processArray(T[] arr, MapFunction<T, JSONObject> mapFunction) {
        JSONArray retMe = new JSONArray();
        for (T o : arr) {
            retMe.add( mapFunction.call(o) );
        }
        return retMe;
    }

}

/**
 * TODO: this is unused. A valiant attempt at using FP ... but alas, java is just too clunky en ce moment.
 * I: input type
 * O: output type
 */
interface MapFunction<I,O> {
    O call(I input);
}
