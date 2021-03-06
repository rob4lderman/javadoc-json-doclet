###A custom doclet for generating javadoc in JSON format.

The JSON data schema closely matches the javadoc API (i.e the method names of 
com.sun.javadoc.\* classes).

E.g. here's a portion of the JSON javadoc for method _`java.lang.String.substring`_:

    {
     "tags": [
      {
       "text": "beginIndex   the beginning index, inclusive.",
       "name": "@param",
       "kind": "@param"
      },
      {
       "text": "the specified substring.",
       "name": "@return",
       "kind": "@return"
      },
      {
       "text": "IndexOutOfBoundsException  if
                 <code>beginIndex</code> is negative or larger than the
                 length of this <code>String</code> object.",
       "name": "@exception",
       "kind": "@throws"
      }
     ],
     "typeParameters": [],
     "qualifiedName": "java.lang.String.substring",
     "paramTags": [
      {
       "text": "beginIndex   the beginning index, inclusive.",
       "parameterComment": "the beginning index, inclusive.",
       "name": "@param",
       "parameterName": "beginIndex",
       "kind": "@param"
      }
     ],
     "containingPackage": {
      "name": "java.lang",
      "metaType": "package"
     },
     "returnType": {
      "typeName": "String",
      "dimension": "",
      "simpleTypeName": "String",
      "qualifiedTypeName": "java.lang.String",
      "toString": "java.lang.String"
     } 
     ...

###Build and test
    $ mvn clean package

###Usage 

See jsondoclet.sh for example usage.






