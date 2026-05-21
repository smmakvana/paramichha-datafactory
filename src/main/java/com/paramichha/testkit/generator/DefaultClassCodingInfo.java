package com.paramichha.testkit.generator;

import java.util.List;

/**
 * Plain class implementation of {@link ClassCodingInfo}.
 */
final class DefaultClassCodingInfo implements ClassCodingInfo {

    private final boolean              nested;
    private final String               header;
    private final String               packageName;
    private final List<String>         imports;
    private final String               className;
    private final String               displayName;
    private final List<String>         modifiers;
    private final List<String>         annotations;
    private final String               extendsClass;
    private final List<FieldCodingInfo>  fields;
    private final List<MethodCodingInfo> methods;
    private final List<ClassCodingInfo>  nestedClasses;

    DefaultClassCodingInfo(
            boolean nested, String header, String packageName, List<String> imports,
            String className, String displayName, List<String> modifiers, List<String> annotations,
            String extendsClass,
            List<FieldCodingInfo> fields, List<MethodCodingInfo> methods,
            List<ClassCodingInfo> nestedClasses) {
        this.nested        = nested;
        this.header        = header;
        this.packageName   = packageName;
        this.imports       = imports;
        this.className     = className;
        this.displayName   = displayName;
        this.modifiers     = modifiers;
        this.annotations   = annotations;
        this.extendsClass  = extendsClass;
        this.fields        = fields;
        this.methods       = methods;
        this.nestedClasses = nestedClasses;
    }

    public boolean               isNested()       { return nested;        }
    public String                header()         { return header;        }
    public String                packageName()    { return packageName;   }
    public List<String>          imports()        { return imports;       }
    public String                className()      { return className;     }
    public String                displayName()    { return displayName;   }
    public List<String>          modifiers()      { return modifiers;     }
    public List<String>          annotations()    { return annotations;   }
    public String                extendsClass()   { return extendsClass;  }
    public List<FieldCodingInfo>  fields()        { return fields;        }
    public List<MethodCodingInfo> methods()       { return methods;       }
    public List<ClassCodingInfo>  nestedClasses() { return nestedClasses; }
}