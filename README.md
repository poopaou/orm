# orm
Beta Android ORM using Annotation processor and Sqlite database.
 
Annotate your class with `@Entity` than define class properties with `@Column`. 
Orm handles query, insert, update and delete and schema generation.

###Example :
```
@Entity
public class Foo {
    
    @Column
    String aString;
    
    @Column
    boolean aBoolean;
    
    Foo(){}
    
    ... getters & setters ... 
}
```

## Basic Rules

Fields type must be one of : 
* Primitives  (`boolean`, `long`, `double`, `int`, `float`, `short`, `byte`, `char`, `short`)
* Primitives wrapper (`Integer`, `Boolean`...)
* `String` or `java.util.Date`
* `byte[]'

Field visibility must be `public` or `default` (package protected).

Class must provide a `public` or `default` (package protected) no-arguments constructor .

##Restrictions.

Orm is still a beta version, this include some limitations.
* Inner class are not supported (yet).
* Foreign keys are not supported (yet).
* Foreign object loading is not supported (and probably never will). 